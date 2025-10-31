package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "GioHang")
@IdClass(CartId.class)
public class Cart {
    private Integer soLuong;
    private BigDecimal donGia;

    @Formula("(SoLuong * DonGia)")
    private BigDecimal thanhTien;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;
}