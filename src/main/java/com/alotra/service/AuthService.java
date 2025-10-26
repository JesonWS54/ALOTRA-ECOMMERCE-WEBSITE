package com.alotra.service;

import com.alotra.dto.request.LoginRequest;
import com.alotra.dto.request.RegisterRequest;
import com.alotra.dto.response.AuthResponse;
import com.alotra.dto.response.JwtTokenResponse;
import com.alotra.entity.RefreshToken;
import com.alotra.entity.User;
import com.alotra.repository.RefreshTokenRepository;
import com.alotra.repository.UserRepository;
import com.alotra.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuthService - Handles authentication business logic
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setIsActive(true);
        user.setEmailVerified(false);

        // Save user
        user = userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole())
                .build();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Build tokens response
        JwtTokenResponse tokens = JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .build();

        // Build auth response
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .tokens(tokens)
                .build();
    }

    /**
     * Login user
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Find user in database
        User user = userRepository.findByUsername(userDetails.getUsername())
                .or(() -> userRepository.findByEmail(userDetails.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Build tokens response
        JwtTokenResponse tokens = JwtTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .build();

        // Build auth response
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .tokens(tokens)
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public JwtTokenResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Extract username from token
        String username = jwtUtil.extractUsername(refreshToken);

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify refresh token exists in database
        boolean tokenExists = refreshTokenRepository.findByToken(refreshToken).isPresent();
        if (!tokenExists) {
            throw new RuntimeException("Refresh token not found in database");
        }

        // Generate new access token
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole())
                .build();

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        // Return new tokens
        return JwtTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs())
                .build();
    }

    /**
     * Logout user - invalidate refresh token
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationMs() / 1000)
        );
        refreshTokenRepository.save(refreshToken);
    }
}