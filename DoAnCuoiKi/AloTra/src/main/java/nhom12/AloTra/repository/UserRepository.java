package nhom12.AloTra.repository;

import nhom12.AloTra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTenDangNhapIgnoreCase(String tenDangNhap);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTenDangNhapIgnoreCase(String tenDangNhap);
    boolean existsByEmailIgnoreCaseAndMaNguoiDungNot(String email, Integer userId);
    boolean existsByTenDangNhapIgnoreCaseAndMaNguoiDungNot(String tenDangNhap, Integer userId);
    boolean existsByVaiTro_MaVaiTro(Integer roleId);
    boolean existsByHangThanhVien_MaHangThanhVien(Integer tierId);

//    User
    Optional<User> findByTenDangNhap(String tenDangNhap);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.hangThanhVien WHERE u.email = :email")
    Optional<User> findByEmailWithMembership(@Param("email") String email);
}