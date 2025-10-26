package com.alotra.service;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Product;
import com.alotra.entity.User;
import com.alotra.repository.CartRepository;
import com.alotra.repository.CartItemRepository;
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
 * CartService - Business logic for shopping cart management
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    // ==================== CREATE / GET CART ====================

    /**
     * Get or create cart for user
     */
    public Cart getOrCreateCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(newCart);
        logger.info("Created new cart for user: {}", userId);
        
        return savedCart;
    }

    /**
     * Get cart by user ID
     */
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    /**
     * Get cart by ID
     */
    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    // ==================== ADD TO CART ====================

    /**
     * Add product to cart
     */
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        logger.info("Adding product {} to cart for user {}, quantity: {}", productId, userId, quantity);

        // Validate product exists and is active
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available: " + product.getName());
        }

        // Check stock availability
        if (!productService.isQuantityAvailable(productId, quantity)) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            // Check stock for new quantity
            if (!productService.isQuantityAvailable(productId, newQuantity)) {
                throw new RuntimeException("Cannot add more. Insufficient stock for product: " + product.getName());
            }

            cartItem.setQuantity(newQuantity);
            logger.info("Updated cart item quantity to: {}", newQuantity);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            logger.info("Created new cart item");
        }

        CartItem savedItem = cartItemRepository.save(cartItem);

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return savedItem;
    }

    // ==================== UPDATE CART ====================

    /**
     * Update cart item quantity
     */
    public CartItem updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        logger.info("Updating cart item {} quantity to: {}", cartItemId, newQuantity);

        if (newQuantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        // Check stock availability
        if (!productService.isQuantityAvailable(cartItem.getProduct().getId(), newQuantity)) {
            throw new RuntimeException("Insufficient stock for product: " + cartItem.getProduct().getName());
        }

        cartItem.setQuantity(newQuantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);

        // Update cart timestamp
        Cart cart = cartItem.getCart();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Cart item quantity updated successfully");
        return updatedItem;
    }

    /**
     * Increase cart item quantity
     */
    public CartItem increaseQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        return updateCartItemQuantity(cartItemId, cartItem.getQuantity() + 1);
    }

    /**
     * Decrease cart item quantity
     */
    public CartItem decreaseQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        int newQuantity = cartItem.getQuantity() - 1;
        
        if (newQuantity <= 0) {
            removeFromCart(cartItemId);
            return null;
        }

        return updateCartItemQuantity(cartItemId, newQuantity);
    }

    // ==================== REMOVE FROM CART ====================

    /**
     * Remove item from cart
     */
    public void removeFromCart(Long cartItemId) {
        logger.info("Removing cart item: {}", cartItemId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartItemId));

        Cart cart = cartItem.getCart();
        
        cartItemRepository.deleteById(cartItemId);

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        logger.info("Cart item removed successfully");
    }

    /**
     * Remove product from cart by product ID
     */
    public void removeProductFromCart(Long userId, Long productId) {
        logger.info("Removing product {} from cart for user {}", productId, userId);

        Cart cart = getOrCreateCart(userId);
        
        Optional<CartItem> cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        
        if (cartItem.isPresent()) {
            cartItemRepository.deleteById(cartItem.get().getId());
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            logger.info("Product removed from cart successfully");
        }
    }

    /**
     * Clear cart (remove all items)
     */
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);

        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
            
            cartItemRepository.deleteAll(items);
            
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            
            logger.info("Cart cleared successfully, removed {} items", items.size());
        }
    }

    // ==================== CART INFORMATION ====================

    /**
     * Get all items in cart
     */
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartId(cart.getId());
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.countByCartId(cart.getId());
    }

    /**
     * Get total quantity in cart
     */
    public int getTotalQuantity(Long userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Calculate cart total
     */
    public BigDecimal calculateTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get cart summary
     */
    public CartSummary getCartSummary(Long userId) {
        List<CartItem> items = getCartItems(userId);
        
        int itemCount = items.size();
        int totalQuantity = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CartSummary(items, itemCount, totalQuantity, totalAmount);
    }

    // ==================== VALIDATION ====================

    /**
     * Check if product is in cart
     */
    public boolean isProductInCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).isPresent();
    }

    /**
     * Validate cart items (check stock availability)
     */
    public CartValidationResult validateCart(Long userId) {
        List<CartItem> items = getCartItems(userId);
        
        CartValidationResult result = new CartValidationResult();
        result.setValid(true);

        for (CartItem item : items) {
            Product product = item.getProduct();
            
            // Check if product is still active
            if (!product.getIsActive()) {
                result.setValid(false);
                result.addError(item.getId(), "Product is no longer available: " + product.getName());
                continue;
            }

            // Check stock availability
            if (!productService.isQuantityAvailable(product.getId(), item.getQuantity())) {
                result.setValid(false);
                result.addError(item.getId(), "Insufficient stock for product: " + product.getName());
            }
        }

        return result;
    }

    /**
     * Sync cart prices with current product prices
     */
    public void syncCartPrices(Long userId) {
        logger.info("Syncing cart prices for user: {}", userId);
        
        List<CartItem> items = getCartItems(userId);
        
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (!item.getPrice().equals(product.getPrice())) {
                item.setPrice(product.getPrice());
                cartItemRepository.save(item);
                logger.info("Updated price for cart item {}: {} -> {}", 
                        item.getId(), item.getPrice(), product.getPrice());
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if cart is empty
     */
    public boolean isCartEmpty(Long userId) {
        return getCartItemCount(userId) == 0;
    }

    /**
     * Get cart age (in days)
     */
    public long getCartAge(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isEmpty()) {
            return 0;
        }

        Cart cart = cartOpt.get();
        return java.time.temporal.ChronoUnit.DAYS.between(
                cart.getCreatedAt().toLocalDate(),
                LocalDateTime.now().toLocalDate()
        );
    }

    // ==================== INNER CLASSES ====================

    /**
     * DTO for cart summary
     */
    public static class CartSummary {
        private List<CartItem> items;
        private int itemCount;
        private int totalQuantity;
        private BigDecimal totalAmount;

        public CartSummary(List<CartItem> items, int itemCount, int totalQuantity, BigDecimal totalAmount) {
            this.items = items;
            this.itemCount = itemCount;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
        }

        // Getters
        public List<CartItem> getItems() { return items; }
        public int getItemCount() { return itemCount; }
        public int getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
    }

    /**
     * DTO for cart validation result
     */
    public static class CartValidationResult {
        private boolean valid;
        private java.util.Map<Long, String> errors = new java.util.HashMap<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public java.util.Map<Long, String> getErrors() { return errors; }
        
        public void addError(Long cartItemId, String error) {
            this.errors.put(cartItemId, error);
        }
    }
}