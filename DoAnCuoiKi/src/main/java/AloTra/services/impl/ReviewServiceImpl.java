package AloTra.services.impl;

import AloTra.Model.ReviewDTO;
import AloTra.entity.Account;
import AloTra.entity.Product;
import AloTra.entity.Review;
import AloTra.entity.ReviewMedia;
import AloTra.repository.AccountRepository;
import AloTra.repository.ProductRepository;
import AloTra.repository.ReviewMediaRepository;
import AloTra.repository.ReviewRepository;
import AloTra.services.CloudinaryService;
import AloTra.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet; // Import HashSet
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewMediaRepository reviewMediaRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CloudinaryService cloudinaryService;

    @Override
    public Page<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByProduct_Id(productId, sortedPageable);
        return reviewPage.map(this::convertToDTO);
    }

    @Transactional
    @Override
    public ReviewDTO addReview(Long userId, Long productId, Integer rating, String comment, List<MultipartFile> mediaFiles) throws IOException {

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        Review review = new Review();
        review.setAccount(account);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        // createdAt được set tự động bởi @CreationTimestamp

        Review savedReview = reviewRepository.save(review); // Lưu review trước

        // *** SỬA LỖI: Không dùng setReviewMedia trực tiếp ***
        // List<ReviewMedia> reviewMediaList = new ArrayList<>(); // Không cần List này nữa

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            for (MultipartFile file : mediaFiles) {
                if (!file.isEmpty()) {
                    try {
                        Map uploadResult = cloudinaryService.uploadFile(file);
                        String mediaUrl = (String) uploadResult.get("url");
                        String contentType = file.getContentType();
                        String mediaType = (contentType != null && contentType.startsWith("video")) ? "VIDEO" : "IMAGE";

                        ReviewMedia media = new ReviewMedia();
                        // media.setReview(savedReview); // Không cần set ở đây
                        media.setMediaUrl(mediaUrl);
                        media.setMediaType(mediaType);
                        // *** SỬA LỖI: Dùng helper method để thêm media và tự set quan hệ ***
                        savedReview.addMedia(media); // Dùng helper method
                        // reviewMediaRepository.save(media); // Không cần save riêng lẻ nếu cascade hoạt động

                    } catch (IOException e) {
                        throw new IOException("Lỗi upload file media: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        // Lưu lại review sau khi đã add media (nếu cascade=ALL hoạt động thì không cần thiết, nhưng để chắc chắn)
        // savedReview = reviewRepository.save(savedReview);

        updateProductRating(product);

        return convertToDTO(savedReview);
    }


    private ReviewDTO convertToDTO(Review review) {
        if (review == null) return null;
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setAccountId(review.getAccount() != null ? review.getAccount().getId() : null);
        dto.setAccountUsername(review.getAccount() != null ? review.getAccount().getUsername() : "Ẩn danh");
        dto.setAccountAvatarUrl(review.getAccount() != null ? review.getAccount().getAvatarUrl() : null);
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setOrderItemId(review.getOrderItem() != null ? review.getOrderItem().getId() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        // Lấy danh sách media URLs từ Set
        if (review.getReviewMedia() != null) {
            dto.setMediaUrls(review.getReviewMedia().stream()
                                  .map(ReviewMedia::getMediaUrl)
                                  .collect(Collectors.toList())); // Vẫn trả về List cho DTO
        } else {
            dto.setMediaUrls(new ArrayList<>());
        }

        return dto;
    }

    private void updateProductRating(Product product) {
         if (product == null) return;
         List<Review> reviews = reviewRepository.findByProduct_Id(product.getId()); // Cần method này
         if (reviews.isEmpty()) {
             product.setRating(0.0);
             product.setReviewCount(0);
         } else {
             double totalRating = reviews.stream().mapToInt(Review::getRating).sum();
             // Sửa lỗi chia cho 0
             product.setRating(reviews.size() > 0 ? totalRating / reviews.size() : 0.0);
             product.setReviewCount(reviews.size());
         }
         productRepository.save(product);
    }
}
