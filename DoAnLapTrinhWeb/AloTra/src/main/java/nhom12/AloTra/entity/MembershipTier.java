package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "HangThanhVien")
public class MembershipTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHangThanhVien")
    private Integer maHangThanhVien;

    @Column(name = "TenHang")
    private String tenHang;

    @Column(name = "DiemToiThieu")
    private Integer diemToiThieu;

    @Column(name = "PhanTramGiamGia")
    private BigDecimal phanTramGiamGia;
}
