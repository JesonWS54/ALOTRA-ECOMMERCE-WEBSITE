package com.alotra.service;

import com.alotra.entity.Product;
import com.alotra.entity.Category;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProductService - Business logic for product management
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NotificationService notificationService;

    // ==================== CREATE ====================

    /**
     * Create new product
     */
    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());

        // Validate category exists
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + product.getCategory().getId()));
            product.setCategory(category);
        }

        // Generate slug from name if not provided
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }

        // Set default values
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getAverageRating() == null) {
            product.setAverageRating(0.0);
        }
        if (product.getReviewCount() == null) {
            product.setReviewCount(0);
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        // Send notification
        notificationService.notifyNewProduct(savedProduct.getId(), savedProduct.getName());

        logger.info("Product created successfully with id: {}", savedProduct.getId());
        return savedProduct;
    }

    // ==================== READ ====================

    /**
     * Get product by ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by slug
     */
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get all products with pagination
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Get active products only
     */
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActive(true);
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Get active products by category
     */
    public List<Product> getActiveProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActive(categoryId, true);
    }

    /**
     * Search products by name
     */
    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Get products by price range
     */
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Get featured products
     */
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedAndIsActive(true, true);
    }

    /**
     * Get products with low stock (below threshold)
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findByStockLessThanAndIsActive(threshold, true);
    }

    /**
     * Get top rated products
     */
    public List<Product> getTopRatedProducts(int limit) {
        return productRepository.findTopByOrderByAverageRatingDesc(limit);
    }

    /**
     * Get best selling products
     */
    public List<Product> getBestSellingProducts(int limit) {
        return productRepository.findTopByOrderBySoldCountDesc(limit);
    }

    /**
     * Get newest products
     */
    public List<Product> getNewestProducts(int limit) {
        return productRepository.findTopByOrderByCreatedAtDesc(limit);
    }

    // ==================== UPDATE ====================

    /**
     * Update product
     */
    public Product updateProduct(Long id, Product productDetails) {
        logger.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getSlug() != null) {
            product.setSlug(productDetails.getSlug());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }
        if (productDetails.getStock() != null) {
            product.setStock(productDetails.getStock());
        }
        if (productDetails.getImageUrl() != null) {
            product.setImageUrl(productDetails.getImageUrl());
        }
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
        if (productDetails.getIsActive() != null) {
            product.setIsActive(productDetails.getIsActive());
        }
        if (productDetails.getIsFeatured() != null) {
            product.setIsFeatured(productDetails.getIsFeatured());
        }

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        logger.info("Product updated successfully: {}", updatedProduct.getId());
        return updatedProduct;
    }

    /**
     * Update product stock
     */
    public Product updateStock(Long id, Integer newStock) {
        logger.info("Updating stock for product id: {} to {}", id, newStock);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Increase stock (when restocking)
     */
    public Product increaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Integer currentStock = product.getStock() != null ? product.getStock() : 0;
        product.setStock(currentStock + quantity);
        product.setUpdatedAt(LocalDateTime.now());

        logger.info("Increased stock for product {}: {} -> {}", id, currentStock, product.getStock());
        return productRepository.save(product);
    }

    /**
     * Decrease stock (when selling)
     */
    public Product decreaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Integer currentStock = product.getStock() != null ? product.getStock() : 0;
        
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(currentStock - quantity);
        product.setUpdatedAt(LocalDateTime.now());

        logger.info("Decreased stock for product {}: {} -> {}", id, currentStock, product.getStock());
        return productRepository.save(product);
    }

    /**
     * Update product rating
     */
    public Product updateRating(Long id, Double newAverageRating, Integer newReviewCount) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setAverageRating(newAverageRating);
        product.setReviewCount(newReviewCount);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Toggle product active status
     */
    public Product toggleActiveStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setIsActive(!product.getIsActive());
        product.setUpdatedAt(LocalDateTime.now());

        logger.info("Toggled active status for product {}: {}", id, product.getIsActive());
        return productRepository.save(product);
    }

    /**
     * Toggle featured status
     */
    public Product toggleFeaturedStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Boolean currentFeatured = product.getIsFeatured() != null ? product.getIsFeatured() : false;
        product.setIsFeatured(!currentFeatured);
        product.setUpdatedAt(LocalDateTime.now());

        logger.info("Toggled featured status for product {}: {}", id, product.getIsFeatured());
        return productRepository.save(product);
    }

    // ==================== DELETE ====================

    /**
     * Delete product (soft delete by setting isActive = false)
     */
    public void deleteProduct(Long id) {
        logger.info("Soft deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        logger.info("Product soft deleted successfully: {}", id);
    }

    /**
     * Hard delete product (permanently remove from database)
     */
    public void hardDeleteProduct(Long id) {
        logger.warn("Hard deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        logger.info("Product hard deleted successfully: {}", id);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generate slug from product name
     */
    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    /**
     * Check if product exists
     */
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Check if slug exists
     */
    public boolean existsBySlug(String slug) {
        return productRepository.findBySlug(slug).isPresent();
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        return product.getStock() != null && product.getStock() > 0;
    }

    /**
     * Check if quantity is available
     */
    public boolean isQuantityAvailable(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        Integer stock = product.getStock() != null ? product.getStock() : 0;
        return stock >= quantity;
    }

    /**
     * Get total product count
     */
    public long getTotalProductCount() {
        return productRepository.count();
    }

    /**
     * Get active product count
     */
    public long getActiveProductCount() {
        return productRepository.countByIsActive(true);
    }

    /**
     * Get products count by category
     */
    public long getProductCountByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
}