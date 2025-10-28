package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Vouchers")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ 'shop' khỏi toString và equals/hashCode
@ToString(exclude = {"shop"})
@EqualsAndHashCode(exclude = {"shop"})
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // "PERCENT", "FIXED_AMOUNT"

    @Column(name = "discount_value", nullable = false, columnDefinition = "DECIMAL(18,2)") // Thêm columnDefinition
    private Double discountValue;

    @Column(name = "max_discount_amount", columnDefinition = "DECIMAL(18,2)") // Thêm columnDefinition
    private Double maxDiscountAmount;

    @Column(name = "min_order_value", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private Double minOrderValue = 0.0; // Khởi tạo

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "used_count", columnDefinition = "INT DEFAULT 0")
    private Integer usedCount = 0; // Khởi tạo

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "created_by_role", length = 20)
    private String createdByRole; // "ADMIN", "VENDOR"

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "shop_id") // Tên cột trong DB
    private Shop shop; // Tên trường tham chiếu

    // Không cần createdAt, updatedAt vì không có trong DB schema gốc
}

