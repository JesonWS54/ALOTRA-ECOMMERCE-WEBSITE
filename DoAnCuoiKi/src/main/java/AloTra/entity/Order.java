package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set

@Entity
@Table(name = "Orders")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"account", "shop", "shippingCarrier", "shipper", "orderItems", "statusHistories", "payments"})
@EqualsAndHashCode(exclude = {"account", "shop", "shippingCarrier", "shipper", "orderItems", "statusHistories", "payments"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "user_id", nullable = false)
    private Account account; // Tên trường tham chiếu (người đặt)

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop; // Tên trường tham chiếu (người bán)

    // Thông tin giao hàng (snapshot)
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "shipping_phone", nullable = false, length = 15)
    private String shippingPhone;

    @Column(name = "shipping_full_name", nullable = false, length = 100)
    private String shippingFullName;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "shipping_carrier_id")
    private ShippingCarrier shippingCarrier; // Tên trường tham chiếu

    @Column(name = "shipping_fee", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private Double shippingFee = 0.0; // Khởi tạo

    @Column(name = "items_total_price", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private Double itemsTotalPrice;

    @Column(name = "voucher_discount", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private Double voucherDiscount = 0.0; // Khởi tạo

    @Column(name = "commission_fee", columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private Double commissionFee = 0.0; // Khởi tạo

    @Column(name = "final_total", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private Double finalTotal;

    @Column(name = "status", nullable = false, length = 30, columnDefinition = "NVARCHAR(30) DEFAULT 'PENDING'")
    private String status = "PENDING"; // Khởi tạo

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod; // "COD", "VNPAY", "MOMO"

    @Column(name = "payment_status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'UNPAID'")
    private String paymentStatus = "UNPAID"; // Khởi tạo

    @Column(name = "notes", length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "shipper_id")
    private Account shipper; // Tên trường tham chiếu (người giao)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // --- Quan hệ ---
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>(); // Dùng Set và khởi tạo

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderStatusHistory> statusHistories = new HashSet<>(); // Dùng Set và khởi tạo

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Payment> payments = new HashSet<>(); // Dùng Set và khởi tạo

    // Helper methods for bidirectional relationship (OrderItem)
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    public void removeItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
     // Helper methods for Status History
     public void addStatusHistory(OrderStatusHistory history) {
        statusHistories.add(history);
        history.setOrder(this);
     }
     // Helper methods for Payments
     public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
     }
}

