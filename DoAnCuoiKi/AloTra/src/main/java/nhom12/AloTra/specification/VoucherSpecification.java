package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Voucher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class VoucherSpecification {

    public static Specification<Voucher> filterByCriteria(String keyword, Integer status, Integer type) {
        return (root, query, criteriaBuilder) -> {
            Specification<Voucher> spec = (root1, query1, cb) -> cb.conjunction();
            if (StringUtils.hasText(keyword)) {
                spec = spec.and((r, q, cb) -> cb.like(r.get("maKhuyenMai"), "%" + keyword + "%"));
            }
            if (status != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("trangThai"), status));
            }
            if (type != null) {
                spec = spec.and((r, q, cb) -> cb.equal(r.get("kieuApDung"), type));
            }
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
}
