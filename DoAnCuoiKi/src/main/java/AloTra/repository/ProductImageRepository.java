package AloTra.repository;

import AloTra.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Tự động tìm danh sách ProductImage dựa trên ID của Product liên kết.
     */
    List<ProductImage> findByProduct_Id(Long productId);

    /**
     * Tìm ảnh thumbnail cho sản phẩm.
     * @param productId ID sản phẩm.
     * @return Optional chứa ProductImage nếu tìm thấy thumbnail.
     */
    Optional<ProductImage> findByProductIdAndIsThumbnailTrue(Long productId);

    /**
     * Tìm ảnh đầu tiên của sản phẩm (sắp xếp theo ID tăng dần).
     * Dùng làm fallback nếu không có ảnh nào được đánh dấu là thumbnail.
     * @param productId ID sản phẩm.
     * @return Optional chứa ProductImage đầu tiên nếu có.
     */
    // *** THÊM PHƯƠNG THỨC NÀY ***
    Optional<ProductImage> findFirstByProductIdOrderByIdAsc(Long productId);

}
