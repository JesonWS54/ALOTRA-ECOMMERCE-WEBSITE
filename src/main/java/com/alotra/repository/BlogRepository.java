package com.alotra.repository;

import com.alotra.entity.Blog;
import com.alotra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    List<Blog> findByAuthor(User author);
    
    Page<Blog> findByAuthor(User author, Pageable pageable);
    
    List<Blog> findByAuthorId(Long authorId);
    
    List<Blog> findByStatus(String status);
    
    Page<Blog> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY b.publishedAt DESC")
    Page<Blog> findPublishedBlogs(Pageable pageable);
    
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublishedBlogs(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY b.viewCount DESC")
    List<Blog> findPopularBlogs(Pageable pageable);
    
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY b.publishedAt DESC")
    List<Blog> findLatestBlogs(Pageable pageable);
    
    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    void incrementViewCount(@Param("blogId") Long blogId);
    
    long countByStatus(String status);
}