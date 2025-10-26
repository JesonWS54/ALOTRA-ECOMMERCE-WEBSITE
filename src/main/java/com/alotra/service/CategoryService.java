package com.alotra.service;

import com.alotra.entity.Category;
import com.alotra.repository.CategoryRepository;
import com.alotra.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CategoryService - Business logic for category management
 */
@Service
@Transactional
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==================== CREATE ====================

    /**
     * Create new category
     */
    public Category createCategory(Category category) {
        logger.info("Creating new category: {}", category.getName());

        // Generate slug from name if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }

        // Validate unique slug
        if (categoryRepository.findBySlug(category.getSlug()).isPresent()) {
            throw new RuntimeException("Category with slug '" + category.getSlug() + "' already exists");
        }

        // Set default values
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        logger.info("Category created successfully with id: {}", savedCategory.getId());
        return savedCategory;
    }

    // ==================== READ ====================

    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Get category by slug
     */
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get active categories only
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActive(true);
    }

    /**
     * Get categories ordered by display order
     */
    public List<Category> getCategoriesOrderedByDisplayOrder() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Get active categories ordered by display order
     */
    public List<Category> getActiveCategoriesOrdered() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Search categories by name
     */
    public List<Category> searchCategoriesByName(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword);
    }

    // ==================== UPDATE ====================

    /**
     * Update category
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        logger.info("Updating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Update fields
        if (categoryDetails.getName() != null) {
            category.setName(categoryDetails.getName());
        }
        if (categoryDetails.getSlug() != null) {
            // Validate unique slug (excluding current category)
            Optional<Category> existingCategory = categoryRepository.findBySlug(categoryDetails.getSlug());
            if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                throw new RuntimeException("Category with slug '" + categoryDetails.getSlug() + "' already exists");
            }
            category.setSlug(categoryDetails.getSlug());
        }
        if (categoryDetails.getDescription() != null) {
            category.setDescription(categoryDetails.getDescription());
        }
        if (categoryDetails.getImageUrl() != null) {
            category.setImageUrl(categoryDetails.getImageUrl());
        }
        if (categoryDetails.getIsActive() != null) {
            category.setIsActive(categoryDetails.getIsActive());
        }
        if (categoryDetails.getDisplayOrder() != null) {
            category.setDisplayOrder(categoryDetails.getDisplayOrder());
        }

        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);

        logger.info("Category updated successfully: {}", updatedCategory.getId());
        return updatedCategory;
    }

    /**
     * Update category display order
     */
    public Category updateDisplayOrder(Long id, Integer newOrder) {
        logger.info("Updating display order for category id: {} to {}", id, newOrder);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setDisplayOrder(newOrder);
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    /**
     * Toggle category active status
     */
    public Category toggleActiveStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        category.setIsActive(!category.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());

        logger.info("Toggled active status for category {}: {}", id, category.getIsActive());
        return categoryRepository.save(category);
    }

    // ==================== DELETE ====================

    /**
     * Delete category (soft delete by setting isActive = false)
     */
    public void deleteCategory(Long id) {
        logger.info("Soft deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if category has products
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with existing products. Move or delete products first.");
        }

        category.setIsActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        logger.info("Category soft deleted successfully: {}", id);
    }

    /**
     * Hard delete category (permanently remove from database)
     */
    public void hardDeleteCategory(Long id) {
        logger.warn("Hard deleting category with id: {}", id);

        // Check if category has products
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category with existing products. Move or delete products first.");
        }

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }

        categoryRepository.deleteById(id);
        logger.info("Category hard deleted successfully: {}", id);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generate slug from category name
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
     * Check if category exists
     */
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Check if slug exists
     */
    public boolean existsBySlug(String slug) {
        return categoryRepository.findBySlug(slug).isPresent();
    }

    /**
     * Get product count for category
     */
    public long getProductCount(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    /**
     * Get active product count for category
     */
    public long getActiveProductCount(Long categoryId) {
        return productRepository.countByCategoryIdAndIsActive(categoryId, true);
    }

    /**
     * Get total category count
     */
    public long getTotalCategoryCount() {
        return categoryRepository.count();
    }

    /**
     * Get active category count
     */
    public long getActiveCategoryCount() {
        return categoryRepository.countByIsActive(true);
    }

    /**
     * Check if category has products
     */
    public boolean hasProducts(Long categoryId) {
        return productRepository.countByCategoryId(categoryId) > 0;
    }

    /**
     * Get category with product count
     */
    public CategoryWithProductCount getCategoryWithProductCount(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        long productCount = productRepository.countByCategoryId(id);
        long activeProductCount = productRepository.countByCategoryIdAndIsActive(id, true);
        
        return new CategoryWithProductCount(category, productCount, activeProductCount);
    }

    /**
     * Get all categories with product counts
     */
    public List<CategoryWithProductCount> getAllCategoriesWithProductCounts() {
        List<Category> categories = categoryRepository.findAll();
        
        return categories.stream()
                .map(category -> {
                    long productCount = productRepository.countByCategoryId(category.getId());
                    long activeProductCount = productRepository.countByCategoryIdAndIsActive(category.getId(), true);
                    return new CategoryWithProductCount(category, productCount, activeProductCount);
                })
                .toList();
    }

    // ==================== INNER CLASS ====================

    /**
     * DTO for category with product count
     */
    public static class CategoryWithProductCount {
        private Category category;
        private long totalProducts;
        private long activeProducts;

        public CategoryWithProductCount(Category category, long totalProducts, long activeProducts) {
            this.category = category;
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
        }

        public Category getCategory() {
            return category;
        }

        public long getTotalProducts() {
            return totalProducts;
        }

        public long getActiveProducts() {
            return activeProducts;
        }
    }
}