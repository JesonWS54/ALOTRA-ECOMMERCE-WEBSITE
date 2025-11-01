package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TinNhanChat")
public class MessageChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTinNhan")
    private Long maTinNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaPhienChat", nullable = false)
    private SessionChat phienChat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung; // Sửa lại: Dùng Entity User

    @Column(name = "NoiDung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "LoaiNguoiGui")
    private String loaiNguoiGui;

    @Column(name = "ThoiGian")
    private LocalDateTime thoiGian;

    @Column(name = "DaXem")
    private Boolean daXem = false;

    @PrePersist
    protected void onCreate() {
        this.thoiGian = LocalDateTime.now();
    }
}
