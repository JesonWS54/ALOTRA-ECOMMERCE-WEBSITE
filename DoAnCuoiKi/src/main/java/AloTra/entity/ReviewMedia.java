package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Review_Media")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ trường quan hệ
@ToString(exclude = {"review"})
@EqualsAndHashCode(exclude = {"review"})
public class ReviewMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "review_id", nullable = false)
    private Review review; // Tên trường tham chiếu

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "media_type", nullable = false, length = 10)
    private String mediaType; // "IMAGE", "VIDEO"
}

