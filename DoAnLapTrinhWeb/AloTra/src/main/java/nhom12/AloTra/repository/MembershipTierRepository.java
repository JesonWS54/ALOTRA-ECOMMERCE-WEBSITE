package nhom12.AloTra.repository;

import nhom12.AloTra.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Integer> {
    Optional<MembershipTier> findByTenHangIgnoreCase(String tenHang);
    Optional<MembershipTier> findByDiemToiThieu(Integer diemToiThieu);
    boolean existsByTenHangIgnoreCase(String tenHang);
    boolean existsByDiemToiThieu(Integer diemToiThieu);
    boolean existsByTenHangIgnoreCaseAndMaHangThanhVienNot(String tenHang, Integer id);
    boolean existsByDiemToiThieuAndMaHangThanhVienNot(Integer diemToiThieu, Integer id);
    List<MembershipTier> findByDiemToiThieuLessThanEqualOrderByDiemToiThieuDesc(int diemToiThieu);
}
