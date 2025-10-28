package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set

@Entity
@Table(name = "Shipping_Carriers")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orders") // Loại bỏ orders khỏi toString
@EqualsAndHashCode(exclude = "orders") // Loại bỏ orders khỏi equals/hashCode
public class ShippingCarrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "base_fee", nullable = false, columnDefinition = "DECIMAL(18,2) DEFAULT 0")
    private Double baseFee = 0.0; // Khởi tạo

    // Dùng Boolean
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true; // Khởi tạo

    // Quan hệ OneToMany đến Order
    @OneToMany(mappedBy = "shippingCarrier", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>(); // Dùng Set và khởi tạo
}

