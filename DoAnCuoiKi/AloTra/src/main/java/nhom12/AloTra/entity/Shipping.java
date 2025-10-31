package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "VanChuyen")
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maVanChuyen;
    private String maVanDon;
    private LocalDateTime guiLuc;
    private LocalDateTime giaoLuc;
    private String trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang")
    private Order donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNVC")
    private ShippingCarrier nhaVanChuyen;
}
