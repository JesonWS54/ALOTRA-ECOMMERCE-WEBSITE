package nhom12.AloTra.repository;

import nhom12.AloTra.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByTenDanhMucIgnoreCase(String tenDanhMuc);
    boolean existsByTenDanhMucIgnoreCase(String tenDanhMuc);
    boolean existsByTenDanhMucIgnoreCaseAndMaDanhMucNot(String tenDanhMuc, Integer categoryId);
}
