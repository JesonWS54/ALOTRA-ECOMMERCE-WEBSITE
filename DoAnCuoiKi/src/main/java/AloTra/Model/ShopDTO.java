package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO {
    private Long id;
    private Long accountId;
    private String accountUsername;
    private String shopName;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String address;
    private Double rating;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}