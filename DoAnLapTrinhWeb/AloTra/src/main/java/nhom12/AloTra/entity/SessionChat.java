package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho một phiên chat giữa khách hàng và admin
 * Mỗi khách hàng có 1 phiên chat riêng biệt
 */
@Data
@Entity
@Table(name = "PhienChat")
public class  SessionChat {
    
    @Id
    @Column(name = "MaPhienChat", length = 100)
    private String maPhienChat;  // VD: "session_1702123456789_abc123"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung", nullable = true)
    private User nguoiDung;  // NULL nếu khách vãng lai
    
    @Column(name = "TenKhach", length = 150)
    private String tenKhach;  // Tên khách nếu chưa đăng nhập
    
    @Column(name = "EmailKhach", length = 255)
    private String emailKhach;  // Email khách nếu chưa đăng nhập
    
    @Column(name = "TinNhanDauTien")
    private LocalDateTime tinNhanDauTien;
    
    @Column(name = "TinNhanCuoiCung")
    private LocalDateTime tinNhanCuoiCung;
    
    @Column(name = "TrangThai", length = 20)
    private String trangThai = "Đang mở";  // "Đang mở" hoặc "Đã đóng"
    
    @Column(name = "SoTinChuaDoc")
    private Integer soTinChuaDoc = 0;  // Số tin admin chưa đọc
}