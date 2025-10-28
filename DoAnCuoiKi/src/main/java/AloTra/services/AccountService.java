package AloTra.services;

import AloTra.Model.AccountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.security.crypto.password.PasswordEncoder; // Tạm thời xóa import này

import java.util.Optional;

public interface AccountService {

    Optional<AccountDTO> getAccountById(Long id);

    AccountDTO updateAccountInfo(Long id, String fullName, String phone);

    AccountDTO updateAccountAvatar(Long id, String avatarUrl);

    Page<AccountDTO> getAllAccounts(Pageable pageable);

    // --- ADMIN ACTIONS ---

    /**
     * Lấy thông tin tài khoản để Admin chỉnh sửa (có thể khác getAccountById).
     * @param id ID tài khoản.
     * @return Optional<AccountDTO>.
     */
    Optional<AccountDTO> getAccountForAdminEdit(Long id);

    /**
     * Admin thêm tài khoản mới.
     * @param accountDTO Dữ liệu tài khoản mới (username, email, password, role...).
     * // Mật khẩu sẽ được lưu trữ dưới dạng plain text cho đến khi PasswordEncoder được tích hợp.
     * @return AccountDTO đã được tạo.
     * @throws RuntimeException Nếu username/email đã tồn tại.
     */
    // Tạm thời xóa PasswordEncoder khỏi signature
    AccountDTO addAccount(AccountDTO accountDTO /*, PasswordEncoder passwordEncoder*/);

    /**
     * Admin cập nhật thông tin tài khoản (có thể đổi Role, Active, Locked).
     * @param id ID tài khoản.
     * @param accountDTO Dữ liệu cập nhật.
     * @return AccountDTO đã được cập nhật.
     * @throws RuntimeException Nếu không tìm thấy tài khoản hoặc lỗi khác.
     */
    AccountDTO updateAccountAdmin(Long id, AccountDTO accountDTO);

    /**
     * Khóa tài khoản.
     * @param id ID tài khoản.
     */
    void lockAccount(Long id);

    /**
     * Mở khóa tài khoản.
     * @param id ID tài khoản.
     */
    void unlockAccount(Long id);

    /**
     * Xóa tài khoản (Cân nhắc soft delete).
     * @param id ID tài khoản.
     */
    void deleteAccount(Long id);

}

