package AloTra.services.impl;

import AloTra.Model.ProductHomeDTO;
import AloTra.entity.Account;
import AloTra.entity.Product;
import AloTra.entity.ProductView;
import AloTra.repository.AccountRepository;
import AloTra.repository.ProductRepository;
import AloTra.repository.ProductViewRepository;
import AloTra.services.ProductViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // Cần import
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Cần import

import java.time.LocalDateTime; // Cần import
import java.util.List;          // Cần import
import java.util.Optional;     // Cần import
import java.util.stream.Collectors; // Cần import


@Service
public class ProductViewServiceImpl implements ProductViewService {

    @Autowired
    private ProductViewRepository viewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Page<ProductHomeDTO> findUserViewedProducts(Long userId, Pageable pageable) {
        Page<ProductView> viewPage = viewRepository.findUserViewedWithDetails(userId, pageable);
        List<ProductHomeDTO> dtoList = viewPage.getContent().stream()
                .map(pv -> convertToProductHomeDTO(pv.getProduct()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, viewPage.getTotalElements());
    }

    @Override
    @Transactional
    public void recordProductView(Long userId, Long productId) {
        Optional<ProductView> existingView = viewRepository.findByAccount_IdAndProduct_Id(userId, productId);

        if (existingView.isPresent()) {
            // Đã xem -> Cập nhật thời gian
            ProductView view = existingView.get();
            view.setViewedAt(LocalDateTime.now());
            viewRepository.save(view);
        } else {
            // Chưa xem -> Tạo mới
            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

            ProductView newView = new ProductView();
            newView.setAccount(account);
            newView.setProduct(product);
            newView.setViewedAt(LocalDateTime.now());
            viewRepository.save(newView);
        }
    }

     // Hàm chuyển đổi Product sang ProductHomeDTO (copy từ ProductFavoriteServiceImpl)
    private ProductHomeDTO convertToProductHomeDTO(Product product) {
         if (product == null) return null;
        // Cần lấy ảnh thumbnail (giả định)
        String thumbnailUrl = product.getShop() != null && !product.getShop().getShopName().isEmpty()
            ? "https://res.cloudinary.com/" + "dzurcxze0" + "/image/upload/v1729994848/product_images/placeholder.png" // Thay cloud_name
            : "https://placehold.co/400x300/F0F0F0/555?text=No+Image";

        return new ProductHomeDTO(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getShop() != null ? product.getShop().getShopName() : "N/A",
                thumbnailUrl,
                product.getSoldCount(),
                null // favoriteCount không cần
        );
    }
}