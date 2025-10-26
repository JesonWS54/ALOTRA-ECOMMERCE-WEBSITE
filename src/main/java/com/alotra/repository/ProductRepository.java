package com.alotra.repository;

import com.alotra.entity.Category;
import com.alotra.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // BASIC FINDERS
    Optional<Product> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    List<Product> findByIsActive(Boolean isActive);
    
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
    
    // CATEGORY QUERIES
    List<Product> findByCategory(Category category);
    
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    Page<Product> findByCategoryAndIsActive(Category category, Boolean isActive, Pageable pageable);
    
    List<Product> findByCategoryId(Long categoryId);
    
    Page<Product> findByCategoryIdAndIsActive(Long categoryId, Boolean isActive, Pageable pageable);
    
    // SPECIAL PRODUCTS
    List<Product> findByIsBestseller(Boolean isBestseller);
    
    Page<Product> findByIsBestsellerAndIsActive(Boolean isBestseller, Boolean isActive, Pageable pageable);
    
    List<Product> findByIsVegan(Boolean isVegan);
    
    Page<Product> findByIsVeganAndIsActive(Boolean isVegan, Boolean isActive, Pageable pageable);
    
    List<Product> findByIsSustainable(Boolean isSustainable);
    
    // PRICE QUERIES
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    Page<Product> findByPriceBetweenAndIsActive(BigDecimal minPrice, BigDecimal maxPrice, Boolean isActive, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice AND p.isActive = true")
    Page<Product> findProductsUnderPrice(@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
    
    // SEARCH QUERIES
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchActiveProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // RATING QUERIES
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.rating >= :minRating ORDER BY p.rating DESC")
    Page<Product> findTopRatedProducts(@Param("minRating") BigDecimal minRating, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.rating DESC")
    List<Product> findTopRatedProducts(Pageable pageable);
    
    // SORTING QUERIES
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.reviewCount DESC")
    Page<Product> findMostReviewedProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Product> findNewestProducts(Pageable pageable);
    
    // TAG QUERIES
    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t.id = :tagId AND p.isActive = true")
    Page<Product> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t.slug = :tagSlug AND p.isActive = true")
    Page<Product> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);
    
    // INGREDIENT QUERIES
    @Query("SELECT p FROM Product p JOIN p.ingredients i WHERE i.id = :ingredientId AND p.isActive = true")
    Page<Product> findByIngredientId(@Param("ingredientId") Long ingredientId, Pageable pageable);
    
    // ALLERGEN QUERIES
    @Query("SELECT p FROM Product p WHERE p.id NOT IN " +
           "(SELECT p2.id FROM Product p2 JOIN p2.allergens a WHERE a.id IN :allergenIds) " +
           "AND p.isActive = true")
    Page<Product> findProductsWithoutAllergens(@Param("allergenIds") List<Long> allergenIds, Pageable pageable);
    
    // STOCK QUERIES
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    Page<Product> findInStockProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();
    
    // STATISTICS
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countActiveProducts();
    
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.isActive = true")
    BigDecimal getAveragePrice();
    
    @Query("SELECT c.name, COUNT(p) FROM Product p JOIN p.category c WHERE p.isActive = true GROUP BY c.name")
    List<Object[]> countProductsByCategory();
    
    // RELATED PRODUCTS
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId AND p.isActive = true ORDER BY p.rating DESC")
    List<Product> findRelatedProducts(@Param("categoryId") Long categoryId, @Param("productId") Long productId, Pageable pageable);
}