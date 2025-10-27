package com.alotra.config;

import com.alotra.security.jwt.JwtAuthenticationEntryPoint;
import com.alotra.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Spring Security Configuration
 * Cấu hình bảo mật cho cả Web và API
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password Encoder - BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Security Filter Chain - Main Security Configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF Configuration
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable CSRF cho API endpoints
            )
            
            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // ==================== PUBLIC ENDPOINTS ====================
                // Trang web không cần đăng nhập
                .requestMatchers(
                    "/",
                    "/home",
                    "/products",
                    "/products/**",
                    "/about",
                    "/contact",
                    "/error"
                ).permitAll()
                
                // Authentication pages
                .requestMatchers(
                    "/login",
                    "/register",
                    "/logout"
                ).permitAll()
                
                // API Authentication endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/api/test/**"
                ).permitAll()
                
                // Static resources (CSS, JS, Images)
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/favicon.ico"
                ).permitAll()
                
                // WebSocket endpoint
                .requestMatchers("/ws/**").permitAll()
                
                // ==================== USER ENDPOINTS ====================
                // Cart - Cần đăng nhập
                .requestMatchers(
                    "/cart",
                    "/cart/**"
                ).authenticated()
                
                // Profile - Cần đăng nhập
                .requestMatchers(
                    "/profile",
                    "/profile/**"
                ).authenticated()
                
                // User API endpoints
                .requestMatchers(
                    "/api/cart/**",
                    "/api/orders/**",
                    "/api/reviews/**"
                ).authenticated()
                
                // ==================== ADMIN ENDPOINTS ====================
                // Admin pages - Cần ROLE_ADMIN
                .requestMatchers(
                    "/admin",
                    "/admin/**"
                ).hasRole("ADMIN")
                
                // Admin API endpoints
                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")
                
                // Product management API (admin only)
                .requestMatchers(
                    "/api/products/**"
                ).hasAnyRole("ADMIN", "MODERATOR")
                
                // ==================== DEFAULT ====================
                // Tất cả requests khác cần authentication
                .anyRequest().authenticated()
            )
            
            // Form Login Configuration
            .formLogin(form -> form
                .loginPage("/login")                    // Custom login page
                .loginProcessingUrl("/login")           // URL xử lý login
                .defaultSuccessUrl("/", true)           // Redirect sau khi login thành công
                .failureUrl("/login?error=true")        // Redirect nếu login thất bại
                .permitAll()
            )
            
            // Logout Configuration
            .logout(logout -> logout
                .logoutUrl("/logout")                   // URL để logout
                .logoutSuccessUrl("/")                  // Redirect sau khi logout
                .invalidateHttpSession(true)            // Xóa session
                .deleteCookies("JSESSIONID")            // Xóa cookies
                .permitAll()
            )
            
            // Session Management (cho API dùng JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Tạo session nếu cần
            )
            
            // Exception Handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}