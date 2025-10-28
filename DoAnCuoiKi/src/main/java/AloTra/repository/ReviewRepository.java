package AloTra.repository;

import AloTra.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Tìm danh sách đánh giá theo ID sản phẩm, có phân trang.
     * Việc sắp xếp sẽ được xử lý bởi Pageable từ Service.
     * @param productId ID của sản phẩm.
     * @param pageable Thông tin phân trang (và sắp xếp).
     * @return Page chứa các đánh giá.
     */
    // *** ĐỔI TÊN PHƯƠNG THỨC ***
    Page<Review> findByProduct_Id(Long productId, Pageable pageable);
    
    List<Review> findByProduct_Id(Long productId); // <-- Thêm phương thức này

    // Có thể thêm các query khác nếu cần
    
    
    boolean existsByOrderItem_Id(Long orderItemId);
}