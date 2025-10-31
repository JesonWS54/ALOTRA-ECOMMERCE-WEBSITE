package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import java.util.List; // QUAN TRỌNG: Thêm import này

public class ProductSpecification {

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("tenSanPham"), "%" + keyword + "%");
    }

    public static Specification<Product> hasStatus(Boolean status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("kichHoat"), status);
    }

    public static Specification<Product> inCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("danhMuc").get("maDanhMuc"), categoryId);
    }

    public static Specification<Product> inBrand(Integer brandId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("thuongHieu").get("maThuongHieu"), brandId);
    }

    public static Specification<Product> inBrands(List<Integer> brandIds) {
        return (root, query, criteriaBuilder) -> root.get("thuongHieu").get("maThuongHieu").in(brandIds);
    }
}