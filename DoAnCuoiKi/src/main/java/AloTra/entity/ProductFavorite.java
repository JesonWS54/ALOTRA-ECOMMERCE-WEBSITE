package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Wishlist", // Tên bảng là Wishlist
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"})) // Đảm bảo unique
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"account", "product"})
@EqualsAndHashCode(exclude = {"account", "product"})
public class ProductFavorite { // Giữ tên class là ProductFavorite cho nhất quán
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()") // Sửa tên cột và kiểu
    private LocalDateTime addedAt; // Sửa tên trường
}

