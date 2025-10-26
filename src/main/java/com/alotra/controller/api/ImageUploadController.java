package com.alotra.controller.api;

import com.alotra.dto.response.ImageUploadResponse;
import com.alotra.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageUploadController - REST API for image upload and management
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    @Autowired
    private CloudinaryService cloudinaryService;

    /**
     * Upload product image
     * POST /api/images/upload/product
     */
    @PostMapping("/upload/product")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }

            // Validate file type
            cloudinaryService.validateImageType(file);

            // Validate file size (max 5MB)
            cloudinaryService.validateFileSize(file, 5);

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadProductImage(file);

            // Build response
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .size(((Number) uploadResult.get("bytes")).longValue())
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .folder("alotra/products")
                    .message("Product image uploaded successfully")
                    .build();

            // Generate thumbnail URL
            response.setThumbnailUrl(cloudinaryService.getThumbnailUrl(response.getPublicId()));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IOException e) {
            logger.error("Upload error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload image: " + e.getMessage()));
        }
    }

    /**
     * Upload user avatar
     * POST /api/images/upload/avatar
     */
    @PostMapping("/upload/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            cloudinaryService.validateImageType(file);
            cloudinaryService.validateFileSize(file, 2); // Max 2MB for avatars

            Map<String, Object> uploadResult = cloudinaryService.uploadAvatar(file);

            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .thumbnailUrl(cloudinaryService.getThumbnailUrl((String) uploadResult.get("public_id")))
                    .message("Avatar uploaded successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Avatar upload error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Upload blog image
     * POST /api/images/upload/blog
     */
    @PostMapping("/upload/blog")
    public ResponseEntity<?> uploadBlogImage(@RequestParam("file") MultipartFile file) {
        try {
            cloudinaryService.validateImageType(file);
            cloudinaryService.validateFileSize(file, 5);

            Map<String, Object> uploadResult = cloudinaryService.uploadBlogImage(file);

            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .message("Blog image uploaded successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Upload promotion banner
     * POST /api/images/upload/promotion
     */
    @PostMapping("/upload/promotion")
    public ResponseEntity<?> uploadPromotionBanner(@RequestParam("file") MultipartFile file) {
        try {
            cloudinaryService.validateImageType(file);
            cloudinaryService.validateFileSize(file, 10); // Max 10MB for banners

            Map<String, Object> uploadResult = cloudinaryService.uploadPromotionBanner(file);

            ImageUploadResponse response = ImageUploadResponse.builder()
                    .imageUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .message("Promotion banner uploaded successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete image by public_id
     * DELETE /api/images/{publicId}
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> deleteImage(@PathVariable String publicId) {
        try {
            // Replace URL-encoded slashes
            String decodedPublicId = publicId.replace("%2F", "/");

            Map<String, Object> deleteResult = cloudinaryService.deleteImage(decodedPublicId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            response.put("publicId", decodedPublicId);
            response.put("result", deleteResult.get("result"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Delete error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete image by URL
     * POST /api/images/delete-by-url
     */
    @PostMapping("/delete-by-url")
    public ResponseEntity<?> deleteImageByUrl(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Image URL is required"));
            }

            Map<String, Object> deleteResult = cloudinaryService.deleteImageByUrl(imageUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            response.put("imageUrl", imageUrl);
            response.put("result", deleteResult.get("result"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Delete by URL error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get optimized image URL
     * GET /api/images/optimize?publicId={publicId}&width={width}&height={height}
     */
    @GetMapping("/optimize")
    public ResponseEntity<?> getOptimizedImageUrl(
            @RequestParam String publicId,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "800") int height) {
        
        try {
            String optimizedUrl = cloudinaryService.getOptimizedImageUrl(publicId, width, height);
            
            if (optimizedUrl == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Failed to generate URL"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("optimizedUrl", optimizedUrl);
            response.put("width", width);
            response.put("height", height);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Test endpoint to check if image upload service is working
     * GET /api/images/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Image upload service is running!");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}