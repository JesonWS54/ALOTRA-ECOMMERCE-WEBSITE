package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "DonHang_ChiTiet")
@IdClass(OrderDetailId.class)
public class OrderDetail {
    private String tenSanPham;
    private BigDecimal donGia;
    private Integer soLuong;

    @Formula("(SoLuong * DonGia)")
    private BigDecimal thanhTien;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaDonHang")
    private Order donHang;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;
}
