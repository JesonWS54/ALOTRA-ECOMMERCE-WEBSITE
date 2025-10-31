package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Page<Supplier> findByTenNCCContainingIgnoreCase(String keyword, Pageable pageable);
    Optional<Supplier> findByTenNCCIgnoreCase(String tenNCC);
    boolean existsByTenNCCIgnoreCase(String tenNCC);
    boolean existsByTenNCCIgnoreCaseAndMaNCCNot(String tenNCC, Integer supplierId);
}
