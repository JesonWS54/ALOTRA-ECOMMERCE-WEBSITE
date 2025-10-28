package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Categories")
@Getter // Đảm bảo @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parent", "subCategories", "products", "appCommissions"})
@EqualsAndHashCode(exclude = {"parent", "subCategories", "products", "appCommissions"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true; // Khởi tạo giá trị mặc định

    // Relationships
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Category> subCategories = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AppCommission> appCommissions = new HashSet<>();

    // Helper methods (if needed, but Lombok provides basic ones)
    public void addSubCategory(Category subCategory) {
        subCategories.add(subCategory);
        subCategory.setParent(this);
    }

    public void removeSubCategory(Category subCategory) {
        subCategories.remove(subCategory);
        subCategory.setParent(null);
    }

}

