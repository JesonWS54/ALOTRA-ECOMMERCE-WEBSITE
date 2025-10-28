package AloTra.repository;

import AloTra.entity.Voucher;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /**
     * Tìm voucher hợp lệ theo mã code.
     * Kiểm tra mã, ngày hiệu lực, số lượng còn lại.
     */
    @Query("SELECT v FROM Voucher v WHERE v.code = :code " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "AND v.usedCount < v.quantity")
    Optional<Voucher> findValidVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    // --- Phương thức cho Vendor ---
    /**
     * Tìm voucher theo ID shop, có phân trang.
     */
    // Sửa tên phương thức theo entity field 'shop'
    Page<Voucher> findByShop_Id(Long shopId, Pageable pageable);

    /**
     * Tìm voucher theo ID và ID shop (kiểm tra quyền sở hữu).
     */
    // Sửa tên phương thức theo entity field 'shop'
    Optional<Voucher> findByIdAndShop_Id(Long voucherId, Long shopId);

    /**
     * Kiểm tra sự tồn tại của voucher theo ID và ID shop.
     */
    // Sửa tên phương thức theo entity field 'shop'
    boolean existsByIdAndShop_Id(Long voucherId, Long shopId);

    /**
     * Kiểm tra xem mã voucher đã tồn tại chưa (không phân biệt hoa thường).
     * @param code Mã voucher cần kiểm tra.
     * @return true nếu đã tồn tại, false nếu chưa.
     */
    // *** THÊM PHƯƠNG THỨC NÀY ***
    boolean existsByCodeIgnoreCase(String code);
    // --- Kết thúc phương thức cho Vendor ---

}

