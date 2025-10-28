package AloTra.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Boolean isThumbnail;
}