package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set

@Entity
@Table(name = "Shops")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ khỏi toString và equals/hashCode
@ToString(exclude = {"account", "products", "vouchers"})
@EqualsAndHashCode(exclude = {"account", "products", "vouchers"})
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sửa thành OneToOne và thêm mappedBy nếu Account có tham chiếu ngược
    @OneToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "shop_name", nullable = false, unique = true, length = 150)
    private String shopName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "rating", columnDefinition = "FLOAT DEFAULT 0")
    private Double rating = 0.0; // Khởi tạo

    @Column(name = "status", length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'PENDING'")
    private String status = "PENDING"; // Khởi tạo

    @CreationTimestamp // Tự động set khi tạo
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @UpdateTimestamp // Tự động set khi cập nhật
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // --- Bổ sung các quan hệ OneToMany ---
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Voucher> vouchers = new HashSet<>();

    // Helper methods nếu cần (ví dụ: addProduct, removeProduct)
}

