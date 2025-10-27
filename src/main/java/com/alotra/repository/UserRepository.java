package com.alotra.repository;

import com.alotra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Data access layer for User entity
 * Provides CRUD operations and custom queries for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===================================
    // BASIC FINDER METHODS
    // ===================================

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ===================================
    // ROLE-BASED QUERIES
    // ===================================

    /**
     * Find all users by role
     */
    List<User> findByRole(String role);

    /**
     * Find all active users by role
     */
    List<User> findByRoleAndIsActive(String role, Boolean isActive);

    /**
     * Count users by role
     */
    long countByRole(String role);

    // ===================================
    // ACTIVE/INACTIVE QUERIES
    // ===================================

    /**
     * Find all active users
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * Find all active users with pagination
     */
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    // ===================================
    // EMAIL VERIFICATION QUERIES
    // ===================================

    /**
     * Find users by email verification status
     */
    List<User> findByEmailVerified(Boolean emailVerified);

    /**
     * Count unverified emails
     */
    long countByEmailVerified(Boolean emailVerified);

    // ===================================
    // SEARCH QUERIES
    // ===================================

    /**
     * Search users by username, email, or full name
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search active users only
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchActiveUsers(@Param("keyword") String keyword, Pageable pageable);

    // ===================================
    // DATE-BASED QUERIES
    // ===================================

    /**
     * Find users registered after a specific date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users who logged in recently
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :date")
    List<User> findRecentlyLoggedInUsers(@Param("date") LocalDateTime date);

    /**
     * Find inactive users (no login for X days)
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    // ===================================
    // STATISTICS QUERIES
    // ===================================

    /**
     * Count total active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Get user registration statistics by date
     */
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY DATE(u.createdAt) " +
           "ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Count users by role
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    // ===================================
    // ADMIN QUERIES
    // ===================================

    /**
     * Find all admin users
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findAllAdmins();

    /**
     * Find top customers by order count
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN u.orders o " +
           "WHERE u.isActive = true " +
           "GROUP BY u " +
           "ORDER BY COUNT(o) DESC")
    Page<User> findTopCustomers(Pageable pageable);

    // ===================================
    // UPDATE QUERIES
    // ===================================

    /**
     * Update last login timestamp
     */
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    Page<User> findByRole(String role, Pageable pageable);
    long countByIsActive(Boolean isActive);
}