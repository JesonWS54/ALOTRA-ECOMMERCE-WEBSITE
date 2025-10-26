package com.alotra.repository;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CartItemRepository
 * Repository for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find cart item by cart and product
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Count items in cart
     */
    long countByCart(Cart cart);

    /**
     * Count items by cart ID
     */
    long countByCartId(Long cartId);

    /**
     * Check if product exists in cart
     */
    boolean existsByCartAndProduct(Cart cart, Product product);

    /**
     * Delete all items in a cart
     */
    @Modifying
    @Transactional
    void deleteByCart(Cart cart);

    /**
     * Delete specific product from cart
     */
    @Modifying
    @Transactional
    void deleteByCartAndProduct(Cart cart, Product product);

    /**
     * Delete all items for a product (when product is deleted)
     */
    @Modifying
    @Transactional
    void deleteByProduct(Product product);

    /**
     * Get total quantity in cart
     */
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart = :cart")
    Integer getTotalQuantityInCart(@Param("cart") Cart cart);

    /**
     * Get cart items by cart ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);
}