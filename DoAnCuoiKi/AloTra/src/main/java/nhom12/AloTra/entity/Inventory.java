package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "KhoHang")
public class Inventory {

    @Id
    private Integer maSanPham; // Khóa chính giờ là MaSanPham

    private Integer soLuongTon;

    private LocalDateTime ngayNhapGanNhat;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Đánh dấu rằng thuộc tính này cũng là một phần của khóa chính
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;
}
