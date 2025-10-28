package AloTra.services;

import AloTra.Model.CartItemDTO;
import AloTra.Model.CartViewDTO; // DTO để hiển thị giỏ hàng
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import java.util.List;

public interface CartService {

    /**
     * Lấy thông tin giỏ hàng để hiển thị.
     * @param userId ID của user.
     * @param appliedVoucherCode Mã voucher đang được áp dụng (có thể null).
     * @return CartViewDTO chứa danh sách CartItemDTO và tổng tiền đã tính toán.
     */
    CartViewDTO getCartView(Long userId, String appliedVoucherCode); // <-- Chữ ký đã được sửa

    /**
     * Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã có, tăng số lượng.
     * @param userId ID của user.
     * @param productId ID của sản phẩm.
     * @param quantity Số lượng cần thêm.
     */
    @Transactional
    void addItemToCart(Long userId, Long productId, int quantity);

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ hàng.
     * @param userId ID của user.
     * @param cartItemId ID của CartItem cần cập nhật.
     * @param quantity Số lượng mới (nếu <= 0 sẽ xóa item).
     */
    @Transactional
    void updateItemQuantity(Long userId, Long cartItemId, int quantity);

    /**
     * Xóa một sản phẩm khỏi giỏ hàng.
     * @param userId ID của user.
     * @param cartItemId ID của CartItem cần xóa.
     */
    @Transactional
    void removeItemFromCart(Long userId, Long cartItemId);

    /**
     * Xóa toàn bộ giỏ hàng của user (thường dùng sau khi checkout).
     * @param userId ID của user.
     */
    @Transactional
    void clearCart(Long userId);

    /**
     * Xóa các CartItem cụ thể (dùng sau khi tạo Order thành công).
     * @param cartItemIds Danh sách ID của CartItem cần xóa.
     */
    @Transactional
    void clearCartItems(List<Long> cartItemIds); // <-- THÊM DÒNG NÀY

    /**
     * Lấy số lượng item trong giỏ hàng của user (để hiển thị trên icon).
     * @param userId ID của user.
     * @return Số lượng item.
     */
    int getCartItemCount(Long userId);

}