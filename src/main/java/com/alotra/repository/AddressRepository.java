package com.alotra.repository;

import com.alotra.entity.Address;
import com.alotra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByUserIdAndIsDefault(Long userId, Boolean isDefault);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetDefaultByUserId(@Param("userId") Long userId);
    
    long countByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}