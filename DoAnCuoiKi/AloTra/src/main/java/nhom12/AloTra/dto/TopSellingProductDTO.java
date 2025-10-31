package nhom12.AloTra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDTO {
    private String productName;
    private String imageUrl;
    private long totalQuantity;
    private BigDecimal totalRevenue;
}
