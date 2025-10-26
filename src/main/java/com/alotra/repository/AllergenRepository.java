package com.alotra.repository;

import com.alotra.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergenRepository extends JpaRepository<Allergen, Long> {

    Optional<Allergen> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT a FROM Allergen a ORDER BY a.name ASC")
    List<Allergen> findAllOrderByName();
    
    @Query("SELECT a FROM Allergen a JOIN a.products p WHERE p.id = :productId")
    List<Allergen> findByProductId(Long productId);
}