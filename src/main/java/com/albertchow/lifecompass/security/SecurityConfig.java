package com.albertchow.lifecompass.security;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.common.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Let Boot's own error dispatch through, otherwise a route with no
                        // controller gets masked as 401 instead of a proper 404.
                        .requestMatchers("/error").permitAll()
                        // Static images (shop photos, uploaded post photos) must be publicly
                        // readable: <img> tags never send an Authorization header.
                        .requestMatchers(HttpMethod.GET, "/images/**", "/uploads/**").permitAll()
                        // Only the actual login endpoints are public; /api/auth/me requires a valid JWT.
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/google", "/api/auth/sms/code", "/api/auth/sms/login", "/api/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/config").permitAll()
                        // Public browsing: shop directory, categories, posts, on-shelf vouchers (reqs 2, 4, 5).
                        .requestMatchers(HttpMethod.GET,
                                "/api/shop/**", "/api/shop-type/**", "/api/blog/**", "/api/voucher/**")
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeError(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(Result.fail(message)));
    }
}
