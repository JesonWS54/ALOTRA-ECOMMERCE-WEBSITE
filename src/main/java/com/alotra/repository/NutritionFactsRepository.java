package com.alotra.repository;

import com.alotra.entity.NutritionFacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionFactsRepository extends JpaRepository<NutritionFacts, Long> {

    Optional<NutritionFacts> findByProductId(Long productId);
    
    boolean existsByProductId(Long productId);
    
    void deleteByProductId(Long productId);
}