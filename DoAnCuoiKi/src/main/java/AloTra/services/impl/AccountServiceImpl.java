package AloTra.services.impl;

import AloTra.Model.AccountDTO;
import AloTra.entity.Account;
import AloTra.repository.AccountRepository;
import AloTra.services.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.security.crypto.password.PasswordEncoder; // Xóa import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    // Xóa PasswordEncoder
    // @Autowired(required = false)
    // private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountById(Long id) {
        return accountRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public AccountDTO updateAccountInfo(Long id, String fullName, String phone) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        account.setFullName(fullName);
        account.setPhone(phone);
        // UpdatedAt được xử lý tự động
        return convertToDTO(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDTO updateAccountAvatar(Long id, String avatarUrl) {
         Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        account.setAvatarUrl(avatarUrl);
        // UpdatedAt được xử lý tự động
        return convertToDTO(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::convertToDTO);
    }

    // --- ADMIN ACTIONS IMPLEMENTATION ---

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDTO> getAccountForAdminEdit(Long id) {
        // Có thể thêm logic kiểm tra quyền Admin ở đây nếu cần
        return accountRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    // **Sửa signature: Bỏ PasswordEncoder**
    public AccountDTO addAccount(AccountDTO accountDTO) {
        if (accountRepository.findByUsername(accountDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại.");
        }
        if (accountRepository.findByEmail(accountDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại.");
        }

        Account account = new Account();
        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());

        // *** LƯU MẬT KHẨU PLAIN TEXT (TẠM THỜI - CẦN SỬA SAU) ***
        if (accountDTO.getPassword() == null || accountDTO.getPassword().isBlank()) {
             throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        account.setPassword(accountDTO.getPassword());
        // **********************************************************

        account.setFullName(accountDTO.getFullName());
        account.setPhone(accountDTO.getPhone());
        try {
            // Chuyển đổi String role từ DTO sang Enum Role cho Entity
            account.setRole(Account.Role.valueOf(accountDTO.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role không hợp lệ: " + accountDTO.getRole());
        } catch (NullPointerException e) {
            throw new RuntimeException("Role không được để trống.");
        }
        account.setIsActive(accountDTO.getIsActive() != null ? accountDTO.getIsActive() : true); // Mặc định active
        account.setIsLocked(false); // Mặc định không khóa

        // createdAt và updatedAt được xử lý tự động bởi @CreationTimestamp/@UpdateTimestamp

        return convertToDTO(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDTO updateAccountAdmin(Long id, AccountDTO accountDTO) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));

        // Kiểm tra trùng username/email (chỉ khi thay đổi)
        if (!account.getUsername().equalsIgnoreCase(accountDTO.getUsername()) &&
            accountRepository.findByUsername(accountDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username mới '" + accountDTO.getUsername() + "' đã tồn tại.");
        }
        if (!account.getEmail().equalsIgnoreCase(accountDTO.getEmail()) &&
            accountRepository.findByEmail(accountDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email mới '" + accountDTO.getEmail() + "' đã tồn tại.");
        }

        account.setUsername(accountDTO.getUsername());
        account.setEmail(accountDTO.getEmail());
        account.setFullName(accountDTO.getFullName());
        account.setPhone(accountDTO.getPhone());
        try {
            account.setRole(Account.Role.valueOf(accountDTO.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role không hợp lệ: " + accountDTO.getRole());
        } catch (NullPointerException e) {
             throw new RuntimeException("Role không được để trống.");
        }
        // Chỉ cập nhật isActive/isLocked nếu giá trị được cung cấp trong DTO
        if (accountDTO.getIsActive() != null) {
            account.setIsActive(accountDTO.getIsActive());
        }
        if (accountDTO.getIsLocked() != null) {
            // Không cho phép khóa tài khoản ADMIN
            if (account.getRole() == Account.Role.ADMIN && accountDTO.getIsLocked()) {
                 throw new RuntimeException("Không thể khóa tài khoản Admin.");
            }
            account.setIsLocked(accountDTO.getIsLocked());
        }
        // updatedAt được xử lý tự động

        return convertToDTO(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void lockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        if (account.getRole() == Account.Role.ADMIN) {
             throw new RuntimeException("Không thể khóa tài khoản Admin.");
        }
        account.setIsLocked(true);
        accountRepository.save(account); // updatedAt sẽ tự cập nhật
    }

    @Override
    @Transactional
    public void unlockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        account.setIsLocked(false);
        accountRepository.save(account); // updatedAt sẽ tự cập nhật
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        if (account.getRole() == Account.Role.ADMIN) {
             throw new RuntimeException("Không thể xóa tài khoản Admin.");
        }
        // TODO: Cân nhắc xử lý các quan hệ (vd: xóa shop, hủy đơn hàng?) trước khi xóa account
        accountRepository.delete(account);
    }

    // --- Helper Method ---
    private AccountDTO convertToDTO(Account account) {
        if (account == null) return null;
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getEmail());
        // Không set password vào DTO
        dto.setFullName(account.getFullName());
        dto.setPhone(account.getPhone());
        dto.setAvatarUrl(account.getAvatarUrl());
        dto.setRole(account.getRole() != null ? account.getRole().name() : null);
        dto.setIsActive(account.getIsActive());
        dto.setIsLocked(account.getIsLocked());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}

