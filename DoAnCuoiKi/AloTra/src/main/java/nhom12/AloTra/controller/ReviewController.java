package nhom12.AloTra.controller;

import nhom12.AloTra.entity.User;
import nhom12.AloTra.entity.Product;
import nhom12.AloTra.entity.Rating;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.repository.RatingRepository;
import nhom12.AloTra.service.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
public class ReviewController {

    private static final long MAX_SIZE = 20L * 1024 * 1024; // 20MB

    @Autowired private RatingRepository ratingRepository;
    @Autowired private UserRepository nguoiDungRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private StorageService storageService;

    @PostMapping("/submit-review")
    public String submitReview(@RequestParam("productId") Integer productId,
                               @RequestParam("rating") Integer ratingScore,
                               @RequestParam("comment") String comment,
                               @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đánh giá.");
            return "redirect:/product/" + productId;
        }

        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            User currentUser = nguoiDungRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

            Rating rating = new Rating();
            rating.setNguoiDung(currentUser);
            rating.setSanPham(product);
            rating.setDiemDanhGia(ratingScore);
            rating.setBinhLuan(comment);
            rating.setNgayTao(LocalDateTime.now());

            // Xử lý 1 file duy nhất: ảnh HOẶC video
            if (mediaFile != null && !mediaFile.isEmpty()) {
                if (mediaFile.getSize() > MAX_SIZE) {
                    redirectAttributes.addFlashAttribute("error", "File vượt quá 20MB. Vui lòng chọn file nhỏ hơn.");
                    return "redirect:/product/" + productId + "#reviews";
                }

                // Lưu file vào thư mục "reviews" (StorageService sẽ tự tạo thư mục nếu chưa có)
                String storedName = storageService.storeFile(mediaFile, "reviews");

                // Xác định loại file để set đúng cột
                String contentType = mediaFile.getContentType();
                if (contentType == null) contentType = "";
                String ctype = contentType.toLowerCase(Locale.ROOT);

                if (ctype.startsWith("image/")) {
                    rating.setImageUrl(storedName);
                    rating.setVideoUrl(null);
                } else if (ctype.equals("video/mp4") || ctype.equals("video/quicktime") || ctype.startsWith("video/")) {
                    rating.setVideoUrl(storedName);
                    rating.setImageUrl(null);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Định dạng file không hợp lệ (chỉ nhận ảnh hoặc video MP4/MOV).");
                    return "redirect:/product/" + productId + "#reviews";
                }
            } else {
                rating.setImageUrl(null);
                rating.setVideoUrl(null);
            }

            ratingRepository.save(rating);
            redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã gửi đánh giá!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi đánh giá: " + e.getMessage());
        }

        return "redirect:/product/" + productId + "#reviews";
    }
    @PostMapping("/update-review")
    public String updateReview(@RequestParam("productId") Integer productId,
                               @RequestParam("rating") Integer ratingScore,
                               @RequestParam("comment") String comment,
                               @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thực hiện.");
            return "redirect:/product/" + productId;
        }

        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = nguoiDungRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            // Tìm bài đánh giá CŨ
            Rating existingRating = ratingRepository
                    .findByNguoiDung_MaNguoiDungAndSanPham_MaSanPham(currentUser.getMaNguoiDung(), productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đánh giá để cập nhật."));

            // Cập nhật thông tin mới
            existingRating.setDiemDanhGia(ratingScore);
            existingRating.setBinhLuan(comment);
            existingRating.setNgayTao(LocalDateTime.now()); // Cập nhật ngày thành ngày sửa

            // Xử lý file media nếu người dùng tải lên file mới
            if (mediaFile != null && !mediaFile.isEmpty()) {
                if (mediaFile.getSize() > MAX_SIZE) {
                    redirectAttributes.addFlashAttribute("error", "File vượt quá 20MB.");
                    return "redirect:/product/" + productId + "#reviews";
                }

                // Xóa file cũ trước khi lưu file mới
                String oldFile = existingRating.getImageUrl() != null ? existingRating.getImageUrl() : existingRating.getVideoUrl();
                if (oldFile != null && !oldFile.isEmpty()) {
                    storageService.deleteFile("reviews/" + oldFile);
                }

                // Lưu file mới
                String storedName = storageService.storeFile(mediaFile, "reviews");
                String contentType = mediaFile.getContentType() != null ? mediaFile.getContentType().toLowerCase(Locale.ROOT) : "";

                if (contentType.startsWith("image/")) {
                    existingRating.setImageUrl(storedName);
                    existingRating.setVideoUrl(null);
                } else if (contentType.startsWith("video/")) {
                    existingRating.setVideoUrl(storedName);
                    existingRating.setImageUrl(null);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Định dạng file không hợp lệ.");
                    return "redirect:/product/" + productId + "#reviews";
                }
            }

            ratingRepository.save(existingRating); // Lưu lại các thay đổi
            redirectAttributes.addFlashAttribute("success", "Cập nhật đánh giá thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật đánh giá: " + e.getMessage());
        }

        return "redirect:/product/" + productId + "#reviews";
    }
}