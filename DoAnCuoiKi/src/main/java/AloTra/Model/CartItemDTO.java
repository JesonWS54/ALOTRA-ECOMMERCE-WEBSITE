package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productThumbnail; // URL ảnh
    private Long productShopId; // <-- Thêm ID Shop của sản phẩm
    private Integer quantity;
    private Double priceAtAdd; // Giá tại thời điểm thêm
    private LocalDateTime addedAt;
}