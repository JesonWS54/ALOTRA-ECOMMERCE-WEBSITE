package com.alotra.controller.api;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CartApiController - REST API for Shopping Cart - COMPLETE
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartApiController {

    @Autowired
    private CartService cartService;

    // ==================== CART OPERATIONS ====================

    /**
     * GET /api/cart/user/{userId}
     * Lấy giỏ hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        try {
            Cart cart = cartService.getOrCreateCart(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cart", cart);
            response.put("itemCount", cart.getItems().size());
            response.put("totalQuantity", cartService.getTotalQuantity(userId));
            response.put("totalAmount", cartService.calculateTotal(userId));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/cart/{userId}/items
     * Lấy danh sách items trong giỏ
     */
    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long userId) {
        List<CartItem> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * GET /api/cart/{userId}/count
     * Đếm số items trong giỏ
     */
    @GetMapping("/{userId}/count")
    public ResponseEntity<Map<String, Object>> getCartCount(@PathVariable Long userId) {
        long itemCount = cartService.getCartItemCount(userId);
        int totalQuantity = cartService.getTotalQuantity(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("itemCount", itemCount);
        response.put("totalQuantity", totalQuantity);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cart/{userId}/total
     * Tính tổng tiền
     */
    @GetMapping("/{userId}/total")
    public ResponseEntity<Map<String, Object>> getCartTotal(@PathVariable Long userId) {
        BigDecimal total = cartService.calculateTotal(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", total);
        response.put("currency", "VND");
        
        return ResponseEntity.ok(response);
    }

    // ==================== ADD TO CART ====================

    /**
     * POST /api/cart/add
     * Thêm sản phẩm vào giỏ
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            CartItem cartItem = cartService.addToCart(userId, productId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng");
            response.put("cartItem", cartItem);
            response.put("itemCount", cartService.getCartItemCount(userId));
            response.put("totalAmount", cartService.calculateTotal(userId));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== UPDATE CART ====================

    /**
     * PUT /api/cart/item/{cartItemId}
     * Cập nhật số lượng item
     */
    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer newQuantity = request.get("quantity");
            if (newQuantity == null || newQuantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Số lượng phải lớn hơn 0"));
            }
            
            CartItem cartItem = cartService.updateCartItemQuantity(cartItemId, newQuantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã cập nhật số lượng");
            response.put("cartItem", cartItem);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/cart/item/{cartItemId}/increase
     * Tăng số lượng
     */
    @PostMapping("/item/{cartItemId}/increase")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long cartItemId) {
        try {
            CartItem cartItem = cartService.increaseQuantity(cartItemId);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/cart/item/{cartItemId}/decrease
     * Giảm số lượng
     */
    @PostMapping("/item/{cartItemId}/decrease")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long cartItemId) {
        try {
            CartItem cartItem = cartService.decreaseQuantity(cartItemId);
            
            if (cartItem == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Item đã được xóa khỏi giỏ hàng");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== REMOVE FROM CART ====================

    /**
     * DELETE /api/cart/item/{cartItemId}
     * Xóa item khỏi giỏ
     */
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeFromCart(cartItemId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/user/{userId}/product/{productId}
     * Xóa sản phẩm khỏi giỏ của user
     */
    @DeleteMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<?> removeProductFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        try {
            cartService.removeProductFromCart(userId, productId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE /api/cart/user/{userId}/clear
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            cartService.clearCart(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa toàn bộ giỏ hàng");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== VALIDATION ====================

    /**
     * GET /api/cart/{userId}/validate
     * Validate giỏ hàng (check stock)
     */
    @GetMapping("/{userId}/validate")
    public ResponseEntity<?> validateCart(@PathVariable Long userId) {
        try {
            boolean isValid = cartService.validateCart(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Giỏ hàng hợp lệ" : "Giỏ hàng không hợp lệ");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET /api/cart/{userId}/check-product/{productId}
     * Kiểm tra sản phẩm có trong giỏ không
     */
    @GetMapping("/{userId}/check-product/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkProductInCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        
        boolean inCart = cartService.isProductInCart(userId, productId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("inCart", inCart);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cart/{userId}/is-empty
     * Kiểm tra giỏ hàng rỗng
     */
    @GetMapping("/{userId}/is-empty")
    public ResponseEntity<Map<String, Boolean>> isCartEmpty(@PathVariable Long userId) {
        boolean isEmpty = cartService.isCartEmpty(userId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isEmpty", isEmpty);
        
        return ResponseEntity.ok(response);
    }

    // ==================== TEST ENDPOINT ====================

    /**
     * GET /api/cart/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Cart API is running!");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}