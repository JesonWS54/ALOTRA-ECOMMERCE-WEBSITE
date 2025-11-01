package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer>, JpaSpecificationExecutor<Brand> {
    Optional<Brand> findByTenThuongHieuIgnoreCase(String tenThuongHieu);
    boolean existsByTenThuongHieuIgnoreCase(String tenThuongHieu);
    boolean existsByTenThuongHieuIgnoreCaseAndMaThuongHieuNot(String tenThuongHieu, Integer brandId);
}
