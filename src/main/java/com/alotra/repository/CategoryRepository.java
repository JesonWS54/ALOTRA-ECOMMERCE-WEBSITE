package com.alotra.repository;

import com.alotra.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);
    
    Optional<Category> findByName(String name);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
    
    List<Category> findByIsActive(Boolean isActive);
    
    List<Category> findByIsActiveOrderByDisplayOrderAsc(Boolean isActive);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveCategories();
    
    @Query("SELECT c FROM Category c LEFT JOIN c.products p WHERE c.isActive = true GROUP BY c ORDER BY COUNT(p) DESC")
    List<Category> findCategoriesByProductCount();
    
    long countByIsActive(Boolean isActive);
}