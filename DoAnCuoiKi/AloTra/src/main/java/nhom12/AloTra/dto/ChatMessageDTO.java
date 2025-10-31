package nhom12.AloTra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO để truyền dữ liệu tin nhắn giữa Frontend và Backend
 * Không expose toàn bộ Entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private Long maTinNhan;
    private String maPhienChat;
    private String noiDung;
    private String loaiNguoiGui;  // "CUSTOMER" hoặc "ADMIN"
    private LocalDateTime thoiGian;
    private Boolean daXem;
    
    // Thông tin thêm cho hiển thị
    private String tenNguoiGui;  // Tên người gửi (nếu có)
    private String avatarUrl;    // Avatar (nếu cần)
}