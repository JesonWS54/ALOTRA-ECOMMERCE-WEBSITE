package AloTra.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Product_Images")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"product"}) // Loại bỏ product
@EqualsAndHashCode(exclude = {"product"}) // Loại bỏ product
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    // Dùng Boolean, getter/setter sẽ là getIsThumbnail/setIsThumbnail
    @Column(name = "is_thumbnail", columnDefinition = "BIT DEFAULT 0")
    private Boolean isThumbnail = false;
}

