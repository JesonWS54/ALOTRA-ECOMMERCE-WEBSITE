package com.alotra.repository;

import com.alotra.entity.Product;
import com.alotra.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct(Product product);
    
    List<ProductImage> findByProductId(Long productId);
    
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);
    
    Optional<ProductImage> findByProductIdAndIsPrimary(Long productId, Boolean isPrimary);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC")
    List<ProductImage> findImagesByProductId(@Param("productId") Long productId);
    
    void deleteByProductId(Long productId);
    
    long countByProductId(Long productId);
}