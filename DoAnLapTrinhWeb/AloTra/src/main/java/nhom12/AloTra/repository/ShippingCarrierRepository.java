package nhom12.AloTra.repository;

import nhom12.AloTra.entity.ShippingCarrier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Integer> {
    Page<ShippingCarrier> findByTenNVCContainingIgnoreCase(String keyword, Pageable pageable);
    Optional<ShippingCarrier> findByTenNVCIgnoreCase(String tenNVC);
    boolean existsByTenNVCIgnoreCase(String tenNVC);
    boolean existsByTenNVCIgnoreCaseAndMaNVCNot(String tenNVC, Integer maNVC);
}
