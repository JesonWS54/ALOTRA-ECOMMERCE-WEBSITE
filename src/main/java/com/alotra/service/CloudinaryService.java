package com.alotra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * CloudinaryService - Handles image upload and management with Cloudinary
 * Fixed for Cloudinary 1.38.0 API
 */
@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * 
     * @param file MultipartFile from request
     * @param folder Folder name in Cloudinary (e.g., "products", "avatars")
     * @return Map containing upload result (url, public_id, etc.)
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Upload to Cloudinary
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "public_id", uniqueFilename.replace(fileExtension, ""),
                "resource_type", "image",
                "overwrite", false
        );

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            logger.info("Image uploaded successfully to Cloudinary: {}", uploadResult.get("secure_url"));
            
            return uploadResult;
        } catch (IOException e) {
            logger.error("Failed to upload image to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Upload product image to Cloudinary
     * 
     * @param file MultipartFile from request
     * @return Map containing upload result
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadProductImage(MultipartFile file) throws IOException {
        return uploadImage(file, "alotra/products");
    }

    /**
     * Upload user avatar to Cloudinary
     * 
     * @param file MultipartFile from request
     * @return Map containing upload result
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadAvatar(MultipartFile file) throws IOException {
        return uploadImage(file, "alotra/avatars");
    }

    /**
     * Upload blog image to Cloudinary
     * 
     * @param file MultipartFile from request
     * @return Map containing upload result
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadBlogImage(MultipartFile file) throws IOException {
        return uploadImage(file, "alotra/blogs");
    }

    /**
     * Upload promotion banner to Cloudinary
     * 
     * @param file MultipartFile from request
     * @return Map containing upload result
     * @throws IOException if upload fails
     */
    public Map<String, Object> uploadPromotionBanner(MultipartFile file) throws IOException {
        return uploadImage(file, "alotra/promotions");
    }

    /**
     * Delete image from Cloudinary by public_id
     * 
     * @param publicId Public ID of the image in Cloudinary
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Public ID cannot be empty");
        }

        try {
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            logger.info("Image deleted successfully from Cloudinary: {}", publicId);
            
            return deleteResult;
        } catch (IOException e) {
            logger.error("Failed to delete image from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    /**
     * Delete image by URL
     * Extracts public_id from URL and deletes the image
     * 
     * @param imageUrl Full Cloudinary URL
     * @return Map containing deletion result
     * @throws IOException if deletion fails
     */
    public Map<String, Object> deleteImageByUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }

        // Extract public_id from URL
        String publicId = extractPublicIdFromUrl(imageUrl);
        
        return deleteImage(publicId);
    }

    /**
     * Extract public_id from Cloudinary URL
     * 
     * @param imageUrl Full Cloudinary URL
     * @return public_id
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        // Example URL: https://res.cloudinary.com/demo/image/upload/v1234567890/alotra/products/abc123.jpg
        // Public ID: alotra/products/abc123
        
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid Cloudinary URL format");
            }
            
            String afterUpload = parts[1];
            // Remove version number (v1234567890)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove file extension
            String publicId = withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
            
            return publicId;
        } catch (Exception e) {
            logger.error("Failed to extract public_id from URL: {}", imageUrl);
            throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl);
        }
    }

    /**
     * Get optimized image URL with transformations
     * FIXED for Cloudinary 1.38.0
     * 
     * @param publicId Public ID of the image
     * @param width Desired width
     * @param height Desired height
     * @return Transformed image URL
     */
    public String getOptimizedImageUrl(String publicId, int width, int height) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return null;
        }

        try {
            // Create Transformation object
            Transformation transformation = new Transformation()
                    .width(width)
                    .height(height)
                    .crop("fill")
                    .quality("auto")
                    .fetchFormat("auto");

            // Generate URL with transformation
            return cloudinary.url()
                    .transformation(transformation)
                    .generate(publicId);
        } catch (Exception e) {
            logger.error("Failed to generate optimized URL for public_id: {}", publicId);
            return null;
        }
    }

    /**
     * Get thumbnail URL for product image
     * 
     * @param publicId Public ID of the image
     * @return Thumbnail URL (400x400)
     */
    public String getThumbnailUrl(String publicId) {
        return getOptimizedImageUrl(publicId, 400, 400);
    }

    /**
     * Get image URL with custom transformation
     * 
     * @param publicId Public ID of the image
     * @param transformation Custom transformation
     * @return Transformed image URL
     */
    public String getImageUrlWithTransformation(String publicId, Transformation transformation) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return null;
        }

        try {
            return cloudinary.url()
                    .transformation(transformation)
                    .generate(publicId);
        } catch (Exception e) {
            logger.error("Failed to generate URL with transformation for public_id: {}", publicId);
            return null;
        }
    }

    /**
     * Get responsive image URLs for different sizes
     * 
     * @param publicId Public ID of the image
     * @return Map of size name to URL
     */
    public Map<String, String> getResponsiveImageUrls(String publicId) {
        return Map.of(
                "thumbnail", getOptimizedImageUrl(publicId, 200, 200),
                "small", getOptimizedImageUrl(publicId, 400, 400),
                "medium", getOptimizedImageUrl(publicId, 800, 800),
                "large", getOptimizedImageUrl(publicId, 1200, 1200)
        );
    }

    /**
     * Validate file size
     * 
     * @param file MultipartFile to validate
     * @param maxSizeMB Maximum size in MB
     * @throws IllegalArgumentException if file is too large
     */
    public void validateFileSize(MultipartFile file, int maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum limit of %d MB", maxSizeMB)
            );
        }
    }

    /**
     * Validate image file type
     * 
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if file is not an image
     */
    public void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image (jpg, png, gif, etc.)");
        }

        // Check allowed image types
        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException(
                    "Only JPEG, PNG, GIF, and WebP images are allowed"
            );
        }
    }
}