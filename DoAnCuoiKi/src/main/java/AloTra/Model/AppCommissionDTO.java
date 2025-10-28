package AloTra.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppCommissionDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Double commissionRate;
    private Long adminId;
    private String adminUsername;
}