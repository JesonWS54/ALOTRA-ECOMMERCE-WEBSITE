package com.alotra.config;

import com.alotra.security.UserDetailsServiceImpl;
import com.alotra.security.jwt.JwtAuthenticationEntryPoint;
import com.alotra.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Spring Security configuration with JWT authentication
 * Configures security filters, authentication, and authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	/**
	 * Password encoder bean - BCrypt for password hashing
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Authentication provider - connects UserDetailsService and PasswordEncoder
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	/**
	 * Authentication manager bean
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	/**
	 * Security filter chain - main security configuration
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
				// Authentication endpoints
				.requestMatchers("/api/auth/**").permitAll()

				// Test endpoints
				.requestMatchers("/api/test/**").permitAll()

				// ===== WEBSOCKET ENDPOINTS (THÊM MỚI) =====
				.requestMatchers("/ws/**").permitAll() // WebSocket connection
				.requestMatchers("/app/**").permitAll() // STOMP app destinations
				.requestMatchers("/topic/**").permitAll() // STOMP topics
				.requestMatchers("/queue/**").permitAll() // STOMP queues
				.requestMatchers("/user/**").permitAll() // User destinations

				// ===== STATIC RESOURCES (THÊM MỚI) =====
				.requestMatchers("/static/**").permitAll().requestMatchers("/websocket-test.html").permitAll()
				.requestMatchers("/*.html").permitAll().requestMatchers("/*.css").permitAll().requestMatchers("/*.js")
				.permitAll().requestMatchers("/favicon.ico").permitAll()

				// ===== IMAGE UPLOAD (NẾU CÒN BỊ BLOCK) =====
				.requestMatchers("/api/images/**").permitAll()

				// ===== NOTIFICATIONS (NẾU MUỐN PUBLIC) =====
				.requestMatchers("/api/notifications/test").permitAll().requestMatchers("/api/notifications/info")
				.permitAll()

				// All other requests require authentication
				.anyRequest().authenticated());

		// Set authentication provider
		http.authenticationProvider(authenticationProvider());

		// Add JWT filter before UsernamePasswordAuthenticationFilter
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * CORS configuration - allow frontend to access API
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Allow origins
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", // React development
				"http://localhost:8080", // Spring Boot
				"http://localhost:4200" // Angular (optional)
		));

		// Allow methods
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

		// Allow headers
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));

		// Allow credentials
		configuration.setAllowCredentials(true);

		// Max age
		configuration.setMaxAge(3600L);

		// Expose headers
		configuration.setExposedHeaders(List.of("Authorization"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}