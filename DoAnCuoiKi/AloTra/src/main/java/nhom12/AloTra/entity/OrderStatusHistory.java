package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "LichSuTrangThaiDon")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maLichSu;
    private String tuTrangThai;
    private String denTrangThai;

    private LocalDateTime thoiDiemThayDoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang")
    private Order donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaQuanTriVien")
    private User nguoiThucHien;
}
