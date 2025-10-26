package com.alotra.repository;

import com.alotra.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    List<Promotion> findByIsActive(Boolean isActive);
    
    Page<Promotion> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
           "AND :now BETWEEN p.startDate AND p.endDate " +
           "ORDER BY p.createdAt DESC")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
           "AND :now BETWEEN p.startDate AND p.endDate " +
           "ORDER BY p.startDate DESC")
    Page<Promotion> findActivePromotions(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :now")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.startDate > :now AND p.isActive = true")
    List<Promotion> findUpcomingPromotions(@Param("now") LocalDateTime now);
}