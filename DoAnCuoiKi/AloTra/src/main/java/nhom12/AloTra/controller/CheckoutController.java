package nhom12.AloTra.controller;

import jakarta.servlet.http.HttpSession;
import nhom12.AloTra.dto.ShippingOptionDTO; // **Import DTO**
import nhom12.AloTra.entity.Address;
import nhom12.AloTra.entity.Cart;
// Remove ShippingFee import if not used directly

import nhom12.AloTra.entity.User;
import nhom12.AloTra.repository.AddressRepository;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.service.CartService;
import nhom12.AloTra.service.CheckoutService;
import nhom12.AloTra.service.ShippingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Import if missing
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI; // Import if missing
import java.security.Principal; // Import if missing
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CheckoutController {

    @Autowired private CartService cartService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private AddressRepository diaChiRepository;
    @Autowired private UserRepository nguoiDungRepository;
    @Autowired private ShippingFeeService shippingFeeService; // Ensure this is Autowired

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        List<Cart> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        User currentUser = getCurrentUser(); // Assume user is logged in for checkout
        List<Address> addresses = diaChiRepository.findByNguoiDung_MaNguoiDung(currentUser.getMaNguoiDung());

        BigDecimal subtotal = cartService.getSubtotal(); // Use service method

        // --- Discount Logic (Membership first, then Coupon) ---
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        Optional<User> userOpt = nguoiDungRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userOpt.isPresent() && userOpt.get().getHangThanhVien() != null) {
            BigDecimal percent = userOpt.get().getHangThanhVien().getPhanTramGiamGia();
            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                membershipDiscount = subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                model.addAttribute("membershipTier", userOpt.get().getHangThanhVien());
            }
        }
        BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);
        BigDecimal couponDiscountValue = (BigDecimal) session.getAttribute("cartDiscount");
        couponDiscountValue = (couponDiscountValue == null) ? BigDecimal.ZERO : couponDiscountValue;
        BigDecimal actualCouponDiscount = couponDiscountValue.min(priceAfterMembershipDiscount);
        // --- End Discount Logic ---

        BigDecimal shippingFee = BigDecimal.ZERO; // Initial shipping fee is 0, JS will update
        BigDecimal total = priceAfterMembershipDiscount.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("couponDiscount", actualCouponDiscount); // Send actual applied coupon value
        model.addAttribute("membershipDiscount", membershipDiscount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);

        return "user/shop/checkout";
    }

    // ===== START: MODIFIED API - works with DTO =====
    @GetMapping("/api/shipping-options")
    @ResponseBody
    public ResponseEntity<?> getShippingOptions(@RequestParam("province") String province) {
        try {
            BigDecimal subtotal = cartService.getSubtotal();
            // Service now returns Optional<ShippingOptionDTO>
            Optional<ShippingOptionDTO> cheapestOptionDto = shippingFeeService.findCheapestShippingOption(province, subtotal);

            if (cheapestOptionDto.isPresent()) {
                // Return the DTO
                return ResponseEntity.ok(cheapestOptionDto.get());
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy phương thức vận chuyển phù hợp cho tỉnh/thành này.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi khi tính phí vận chuyển: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    // ===== END: MODIFIED API =====

    // ===== START: MODIFIED placeOrder - accepts shipping details =====
    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("shipping_address") Integer diaChiId,
                             @RequestParam("payment_method") String paymentMethod,
                             // **Accept hidden input values from the form**
                             @RequestParam("calculated_shipping_fee") BigDecimal shippingFee,
                             @RequestParam("shipping_method_name") String shippingMethodName,
                             RedirectAttributes redirectAttributes) {
        try {
            // **Input Validation (Important!)**
            if (shippingMethodName == null || shippingMethodName.isBlank()) {
                // This might happen if JS failed to set the value before submit
                throw new IllegalArgumentException("Chưa chọn được phương thức vận chuyển hợp lệ. Vui lòng chọn lại địa chỉ.");
            }
            if (shippingFee == null || shippingFee.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Phí vận chuyển không hợp lệ.");
            }

            // Payment method routing
            if ("COD".equalsIgnoreCase(paymentMethod)) {
                // **Pass shipping details to the service**
                // **MAKE SURE your checkoutService.placeOrder method accepts these parameters**
                checkoutService.placeOrder(diaChiId, "COD", shippingFee, "Tiết Kiệm");
                return "redirect:/order-success?method=COD";
            } else if ("MOMO".equalsIgnoreCase(paymentMethod)) {
                // TODO: Implement MoMo logic (needs order info, shipping details)
                redirectAttributes.addFlashAttribute("info", "Chức năng thanh toán MoMo đang được phát triển.");
                return "redirect:/checkout/momo";
            } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                // TODO: Implement VNPAY logic (needs order info, shipping details)
                redirectAttributes.addFlashAttribute("info", "Chức năng thanh toán VNPAY đang được phát triển.");
                return "redirect:/checkout/vnpay";
            } else {
                redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ.");
                return "redirect:/checkout";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi đặt hàng: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return "redirect:/checkout"; // Redirect back to checkout on error
        }
    }
    // ===== END: MODIFIED placeOrder =====

    // --- Other methods (Keep existing) ---
    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam(value = "method", required = false) String method, Model model) {
        model.addAttribute("method", method);
        return "user/shop/order-success";
    }

    @GetMapping("/checkout/edit-address/{id}")
    public String showEditAddressForm(@PathVariable("id") Integer addressId, Model model) {
        Address address = diaChiRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại."));
        model.addAttribute("address", address);
        return "user/shop/edit-address";
    }

    @PostMapping("/checkout/save-address")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "return", required = false) String returnUrl,
                              RedirectAttributes ra) {
        // **IMPORTANT: Set the current user to the address before saving**
        try {
            User currentUser = getCurrentUser();
            address.setNguoiDung(currentUser);
            diaChiRepository.save(address);
            ra.addFlashAttribute("success", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            // Decide where to redirect on error, maybe back to edit form?
            // return "redirect:/checkout/edit-address/" + address.getMaDiaChi();
        }


        if (returnUrl != null && !returnUrl.isBlank()) {
            if (returnUrl.startsWith("/")) {
                return "redirect:" + returnUrl;
            }
            return "redirect:/my-account?tab=addresses"; // Fallback
        }
        return "redirect:/checkout"; // Default redirect
    }

    @GetMapping("/checkout/momo")
    public String momoGuide() { return "user/shop/momo-guide"; }

    @GetMapping("/checkout/vnpay")
    public String vnpayGuide() { return "user/shop/vnpay-guide"; }

    // Helper method to get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Người dùng chưa đăng nhập."); // Throw exception if not logged in
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString(); // Fallback
        }
        return nguoiDungRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại trong CSDL."));
    }
}