package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long>, JpaSpecificationExecutor<Shipping> {
    boolean existsByDonHang_MaDonHang(Long maDonHang);
}
