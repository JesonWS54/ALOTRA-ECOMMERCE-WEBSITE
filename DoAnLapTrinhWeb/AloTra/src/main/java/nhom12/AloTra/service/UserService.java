package nhom12.AloTra.service;

import nhom12.AloTra.entity.User;
import nhom12.AloTra.request.UserRequest;
import org.springframework.data.domain.Page;
import nhom12.AloTra.request.SignUpRequest;

public interface UserService {
    Page<User> findAll(String keyword, Integer roleId, Integer tierId, Integer status, int page, int size);
    User findById(int id);
    void save(UserRequest userRequest);
    void delete(int id);

    User registerNewUser(SignUpRequest signUpRequest);
    boolean verifyEmailOtp(String email, String otp);
    User findByEmail(String email);

    void sendResetPasswordOtp(String email);
    boolean verifyResetPasswordOtp(String email, String otp);
    void resetPassword(String email, String newPassword);
}