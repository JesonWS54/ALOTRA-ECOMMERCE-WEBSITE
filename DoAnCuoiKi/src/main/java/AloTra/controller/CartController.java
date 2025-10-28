package AloTra.controller;

import AloTra.Model.CartViewDTO;
import AloTra.entity.Voucher;
import AloTra.services.AccountService;
import AloTra.services.AddressService;
import AloTra.services.CartService;
import AloTra.services.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private VoucherService voucherService;

    private static final Long MOCK_USER_ID = 7L;
    private static final String APPLIED_VOUCHER_SESSION_KEY = "appliedVoucherCode";

    @GetMapping
    public String viewCart(Model model, HttpServletRequest request, HttpSession session) {
        try {
            String appliedVoucherCode = (String) session.getAttribute(APPLIED_VOUCHER_SESSION_KEY);

            // *** SỬA LỖI Ở ĐÂY: Truyền appliedVoucherCode vào getCartView ***
            CartViewDTO cartView = cartService.getCartView(MOCK_USER_ID, appliedVoucherCode);

            model.addAttribute("cartView", cartView);
            model.addAttribute("addresses", addressService.getAddressesByUserId(MOCK_USER_ID));
            model.addAttribute("cartItemCount", cartService.getCartItemCount(MOCK_USER_ID));
            model.addAttribute("currentUri", request.getServletPath());
            model.addAttribute("appliedVoucherCode", appliedVoucherCode);

        } catch (Exception e) {
             model.addAttribute("errorMessage", "Lỗi tải giỏ hàng: " + e.getMessage());
             // Khởi tạo giá trị mặc định để tránh lỗi Thymeleaf
             CartViewDTO emptyCart = new CartViewDTO();
             emptyCart.setItems(List.of()); // Đảm bảo items không null
             model.addAttribute("cartView", emptyCart);
             model.addAttribute("addresses", List.of());
             model.addAttribute("cartItemCount", 0);
             model.addAttribute("currentUri", request.getServletPath());
             e.printStackTrace(); // In lỗi ra console để debug
        }
        return "user/cart"; // Trả về view giỏ hàng
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            // Sử dụng tên phương thức đúng: addItemToCart
            cartService.addItemToCart(MOCK_USER_ID, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm vào giỏ: " + e.getMessage());
             // Nếu lỗi khi thêm từ trang chi tiết, quay lại trang chi tiết
             // Nếu từ menu, quay lại menu (cần kiểm tra header Referer hoặc thêm tham số)
             // Tạm thời quay lại menu
             return "redirect:/user/menu";
        }
        return "redirect:/user/cart"; // Chuyển đến trang giỏ hàng sau khi thêm thành công
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("itemId") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 RedirectAttributes redirectAttributes) {
         if (quantity <= 0) {
              // Gọi hàm xóa nếu số lượng <= 0
              return removeCartItem(cartItemId, redirectAttributes);
         }
        try {
            // Sử dụng tên phương thức đúng: updateItemQuantity
            cartService.updateItemQuantity(MOCK_USER_ID, cartItemId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật giỏ hàng: " + e.getMessage());
        }
        return "redirect:/user/cart"; // Luôn quay lại trang giỏ hàng
    }

    @PostMapping("/remove")
    public String removeCartItem(@RequestParam("itemId") Long cartItemId, RedirectAttributes redirectAttributes) {
        try {
            // Sử dụng tên phương thức đúng: removeItemFromCart
            cartService.removeItemFromCart(MOCK_USER_ID, cartItemId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/user/cart"; // Luôn quay lại trang giỏ hàng
    }

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam("voucherCode") String voucherCode,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
         try {
             CartViewDTO currentCartView = cartService.getCartView(MOCK_USER_ID, null);
             Optional<Voucher> voucherOpt = voucherService.validateAndGetVoucher(voucherCode, currentCartView);

             if (voucherOpt.isPresent()) {
                 session.setAttribute(APPLIED_VOUCHER_SESSION_KEY, voucherCode.trim());
                 redirectAttributes.addFlashAttribute("successMessage", "Đã áp dụng mã giảm giá thành công!");
             } else {
                  session.removeAttribute(APPLIED_VOUCHER_SESSION_KEY);
                  redirectAttributes.addFlashAttribute("errorMessage", "Mã voucher không hợp lệ.");
             }

         } catch (RuntimeException e) {
              session.removeAttribute(APPLIED_VOUCHER_SESSION_KEY);
              redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
         } catch (Exception e) {
             session.removeAttribute(APPLIED_VOUCHER_SESSION_KEY);
             redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi áp dụng mã giảm giá.");
             e.printStackTrace(); // In lỗi ra console
         }
        return "redirect:/user/cart";
    }

    @GetMapping("/remove-voucher")
    public String removeVoucher(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(APPLIED_VOUCHER_SESSION_KEY);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa mã giảm giá.");
        return "redirect:/user/cart";
    }


    @GetMapping("/checkout") // Đổi thành GET nếu nút "Tiến hành thanh toán" là link/button GET
     public String proceedToCheckout(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
         String appliedVoucherCode = (String) session.getAttribute(APPLIED_VOUCHER_SESSION_KEY);
         CartViewDTO cartView = cartService.getCartView(MOCK_USER_ID, appliedVoucherCode);

         if (cartView.getItems() == null || cartView.getItems().isEmpty()) {
              redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống, không thể thanh toán.");
              return "redirect:/user/cart";
         }

         // TODO: Chuyển dữ liệu sang trang checkout
         System.out.println("Proceeding to checkout with cart: " + cartView);
         System.out.println("Applied Voucher: " + appliedVoucherCode);

         // Tạm thời chỉ trả về view checkout (cần tạo file checkout.html)
         // model.addAttribute("cartView", cartView);
         // model.addAttribute("addresses", addressService.getAddressesByUserId(MOCK_USER_ID));
         // return "user/checkout"; // Tên view checkout

         redirectAttributes.addFlashAttribute("infoMessage", "Chức năng thanh toán đang được xây dựng.");
         return "redirect:/user/cart"; // Quay lại giỏ hàng
     }
}