package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "PhieuNhap")
public class Import {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maPhieuNhap;

    private LocalDateTime ngayTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNCC")
    private Supplier nhaCungCap;

    @Formula("(SELECT SUM(ct.SoLuong * ct.GiaNhap) FROM ChiTietPhieuNhap ct WHERE ct.MaPhieuNhap = MaPhieuNhap)")
    private BigDecimal tongTien;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImportDetail> chiTietPhieuNhapList;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }
}
