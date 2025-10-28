package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String transactionCode;
    private Double amount;
    private String paymentGateway;
    private String status;
    private LocalDateTime createdAt;
}