package AloTra.controller;

import AloTra.Model.AccountDTO;
import AloTra.Model.AddressDTO;
import AloTra.Model.CartViewDTO;
import AloTra.entity.Order; // Import Order entity
import AloTra.services.AccountService;
import AloTra.services.AddressService;
import AloTra.services.CartService;
import AloTra.services.OrderService; // Import OrderService
import AloTra.services.PaymentService; // Import PaymentService
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import jakarta.servlet.http.HttpSession; // Import HttpSession
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // Import PostMapping, PathVariable
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class CheckoutController {

    @Autowired
    private CartService cartService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OrderService orderService; // Inject OrderService
    @Autowired
    private PaymentService paymentService; // Inject PaymentService

    // User ID giả lập (cần đồng bộ với các Controller khác)
    private static final Long MOCK_USER_ID = 7L;

    // Hiển thị trang checkout
    @GetMapping("/checkout")
    public String viewCheckout(Model model, HttpSession session) { // Thêm HttpSession
        // Lấy mã voucher từ session
        String appliedVoucherCode = (String) session.getAttribute("appliedVoucherCode");

        CartViewDTO cartView = cartService.getCartView(MOCK_USER_ID, appliedVoucherCode); // Truyền voucher code
        List<AddressDTO> addresses = addressService.getAddressesByUserId(MOCK_USER_ID); // Sửa tên hàm
        // Lấy thông tin tài khoản (nếu cần)
        Optional<AccountDTO> accountOpt = accountService.getAccountById(MOCK_USER_ID);

        if (cartView.getItems() == null || cartView.getItems().isEmpty()) {
            // Nếu giỏ hàng trống, chuyển về trang giỏ hàng với thông báo
            // RedirectAttributes có thể không hoạt động tốt với GET mapping, dùng Model tạm
             model.addAttribute("infoMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/user/cart"; // Chuyển hướng về trang giỏ hàng
        }

         // Tìm địa chỉ mặc định hoặc địa chỉ đầu tiên
         AddressDTO defaultAddress = addresses.stream()
                                             .filter(a -> a.getIsDefault() != null && a.getIsDefault())
                                             .findFirst()
                                             .orElse(addresses.isEmpty() ? null : addresses.get(0));


        model.addAttribute("cartView", cartView);
        model.addAttribute("addresses", addresses);
        model.addAttribute("defaultAddress", defaultAddress); // Gửi địa chỉ mặc định ra view
        accountOpt.ifPresent(account -> model.addAttribute("account", account)); // Gửi account nếu tồn tại

        return "user/checkout"; // Trả về view checkout.html
    }

    // Xử lý đặt hàng
    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam("addressId") Long addressId,
                             @RequestParam("paymentMethod") String paymentMethod,
                             @RequestParam(name = "notes", required = false) String notes,
                             HttpSession session, // Lấy HttpSession
                             HttpServletRequest request, // Thêm HttpServletRequest cho VNPAY
                             RedirectAttributes redirectAttributes) {

        try {
            // Lấy mã voucher từ session
            String appliedVoucherCode = (String) session.getAttribute("appliedVoucherCode");

            // Gọi service để tạo Order (truyền voucher code)
            List<Order> createdOrders = orderService.createOrdersFromCart(MOCK_USER_ID, addressId, paymentMethod, notes, appliedVoucherCode);

            // Xóa voucher khỏi session sau khi đã dùng để tạo đơn
            session.removeAttribute("appliedVoucherCode");

            // Xử lý tiếp theo dựa trên phương thức thanh toán
            if ("COD".equalsIgnoreCase(paymentMethod)) {
                // Nếu là COD, chuyển hướng đến trang chi tiết đơn hàng đầu tiên (hoặc trang thành công)
                if (!createdOrders.isEmpty()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Đơn hàng sẽ sớm được xử lý.");
                    return "redirect:/user/order/" + createdOrders.get(0).getId(); // Chuyển đến chi tiết đơn hàng đầu tiên
                } else {
                     // Trường hợp hiếm gặp: không tạo được đơn nào
                     redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo đơn hàng.");
                     return "redirect:/user/cart";
                }
            } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // Nếu là VNPAY, tạo URL thanh toán và redirect
                // Giả định chỉ có 1 order được tạo khi thanh toán online (cần xem lại logic nếu giỏ hàng có nhiều shop)
                if (createdOrders.size() == 1) {
                    try {
                        String paymentUrl = paymentService.createVnPayPaymentUrl(createdOrders.get(0), request);
                        return "redirect:" + paymentUrl; // Chuyển hướng đến cổng VNPAY
                    } catch (Exception e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo URL thanh toán VNPAY.");
                        return "redirect:/user/checkout";
                    }
                } else {
                     // Xử lý trường hợp có nhiều order từ nhiều shop khi chọn VNPAY (cần thiết kế lại luồng)
                     redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán VNPAY hiện chỉ hỗ trợ đơn hàng từ một cửa hàng.");
                     return "redirect:/user/cart";
                }
            }
            // TODO: Thêm xử lý cho MOMO

            // Trường hợp paymentMethod không hợp lệ
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ.");
            return "redirect:/user/checkout";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/checkout"; // Quay lại checkout nếu có lỗi
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt hàng: " + e.getMessage());
             e.printStackTrace(); // In lỗi ra log
            return "redirect:/user/cart"; // Quay lại giỏ hàng nếu có lỗi nghiêm trọng
        }
    }
}