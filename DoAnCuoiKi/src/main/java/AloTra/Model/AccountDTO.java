package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String username;
    private String email;
    private String password; // *** THÊM TRƯỜNG NÀY ***
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String role;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
