package com.alotra.repository;

import com.alotra.entity.Coupon;
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
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Coupon> findByIsActive(Boolean isActive);
    
    Page<Coupon> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true " +
           "AND :now BETWEEN c.startDate AND c.endDate " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND :now BETWEEN c.startDate AND c.endDate " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAllActiveCoupons(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.endDate < :now")
    List<Coupon> findExpiredCoupons(@Param("now") LocalDateTime now);
}