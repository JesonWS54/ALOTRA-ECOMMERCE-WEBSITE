package com.alotra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JwtTokenResponse DTO - Contains JWT tokens
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
}