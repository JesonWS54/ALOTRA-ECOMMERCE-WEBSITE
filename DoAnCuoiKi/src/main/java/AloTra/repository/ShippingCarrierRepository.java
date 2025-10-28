package AloTra.repository;

import AloTra.entity.ShippingCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingCarrierRepository extends JpaRepository<ShippingCarrier, Long> {

    /**
     * Tìm các nhà vận chuyển đang hoạt động.
     * Spring Data JPA sẽ tự tạo query dựa trên tên phương thức.
     * @return Danh sách ShippingCarrier đang active.
     */
    List<ShippingCarrier> findByIsActiveTrue(); // *** THÊM PHƯƠNG THỨC NÀY ***

    /**
     * Kiểm tra xem tên nhà vận chuyển đã tồn tại chưa (không phân biệt hoa thường).
     * @param name Tên nhà vận chuyển cần kiểm tra.
     * @return true nếu tên đã tồn tại, false nếu chưa.
     */
    boolean existsByNameIgnoreCase(String name); // *** Đảm bảo phương thức này có ***
}

