package AloTra.services.impl;

import AloTra.Model.AccountDTO; // Import AccountDTO nếu cần
import AloTra.Model.ShopDTO;
import AloTra.entity.Account;
import AloTra.entity.Shop;
import AloTra.repository.AccountRepository; // Cần AccountRepository
import AloTra.repository.ShopRepository;
import AloTra.services.CloudinaryService; // Cần CloudinaryService
import AloTra.services.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException; // Import IOException
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.Map;
import java.util.Optional;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private AccountRepository accountRepository; // Inject AccountRepository

    @Autowired
    private CloudinaryService cloudinaryService; // Inject CloudinaryService

    @Override
    public Optional<ShopDTO> getShopByUserId(Long userId) {
        return shopRepository.findByAccount_Id(userId).map(this::convertToDTO);
    }

    @Override
    @Transactional // Đảm bảo tất cả lưu hoặc không lưu gì cả
    public ShopDTO registerShop(Long userId, ShopDTO shopDTO, MultipartFile logoFile, MultipartFile bannerFile) throws IOException {
        // 1. Kiểm tra xem user đã có shop chưa
        if (shopRepository.existsByAccount_Id(userId)) {
            throw new RuntimeException("Mỗi tài khoản chỉ được đăng ký một cửa hàng.");
        }

        // 2. Lấy thông tin Account (User)
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản để đăng ký shop."));

        // 3. Upload ảnh (nếu có)
        String logoUrl = null;
        if (logoFile != null && !logoFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(logoFile);
            logoUrl = (String) uploadResult.get("url");
        }

        String bannerUrl = null;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            Map uploadResult = cloudinaryService.uploadFile(bannerFile);
            bannerUrl = (String) uploadResult.get("url");
        }

        // 4. Tạo đối tượng Shop entity
        Shop shop = new Shop();
        shop.setAccount(account);
        shop.setShopName(shopDTO.getShopName());
        shop.setDescription(shopDTO.getDescription());
        shop.setAddress(shopDTO.getAddress());
        shop.setLogoUrl(logoUrl);
        shop.setBannerUrl(bannerUrl);
        shop.setStatus("PENDING"); // Trạng thái chờ duyệt
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());

        // 5. Lưu vào CSDL
        Shop savedShop = shopRepository.save(shop);

        // 6. Chuyển đổi sang DTO để trả về
        return convertToDTO(savedShop);
    }

    @Override
    @Transactional
    public ShopDTO updateShopInfo(Long shopId, Long userId, ShopDTO shopDTO) {
         // *** THÊM TRIỂN KHAI NÀY ***
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng."));

        // Kiểm tra quyền sở hữu
        if (!shop.getAccount().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa cửa hàng này.");
        }

        // Cập nhật các trường cho phép thay đổi
        shop.setShopName(shopDTO.getShopName());
        shop.setDescription(shopDTO.getDescription());
        shop.setAddress(shopDTO.getAddress());
        shop.setUpdatedAt(LocalDateTime.now());

        Shop updatedShop = shopRepository.save(shop);
        return convertToDTO(updatedShop);
    }

    @Override
    @Transactional
    public String updateShopLogo(Long shopId, Long userId, MultipartFile logoFile) throws IOException {
         // *** THÊM TRIỂN KHAI NÀY ***
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng."));

        if (!shop.getAccount().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa cửa hàng này.");
        }

        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file logo.");
        }

        // Upload logo mới
        Map uploadResult = cloudinaryService.uploadFile(logoFile);
        String newLogoUrl = (String) uploadResult.get("url");

        // (Tùy chọn: Xóa logo cũ trên Cloudinary nếu có)
        // if (shop.getLogoUrl() != null && !shop.getLogoUrl().isBlank()) {
        //     try {
        //         String publicId = extractPublicIdFromUrl(shop.getLogoUrl()); // Cần hàm helper
        //         cloudinaryService.deleteFile(publicId);
        //     } catch (Exception e) {
        //         System.err.println("Lỗi xóa logo cũ: " + e.getMessage());
        //     }
        // }

        // Cập nhật URL logo mới và lưu
        shop.setLogoUrl(newLogoUrl);
        shop.setUpdatedAt(LocalDateTime.now());
        shopRepository.save(shop);

        return newLogoUrl;
    }

     @Override
     @Transactional
     public String updateShopBanner(Long shopId, Long userId, MultipartFile bannerFile) throws IOException {
          // *** THÊM TRIỂN KHAI NÀY ***
         Shop shop = shopRepository.findById(shopId)
                 .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng."));

         if (!shop.getAccount().getId().equals(userId)) {
             throw new RuntimeException("Bạn không có quyền chỉnh sửa cửa hàng này.");
         }

         if (bannerFile == null || bannerFile.isEmpty()) {
             throw new IllegalArgumentException("Vui lòng chọn file banner.");
         }

         Map uploadResult = cloudinaryService.uploadFile(bannerFile);
         String newBannerUrl = (String) uploadResult.get("url");

         // (Tùy chọn: Xóa banner cũ)

         shop.setBannerUrl(newBannerUrl);
         shop.setUpdatedAt(LocalDateTime.now());
         shopRepository.save(shop);

         return newBannerUrl;
     }


    // --- Hàm helper chuyển đổi Entity sang DTO ---
    private ShopDTO convertToDTO(Shop shop) {
        if (shop == null) return null;
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        if (shop.getAccount() != null) {
            dto.setAccountId(shop.getAccount().getId());
            dto.setAccountUsername(shop.getAccount().getUsername()); // Giả định Account có getUsername
        }
        dto.setShopName(shop.getShopName());
        dto.setDescription(shop.getDescription());
        dto.setLogoUrl(shop.getLogoUrl());
        dto.setBannerUrl(shop.getBannerUrl());
        dto.setAddress(shop.getAddress());
        dto.setRating(shop.getRating());
        dto.setStatus(shop.getStatus());
        dto.setCreatedAt(shop.getCreatedAt());
        dto.setUpdatedAt(shop.getUpdatedAt());
        return dto;
    }
}

