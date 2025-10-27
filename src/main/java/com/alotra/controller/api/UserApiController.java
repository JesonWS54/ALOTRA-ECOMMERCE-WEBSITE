package com.alotra.controller.api;

import com.alotra.entity.User;
import com.alotra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UserApiController - REST API for User Management
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserApiController {

    @Autowired
    private UserService userService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * GET /api/users
     * Lấy tất cả users với phân trang
     */
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id}
     * Lấy user theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy user với ID: " + id));
    }

    /**
     * GET /api/users/username/{username}
     * Lấy user theo username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy user: " + username));
    }

    /**
     * GET /api/users/email/{email}
     * Lấy user theo email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy user với email: " + email));
    }

    /**
     * POST /api/users
     * Tạo user mới
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id}
     * Cập nhật user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE /api/users/{id}
     * Xóa user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa user thành công");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== PASSWORD MANAGEMENT ====================

    /**
     * POST /api/users/{id}/change-password
     * Đổi mật khẩu
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        try {
            String oldPassword = passwords.get("oldPassword");
            String newPassword = passwords.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Old password và new password là bắt buộc"));
            }
            
            userService.changePassword(id, oldPassword, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã đổi mật khẩu thành công");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/reset-password
     * Reset mật khẩu (admin only)
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            
            if (newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("New password là bắt buộc"));
            }
            
            userService.resetPassword(id, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã reset mật khẩu thành công");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== PROFILE MANAGEMENT ====================

    /**
     * PUT /api/users/{id}/profile
     * Cập nhật profile
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> profileData) {
        try {
            String fullName = profileData.get("fullName");
            String phone = profileData.get("phone");
            
            User updatedUser = userService.updateProfile(id, fullName, phone);
            
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id}/avatar
     * Cập nhật avatar
     */
    @PutMapping("/{id}/avatar")
    public ResponseEntity<?> updateAvatar(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String avatarUrl = request.get("avatarUrl");
            
            if (avatarUrl == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Avatar URL là bắt buộc"));
            }
            
            User updatedUser = userService.updateAvatar(id, avatarUrl);
            
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id}/email
     * Cập nhật email
     */
    @PutMapping("/{id}/email")
    public ResponseEntity<?> updateEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newEmail = request.get("email");
            
            if (newEmail == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email là bắt buộc"));
            }
            
            User updatedUser = userService.updateEmail(id, newEmail);
            
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id}/phone
     * Cập nhật phone
     */
    @PutMapping("/{id}/phone")
    public ResponseEntity<?> updatePhone(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            
            User updatedUser = userService.updatePhone(id, phone);
            
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== ACCOUNT STATUS ====================

    /**
     * POST /api/users/{id}/activate
     * Kích hoạt user
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User user = userService.activateUser(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/deactivate
     * Vô hiệu hóa user
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            User user = userService.deactivateUser(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/toggle-active
     * Toggle active status
     */
    @PostMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleActiveStatus(@PathVariable Long id) {
        try {
            User user = userService.toggleActiveStatus(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/verify-email
     * Verify email
     */
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable Long id) {
        try {
            User user = userService.verifyEmail(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email đã được xác thực");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== ROLE MANAGEMENT ====================

    /**
     * PUT /api/users/{id}/role
     * Cập nhật role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            
            if (role == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Role là bắt buộc"));
            }
            
            User user = userService.updateRole(id, role);
            
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/promote-admin
     * Promote to admin
     */
    @PostMapping("/{id}/promote-admin")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long id) {
        try {
            User user = userService.promoteToAdmin(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/users/{id}/demote-user
     * Demote to user
     */
    @PostMapping("/{id}/demote-user")
    public ResponseEntity<?> demoteToUser(@PathVariable Long id) {
        try {
            User user = userService.demoteToUser(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== FILTERS & SEARCH ====================

    /**
     * GET /api/users/role/{role}
     * Lấy users theo role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<User>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userService.getUsersByRole(role, pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/active
     * Lấy active users
     */
    @GetMapping("/active")
    public ResponseEntity<Page<User>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userService.getActiveUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/search
     * Tìm kiếm users
     */
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userService.searchUsers(keyword, pageable);
        
        return ResponseEntity.ok(users);
    }

    // ==================== STATISTICS ====================

    /**
     * GET /api/users/count
     * Đếm số lượng users
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countUsers() {
        long total = userService.countAllUsers();
        long active = userService.countActiveUsers();
        long admins = userService.countUsersByRole("ADMIN");
        long verified = userService.countVerifiedUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", total);
        response.put("activeUsers", active);
        response.put("adminUsers", admins);
        response.put("verifiedUsers", verified);
        response.put("inactiveUsers", total - active);
        
        return ResponseEntity.ok(response);
    }

    // ==================== VALIDATION ====================

    /**
     * GET /api/users/check-username/{username}
     * Kiểm tra username tồn tại
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/check-email/{email}
     * Kiểm tra email tồn tại
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    // ==================== TEST ENDPOINT ====================

    /**
     * GET /api/users/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "User API is running!");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}