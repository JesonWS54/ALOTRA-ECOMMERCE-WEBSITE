package nhom12.AloTra.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Lặp qua các quyền của người dùng
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            // Nếu có quyền 'ROLE_ADMIN', chuyển hướng đến trang dashboard của admin
            if ("ROLE_ADMIN".equals(auth.getAuthority())) {
                response.sendRedirect("/admin/dashboard");
                return; // Kết thúc để tránh chuyển hướng nhiều lần
            }
        }
        // Nếu không phải admin, chuyển hướng về trang chủ
        response.sendRedirect("/");
    }
}