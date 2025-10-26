package com.alotra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ImageUploadResponse DTO - Response after uploading image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {

    private String imageUrl;
    private String publicId;
    private String thumbnailUrl;
    private String format;
    private Long size;
    private Integer width;
    private Integer height;
    private String folder;
    private String message;
}