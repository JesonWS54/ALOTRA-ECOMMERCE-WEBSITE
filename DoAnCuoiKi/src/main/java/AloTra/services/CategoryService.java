package AloTra.services;

import AloTra.Model.CategoryDTO; // Import CategoryDTO
import AloTra.entity.Category;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException; // Import IOException
import java.util.List;
import java.util.Optional; // Import Optional

/**
 * Interface cho logic nghiệp vụ liên quan đến Category.
 */
public interface CategoryService {

    /**
     * Lấy tất cả danh mục đang hoạt động (active) - Dùng cho User & Vendor
     * @return List<Category>
     */
    List<Category> getActiveCategories();

    // --- **QUẢN LÝ ADMIN** ---

    /**
     * Lấy tất cả danh mục (bao gồm cả active và inactive) - Dùng cho Admin (phân trang).
     * @param pageable Thông tin phân trang.
     * @return Page<CategoryDTO>
     */
    Page<CategoryDTO> getAllCategoriesAdmin(Pageable pageable); // *** THÊM ***

    /**
     * Lấy danh sách tất cả các danh mục gốc (không có parent) để làm dropdown khi tạo/sửa.
     * @return List<CategoryDTO>
     */
    List<CategoryDTO> getRootCategoriesAdmin(); // *** THÊM ***

    /**
     * Lấy thông tin chi tiết một danh mục để sửa.
     * @param categoryId ID danh mục.
     * @return Optional<CategoryDTO>
     */
    Optional<CategoryDTO> getCategoryByIdAdmin(Long categoryId); // *** THÊM ***

    /**
     * Thêm danh mục mới.
     * @param categoryDTO Dữ liệu danh mục mới (tên, parentId, isActive).
     * @param imageFile Ảnh danh mục (có thể null).
     * @return CategoryDTO đã được tạo.
     * @throws IOException Nếu có lỗi upload ảnh.
     * @throws RuntimeException Nếu tên danh mục đã tồn tại hoặc parentId không hợp lệ.
     */
    @Transactional
    CategoryDTO addCategoryAdmin(CategoryDTO categoryDTO, MultipartFile imageFile) throws IOException; // *** THÊM ***

    /**
     * Cập nhật thông tin danh mục.
     * @param categoryId ID danh mục cần cập nhật.
     * @param categoryDTO Dữ liệu mới (tên, parentId, isActive).
     * @param imageFile Ảnh danh mục mới (có thể null, nếu null thì giữ ảnh cũ).
     * @return CategoryDTO đã được cập nhật.
     * @throws IOException Nếu có lỗi upload ảnh.
     * @throws RuntimeException Nếu không tìm thấy danh mục, tên trùng lặp, hoặc parentId không hợp lệ.
     */
    @Transactional
    CategoryDTO updateCategoryAdmin(Long categoryId, CategoryDTO categoryDTO, MultipartFile imageFile) throws IOException; // *** THÊM ***

    /**
     * Xóa danh mục (có thể là xóa mềm hoặc xóa cứng tùy logic).
     * Cần xử lý các danh mục con và sản phẩm liên quan.
     * @param categoryId ID danh mục cần xóa.
     * @throws RuntimeException Nếu không tìm thấy danh mục hoặc không thể xóa (vd: còn sản phẩm).
     */
    @Transactional
    void deleteCategoryAdmin(Long categoryId); // *** THÊM ***

}

