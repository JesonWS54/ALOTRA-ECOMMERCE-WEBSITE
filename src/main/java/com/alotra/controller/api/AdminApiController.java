package com.alotra.controller.api;

import com.alotra.entity.Order;
import com.alotra.entity.Product;
import com.alotra.entity.Review;
import com.alotra.entity.User;
import com.alotra.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminApiController - REST API for Admin Dashboard & Statistics
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CategoryService categoryService;

    // ==================== DASHBOARD OVERVIEW ====================

    /**
     * GET /api/admin/dashboard
     * Lấy thống kê tổng quan cho dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Product stats
        long totalProducts = productService.countAllProducts();
        long activeProducts = productService.countActiveProducts();
        List<Product> lowStockProducts = productService.getLowStockProducts(10);
        List<Product> outOfStockProducts = productService.getOutOfStockProducts();

        Map<String, Object> productStats = new HashMap<>();
        productStats.put("total", totalProducts);
        productStats.put("active", activeProducts);
        productStats.put("lowStock", lowStockProducts.size());
        productStats.put("outOfStock", outOfStockProducts.size());

        // Order stats
        long totalOrders = orderService.countAllOrders();
        long pendingOrders = orderService.countOrdersByStatus("PENDING");
        long confirmedOrders = orderService.countOrdersByStatus("CONFIRMED");
        long shippingOrders = orderService.countOrdersByStatus("SHIPPING");
        long deliveredOrders = orderService.countOrdersByStatus("DELIVERED");
        long cancelledOrders = orderService.countOrdersByStatus("CANCELLED");

        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("total", totalOrders);
        orderStats.put("pending", pendingOrders);
        orderStats.put("confirmed", confirmedOrders);
        orderStats.put("shipping", shippingOrders);
        orderStats.put("delivered", deliveredOrders);
        orderStats.put("cancelled", cancelledOrders);

        // User stats
        long totalUsers = userService.countAllUsers();
        long activeUsers = userService.countActiveUsers();
        long adminUsers = userService.countUsersByRole("ADMIN");
        long verifiedUsers = userService.countVerifiedUsers();

        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("active", activeUsers);
        userStats.put("admins", adminUsers);
        userStats.put("verified", verifiedUsers);

        // Review stats
        long totalReviews = reviewService.countAllReviews();

        Map<String, Object> reviewStats = new HashMap<>();
        reviewStats.put("total", totalReviews);

        // Category stats
        long totalCategories = categoryService.countAllCategories();
//        long activeCategories = categoryService.countActiveCategories();

        Map<String, Object> categoryStats = new HashMap<>();
        categoryStats.put("total", totalCategories);
//        categoryStats.put("active", activeCategories);

        // Revenue stats (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        BigDecimal monthlyRevenue = orderService.getTotalRevenue(thirtyDaysAgo, now);

        Map<String, Object> revenueStats = new HashMap<>();
        revenueStats.put("monthly", monthlyRevenue);
        revenueStats.put("period", "Last 30 days");

        // Combine all stats
        stats.put("products", productStats);
        stats.put("orders", orderStats);
        stats.put("users", userStats);
        stats.put("reviews", reviewStats);
        stats.put("categories", categoryStats);
        stats.put("revenue", revenueStats);
        stats.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    // ==================== PRODUCT STATISTICS ====================

    /**
     * GET /api/admin/stats/products
     * Thống kê chi tiết sản phẩm
     */
    @GetMapping("/stats/products")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", productService.countAllProducts());
        stats.put("active", productService.countActiveProducts());
        stats.put("inactive", productService.countAllProducts() - productService.countActiveProducts());

        // Low stock products
        List<Product> lowStock = productService.getLowStockProducts(10);
        stats.put("lowStock", lowStock);
        stats.put("lowStockCount", lowStock.size());

        // Out of stock products
        List<Product> outOfStock = productService.getOutOfStockProducts();
        stats.put("outOfStock", outOfStock);
        stats.put("outOfStockCount", outOfStock.size());

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/stats/top-products
     * Top sản phẩm bán chạy
     */
    @GetMapping("/stats/top-products")
    public ResponseEntity<Page<Product>> getTopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> topProducts = productService.getBestsellerProducts(pageable);
        
        return ResponseEntity.ok(topProducts);
    }

    /**
     * GET /api/admin/stats/top-rated-products
     * Top sản phẩm được đánh giá cao
     */
    @GetMapping("/stats/top-rated-products")
    public ResponseEntity<Page<Product>> getTopRatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> topRated = productService.getTopRatedProducts(pageable);
        
        return ResponseEntity.ok(topRated);
    }

    // ==================== ORDER STATISTICS ====================

    /**
     * GET /api/admin/stats/orders
     * Thống kê đơn hàng
     */
    @GetMapping("/stats/orders")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", orderService.countAllOrders());
        stats.put("pending", orderService.countOrdersByStatus("PENDING"));
        stats.put("confirmed", orderService.countOrdersByStatus("CONFIRMED"));
        stats.put("preparing", orderService.countOrdersByStatus("PREPARING"));
        stats.put("shipping", orderService.countOrdersByStatus("SHIPPING"));
        stats.put("delivered", orderService.countOrdersByStatus("DELIVERED"));
        stats.put("cancelled", orderService.countOrdersByStatus("CANCELLED"));

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/stats/recent-orders
     * Đơn hàng gần đây
     */
    @GetMapping("/stats/recent-orders")
    public ResponseEntity<Page<Order>> getRecentOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> recentOrders = orderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(recentOrders);
    }

    /**
     * GET /api/admin/stats/active-orders
     * Đơn hàng đang hoạt động (pending, confirmed, preparing, shipping)
     */
    @GetMapping("/stats/active-orders")
    public ResponseEntity<Page<Order>> getActiveOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> activeOrders = orderService.getActiveOrders(pageable);
        
        return ResponseEntity.ok(activeOrders);
    }

    // ==================== USER STATISTICS ====================

    /**
     * GET /api/admin/stats/users
     * Thống kê người dùng
     */
    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        long total = userService.countAllUsers();
        long active = userService.countActiveUsers();
        long admins = userService.countUsersByRole("ADMIN");
        long verified = userService.countVerifiedUsers();

        stats.put("total", total);
        stats.put("active", active);
        stats.put("inactive", total - active);
        stats.put("admins", admins);
        stats.put("regularUsers", total - admins);
        stats.put("verified", verified);
        stats.put("unverified", total - verified);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/stats/recent-users
     * Người dùng đăng ký gần đây
     */
    @GetMapping("/stats/recent-users")
    public ResponseEntity<Page<User>> getRecentUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> recentUsers = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(recentUsers);
    }

    // ==================== REVENUE STATISTICS ====================

    /**
     * GET /api/admin/stats/revenue
     * Thống kê doanh thu
     */
    @GetMapping("/stats/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStats(
            @RequestParam(required = false) String period) {
        
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        String periodName;

        // Determine period
        if ("today".equalsIgnoreCase(period)) {
            startDate = now.toLocalDate().atStartOfDay();
            periodName = "Today";
        } else if ("week".equalsIgnoreCase(period)) {
            startDate = now.minusWeeks(1);
            periodName = "Last 7 days";
        } else if ("year".equalsIgnoreCase(period)) {
            startDate = now.minusYears(1);
            periodName = "Last year";
        } else {
            // Default: last 30 days
            startDate = now.minusDays(30);
            periodName = "Last 30 days";
        }

        BigDecimal revenue = orderService.getTotalRevenue(startDate, now);

        stats.put("revenue", revenue);
        stats.put("period", periodName);
        stats.put("startDate", startDate);
        stats.put("endDate", now);
        stats.put("currency", "VND");

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/stats/revenue/custom
     * Doanh thu theo khoảng thời gian tùy chỉnh
     */
    @GetMapping("/stats/revenue/custom")
    public ResponseEntity<Map<String, Object>> getCustomRevenueStats(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            BigDecimal revenue = orderService.getTotalRevenue(start, end);

            stats.put("revenue", revenue);
            stats.put("startDate", start);
            stats.put("endDate", end);
            stats.put("currency", "VND");

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            stats.put("error", "Invalid date format. Use ISO format: 2024-01-01T00:00:00");
            return ResponseEntity.badRequest().body(stats);
        }
    }

    // ==================== REVIEW STATISTICS ====================

    /**
     * GET /api/admin/stats/reviews
     * Thống kê đánh giá
     */
    @GetMapping("/stats/reviews")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        Map<String, Object> stats = new HashMap<>();

        long total = reviewService.countAllReviews();

        stats.put("total", total);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/stats/recent-reviews
     * Đánh giá gần đây
     */
    @GetMapping("/stats/recent-reviews")
    public ResponseEntity<List<Review>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> recentReviews = reviewService.getRecentReviews(pageable);
        
        return ResponseEntity.ok(recentReviews);
    }

    // ==================== INVENTORY ALERTS ====================

    /**
     * GET /api/admin/alerts/low-stock
     * Cảnh báo sản phẩm sắp hết hàng
     */
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockAlerts(
            @RequestParam(defaultValue = "10") int threshold) {
        
        List<Product> lowStockProducts = productService.getLowStockProducts(threshold);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", lowStockProducts);
        response.put("count", lowStockProducts.size());
        response.put("threshold", threshold);
        response.put("severity", lowStockProducts.size() > 5 ? "HIGH" : "MEDIUM");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/alerts/out-of-stock
     * Cảnh báo sản phẩm hết hàng
     */
    @GetMapping("/alerts/out-of-stock")
    public ResponseEntity<Map<String, Object>> getOutOfStockAlerts() {
        List<Product> outOfStockProducts = productService.getOutOfStockProducts();
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", outOfStockProducts);
        response.put("count", outOfStockProducts.size());
        response.put("severity", "CRITICAL");

        return ResponseEntity.ok(response);
    }

    // ==================== SYSTEM INFO ====================

    /**
     * GET /api/admin/system-info
     * Thông tin hệ thống
     */
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        // Database stats
        Map<String, Object> dbStats = new HashMap<>();
        dbStats.put("products", productService.countAllProducts());
        dbStats.put("orders", orderService.countAllOrders());
        dbStats.put("users", userService.countAllUsers());
        dbStats.put("reviews", reviewService.countAllReviews());
        dbStats.put("categories", categoryService.countAllCategories());

        // System info
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
        long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024); // MB
        long usedMemory = totalMemory - freeMemory;

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("maxMemoryMB", maxMemory);
        memoryInfo.put("totalMemoryMB", totalMemory);
        memoryInfo.put("usedMemoryMB", usedMemory);
        memoryInfo.put("freeMemoryMB", freeMemory);

        info.put("database", dbStats);
        info.put("memory", memoryInfo);
        info.put("timestamp", LocalDateTime.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));

        return ResponseEntity.ok(info);
    }

    // ==================== TEST ENDPOINT ====================

    /**
     * GET /api/admin/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Admin API is running!");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}