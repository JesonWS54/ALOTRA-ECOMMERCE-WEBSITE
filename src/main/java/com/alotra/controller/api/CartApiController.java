package com.alotra.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * CartApiController - REST API for Shopping Cart
 * NOTE: Cần CartService để implement đầy đủ
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartApiController {

    // TODO: Uncomment khi có CartService
    // @Autowired
    // private CartService cartService;

    /**
     * GET /api/cart/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Cart API is ready!");
        response.put("note", "Cần CartService để implement đầy đủ");
        return ResponseEntity.ok(response);
    }

    // ==================== PLACEHOLDER ENDPOINTS ====================
    // TODO: Implement khi có CartService

    /**
     * GET /api/cart/user/{userId}
     * Lấy giỏ hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart API endpoint - Cần CartService");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/cart/add
     * Thêm sản phẩm vào giỏ
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Add to cart endpoint - Cần CartService");
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/cart/update
     * Cập nhật số lượng
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Update cart endpoint - Cần CartService");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/cart/remove/{itemId}
     * Xóa item khỏi giỏ
     */
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Remove from cart endpoint - Cần CartService");
        response.put("itemId", itemId.toString());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/cart/clear/{userId}
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Clear cart endpoint - Cần CartService");
        response.put("userId", userId.toString());
        return ResponseEntity.ok(response);
    }
}