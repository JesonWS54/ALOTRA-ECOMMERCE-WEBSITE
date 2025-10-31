package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "SanPhamYeuThich")
@IdClass(WishListId.class)
public class WishList {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung;
}
