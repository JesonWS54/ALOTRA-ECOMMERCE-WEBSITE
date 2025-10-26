package com.alotra.repository;

import com.alotra.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT i FROM Ingredient i ORDER BY i.name ASC")
    List<Ingredient> findAllOrderByName();
    
    @Query("SELECT i FROM Ingredient i JOIN i.products p WHERE p.id = :productId")
    List<Ingredient> findByProductId(Long productId);
}