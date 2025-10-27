package com.alotra.service;

import com.alotra.entity.User;
import com.alotra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserService - User Management - FIXED VERSION
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Tạo user mới
     */
    public User createUser(User user) {
        // Validate
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Encode password
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        // Set defaults
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Lấy user theo ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Lấy user theo username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Lấy user theo email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Lấy tất cả users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy users với phân trang
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Lấy users theo role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * Lấy users theo role với phân trang
     */
    public Page<User> getUsersByRole(String role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    /**
     * Lấy active users
     */
    public List<User> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    /**
     * Lấy active users với phân trang
     */
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findByIsActive(true, pageable);
    }

    /**
     * Cập nhật user
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));

        // Update basic fields (không update password ở đây)
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            // Check if new email exists
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }

        if (userDetails.getAvatarUrl() != null) {
            user.setAvatarUrl(userDetails.getAvatarUrl());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Xóa user
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy user với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    // ==================== PASSWORD MANAGEMENT ====================

    /**
     * Đổi mật khẩu
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);

        System.out.println("✅ Đã đổi mật khẩu cho user: " + user.getUsername());
    }

    /**
     * Reset mật khẩu (admin only)
     */
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);

        System.out.println("✅ Đã reset mật khẩu cho user: " + user.getUsername());
    }

    // ==================== PROFILE MANAGEMENT ====================

    /**
     * Cập nhật profile
     */
    public User updateProfile(Long userId, String fullName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Cập nhật avatar
     */
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Cập nhật email
     */
    public User updateEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Check if email exists
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(newEmail);
        user.setEmailVerified(false); // Require re-verification
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Cập nhật phone
     */
    public User updatePhone(Long userId, String newPhone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setPhone(newPhone);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    // ==================== ACCOUNT STATUS ====================

    /**
     * Kích hoạt user
     */
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Vô hiệu hóa user
     */
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Toggle active status
     */
    public User toggleActiveStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Verify email
     */
    public User verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    // ==================== ROLE MANAGEMENT ====================

    /**
     * Cập nhật role
     */
    public User updateRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        // Validate role
        if (!isValidRole(newRole)) {
            throw new RuntimeException("Role không hợp lệ: " + newRole);
        }

        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Promote to admin
     */
    public User promoteToAdmin(Long userId) {
        return updateRole(userId, "ADMIN");
    }

    /**
     * Demote to user
     */
    public User demoteToUser(Long userId) {
        return updateRole(userId, "USER");
    }

    // ==================== SEARCH ====================

    /**
     * Tìm kiếm users theo keyword
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable);
    }

    // ==================== STATISTICS ====================

    /**
     * Đếm tổng số users
     */
    public long countAllUsers() {
        return userRepository.count();
    }

    /**
     * Đếm users theo role
     */
    public long countUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    /**
     * Đếm active users
     */
    public long countActiveUsers() {
        return userRepository.countByIsActive(true);
    }

    /**
     * Đếm users đã verify email
     */
    public long countVerifiedUsers() {
        return userRepository.countByEmailVerified(true);
    }

    // ==================== VALIDATION ====================

    /**
     * Kiểm tra username tồn tại
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Kiểm tra email tồn tại
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Kiểm tra user tồn tại
     */
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Validate role
     */
    private boolean isValidRole(String role) {
        return role != null && (role.equals("USER") || role.equals("ADMIN") || role.equals("MODERATOR"));
    }

    // ==================== LAST LOGIN ====================

    /**
     * Cập nhật last login
     */
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Cập nhật last login by username
     */
    public void updateLastLoginByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với username: " + username));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}