package com.alotra.repository;

import com.alotra.entity.Cart;
import com.alotra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CartRepository
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUser(User user);

    /**
     * Check if cart exists for user ID
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete cart by user
     */
    void deleteByUser(User user);

    /**
     * Delete cart by user ID
     */
    void deleteByUserId(Long userId);
}