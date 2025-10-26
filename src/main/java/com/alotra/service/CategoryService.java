package com.alotra.service;

import com.alotra.entity.Category;
import com.alotra.repository.CategoryRepository;
import com.alotra.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CategoryService - FINAL VERSION
 * Phù hợp với Entity tiếng Anh
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Tạo danh mục mới
     */
    public Category createCategory(Category category) {
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    /**
     * Lấy danh mục theo ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Lấy danh mục theo slug
     */
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /**
     * Lấy danh mục theo tên
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * Lấy tất cả danh mục
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy danh mục với phân trang
     */
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    /**
     * Lấy danh mục đang hoạt động
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActive(true);
    }

    /**
     * Lấy danh mục active theo thứ tự
     */
    public List<Category> getActiveCategoriesOrdered() {
        return categoryRepository.findByIsActiveOrderByDisplayOrderAsc(true);
    }

    /**
     * Lấy tất cả danh mục active
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllActiveCategories();
    }

    /**
     * Cập nhật danh mục
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        // Cập nhật các trường
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setImageUrl(categoryDetails.getImageUrl());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setIsActive(categoryDetails.getIsActive());

        // Cập nhật slug nếu tên thay đổi
        if (!category.getName().equals(categoryDetails.getName())) {
            category.setSlug(generateSlug(categoryDetails.getName()));
        }

        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    /**
     * Xóa danh mục
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        long productCount = productRepository.countByCategory(category);
        if (productCount > 0) {
            throw new RuntimeException("Không thể xóa danh mục này vì còn " + productCount + " sản phẩm đang sử dụng");
        }

        categoryRepository.deleteById(id);
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Đếm số sản phẩm trong danh mục
     */
    public long getProductCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        
        return productRepository.countByCategory(category);
    }

    /**
     * Đếm số sản phẩm active trong danh mục
     */
    public long getActiveProductCount(Long categoryId) {
        return productRepository.countByCategoryIdAndIsActive(categoryId, true);
    }

    /**
     * Kiểm tra danh mục có sản phẩm không
     */
    public boolean hasProducts(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        
        return productRepository.countByCategory(category) > 0;
    }

    /**
     * Chuyển đổi trạng thái hoạt động
     */
    public Category toggleActiveStatus(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        category.setIsActive(!category.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    /**
     * Cập nhật thứ tự hiển thị
     */
    public Category updateDisplayOrder(Long id, Integer displayOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        category.setDisplayOrder(displayOrder);
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    // ==================== UTILITIES ====================

    /**
     * Đếm tổng số danh mục
     */
    public long countAllCategories() {
        return categoryRepository.count();
    }

    /**
     * Đếm danh mục đang hoạt động
     */
    public long countActiveCategories() {
        return categoryRepository.countByIsActive(true);
    }

    /**
     * Kiểm tra danh mục tồn tại
     */
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    /**
     * Kiểm tra tên đã tồn tại chưa
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * Tạo slug từ tên danh mục
     */
    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        
        String slug = name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        return slug;
    }
}