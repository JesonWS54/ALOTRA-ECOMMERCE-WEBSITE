package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "DiaChi")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDiaChi;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String soNhaDuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private User nguoiDung;
}
