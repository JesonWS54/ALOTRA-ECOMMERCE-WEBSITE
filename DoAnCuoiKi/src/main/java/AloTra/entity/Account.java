package AloTra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // *** THÊM IMPORT ***
import org.hibernate.annotations.UpdateTimestamp; // *** THÊM IMPORT ***

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"addresses", "shop", "cart", "userOrders", "shipperOrders",
                     "statusHistories", "commissions", "favorites", "views", "reviews"})
@EqualsAndHashCode(exclude = {"addresses", "shop", "cart", "userOrders", "shipperOrders",
                           "statusHistories", "commissions", "favorites", "views", "reviews"})
public class Account {

    public enum Role { USER, VENDOR, ADMIN, SHIPPER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", unique = true, length = 15)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 0")
    private Boolean isActive = false;

    @Column(name = "is_locked", columnDefinition = "BIT DEFAULT 0")
    private Boolean isLocked = false;

    @CreationTimestamp // *** SỬA LỖI BIÊN DỊCH ***
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime createdAt;

    @UpdateTimestamp // *** SỬA LỖI BIÊN DỊCH ***
    @Column(name = "updated_at", columnDefinition = "DATETIME2 DEFAULT GETDATE()")
    private LocalDateTime updatedAt;

    // --- Quan hệ ---
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Addresses> addresses = new HashSet<>();

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Shop shop;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Order> userOrders = new HashSet<>();

    @OneToMany(mappedBy = "shipper", fetch = FetchType.LAZY)
    private Set<Order> shipperOrders = new HashSet<>();

    @OneToMany(mappedBy = "changedBy", fetch = FetchType.LAZY)
    private Set<OrderStatusHistory> statusHistories = new HashSet<>();

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY)
    private Set<AppCommission> commissions = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductFavorite> favorites = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProductView> views = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

}

