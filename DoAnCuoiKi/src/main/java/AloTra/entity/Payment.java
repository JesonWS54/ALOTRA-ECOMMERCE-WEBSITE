package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ trường quan hệ
@ToString(exclude = {"order"})
@EqualsAndHashCode(exclude = {"order"})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Tên trường tham chiếu

    @Column(name = "transaction_code", unique = true, length = 100)
    private String transactionCode; // Mã giao dịch của cổng TT hoặc mã nội bộ

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private Double amount;

    @Column(name = "payment_gateway", nullable = false, length = 20)
    private String paymentGateway; // "VNPAY", "MOMO", "COD" (nếu cần ghi nhận COD payment)

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "PENDING", "SUCCESS", "FAILED"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;
}

