package AloTra.services;

import AloTra.Model.ShopDTO;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException; // Import IOException
import java.util.Optional;

public interface ShopService {

    /**
     * Lấy thông tin shop dựa trên ID của user (vendor).
     * @param userId ID của user.
     * @return Optional chứa ShopDTO nếu tìm thấy.
     */
    Optional<ShopDTO> getShopByUserId(Long userId);

    /**
     * Đăng ký shop mới cho user.
     * @param userId ID của user đăng ký.
     * @param shopDTO Thông tin shop cần đăng ký.
     * @param logoFile File logo (có thể null).
     * @param bannerFile File banner (có thể null).
     * @return ShopDTO đã được tạo.
     * @throws IOException Nếu có lỗi upload ảnh.
     * @throws RuntimeException Nếu user đã có shop hoặc lỗi khác.
     */
    ShopDTO registerShop(Long userId, ShopDTO shopDTO, MultipartFile logoFile, MultipartFile bannerFile) throws IOException;

     /**
      * Cập nhật thông tin cơ bản của shop (tên, mô tả, địa chỉ).
      * @param shopId ID của shop cần cập nhật.
      * @param userId ID của user sở hữu shop (để kiểm tra quyền).
      * @param shopDTO Dữ liệu mới cần cập nhật.
      * @return ShopDTO đã được cập nhật.
      * @throws RuntimeException Nếu không tìm thấy shop hoặc không có quyền.
      */
     ShopDTO updateShopInfo(Long shopId, Long userId, ShopDTO shopDTO); // *** THÊM PHƯƠNG THỨC NÀY ***

     /**
      * Cập nhật logo cho shop.
      * @param shopId ID của shop.
      * @param userId ID của user sở hữu.
      * @param logoFile File logo mới.
      * @return URL của logo mới đã upload.
      * @throws IOException Nếu có lỗi upload.
      * @throws RuntimeException Nếu không tìm thấy shop hoặc không có quyền.
      */
     String updateShopLogo(Long shopId, Long userId, MultipartFile logoFile) throws IOException; // *** THÊM PHƯƠNG THỨC NÀY ***

     /**
      * Cập nhật banner cho shop.
      * @param shopId ID của shop.
      * @param userId ID của user sở hữu.
      * @param bannerFile File banner mới.
      * @return URL của banner mới đã upload.
      * @throws IOException Nếu có lỗi upload.
      * @throws RuntimeException Nếu không tìm thấy shop hoặc không có quyền.
      */
     String updateShopBanner(Long shopId, Long userId, MultipartFile bannerFile) throws IOException; // *** THÊM PHƯƠNG THỨC NÀY ***

    // Có thể thêm các hàm khác: getShopById, adminApproveShop...
}