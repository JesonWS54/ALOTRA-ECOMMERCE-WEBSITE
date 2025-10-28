package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Cart_Items")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"cart", "product"})
@EqualsAndHashCode(exclude = {"cart", "product"})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart; // Tên trường tham chiếu

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Tên trường tham chiếu

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_add", nullable = false, columnDefinition = "DECIMAL(18,2)") // Thêm columnDefinition
    private Double priceAtAdd; // Giữ Double

    @CreationTimestamp // Tự set khi tạo
    @Column(name = "added_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime addedAt;
}

