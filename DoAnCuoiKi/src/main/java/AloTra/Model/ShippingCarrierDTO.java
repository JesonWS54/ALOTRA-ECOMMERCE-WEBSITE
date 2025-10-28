package AloTra.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCarrierDTO {
    private Long id;
    private String name;
    private Double baseFee;
    private Boolean isActive;
}