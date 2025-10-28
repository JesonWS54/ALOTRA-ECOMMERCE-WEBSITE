package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List; // Thêm import List

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long accountId;
    private String accountUsername;
    private String accountAvatarUrl; // *** THÊM TRƯỜNG NÀY ***
    private Long productId;
    // private String productName; // Bỏ vì đã ở trang chi tiết sản phẩm
    private Long orderItemId;
    private Integer rating;
    private String comment;
    private List<String> mediaUrls; // *** THÊM TRƯỜNG NÀY ***
    private LocalDateTime createdAt;
}