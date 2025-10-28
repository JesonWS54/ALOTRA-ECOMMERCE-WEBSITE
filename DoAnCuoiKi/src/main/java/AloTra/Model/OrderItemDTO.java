package AloTra.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productThumbnail; // <-- THÊM TRƯỜNG NÀY
    private Integer quantity;
    private Double price; // <-- THÊM TRƯỜNG NÀY
}