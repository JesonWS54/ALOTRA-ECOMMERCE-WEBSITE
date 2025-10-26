package com.alotra.service;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Product;
import com.alotra.entity.User;
import com.alotra.repository.OrderRepository;
import com.alotra.repository.OrderItemRepository;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderService - Business logic for order management
 * Includes real-time notifications via WebSocket
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private NotificationService notificationService;

    // ==================== CREATE ====================

    /**
     * Create new order
     */
    public Order createOrder(Order order) {
        logger.info("Creating new order for user: {}", order.getUser().getId());

        // Validate user exists
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + order.getUser().getId()));
        order.setUser(user);

        // Set default values
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("PENDING");
        }
        if (order.getPaymentStatus() == null || order.getPaymentStatus().isEmpty()) {
            order.setPaymentStatus("UNPAID");
        }

        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Calculate total from order items
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                // Validate product exists and has stock
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + item.getProduct().getId()));

                // Check stock availability
                if (!productService.isQuantityAvailable(product.getId(), item.getQuantity())) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                // Set product and price
                item.setProduct(product);
                item.setUnitPrice(product.getPrice());
                item.setOrder(order);

                // Calculate subtotal
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                calculatedTotal = calculatedTotal.add(itemTotal);

                // Decrease product stock
                productService.decreaseStock(product.getId(), item.getQuantity());
            }
        }

        // Set total amount
        order.setTotalAmount(calculatedTotal);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Send notifications
        notificationService.notifyNewOrder(
                savedOrder.getId(),
                user.getUsername(),
                savedOrder.getCustomerName()
        );

        logger.info("Order created successfully with id: {}", savedOrder.getId());
        return savedOrder;
    }

    // ==================== READ ====================

    /**
     * Get order by ID
     */
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get orders by user
     */
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Get orders by payment status
     */
    public List<Order> getOrdersByPaymentStatus(String paymentStatus) {
        return orderRepository.findByPaymentStatus(paymentStatus);
    }

    /**
     * Get recent orders (latest first)
     */
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findTopByOrderByCreatedAtDesc(limit);
    }

    /**
     * Get orders by date range
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get pending orders
     */
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus("PENDING");
    }

    /**
     * Get confirmed orders
     */
    public List<Order> getConfirmedOrders() {
        return orderRepository.findByStatus("CONFIRMED");
    }

    /**
     * Get completed orders
     */
    public List<Order> getCompletedOrders() {
        return orderRepository.findByStatus("COMPLETED");
    }

    // ==================== UPDATE ====================

    /**
     * Update order
     */
    public Order updateOrder(Long id, Order orderDetails) {
        logger.info("Updating order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // Update fields
        if (orderDetails.getCustomerName() != null) {
            order.setCustomerName(orderDetails.getCustomerName());
        }
        if (orderDetails.getCustomerPhone() != null) {
            order.setCustomerPhone(orderDetails.getCustomerPhone());
        }
        if (orderDetails.getCustomerEmail() != null) {
            order.setCustomerEmail(orderDetails.getCustomerEmail());
        }
        if (orderDetails.getDeliveryAddress() != null) {
            order.setDeliveryAddress(orderDetails.getDeliveryAddress());
        }
        if (orderDetails.getNotes() != null) {
            order.setNotes(orderDetails.getNotes());
        }

        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        logger.info("Order updated successfully: {}", updatedOrder.getId());
        return updatedOrder;
    }

    /**
     * Update order status
     */
    public Order updateOrderStatus(Long id, String newStatus) {
        logger.info("Updating order status for id: {} to {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        // Send notification
        notificationService.notifyOrderStatusChanged(
                updatedOrder.getId(),
                updatedOrder.getUser().getUsername(),
                oldStatus,
                newStatus
        );

        logger.info("Order status updated: {} -> {}", oldStatus, newStatus);
        return updatedOrder;
    }

    /**
     * Update payment status
     */
    public Order updatePaymentStatus(Long id, String newPaymentStatus) {
        logger.info("Updating payment status for order id: {} to {}", id, newPaymentStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setPaymentStatus(newPaymentStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        // Send notification if payment is confirmed
        if ("PAID".equalsIgnoreCase(newPaymentStatus)) {
            notificationService.notifyPaymentReceived(
                    updatedOrder.getId(),
                    updatedOrder.getUser().getUsername(),
                    updatedOrder.getTotalAmount().doubleValue()
            );
        }

        logger.info("Payment status updated to: {}", newPaymentStatus);
        return updatedOrder;
    }

    /**
     * Confirm order (change status from PENDING to CONFIRMED)
     */
    public Order confirmOrder(Long id) {
        return updateOrderStatus(id, "CONFIRMED");
    }

    /**
     * Process order (change status to PROCESSING)
     */
    public Order processOrder(Long id) {
        return updateOrderStatus(id, "PROCESSING");
    }

    /**
     * Ship order (change status to SHIPPING)
     */
    public Order shipOrder(Long id) {
        return updateOrderStatus(id, "SHIPPING");
    }

    /**
     * Complete order (change status to COMPLETED)
     */
    public Order completeOrder(Long id) {
        return updateOrderStatus(id, "COMPLETED");
    }

    /**
     * Cancel order
     */
    public Order cancelOrder(Long id, String reason) {
        logger.info("Cancelling order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // Only allow cancellation if order is PENDING or CONFIRMED
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus("CANCELLED");
        order.setNotes(order.getNotes() != null ? order.getNotes() + "\nCancellation reason: " + reason : "Cancellation reason: " + reason);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        // Send notification
        notificationService.sendCustomNotificationToUser(
                order.getUser().getUsername(),
                "Đơn hàng đã bị hủy",
                "Đơn hàng #" + id + " đã bị hủy. Lý do: " + reason,
                com.alotra.dto.NotificationMessage.NotificationType.ORDER_CANCELLED
        );

        logger.info("Order cancelled successfully: {}", id);
        return cancelledOrder;
    }

    // ==================== DELETE ====================

    /**
     * Delete order (only if status is CANCELLED)
     */
    public void deleteOrder(Long id) {
        logger.info("Deleting order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (!"CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Can only delete cancelled orders");
        }

        orderRepository.deleteById(id);
        logger.info("Order deleted successfully: {}", id);
    }

    // ==================== STATISTICS ====================

    /**
     * Get total order count
     */
    public long getTotalOrderCount() {
        return orderRepository.count();
    }

    /**
     * Get order count by status
     */
    public long getOrderCountByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Get order count by user
     */
    public long getOrderCountByUser(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    /**
     * Get total revenue
     */
    public BigDecimal getTotalRevenue() {
        List<Order> completedOrders = orderRepository.findByStatus("COMPLETED");
        return completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get revenue by date range
     */
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get average order value
     */
    public BigDecimal getAverageOrderValue() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if order exists
     */
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }

    /**
     * Check if order belongs to user
     */
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        return order != null && order.getUser().getId().equals(userId);
    }

    /**
     * Check if order can be cancelled
     */
    public boolean canBeCancelled(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return false;
        }
        return "PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus());
    }

    /**
     * Get order summary
     */
    public OrderSummary getOrderSummary(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        int totalItems = order.getOrderItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        return new OrderSummary(order, totalItems);
    }

    // ==================== INNER CLASS ====================

    /**
     * DTO for order summary
     */
    public static class OrderSummary {
        private Order order;
        private int totalItems;

        public OrderSummary(Order order, int totalItems) {
            this.order = order;
            this.totalItems = totalItems;
        }

        public Order getOrder() {
            return order;
        }

        public int getTotalItems() {
            return totalItems;
        }
    }
}