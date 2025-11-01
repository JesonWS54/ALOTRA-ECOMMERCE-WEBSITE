package nhom12.AloTra.controller;

import jakarta.servlet.http.HttpSession;
import nhom12.AloTra.entity.Cart;
import nhom12.AloTra.dto.CartItemDTO;
import nhom12.AloTra.entity.User; // Import User
import nhom12.AloTra.entity.Voucher;
import nhom12.AloTra.repository.UserRepository; // Import UserRepository
import nhom12.AloTra.repository.VoucherRepository;
import nhom12.AloTra.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private VoucherRepository khuyenMaiRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        List<Cart> cartItems = cartService.getCartItems();

        BigDecimal subtotal = cartService.getSubtotal();

        BigDecimal membershipDiscount = BigDecimal.ZERO;
        User currentUser = null; // Khởi tạo là null
        try {
            currentUser = getCurrentUser(); // Lấy User từ phương thức helper của Controller
            if (currentUser != null) {
                Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
                if (userWithTierOpt.isPresent() && userWithTierOpt.get().getHangThanhVien() != null) {
                    User userWithTier = userWithTierOpt.get();
                    BigDecimal percent = userWithTier.getHangThanhVien().getPhanTramGiamGia();
                    if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                        membershipDiscount = subtotal.multiply(
                                percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                        );
                        model.addAttribute("membershipTier", userWithTier.getHangThanhVien());
                    }
                }
            }
        } catch (IllegalStateException e) {
            // Người dùng chưa đăng nhập, bỏ qua tính giảm giá thành viên
            System.out.println("User not logged in, skipping membership discount.");
        }


        BigDecimal priceAfterMembershipDiscount = subtotal.subtract(membershipDiscount);
        if (priceAfterMembershipDiscount.compareTo(BigDecimal.ZERO) < 0) {
            priceAfterMembershipDiscount = BigDecimal.ZERO;
        }

        BigDecimal couponDiscountValue = (BigDecimal) session.getAttribute("cartDiscount");
        if (couponDiscountValue == null) {
            couponDiscountValue = BigDecimal.ZERO;
        }

        BigDecimal actualCouponDiscount = couponDiscountValue.min(priceAfterMembershipDiscount);

        BigDecimal total = priceAfterMembershipDiscount.subtract(actualCouponDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("membershipDiscount", membershipDiscount);
        model.addAttribute("discount", actualCouponDiscount);
        model.addAttribute("total", total);
        model.addAttribute("appliedCouponCode", session.getAttribute("appliedCouponCode"));

        return "user/shop/cart";
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@RequestParam("productId") Integer productId,
                                       @RequestParam(name="quantity", defaultValue="1") int quantity,
                                       @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {

        if (principal == null) { // Kiểm tra đăng nhập
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm sản phẩm.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/sign-in")).build();
            }
        }

        try {
            cartService.addToCart(productId, quantity); // Thực hiện thêm vào giỏ
            List<Cart> updatedCartEntities = cartService.getCartItems(); // Lấy lại danh sách entity

            // **Chuyển đổi List<Cart> thành List<CartItemDTO>**
            List<CartItemDTO> updatedCartDTOs = updatedCartEntities.stream()
                    .map(CartItemDTO::fromEntity)
                    .collect(Collectors.toList());
            // Tính tổng số lượng item (hoặc tổng số lượng sản phẩm tùy bạn muốn)
            int newCount = updatedCartDTOs.size(); // Số loại sản phẩm
            // int totalQuantity = updatedCartDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum(); // Tổng số lượng

            // Nếu là AJAX, trả về DTO
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Đã thêm sản phẩm vào giỏ hàng!");
                response.put("cartCount", newCount); // Gửi số lượng item
                response.put("cartItems", updatedCartDTOs); // **Gửi danh sách DTO**
                return ResponseEntity.ok(response);
            } else {
                // Nếu không phải AJAX, chuyển hướng như cũ
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng!");
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/cart")).build();
            }
        } catch (RuntimeException e) { // Xử lý lỗi
            if ("XMLHttpRequest".equals(requestedWith)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                String referer = "/product/" + productId;
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(referer)).build();
            }
        }
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("productId") Integer productId, @RequestParam("quantity") int quantity, RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
        try {
            cartService.updateQuantity(productId, quantity);
        } catch (Exception e) {
            // Gửi thông báo lỗi ra view
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("productId") Integer productId, RedirectAttributes redirectAttributes) { // Thêm RedirectAttributes
        try {
            cartService.removeItem(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng."); // Thêm thông báo thành công
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage()); // Thêm thông báo lỗi
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/apply-coupon")
    public String applyCoupon(@RequestParam("coupon_code") String couponCode, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Voucher> couponOpt = khuyenMaiRepository.findByMaKhuyenMaiAndTrangThai(couponCode, 1);

        // **QUAN TRỌNG: Kiểm tra điều kiện coupon trước khi áp dụng**
        BigDecimal subtotalAfterMembership = BigDecimal.ZERO; // Cần tính lại subtotal sau khi trừ membership
        // Lấy subtotal gốc
        BigDecimal originalSubtotal = cartService.getSubtotal();
        // Tính lại membership discount (logic tương tự viewCart)
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        User currentUser = null;
        try { currentUser = getCurrentUser(); } catch (IllegalStateException e) { /* Bỏ qua nếu chưa đăng nhập */ }
        if (currentUser != null) {
            Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
            if (userWithTierOpt.isPresent() && userWithTierOpt.get().getHangThanhVien() != null) {
                BigDecimal percent = userWithTierOpt.get().getHangThanhVien().getPhanTramGiamGia();
                if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                    membershipDiscount = originalSubtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                }
            }
        }
        subtotalAfterMembership = originalSubtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);


        if (couponOpt.isPresent()
                && couponOpt.get().getBatDauLuc().isBefore(LocalDateTime.now()) // Đã bắt đầu
                && couponOpt.get().getKetThucLuc().isAfter(LocalDateTime.now())  // Chưa kết thúc
                // Kiểm tra tổng tiền tối thiểu (so sánh với giá SAU KHI trừ membership)
                && (couponOpt.get().getTongTienToiThieu() == null || subtotalAfterMembership.compareTo(couponOpt.get().getTongTienToiThieu()) >= 0)
            // TODO: Kiểm tra số lần sử dụng tổng và mỗi người nếu cần
        )
        {
            Voucher coupon = couponOpt.get();
            BigDecimal discountAmount = BigDecimal.ZERO;

            // Tính giá trị giảm giá dựa trên kiểu (0: Phần trăm, 1: Số tiền cố định)
            if (coupon.getKieuApDung() != null && coupon.getKieuApDung() == 0) { // Giảm theo %
                discountAmount = subtotalAfterMembership.multiply(coupon.getGiaTri().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
                // Kiểm tra giới hạn giảm tối đa
                if (coupon.getGiamToiDa() != null && discountAmount.compareTo(coupon.getGiamToiDa()) > 0) {
                    discountAmount = coupon.getGiamToiDa();
                }
            } else if (coupon.getKieuApDung() != null && coupon.getKieuApDung() == 1) { // Giảm số tiền cố định
                discountAmount = coupon.getGiaTri();
            }

            // Đảm bảo giảm giá không làm âm tiền
            discountAmount = discountAmount.min(subtotalAfterMembership);

            session.setAttribute("cartDiscount", discountAmount); // Lưu giá trị đã tính toán
            session.setAttribute("appliedCouponCode", coupon.getMaKhuyenMai());
            redirectAttributes.addFlashAttribute("success", "Áp dụng mã giảm giá thành công!");
        } else {
            session.removeAttribute("cartDiscount");
            session.removeAttribute("appliedCouponCode");
            redirectAttributes.addFlashAttribute("error", "Mã giảm giá không hợp lệ, hết hạn hoặc không đủ điều kiện.");
        }
        return "redirect:/cart";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("Người dùng chưa đăng nhập."); // Ném lỗi nếu chưa đăng nhập
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng hiện tại trong CSDL."));
    }
}