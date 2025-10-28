package AloTra.services.impl;

import AloTra.Model.ProductHomeDTO;
import AloTra.entity.Account;
import AloTra.entity.Product;
import AloTra.entity.ProductFavorite;
import AloTra.repository.AccountRepository;
import AloTra.repository.ProductFavoriteRepository;
import AloTra.repository.ProductRepository;
import AloTra.services.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // Cần import
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Cần import

import java.time.LocalDateTime; // Cần import
import java.util.List; // Cần import
import java.util.Optional; // Cần import Optional
import java.util.stream.Collectors; // Cần import

@Service
public class ProductFavoriteServiceImpl implements ProductFavoriteService {

    @Autowired
    private ProductFavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository; // Để lấy Product

    @Autowired
    private AccountRepository accountRepository; // Để lấy Account

    @Override
    public Page<ProductHomeDTO> findUserFavorites(Long userId, Pageable pageable) {
        Page<ProductFavorite> favoritePage = favoriteRepository.findUserFavoritesWithDetails(userId, pageable);
        List<ProductHomeDTO> dtoList = favoritePage.getContent().stream()
                .map(pf -> convertToProductHomeDTO(pf.getProduct())) // Chuyển đổi Product sang DTO
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, favoritePage.getTotalElements());
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        if (isFavorite(userId, productId)) {
            // Có thể bỏ qua hoặc ném lỗi tùy logic
            System.out.println("Sản phẩm đã có trong danh sách yêu thích.");
            return; // Không làm gì nếu đã tồn tại
            // Hoặc: throw new RuntimeException("Sản phẩm đã có trong danh sách yêu thích.");
        }
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        ProductFavorite favorite = new ProductFavorite();
        favorite.setAccount(account);
        favorite.setProduct(product);
        // SỬA LỖI Ở ĐÂY: Đổi setCreatedAt thành setAddedAt
        favorite.setAddedAt(LocalDateTime.now());
        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        // Sử dụng phương thức mới sẽ định nghĩa trong Repository
        ProductFavorite favorite = favoriteRepository.findByAccount_IdAndProduct_Id(userId, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong danh sách yêu thích để xóa."));
        favoriteRepository.delete(favorite);
    }

     @Override
     public boolean isFavorite(Long userId, Long productId) {
          // Sử dụng phương thức mới sẽ định nghĩa trong Repository
          return favoriteRepository.findByAccount_IdAndProduct_Id(userId, productId).isPresent();
     }


    // Hàm chuyển đổi Product sang ProductHomeDTO (có thể dùng lại từ ProductServiceImpl)
    private ProductHomeDTO convertToProductHomeDTO(Product product) {
         if (product == null) return null; // Thêm kiểm tra null
         // Cần lấy ảnh thumbnail (giả định có hàm hoặc query riêng)
         // Tạm thời dùng placeholder
        String thumbnailUrl = product.getShop() != null && product.getShop().getShopName() != null // Lấy ảnh đầu tiên nếu có - Tạm thời chưa lấy ảnh từ DB
            ? "https://placehold.co/400x300/F0F0F0/555" // Ảnh placeholder
            : "https://placehold.co/400x300/F0F0F0/555?text=No+Image"; // Ảnh mặc định

        return new ProductHomeDTO(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getShop() != null ? product.getShop().getShopName() : "N/A", // Kiểm tra null cho shop
                thumbnailUrl, // Ảnh thumbnail
                product.getSoldCount(), // Số lượng bán
                null // favoriteCount không cần ở đây
        );
    }
}