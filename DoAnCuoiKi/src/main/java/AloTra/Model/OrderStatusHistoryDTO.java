package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDTO {
    private Long id;
    private Long orderId;
    private String status;
    private Long changedByUserId;
    private String changedByUsername;
    private LocalDateTime timestamp;
    private String notes;
}