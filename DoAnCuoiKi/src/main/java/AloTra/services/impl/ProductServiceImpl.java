package AloTra.services.impl;

import AloTra.Model.ProductDTO;
import AloTra.Model.ProductHomeDTO;
import AloTra.entity.Category;
import AloTra.entity.Product;
import AloTra.entity.ProductImage;
import AloTra.entity.Shop;
import AloTra.repository.CategoryRepository;
import AloTra.repository.ProductImageRepository;
import AloTra.repository.ProductRepository;
import AloTra.repository.ShopRepository;
import AloTra.services.CloudinaryService;
import AloTra.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*; // Import Set, List, Map, Comparator
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductImageRepository productImageRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ShopRepository shopRepository;
    @Autowired private CloudinaryService cloudinaryService;

    private static final int HOME_BEST_SELLER_THRESHOLD = 10;

    // --- USER FACING METHODS ---

    @Override
    @Transactional(readOnly = true) // Cần Transactional để ensureThumbnailUrl hoạt động
    public Page<ProductDTO> getTopSellingProductsForHome(Pageable pageable) {
        return productRepository.findTopSellingForHome(HOME_BEST_SELLER_THRESHOLD, pageable).map(this::ensureThumbnailUrl);
    }

    @Override
    @Transactional(readOnly = true) // Cần Transactional để ensureThumbnailUrl hoạt động
    public Page<ProductDTO> getTop10BestSellingForMenu(Pageable pageable) {
        return productRepository.findTop10BestSellingForMenu(pageable).map(this::ensureThumbnailUrl);
    }

    @Override
    @Transactional(readOnly = true) // Cần Transactional để ensureThumbnailUrl hoạt động
    public Page<ProductDTO> getAllActiveProductsForMenu(Long categoryId, Pageable pageable) {
        return productRepository.findAllActiveProductsForMenu(categoryId, pageable).map(this::ensureThumbnailUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductDetails(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) { return Optional.empty(); }
        Product product = productOpt.get();
        // Lấy ảnh từ entity (đã được fetch LAZY nhờ @Transactional)
        List<String> imageUrls = product.getImages().stream()
                                     .sorted(Comparator.comparing(ProductImage::getIsThumbnail, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(ProductImage::getId))
                                     .map(ProductImage::getImageUrl)
                                     .collect(Collectors.toList());
        ProductDTO dto = convertToFullDTO(product);
        dto.setImages(imageUrls);
        dto.setThumbnailUrl(!imageUrls.isEmpty() ? imageUrls.get(0) : null);
        return Optional.of(dto);
    }

    // --- VENDOR MANAGEMENT METHODS ---

     @Override
     @Transactional(readOnly = true) // Cần Transactional để ensureThumbnailUrl hoạt động
     public Page<ProductDTO> getProductsByShopId(Long shopId, Pageable pageable) {
          // Query trả về DTO, dùng ensureThumbnailUrl để chắc chắn có ảnh
          return productRepository.findProductsByShopId(shopId, pageable).map(this::ensureThumbnailUrl);
     }

     @Override
     @Transactional
     public ProductDTO addProduct(Long shopId, ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException {
         Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new EntityNotFoundException("Shop không tồn tại"));
         Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("Category không tồn tại"));

         Product product = new Product();
         product.setShop(shop); product.setCategory(category); product.setName(productDTO.getName()); product.setDescription(productDTO.getDescription());
         product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : 0.0); product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
         // status, createdAt, updatedAt được xử lý tự động

         List<String> imageUrls = saveProductImages(product, imageFiles); // Gọi helper để add ảnh vào product entity

         Product savedProduct = productRepository.save(product); // Lưu product (cascade sẽ lưu ảnh)

         ProductDTO resultDTO = convertToFullDTO(savedProduct);
         resultDTO.setImages(imageUrls); resultDTO.setThumbnailUrl(!imageUrls.isEmpty() ? imageUrls.get(0) : null);
         return resultDTO;
     }

     @Override
     @Transactional(readOnly = true) // Cần Transactional để load LAZY images
     public Optional<ProductDTO> getEditProductDetails(Long productId, Long shopId) {
         Optional<Product> productOpt = productRepository.findByIdAndShop_Id(productId, shopId);
         if (productOpt.isEmpty()) { return Optional.empty(); }
         Product product = productOpt.get();
         // Lấy ảnh từ entity
         List<String> imageUrls = product.getImages().stream()
                                      .sorted(Comparator.comparing(ProductImage::getIsThumbnail, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(ProductImage::getId))
                                      .map(ProductImage::getImageUrl)
                                      .collect(Collectors.toList());
         ProductDTO dto = convertToFullDTO(product);
         dto.setImages(imageUrls); dto.setThumbnailUrl(!imageUrls.isEmpty() ? imageUrls.get(0) : null);
         return Optional.of(dto);
     }

     @Override
     @Transactional
     public ProductDTO updateProduct(Long productId, Long shopId, ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException {
         Product product = productRepository.findByIdAndShop_Id(productId, shopId)
                 .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc bạn không có quyền sửa."));

         Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("Category không tồn tại"));

         product.setCategory(category); product.setName(productDTO.getName()); product.setDescription(productDTO.getDescription());
         product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : 0.0); product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
         // updatedAt được cập nhật tự động

         List<String> finalImageUrls;
         if (imageFiles != null && !imageFiles.isEmpty() && !allFilesAreEmpty(imageFiles)) {
             // Xóa ảnh cũ Cloudinary (nếu cần) ...
             // Xóa ảnh cũ khỏi DB (orphanRemoval=true sẽ tự xóa khi clear list)
             product.getImages().clear(); // Quan trọng: Clear collection để kích hoạt orphanRemoval
             finalImageUrls = saveProductImages(product, imageFiles); // Add ảnh mới vào collection (cascade sẽ lưu)
         } else {
             // Giữ nguyên ảnh cũ
             finalImageUrls = product.getImages().stream()
                                  .sorted(Comparator.comparing(ProductImage::getIsThumbnail, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(ProductImage::getId))
                                  .map(ProductImage::getImageUrl).collect(Collectors.toList());
         }

         // Không cần gọi save() rõ ràng nếu entity được quản lý trong transaction (dirty checking)
         // Nhưng gọi save() cũng không sao và rõ ràng hơn.
         Product updatedProduct = productRepository.save(product);


         ProductDTO resultDTO = convertToFullDTO(updatedProduct);
         resultDTO.setImages(finalImageUrls); resultDTO.setThumbnailUrl(!finalImageUrls.isEmpty() ? finalImageUrls.get(0) : null);
         return resultDTO;
     }

     @Override
     @Transactional
     public void deleteProduct(Long productId, Long shopId) {
         Product product = productRepository.findByIdAndShop_Id(productId, shopId)
               .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại hoặc bạn không có quyền xóa."));
         // Xóa ảnh Cloudinary (nếu cần) ...
         productRepository.delete(product); // Cascade sẽ xóa ảnh, review...
     }

    // --- **ADMIN MANAGEMENT METHODS** ---
    @Override
    @Transactional(readOnly = true) // Cần Transactional để ensureThumbnailUrl hoạt động
    public Page<ProductDTO> getAllProductsAdmin(Pageable pageable) {
        // Query trả về DTO, dùng ensureThumbnailUrl để chắc chắn có ảnh
        return productRepository.findAllProductsAdmin(pageable).map(this::ensureThumbnailUrl);
    }

    @Override
    @Transactional
    public ProductDTO updateProductAdmin(Long productId, ProductDTO productDTO) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Category với ID: " + productDTO.getCategoryId()));

        // Cập nhật các trường cho phép Admin sửa
        product.setCategory(category);
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : product.getBasePrice()); // Giữ giá cũ nếu null
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : product.getStockQuantity()); // Giữ kho cũ nếu null
        if (productDTO.getStatus() != null && !productDTO.getStatus().isBlank()) { // Chỉ cập nhật status nếu được cung cấp
            product.setStatus(productDTO.getStatus());
        }
        // updatedAt được cập nhật tự động

        Product updatedProduct = productRepository.save(product);

        // Lấy lại ảnh từ DB để trả về DTO hoàn chỉnh
        List<String> imageUrls = updatedProduct.getImages().stream()
                                  .sorted(Comparator.comparing(ProductImage::getIsThumbnail, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(ProductImage::getId))
                                  .map(ProductImage::getImageUrl).collect(Collectors.toList());

        ProductDTO resultDTO = convertToFullDTO(updatedProduct);
        resultDTO.setImages(imageUrls);
        resultDTO.setThumbnailUrl(!imageUrls.isEmpty() ? imageUrls.get(0) : null);
        return resultDTO;
    }

    @Override
    @Transactional
    public void deleteProductAdmin(Long productId) {
         Product product = productRepository.findById(productId)
               .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
         // Xóa ảnh Cloudinary (nếu cần) ...
         productRepository.delete(product); // Cascade sẽ xóa
    }
    // --- **END ADMIN METHODS** ---


    // --- HÀM HELPER CHUYỂN ĐỔI ---
    // Chuyển đổi cơ bản từ Entity sang DTO (không gồm ảnh)
     private ProductDTO convertToFullDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setShopId(product.getShop() != null ? product.getShop().getId() : null);
        dto.setShopName(product.getShop() != null ? product.getShop().getShopName() : "N/A");
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : "N/A");
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSoldCount(product.getSoldCount());
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
     }

     // Đảm bảo DTO có thumbnail URL (dùng cho page lấy từ repo DTO query)
     @Transactional(readOnly = true) // Cần transactional nếu query lại ảnh
     private ProductDTO ensureThumbnailUrl(ProductDTO dto) {
          if (dto != null && (dto.getThumbnailUrl() == null || dto.getThumbnailUrl().isBlank())) {
               // Thử query ảnh đầu tiên nếu query DTO không có
               Optional<ProductImage> firstImage = productImageRepository.findFirstByProductIdOrderByIdAsc(dto.getId());
               firstImage.ifPresent(img -> dto.setThumbnailUrl(img.getImageUrl()));
               // Nếu vẫn không có, view sẽ dùng placeholder
          }
          return dto;
     }

     // --- HÀM HELPER LƯU ẢNH (Đã sửa cho cascade) ---
      private List<String> saveProductImages(Product product, List<MultipartFile> imageFiles) throws IOException {
          List<String> imageUrls = new ArrayList<>();
          boolean isFirstImage = true;
          if (imageFiles != null && !imageFiles.isEmpty()) {
              for (MultipartFile file : imageFiles) {
                  if (file != null && !file.isEmpty()) {
                      Map uploadResult = cloudinaryService.uploadFile(file);
                      String imageUrl = (String) uploadResult.get("secure_url"); // Nên dùng secure_url
                      if (imageUrl != null) {
                          imageUrls.add(imageUrl);
                          ProductImage productImage = new ProductImage();
                          productImage.setImageUrl(imageUrl);
                          productImage.setIsThumbnail(isFirstImage); // Dùng setIsThumbnail
                          product.addImage(productImage); // Dùng helper method để thêm vào Set và set product
                          isFirstImage = false;
                      }
                  }
              }
          }
          // Nếu không có ảnh nào được upload, collection product.getImages() sẽ rỗng
          return imageUrls;
      }

      // Hàm kiểm tra xem list file có rỗng hoàn toàn không
      private boolean allFilesAreEmpty(List<MultipartFile> files) {
          if (files == null || files.isEmpty()) return true;
          for (MultipartFile file : files) {
              if (file != null && !file.isEmpty()) {
                  return false;
              }
          }
          return true;
      }


    // --- Deprecated ---
    @Deprecated @Override public List<ProductHomeDTO> getBestSellingProducts() { return Collections.emptyList();}
    @Deprecated @Override public Page<ProductHomeDTO> getProducts(Long categoryId, Long userId, String sortType, int page, int size) {return Page.empty();}

}

