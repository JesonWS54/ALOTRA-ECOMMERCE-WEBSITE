package AloTra.repository;

import AloTra.entity.Addresses; // Sửa tên Entity cho đúng
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Addresses, Long> {

    /**
     * Tìm tất cả địa chỉ của một user cụ thể.
     * @param accountId ID của user.
     * @return Danh sách địa chỉ.
     */
    List<Addresses> findByAccount_IdOrderByIsDefaultDesc(Long accountId); // Sửa tên trường accountId

    /**
     * Bỏ đánh dấu mặc định cho tất cả địa chỉ của user.
     * Dùng trước khi đặt địa chỉ mới làm mặc định.
     * @param accountId ID của user.
     */
    @Modifying
    @Query("UPDATE Addresses a SET a.isDefault = false WHERE a.account.id = :accountId") // Sửa tên trường account.id
    void clearDefaultForUser(Long accountId);
    
    
    Optional<Addresses> findByIdAndAccount_Id(Long id, Long accountId);
}