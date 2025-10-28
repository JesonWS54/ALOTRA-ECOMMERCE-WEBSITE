package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "App_Commissions")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ các trường quan hệ
@ToString(exclude = {"category", "admin"})
@EqualsAndHashCode(exclude = {"category", "admin"})
public class AppCommission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sửa thành OneToOne vì category_id là unique
    @OneToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "category_id", nullable = false, unique = true)
    private Category category;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate; // Giữ Double

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "last_updated_by_admin_id", nullable = false)
    private Account admin; // Tên trường tham chiếu
}

