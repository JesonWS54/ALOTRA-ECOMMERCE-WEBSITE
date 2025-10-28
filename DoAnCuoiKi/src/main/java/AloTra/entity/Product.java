package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Products")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ khỏi toString và equals/hashCode
@ToString(exclude = {"shop", "category", "images", "reviews", "favorites", "views", "orderItems"})
@EqualsAndHashCode(exclude = {"shop", "category", "images", "reviews", "favorites", "views", "orderItems"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "base_price", nullable = false, columnDefinition = "DECIMAL(18,2)")
    private Double basePrice;

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stockQuantity = 0;

    @Column(name = "sold_count", columnDefinition = "INT DEFAULT 0")
    private Integer soldCount = 0;

    @Column(name = "rating", columnDefinition = "FLOAT DEFAULT 0")
    private Double rating = 0.0;

    @Column(name = "review_count", columnDefinition = "INT DEFAULT 0")
    private Integer reviewCount = 0;

    @Column(name = "status", length = 20, columnDefinition = "NVARCHAR(20) DEFAULT 'ACTIVE'")
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // --- Quan hệ ---
    // Sử dụng Set và FetchType.LAZY
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductFavorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductView> views = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new HashSet<>();

    // Helper methods
    public void addImage(ProductImage image) { images.add(image); image.setProduct(this); }
    public void removeImage(ProductImage image) { images.remove(image); image.setProduct(null); }
    // (Tương tự cho Review, Favorite, View nếu cần)
}

