package com.alotra.controller.api;

import com.alotra.entity.Order;
import com.alotra.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OrderApiController - REST API for Orders
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * GET /api/orders
     * Lấy tất cả đơn hàng
     */
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{id}
     * Lấy đơn hàng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy đơn hàng với ID: " + id));
    }

    /**
     * GET /api/orders/number/{orderNumber}
     * Lấy đơn hàng theo order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Optional<Order> order = orderService.getOrderByOrderNumber(orderNumber);
        
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy đơn hàng: " + orderNumber));
    }

    /**
     * POST /api/orders
     * Tạo đơn hàng mới
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Order savedOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/orders/{id}
     * Cập nhật đơn hàng
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Order orderDetails) {
        try {
            Order updatedOrder = orderService.updateOrder(id, orderDetails);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== STATUS MANAGEMENT ====================

    /**
     * PUT /api/orders/{id}/status
     * Cập nhật trạng thái đơn hàng
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Status is required"));
            }
            
            Order order = orderService.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/confirm
     * Xác nhận đơn hàng
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            Order order = orderService.confirmOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/prepare
     * Chuẩn bị đơn hàng
     */
    @PostMapping("/{id}/prepare")
    public ResponseEntity<?> prepareOrder(@PathVariable Long id) {
        try {
            Order order = orderService.prepareOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/ship
     * Giao đơn hàng
     */
    @PostMapping("/{id}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            Order order = orderService.shipOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/complete
     * Hoàn thành đơn hàng
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            Order order = orderService.completeOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/orders/{id}/cancel
     * Hủy đơn hàng
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== PAYMENT MANAGEMENT ====================

    /**
     * PUT /api/orders/{id}/payment-status
     * Cập nhật trạng thái thanh toán
     */
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String paymentStatus = request.get("paymentStatus");
            if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Payment status is required"));
            }
            
            Order order = orderService.updatePaymentStatus(id, paymentStatus);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * GET /api/orders/user/{userId}
     * Lấy đơn hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Order>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersByUserId(userId, pageable);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/status/{status}
     * Lấy đơn hàng theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/active
     * Lấy đơn hàng đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Order>> getActiveOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderService.getActiveOrders(pageable);
        
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/user/{userId}/recent
     * Lấy đơn hàng gần đây của user
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<Order>> getRecentOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Order> orders = orderService.getRecentOrdersByUserId(userId, pageable);
        
        return ResponseEntity.ok(orders);
    }

    // ==================== STATISTICS ====================

    /**
     * GET /api/orders/count
     * Đếm tổng số đơn hàng
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countOrders() {
        long total = orderService.countAllOrders();
        long pending = orderService.countOrdersByStatus("PENDING");
        long confirmed = orderService.countOrdersByStatus("CONFIRMED");
        long shipping = orderService.countOrdersByStatus("SHIPPING");
        long delivered = orderService.countOrdersByStatus("DELIVERED");
        long cancelled = orderService.countOrdersByStatus("CANCELLED");
        
        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("pending", pending);
        response.put("confirmed", confirmed);
        response.put("shipping", shipping);
        response.put("delivered", delivered);
        response.put("cancelled", cancelled);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/orders/revenue
     * Tính doanh thu
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        LocalDateTime start = startDate != null 
            ? LocalDateTime.parse(startDate) 
            : LocalDateTime.now().minusMonths(1);
        
        LocalDateTime end = endDate != null 
            ? LocalDateTime.parse(endDate) 
            : LocalDateTime.now();
        
        BigDecimal revenue = orderService.getTotalRevenue(start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("startDate", start.toString());
        response.put("endDate", end.toString());
        response.put("totalRevenue", revenue);
        response.put("currency", "VND");
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/orders/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Order API is running!");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}