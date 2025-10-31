package nhom12.AloTra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho một conversation trong Admin Dashboard
 * Hiển thị thông tin tóm tắt của phiên chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    
    private String maPhienChat;
    private String tenKhach;
    private String emailKhach;
    private String tinNhanCuoi;      // Nội dung tin nhắn cuối
    private LocalDateTime thoiGianCuoi;
    private Integer soTinChuaDoc;
    private String trangThai;
    
    // Thông tin user (nếu đã đăng nhập)
    private Integer maNguoiDung;
    private String avatarUrl;
}