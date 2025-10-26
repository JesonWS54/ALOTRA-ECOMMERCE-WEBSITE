package com.alotra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Author is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NotBlank(message = "Title is required")
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 300)
    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Size(max = 500)
    @Column(length = 500)
    private String excerpt;

    @NotBlank(message = "Content is required")
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "featured_image", length = 500)
    private String featuredImage; // Cloudinary URL

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, ARCHIVED

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isPublished() {
        return "PUBLISHED".equalsIgnoreCase(this.status);
    }
}