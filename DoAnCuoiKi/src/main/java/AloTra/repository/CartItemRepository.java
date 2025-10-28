package AloTra.repository;

import AloTra.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import Modifying
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List; // Import List

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Tìm CartItem theo CartId và ProductId (để kiểm tra tồn tại hoặc cập nhật số lượng)
    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    // Xóa tất cả CartItem của một Cart (dùng khi clear cart)
    @Modifying // Cần thiết cho các thao tác DELETE hoặc UPDATE
    void deleteByCart_Id(Long cartId);

    // Đếm số lượng item trong cart
    int countByCart_Id(Long cartId);

    // Kiểm tra xem CartItem có tồn tại và thuộc về user không
    boolean existsByIdAndCart_Account_Id(Long cartItemId, Long accountId);

    // Tìm CartItem theo ID và ID của user (để cập nhật/xóa)
    Optional<CartItem> findByIdAndCart_Account_Id(Long cartItemId, Long accountId);

    // *** THÊM PHƯƠNG THỨC NÀY ***
    // Lấy danh sách CartItem của một Cart, sắp xếp theo thời gian thêm mới nhất
    List<CartItem> findByCart_IdOrderByAddedAtDesc(Long cartId);

}