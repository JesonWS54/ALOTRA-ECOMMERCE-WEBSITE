package AloTra.repository;

import AloTra.entity.AppCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import
import org.springframework.stereotype.Repository;

import java.util.List; // Import
import java.util.Optional;

@Repository
public interface AppCommissionRepository extends JpaRepository<AppCommission, Long> {

    // Tìm chiết khấu theo ID danh mục
    Optional<AppCommission> findByCategory_Id(Long categoryId);

    // Lấy tất cả chiết khấu cùng thông tin Category và Admin (dùng JOIN FETCH)
    @Query("SELECT ac FROM AppCommission ac JOIN FETCH ac.category JOIN FETCH ac.admin")
    List<AppCommission> findAllWithDetails();
}
