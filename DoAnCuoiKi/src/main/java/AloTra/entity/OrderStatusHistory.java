package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Order_Status_History")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"order", "changedBy"})
@EqualsAndHashCode(exclude = {"order", "changedBy"})
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Tên trường tham chiếu

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "changed_by_user_id") // Cho phép null nếu hệ thống tự đổi
    private Account changedBy; // Tên trường tham chiếu

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime timestamp;

    @Column(name = "notes", length = 255)
    private String notes;
}

