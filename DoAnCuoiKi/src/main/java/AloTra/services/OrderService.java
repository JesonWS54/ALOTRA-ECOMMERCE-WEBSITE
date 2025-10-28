package AloTra.services;

import AloTra.Model.OrderDTO;
import AloTra.entity.Order; // Import Order
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {

    // --- Giữ nguyên các phương thức cũ ---
    Page<OrderDTO> findUserOrders(Long userId, String status, Pageable pageable);
    boolean checkUserHasPurchasedProduct(Long userId, Long productId);
    @Transactional
    List<Order> createOrdersFromCart(Long userId, Long addressId, String paymentMethod, String notes, String appliedVoucherCode);
    Optional<OrderDTO> getOrderDetails(Long orderId, Long userId);

    // --- Phương thức mới cho Vendor ---
    Page<OrderDTO> findShopOrders(Long shopId, String status, Pageable pageable); // Đã thêm

    // --- **THÊM PHƯƠNG THỨC LẤY DOANH THU** ---
    double getTotalRevenueForShop(Long shopId);
    // ------------------------------------------

    // TODO: Thêm các hàm khác (vd: cancelOrder, updateOrderStatus...)
}

