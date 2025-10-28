package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Order_Items")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"order", "product", "review"}) // Thêm review
@EqualsAndHashCode(exclude = {"order", "product", "review"})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Quan trọng: Dùng LAZY loading
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Tên trường tham chiếu

    // Sửa lại: Dùng ON DELETE SET NULL
    @ManyToOne(fetch = FetchType.LAZY) // Có thể LAZY
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES Products(id) ON DELETE SET NULL"))
    private Product product; // Tên trường tham chiếu

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName; // Snapshot tên sản phẩm

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private Double price; // Snapshot giá tại thời điểm mua

    // Quan hệ OneToOne đến Review
    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Review review;
}

