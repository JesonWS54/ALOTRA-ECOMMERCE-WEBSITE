package nhom17.OneShop.specification;

import nhom17.OneShop.entity.Import;
import org.springframework.data.jpa.domain.Specification;

public class ImportSpecification {

    public static Specification<Import> hasId(Integer id) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("maPhieuNhap"), id);
    }

    public static Specification<Import> hasSupplier(Integer supplierId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("nhaCungCap").get("maNCC"), supplierId);
    }
}
