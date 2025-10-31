package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ChiTietPhieuNhap")
@IdClass(ImportDetailId.class)
public class ImportDetail {

    private Integer soLuong;
    private BigDecimal giaNhap;

    @Formula("(SoLuong * GiaNhap)")
    private BigDecimal thanhTien;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhieuNhap")
    private Import phieuNhap;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;
}
