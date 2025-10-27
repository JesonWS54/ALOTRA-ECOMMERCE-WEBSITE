package com.alotra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Đây là trường để liên kết ngược lại với Review
    // Một hình ảnh chỉ thuộc về một Review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // Đường dẫn của hình ảnh được lưu trên Cloudinary hoặc server
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}