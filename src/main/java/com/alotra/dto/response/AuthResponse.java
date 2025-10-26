package com.alotra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO - Complete authentication response with user info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
    private JwtTokenResponse tokens;
}