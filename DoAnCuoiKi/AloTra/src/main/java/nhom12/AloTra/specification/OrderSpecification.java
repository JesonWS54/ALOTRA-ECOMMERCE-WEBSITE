package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class OrderSpecification {
    public static Specification<Order> filterOrders(String keyword, String status, String paymentMethod, String paymentStatus, String shippingMethod) {
        return (root, query, criteriaBuilder) -> {
            Specification<Order> spec = (r, q, cb) -> cb.conjunction();

            // Lọc theo Mã đơn hàng (keyword)
            if (StringUtils.hasText(keyword)) {
                try {
                    Long orderId = Long.parseLong(keyword);
                    spec = spec.and((r, q, cb) -> cb.equal(r.get("maDonHang"), orderId));
                } catch (NumberFormatException e) {
                    // Nếu người dùng nhập chữ, ta có thể trả về 1 điều kiện luôn sai để không có kết quả nào
                    spec = spec.and((r, q, cb) -> cb.disjunction());
                }
            }

            // Lọc theo Trạng thái đơn hàng
            if (StringUtils.hasText(status)) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("trangThai"), status));
            }

            // Lọc theo Phương thức thanh toán
            if (StringUtils.hasText(paymentMethod)) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("phuongThucThanhToan"), paymentMethod));
            }

            // Lọc theo Trạng thái thanh toán
            if (StringUtils.hasText(paymentStatus)) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("trangThaiThanhToan"), paymentStatus));
            }

            if (StringUtils.hasText(shippingMethod)) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("phuongThucVanChuyen"), shippingMethod));
            }

            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
