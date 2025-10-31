package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String>, JpaSpecificationExecutor<Voucher> {
    boolean existsByTenChienDichIgnoreCase(String tenChienDich);

    Optional<Voucher> findByMaKhuyenMaiAndTrangThai(String maKhuyenMai, Integer trangThai);
}