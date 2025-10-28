package AloTra.repository;

import AloTra.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    // Kiểm tra xem user đã có shop chưa
    boolean existsByAccount_Id(Long accountId);

    // Tìm shop theo user ID (nếu cần)
    Optional<Shop> findByAccount_Id(Long accountId);

    // Kiểm tra tên shop đã tồn tại chưa (tránh trùng lặp)
    boolean existsByShopNameIgnoreCase(String shopName);
}
