package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Shipping;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ShippingSpecification {
    public static Specification<Shipping> filterBy(String keyword, Integer carrierId, String status, String shippingMethod) {
        return (root, query, cb) -> {
            Specification<Shipping> spec = (r, q, builder) -> builder.conjunction();

            if (StringUtils.hasText(keyword)) {
                try {
                    // Cố gắng chuyển đổi từ khóa thành số (cho Mã Đơn hàng)
                    Long orderId = Long.parseLong(keyword);
                    // Nếu thành công, tìm theo Mã ĐH HOẶC Mã vận đơn
                    spec = spec.and((r, q, builder) ->
                            builder.or(
                                    cb.equal(r.get("donHang").get("maDonHang"), orderId),
                                    cb.like(r.get("maVanDon"), "%" + keyword + "%")
                            )
                    );
                } catch (NumberFormatException e) {
                    // Nếu không phải là số, chỉ tìm theo Mã vận đơn
                    spec = spec.and((r, q, builder) -> cb.like(r.get("maVanDon"), "%" + keyword + "%"));
                }
            }

            if (carrierId != null) {
                spec = spec.and((r, q, builder) -> builder.equal(r.get("nhaVanChuyen").get("maNVC"), carrierId));
            }

            if (StringUtils.hasText(status)) {
                spec = spec.and((r, q, builder) -> builder.equal(r.get("trangThai"), status));
            }

            if (StringUtils.hasText(shippingMethod)) {
                spec = spec.and((r, q, builder) -> builder.equal(r.get("donHang").get("phuongThucVanChuyen"), shippingMethod));
            }

            return spec.toPredicate(root, query, cb);
        };
    }
}
