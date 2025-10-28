package AloTra.repository;

import AloTra.entity.ReviewMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {

    // Tìm tất cả media theo ID của Review
    List<ReviewMedia> findByReview_Id(Long reviewId);

}