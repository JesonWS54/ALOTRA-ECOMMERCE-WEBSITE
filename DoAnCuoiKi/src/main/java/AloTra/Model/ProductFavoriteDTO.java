package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFavoriteDTO {
    private Long id;
    private Long accountId;
    private String accountUsername;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
}