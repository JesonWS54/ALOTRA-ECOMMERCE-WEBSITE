package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.TemporaryRegister;
import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.entity.Role;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.*;
import nhom17.OneShop.repository.TemporaryRegositerRepository;
import nhom17.OneShop.request.SignUpRequest;
import nhom17.OneShop.request.UserRequest;
import nhom17.OneShop.service.OtpService;
import nhom17.OneShop.service.StorageService;
import nhom17.OneShop.service.UserService;
import nhom17.OneShop.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MembershipTierRepository membershipTierRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TemporaryRegositerRepository dangKyTamThoiRepository;

    @Autowired
    private OtpService otpService;

    @Override
    public Page<User> findAll(String keyword, Integer roleId, Integer tierId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("maNguoiDung").descending());

        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (StringUtils.hasText(keyword)) {
            spec = spec.and(UserSpecification.hasUsername(keyword));
        }
        if (roleId != null) {
            spec = spec.and(UserSpecification.hasRole(roleId));
        }
        if (tierId != null) {
            spec = spec.and(UserSpecification.hasMembershipTier(tierId));
        }
        if (status != null) {
            spec = spec.and(UserSpecification.hasStatus(status));
        }

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    @Override
    @Transactional
    public void save(UserRequest userRequest) {
        validateUniqueFields(userRequest);
        User user = prepareUserEntity(userRequest);
        String oldAvatar = user.getAnhDaiDien();
        mapRequestToEntity(userRequest, user);
        userRepository.save(user);

        if (StringUtils.hasText(userRequest.getAnhDaiDien()) && StringUtils.hasText(oldAvatar) && !oldAvatar.equals(userRequest.getAnhDaiDien())) {
            storageService.deleteFile(oldAvatar);
        }
    }

    private void validateUniqueFields(UserRequest request) {
        if (request.getMaNguoiDung() == null) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new DuplicateRecordException("Email '" + request.getEmail() + "' đã được sử dụng.");
            }
            if (userRepository.existsByTenDangNhapIgnoreCase(request.getTenDangNhap())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + request.getTenDangNhap() + "' đã tồn tại.");
            }
        } else {
            if (userRepository.existsByEmailIgnoreCaseAndMaNguoiDungNot(request.getEmail(), request.getMaNguoiDung())) {
                throw new DuplicateRecordException("Email '" + request.getEmail() + "' đã được người dùng khác sử dụng.");
            }
            if (userRepository.existsByTenDangNhapIgnoreCaseAndMaNguoiDungNot(request.getTenDangNhap(), request.getMaNguoiDung())) {
                throw new DuplicateRecordException("Tên đăng nhập '" + request.getTenDangNhap() + "' đã được người dùng khác sử dụng.");
            }
        }
    }

    private User prepareUserEntity(UserRequest userRequest) {
        if (userRequest.getMaNguoiDung() != null) {
            return findById(userRequest.getMaNguoiDung());
        }
        return new User();
    }

    private void mapRequestToEntity(UserRequest request, User user) {
        user.setHoTen(request.getHoTen());
        user.setEmail(request.getEmail());
        user.setTenDangNhap(request.getTenDangNhap());
        user.setSoDienThoai(request.getSoDienThoai());
        user.setTrangThai(request.getTrangThai());
        if (StringUtils.hasText(request.getAnhDaiDien())) {
            user.setAnhDaiDien(request.getAnhDaiDien());
        }
        if (StringUtils.hasText(request.getMatKhau())) {
            user.setMatKhau(passwordEncoder.encode(request.getMatKhau()));
        }
        Role role = roleRepository.findById(request.getMaVaiTro())
                .orElseThrow(() -> new NotFoundException("Vai trò không hợp lệ với ID: " + request.getMaVaiTro()));
        user.setVaiTro(role);
        if (request.getMaHangThanhVien() != null) {
            MembershipTier tier = membershipTierRepository.findById(request.getMaHangThanhVien())
                    .orElseThrow(() -> new NotFoundException("Hạng thành viên không hợp lệ với ID: " + request.getMaHangThanhVien()));
            user.setHangThanhVien(tier);
        } else {
            user.setHangThanhVien(null);
        }
    }

    @Override
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public void delete(int id) {
        User userToDelete = findById(id);

        if (orderRepository.existsByNguoiDung_MaNguoiDung(id)) {
            userToDelete.setTrangThai(0);
            userRepository.save(userToDelete);
            throw new DataIntegrityViolationException("Không thể xóa người dùng '" + userToDelete.getHoTen() + "' vì đã có lịch sử đặt hàng. Tài khoản đã được chuyển sang trạng thái 'Khóa'.");
        }

        if (StringUtils.hasText(userToDelete.getAnhDaiDien())) {
            storageService.deleteFile(userToDelete.getAnhDaiDien());
        }
        userRepository.delete(userToDelete);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Override
    @Transactional
    public User registerNewUser(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được đăng ký: " + signUpRequest.getEmail());
        }

        if (userRepository.findByTenDangNhap(signUpRequest.getTenDangNhap()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại: " + signUpRequest.getTenDangNhap());
        }

        dangKyTamThoiRepository.deleteByEmail(signUpRequest.getEmail());

        TemporaryRegister dangKyTam = new TemporaryRegister();
        dangKyTam.setEmail(signUpRequest.getEmail());
        dangKyTam.setTenDangNhap(signUpRequest.getTenDangNhap());
        dangKyTam.setMatKhau(passwordEncoder.encode(signUpRequest.getPassword()));
        dangKyTam.setHoTen(signUpRequest.getHoTen());
        dangKyTam.setHetHanLuc(LocalDateTime.now().plusMinutes(30));

        dangKyTamThoiRepository.save(dangKyTam);

        otpService.generateOtpForEmail(signUpRequest.getEmail(), "Đăng ký");

        return null;
    }

    @Override
    @Transactional
    public boolean verifyEmailOtp(String email, String otp) {
        boolean isValid = otpService.validateOtp(email, otp, "Đăng ký");

        if (!isValid) {
            return false;
        }

        TemporaryRegister dangKyTam = dangKyTamThoiRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký cho email này."));

        if (dangKyTam.getHetHanLuc().isBefore(LocalDateTime.now())) {
            dangKyTamThoiRepository.delete(dangKyTam);
            throw new RuntimeException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }

        User newUser = new User();
        newUser.setHoTen(dangKyTam.getHoTen());
        newUser.setEmail(dangKyTam.getEmail());
        newUser.setTenDangNhap(dangKyTam.getTenDangNhap());
        newUser.setMatKhau(dangKyTam.getMatKhau());

        Role userRole = new Role();
        userRole.setMaVaiTro(2);
        newUser.setVaiTro(userRole);

        newUser.setTrangThai(1);
        newUser.setXacThucEmail(true);

        userRepository.save(newUser);

        dangKyTamThoiRepository.delete(dangKyTam);

        return true;
    }

    @Override
    @Transactional
    public void sendResetPasswordOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        otpService.generateOtpForEmail(email, "Quên mật khẩu");
    }

    @Override
    @Transactional
    public boolean verifyResetPasswordOtp(String email, String otp) {
        return otpService.validateOtp(email, otp, "Quên mật khẩu");
    }

    // ✅ PHƯƠNG THỨC QUAN TRỌNG NHẤT - ĐÃ SỬA
    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        System.out.println("========== BẮT ĐẦU RESET PASSWORD ==========");
        System.out.println("📧 Email: " + email);
        System.out.println("🔑 Mật khẩu mới (raw): " + newPassword);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

            System.out.println("✅ Tìm thấy user ID: " + user.getMaNguoiDung());
            System.out.println("👤 Username: " + user.getTenDangNhap());
            System.out.println("🔐 Mật khẩu CŨ (30 ký tự đầu): " + user.getMatKhau().substring(0, 30) + "...");
            
            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            System.out.println("🔐 Mật khẩu MỚI đã mã hóa (30 ký tự đầu): " + encodedPassword.substring(0, 30) + "...");
            
            // ✅ SET MẬT KHẨU MỚI
            user.setMatKhau(encodedPassword);
            
            // ✅ SET NGÀY CẬP NHẬT THỦ CÔNG (QUAN TRỌNG!)
            user.setNgayCapNhat(LocalDateTime.now());
            
            System.out.println("📝 Đã set: MatKhau + NgayCapNhat");
            
            // Lưu và flush
            User savedUser = userRepository.save(user);
            System.out.println("💾 Đã gọi userRepository.save()");
            
            userRepository.flush();
            System.out.println("✅ Đã flush vào database");
            
            // Kiểm tra lại từ database
            User reloadedUser = userRepository.findById(user.getMaNguoiDung()).orElse(null);
            if (reloadedUser != null) {
                System.out.println("🔄 Reload user từ DB - Mật khẩu (30 ký tự đầu): " + reloadedUser.getMatKhau().substring(0, 30) + "...");
                System.out.println("📅 NgayCapNhat: " + reloadedUser.getNgayCapNhat());
            }
            
            System.out.println("========== KẾT THÚC RESET PASSWORD ==========");
            
        } catch (Exception e) {
            System.err.println("❌ LỖI KHI RESET PASSWORD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}