package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Brand;
import org.springframework.data.jpa.domain.Specification;

public class BrandSpecification {

    public static Specification<Brand> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("tenThuongHieu"), "%" + keyword + "%");
    }

    public static Specification<Brand> hasStatus(Boolean status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("kichHoat"), status);
    }
}
