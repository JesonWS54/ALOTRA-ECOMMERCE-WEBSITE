package AloTra.repository;

import AloTra.entity.Account;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findByEmail(String email);

    // *** THÊM: Lấy tất cả tài khoản với phân trang (sắp xếp sẽ do Pageable quyết định) ***
    Page<Account> findAll(Pageable pageable);

    // TODO: Sau này có thể thêm:
    // Page<Account> findByRole(Account.Role role, Pageable pageable); // Lọc theo Role
    // Page<Account> findByUsernameContainingIgnoreCase(String username, Pageable pageable); // Tìm kiếm
}
