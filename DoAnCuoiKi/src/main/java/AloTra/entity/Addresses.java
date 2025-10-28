package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Addresses")
@Getter // Dùng @Getter
@Setter // Dùng @Setter
@NoArgsConstructor
@AllArgsConstructor
// Loại bỏ 'account' khỏi toString và equals/hashCode
@ToString(exclude = {"account"})
@EqualsAndHashCode(exclude = {"account"})
public class Addresses { // Đổi tên class cho đúng chuẩn Java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm LAZY
    @JoinColumn(name = "user_id", nullable = false)
    private Account account; // Tên trường tham chiếu

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "ward_code", length = 20)
    private String wardCode;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @Column(name = "province_code", length = 20)
    private String provinceCode;

    @Column(name = "full_address_text", nullable = false, length = 500)
    private String fullAddressText;

    // Dùng Boolean
    @Column(name = "is_default", columnDefinition = "BIT DEFAULT 0")
    private Boolean isDefault = false; // Khởi tạo
}

