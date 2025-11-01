package nhom12.AloTra.repository;

import nhom12.AloTra.entity.TemporaryRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TemporaryRegositerRepository extends JpaRepository<TemporaryRegister, Integer> {
    Optional<TemporaryRegister> findByEmail(String email);
    
    @Transactional  // ✅ THÊM DÒNG NÀY
    void deleteByEmail(String email);
    
    @Transactional  // ✅ THÊM DÒNG NÀY
    void deleteByHetHanLucBefore(LocalDateTime dateTime);
}