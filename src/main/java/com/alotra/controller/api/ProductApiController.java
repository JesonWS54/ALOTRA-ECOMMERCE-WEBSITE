package com.alotra.controller.api;

import com.alotra.entity.Product;
import com.alotra.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ProductApiController - REST API for Products
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * GET /api/products
     * Lấy tất cả sản phẩm với phân trang
     */
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/active
     * Lấy sản phẩm đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<Page<Product>> getActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getActiveProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id}
     * Lấy sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy sản phẩm với ID: " + id));
    }

    /**
     * GET /api/products/slug/{slug}
     * Lấy sản phẩm theo slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        Optional<Product> product = productService.getProductBySlug(slug);
        
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy sản phẩm với slug: " + slug));
    }

    /**
     * POST /api/products
     * Tạo sản phẩm mới
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Lỗi khi tạo sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/products/{id}
     * Cập nhật sản phẩm
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Lỗi khi cập nhật: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/products/{id}
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm thành công");
            response.put("productId", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== SEARCH & FILTER ====================

    /**
     * GET /api/products/search
     * Tìm kiếm sản phẩm
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchActiveProducts(keyword, pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/category/{categoryId}
     * Lấy sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getActiveProductsByCategory(categoryId, pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/bestseller
     * Lấy sản phẩm bán chạy
     */
    @GetMapping("/bestseller")
    public ResponseEntity<Page<Product>> getBestsellerProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getBestsellerProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/vegan
     * Lấy sản phẩm vegan
     */
    @GetMapping("/vegan")
    public ResponseEntity<Page<Product>> getVeganProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getVeganProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/newest
     * Lấy sản phẩm mới nhất
     */
    @GetMapping("/newest")
    public ResponseEntity<Page<Product>> getNewestProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getNewestProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/top-rated
     * Lấy sản phẩm đánh giá cao
     */
    @GetMapping("/top-rated")
    public ResponseEntity<Page<Product>> getTopRatedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.getTopRatedProducts(pageable);
        
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/sustainable
     * Lấy sản phẩm bền vững
     */
    @GetMapping("/sustainable")
    public ResponseEntity<List<Product>> getSustainableProducts() {
        List<Product> products = productService.getSustainableProducts();
        return ResponseEntity.ok(products);
    }

    // ==================== STOCK MANAGEMENT ====================

    /**
     * PUT /api/products/{id}/stock
     * Cập nhật stock
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            if (quantity == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Quantity is required"));
            }
            
            Product product = productService.updateStock(id, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/products/{id}/increase-stock
     * Tăng stock
     */
    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<?> increaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Valid quantity is required"));
            }
            
            Product product = productService.increaseStock(id, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/products/{id}/decrease-stock
     * Giảm stock
     */
    @PostMapping("/{id}/decrease-stock")
    public ResponseEntity<?> decreaseStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer quantity = request.get("quantity");
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Valid quantity is required"));
            }
            
            Product product = productService.decreaseStock(id, quantity);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/products/{id}/check-stock
     * Kiểm tra stock
     */
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<?> checkStock(@PathVariable Long id) {
        try {
            boolean inStock = productService.isInStock(id);
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("productId", id);
            response.put("inStock", inStock);
            response.put("stockQuantity", product.getStockQuantity());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/products/low-stock
     * Lấy sản phẩm sắp hết hàng
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/out-of-stock
     * Lấy sản phẩm hết hàng
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        List<Product> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

    // ==================== UTILITIES ====================

    /**
     * GET /api/products/count
     * Đếm số lượng sản phẩm
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countProducts() {
        long total = productService.countAllProducts();
        long active = productService.countActiveProducts();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalProducts", total);
        response.put("activeProducts", active);
        response.put("inactiveProducts", total - active);
        
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/products/{id}/toggle-active
     * Bật/tắt trạng thái sản phẩm
     */
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActiveStatus(@PathVariable Long id) {
        try {
            Product product = productService.toggleActiveStatus(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET /api/products/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Product API is running!");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}