package AloTra.repository;

import AloTra.entity.Category;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Category.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tự động tìm tất cả Category đang active.
     * @return List<Category>
     */
    List<Category> findByIsActiveTrue();

    /**
     * Kiểm tra xem tên danh mục đã tồn tại chưa (không phân biệt hoa thường).
     * @param name Tên danh mục cần kiểm tra.
     * @return true nếu tên đã tồn tại, false nếu chưa.
     */
    boolean existsByNameIgnoreCase(String name); // *** THÊM PHƯƠNG THỨC NÀY ***

    /**
     * Tìm các danh mục gốc (không có parent) và đang active, sắp xếp theo tên.
     * @param sort Thông tin sắp xếp (vd: Sort.by("name")).
     * @return List<Category>
     */
    List<Category> findByParentIsNullAndIsActiveTrue(Sort sort); // *** THÊM PHƯƠNG THỨC NÀY ***

    /**
     * Tìm các danh mục con trực tiếp của một danh mục cha.
     * @param parentId ID của danh mục cha.
     * @return List<Category>
     */
    List<Category> findByParent_Id(Long parentId); // *** THÊM PHƯƠNG THỨC NÀY ***

}

