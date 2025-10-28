package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp; // Dùng UpdateTimestamp

import java.time.LocalDateTime;

@Entity
// Sửa tên bảng cho khớp CSDL và thêm unique constraint
@Table(name = "Viewed_Products",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"account", "product"})
@EqualsAndHashCode(exclude = {"account", "product"})
public class ProductView { // Giữ tên class là ProductView
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @UpdateTimestamp // Tự động cập nhật khi record được update (hoặc tạo mới)
    @Column(name = "viewed_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()") // Sửa tên cột và kiểu
    private LocalDateTime viewedAt; // Sửa tên trường
}

