package com.alotra.repository;

import com.alotra.entity.RefreshToken;
import com.alotra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RefreshTokenRepository - Manages JWT refresh tokens
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all tokens for a specific user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all tokens for a user by user ID
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Check if token exists
     */
    boolean existsByToken(String token);

    /**
     * Delete token by token string
     */
    void deleteByToken(String token);

    /**
     * Delete all tokens for a user
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiryDate > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}