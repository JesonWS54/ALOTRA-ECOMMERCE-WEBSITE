package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private Long accountId;
    private String accountUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}