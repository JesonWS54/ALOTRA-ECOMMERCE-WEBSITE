package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set

@Entity
@Table(name = "Reviews")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"account", "product", "orderItem", "reviewMedia"})
@EqualsAndHashCode(exclude = {"account", "product", "orderItem", "reviewMedia"})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Sửa lại: Không còn unique, nullable=true (cho phép review không cần mua)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", unique = false, nullable = true)
    private OrderItem orderItem;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    // Giữ nullable=false, min length check ở Service/Controller
    @Column(name = "comment", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    // Sửa lại quan hệ và dùng Set
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ReviewMedia> reviewMedia = new HashSet<>(); // Dùng Set và khởi tạo

    // Helper methods for bidirectional relationship (ReviewMedia)
    public void addMedia(ReviewMedia media) {
        reviewMedia.add(media);
        media.setReview(this);
    }
    public void removeMedia(ReviewMedia media) {
        reviewMedia.remove(media);
        media.setReview(null);
    }
}

