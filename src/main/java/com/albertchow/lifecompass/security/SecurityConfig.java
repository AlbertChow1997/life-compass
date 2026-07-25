package com.albertchow.lifecompass.security;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.common.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Stateless JWT-based security: no sessions, no CSRF, one filter that resolves
 * the {@code Authorization} header into a {@link LoginUser} for every request.
 * Errors are returned as JSON using the same {@link Result} envelope as the
 * rest of the API, rather than Spring Security's default HTML/plain-text pages.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /** Defines the whole access-control policy: which routes are public, which need a role, and how auth/permission errors are rendered as JSON. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Let Boot's own error dispatch through, otherwise a route with no
                        // controller gets masked as 401 instead of a proper 404.
                        .requestMatchers("/error").permitAll()
                        // Swagger UI and its generated OpenAPI JSON: public so every endpoint
                        // can be browsed and tried without a token first (protected endpoints
                        // still need a real Bearer token entered in the UI's "Authorize" dialog).
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Static images (shop photos, uploaded post photos) must be publicly
                        // readable: <img> tags never send an Authorization header.
                        .requestMatchers(HttpMethod.GET, "/images/**", "/uploads/**").permitAll()
                        // Only the actual login/register endpoints are public; /api/auth/me requires a valid JWT.
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/google", "/api/auth/sms/code", "/api/auth/sms/login",
                                "/api/auth/login", "/api/auth/register")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/config").permitAll()
                        // Support widget: works whether or not the visitor is signed in.
                        .requestMatchers(HttpMethod.POST, "/api/support/ask").permitAll()
                        // Public browsing: shop directory, categories, posts, on-shelf vouchers (reqs 2, 4, 5),
                        // and the /api/users people directory (distinct from the always-authenticated
                        // /api/user/** personal-center prefix below).
                        .requestMatchers(HttpMethod.GET,
                                "/api/shop/**", "/api/shop-type/**", "/api/blog/**", "/api/voucher/**", "/api/users/**")
                        .permitAll()
                        .requestMatchers("/api/merchant/**").hasRole(Role.MERCHANT.name())
                        .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeError(res, 401, "Authentication required"))
                        .accessDeniedHandler((req, res, ex) ->
                                writeError(res, 403, "You do not have permission to perform this action")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** The hashing algorithm used to store and verify user passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Allows the deployed frontend (Vercel/CloudFront/etc.) to call the API when
     * it is served from a different origin. Configure the comma-separated list
     * with LIFECOMPASS_CORS_ALLOWED_ORIGINS in production.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${lifecompass.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(splitCsv(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/images/**", config);
        source.registerCorsConfiguration("/uploads/**", config);
        return source;
    }

    private List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** Writes a JSON {@link Result#fail} error body with the given HTTP status, matching the rest of the API's response shape. */
    private void writeError(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(Result.fail(message)));
    }
}
