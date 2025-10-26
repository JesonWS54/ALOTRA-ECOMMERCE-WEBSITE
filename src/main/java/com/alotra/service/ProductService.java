package com.alotra.service;

import com.alotra.entity.Product;
import com.alotra.entity.Category;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.CategoryRepository;
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
 * ProductService - FINAL VERSION
 * Phù hợp với Entity tiếng Anh
 */
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ⚠️ Comment out nếu NotificationService chưa có hoặc lỗi
    // @Autowired
    // private NotificationService notificationService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Tạo sản phẩm mới
     */
    public Product createProduct(Product product) {
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        
        // Log thay vì notification
        System.out.println("✅ Sản phẩm mới đã tạo: " + savedProduct.getName());
        
        return savedProduct;
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Lấy sản phẩm theo slug
     */
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Lấy sản phẩm với phân trang
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Lấy sản phẩm đang hoạt động
     */
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActive(true);
    }

    /**
     * Lấy sản phẩm đang hoạt động với phân trang
     */
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByIsActive(true, pageable);
    }

    /**
     * Cập nhật sản phẩm
     */
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Cập nhật các trường
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setLongDescription(productDetails.getLongDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCalories(productDetails.getCalories());
        product.setIsVegan(productDetails.getIsVegan());
        product.setIsSustainable(productDetails.getIsSustainable());
        product.setIsBestseller(productDetails.getIsBestseller());
        product.setIsActive(productDetails.getIsActive());
        product.setStockQuantity(productDetails.getStockQuantity());
        
        if (productDetails.getCategory() != null) {
            product.setCategory(productDetails.getCategory());
        }

        // Cập nhật slug nếu tên thay đổi
        if (!product.getName().equals(productDetails.getName())) {
            product.setSlug(generateSlug(productDetails.getName()));
        }

        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Xóa sản phẩm
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // ==================== CATEGORY QUERIES ====================

    /**
     * Lấy sản phẩm theo danh mục
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        return productRepository.findByCategory(category);
    }

    /**
     * Lấy sản phẩm theo danh mục với phân trang
     */
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * Lấy sản phẩm active theo danh mục
     */
    public Page<Product> getActiveProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + categoryId));
        return productRepository.findByCategoryAndIsActive(category, true, pageable);
    }

    // ==================== SEARCH ====================

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Tìm kiếm sản phẩm theo tên với phân trang
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    /**
     * Tìm kiếm sản phẩm active
     */
    public Page<Product> searchActiveProducts(String keyword, Pageable pageable) {
        return productRepository.searchActiveProducts(keyword, pageable);
    }

    // ==================== SPECIAL PRODUCTS ====================

    /**
     * Lấy sản phẩm bestseller
     */
    public Page<Product> getBestsellerProducts(Pageable pageable) {
        return productRepository.findByIsBestsellerAndIsActive(true, true, pageable);
    }

    /**
     * Lấy sản phẩm vegan
     */
    public Page<Product> getVeganProducts(Pageable pageable) {
        return productRepository.findByIsVeganAndIsActive(true, true, pageable);
    }

    /**
     * Lấy sản phẩm bền vững
     */
    public List<Product> getSustainableProducts() {
        return productRepository.findByIsSustainable(true);
    }

    /**
     * Lấy sản phẩm mới nhất
     */
    public Page<Product> getNewestProducts(Pageable pageable) {
        return productRepository.findNewestProducts(pageable);
    }

    /**
     * Lấy sản phẩm được đánh giá cao
     */
    public Page<Product> getTopRatedProducts(Pageable pageable) {
        return productRepository.findTopRatedProducts(BigDecimal.valueOf(4.0), pageable);
    }

    // ==================== STOCK MANAGEMENT ====================

    /**
     * Cập nhật số lượng tồn kho
     */
    public Product updateStock(Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        product.setStockQuantity(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Tăng số lượng tồn kho
     */
    public Product increaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        product.setStockQuantity(currentStock + quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("✅ Tăng stock: " + product.getName() + " +" + quantity + " = " + product.getStockQuantity());
        
        return productRepository.save(product);
    }

    /**
     * Giảm số lượng tồn kho
     */
    public Product decreaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        
        if (currentStock < quantity) {
            throw new RuntimeException("Không đủ số lượng trong kho. Còn lại: " + currentStock);
        }
        
        product.setStockQuantity(currentStock - quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("✅ Giảm stock: " + product.getName() + " -" + quantity + " = " + product.getStockQuantity());
        
        return productRepository.save(product);
    }

    /**
     * Kiểm tra còn hàng
     */
    public boolean isInStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        return product.isInStock();
    }

    /**
     * Kiểm tra số lượng có sẵn
     */
    public boolean isQuantityAvailable(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
        
        Integer stock = product.getStockQuantity();
        return stock != null && stock >= quantity;
    }

    /**
     * Lấy sản phẩm sắp hết hàng
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    /**
     * Lấy sản phẩm hết hàng
     */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    // ==================== UTILITIES ====================

    /**
     * Đếm tổng số sản phẩm
     */
    public long countAllProducts() {
        return productRepository.count();
    }

    /**
     * Đếm sản phẩm đang hoạt động
     */
    public long countActiveProducts() {
        return productRepository.countActiveProducts();
    }

    /**
     * Kiểm tra sản phẩm tồn tại
     */
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Kiểm tra slug tồn tại
     */
    public boolean existsBySlug(String slug) {
        return productRepository.existsBySlug(slug);
    }

    /**
     * Chuyển đổi trạng thái hoạt động
     */
    public Product toggleActiveStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        product.setIsActive(!product.getIsActive());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Tạo slug từ tên sản phẩm
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