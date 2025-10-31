package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.*;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private HttpSession httpSession;
    @Autowired private VoucherRepository voucherRepository;


    @Override
    @Transactional
    public Order placeOrder(Integer diaChiId, String paymentMethod, BigDecimal shippingFee, String shippingMethodName) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập."));
        List<Cart> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }
        Address shippingAddress = addressRepository.findById(diaChiId)
                .filter(addr -> Objects.equals(addr.getNguoiDung().getMaNguoiDung(), currentUser.getMaNguoiDung()))
                .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ."));

        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal membershipDiscount = BigDecimal.ZERO;
        Optional<User> userWithTierOpt = userRepository.findByEmailWithMembership(currentUser.getEmail());
        if (userWithTierOpt.isPresent() && userWithTierOpt.get().getHangThanhVien() != null) {
            BigDecimal percent = userWithTierOpt.get().getHangThanhVien().getPhanTramGiamGia();
            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                membershipDiscount = subtotal.multiply(percent.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            }
        }
        BigDecimal priceAfterMembership = subtotal.subtract(membershipDiscount).max(BigDecimal.ZERO);

        BigDecimal actualCouponDiscount = BigDecimal.ZERO;
        String appliedCouponCode = (String) httpSession.getAttribute("appliedCouponCode");
        Voucher appliedVoucher = null; // Khởi tạo voucher áp dụng là null
        if (appliedCouponCode != null) {
            Optional<Voucher> voucherOpt = voucherRepository.findByMaKhuyenMaiAndTrangThai(appliedCouponCode, 1);
            // Kiểm tra lại điều kiện voucher tại thời điểm đặt hàng
            if (voucherOpt.isPresent()
                    && voucherOpt.get().getBatDauLuc().isBefore(LocalDateTime.now())
                    && voucherOpt.get().getKetThucLuc().isAfter(LocalDateTime.now())
                    && (voucherOpt.get().getTongTienToiThieu() == null || priceAfterMembership.compareTo(voucherOpt.get().getTongTienToiThieu()) >= 0))
            {
                appliedVoucher = voucherOpt.get(); // Gán voucher nếu hợp lệ
                BigDecimal discountValue = appliedVoucher.getGiaTri();
                // Tính giá trị giảm thực tế
                if (appliedVoucher.getKieuApDung() != null && appliedVoucher.getKieuApDung() == 0) { // Percentage
                    actualCouponDiscount = priceAfterMembership.multiply(discountValue.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    if (appliedVoucher.getGiamToiDa() != null && actualCouponDiscount.compareTo(appliedVoucher.getGiamToiDa()) > 0) {
                        actualCouponDiscount = appliedVoucher.getGiamToiDa();
                    }
                } else { // Fixed amount
                    actualCouponDiscount = discountValue;
                }
                actualCouponDiscount = actualCouponDiscount.min(priceAfterMembership); // Đảm bảo không âm
            } else {
                // Nếu coupon không còn hợp lệ, xóa khỏi session và không áp dụng
                httpSession.removeAttribute("cartDiscount");
                httpSession.removeAttribute("appliedCouponCode");
                appliedVoucher = null; // Đảm bảo voucher là null
                actualCouponDiscount = BigDecimal.ZERO;
            }
        }
        BigDecimal finalTotal = priceAfterMembership.subtract(actualCouponDiscount).add(shippingFee).max(BigDecimal.ZERO);

        // Create Order
        Order order = new Order();
        order.setNguoiDung(currentUser);
        order.setNgayDat(LocalDateTime.now());
        order.setTrangThai("Đang xử lý");
        order.setPhuongThucThanhToan(paymentMethod);
        order.setTrangThaiThanhToan("Chưa thanh toán");
        order.setTienHang(subtotal);

        order.setKhuyenMai(appliedVoucher);

        order.setPhiVanChuyen(shippingFee);
        order.setPhuongThucVanChuyen(shippingMethodName);
        order.setTongTien(finalTotal);
        order.setTenNguoiNhan(shippingAddress.getTenNguoiNhan());
        order.setSoDienThoaiNhan(shippingAddress.getSoDienThoai());
        String fullAddress = String.format("%s, %s, %s, %s",
                shippingAddress.getSoNhaDuong(), shippingAddress.getPhuongXa(),
                shippingAddress.getQuanHuyen(), shippingAddress.getTinhThanh());
        order.setDiaChiNhan(fullAddress);

        order.setDiaChi(shippingAddress);


        Order savedOrder = orderRepository.save(order);

        // Create Order Details and update Inventory
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Cart cartItem : cartItems) {
            Product product = cartItem.getSanPham();
            int orderedQuantity = cartItem.getSoLuong();

            OrderDetail detail = new OrderDetail();
            detail.setDonHang(savedOrder);
            detail.setSanPham(product);
            detail.setTenSanPham(product.getTenSanPham());
            detail.setDonGia(cartItem.getDonGia());
            detail.setSoLuong(orderedQuantity);
            orderDetails.add(detail);

            Inventory inventory = inventoryRepository.findById(product.getMaSanPham())
                    .orElseThrow(() -> new RuntimeException("Hết hàng tồn kho cho sản phẩm: " + product.getTenSanPham()));
            if (inventory.getSoLuongTon() < orderedQuantity) {
                throw new IllegalStateException("Số lượng tồn kho không đủ cho sản phẩm: " + product.getTenSanPham());
            }
            inventory.setSoLuongTon(inventory.getSoLuongTon() - orderedQuantity);
            inventoryRepository.save(inventory);
        }

        orderDetailRepository.saveAll(orderDetails);

        cartService.clearCart(); // Xóa giỏ hàng

        // Xóa thông tin giảm giá khỏi session
        httpSession.removeAttribute("cartDiscount");
        httpSession.removeAttribute("appliedCouponCode");

        return savedOrder;
    }

    // Helper method
    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(username);
    }
}