package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {
    public static Specification<Category> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("tenDanhMuc"), "%" + keyword + "%");
    }

    public static Specification<Category> hasStatus(Boolean status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("kichHoat"), status);
    }
}
