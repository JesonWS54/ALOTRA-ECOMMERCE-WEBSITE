package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "DanhGia")
@IdClass(RatingId.class)
public class Rating {
    private Integer diemDanhGia;
    private String binhLuan;
    private LocalDateTime ngayTao;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham")
    private Product sanPham;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "VideoUrl")
    private String videoUrl;
}
