package nhom17.OneShop.specification;

import nhom17.OneShop.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> cb.like(root.get("tenDangNhap"), "%" + username + "%");
    }

    public static Specification<User> hasRole(Integer roleId) {
        return (root, query, cb) -> cb.equal(root.get("vaiTro").get("maVaiTro"), roleId);
    }

    public static Specification<User> hasMembershipTier(Integer tierId) {
        return (root, query, cb) -> cb.equal(root.get("hangThanhVien").get("maHangThanhVien"), tierId);
    }

    public static Specification<User> hasStatus(Integer status) {
        return (root, query, cb) -> cb.equal(root.get("trangThai"), status);
    }
}
