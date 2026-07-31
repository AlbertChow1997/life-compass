package com.albertchow.lifecompass.security;

import com.albertchow.lifecompass.common.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtUtil}, the token used by every one of the three
 * sign-in paths (Section 2.2.1 of the technical report). No Spring context
 * or database is needed — {@code JwtUtil} is a plain, directly
 * constructible component.
 */
class JwtUtilTest {

    // 32+ bytes, matching the length LIFECOMPASS_JWT_SECRET must satisfy in production.
    private static final String TEST_SECRET = "test-secret-key-at-least-32-bytes-long-for-hmac-sha";

    @Test
    void generateThenParse_roundTripsTheUserIdAndRole() {
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 3600);

        String token = jwtUtil.generate(42L, Role.MERCHANT);
        LoginUser loginUser = jwtUtil.parse(token);

        assertThat(loginUser.id()).isEqualTo(42L);
        assertThat(loginUser.role()).isEqualTo(Role.MERCHANT);
    }

    @Test
    void parse_throws_forAnAlreadyExpiredToken() {
        // Expiration in the past at the moment of issuance — deterministic, no sleeping.
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, -10);

        String token = jwtUtil.generate(1L, Role.USER);

        assertThatThrownBy(() -> jwtUtil.parse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parse_throws_forAMalformedToken() {
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 3600);

        assertThatThrownBy(() -> jwtUtil.parse("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parse_throws_whenTheSignatureWasSignedWithADifferentSecret() {
        JwtUtil issuer = new JwtUtil(TEST_SECRET, 3600);
        JwtUtil verifier = new JwtUtil("a-completely-different-secret-that-is-also-32-plus-bytes", 3600);

        String token = issuer.generate(1L, Role.USER);

        assertThatThrownBy(() -> verifier.parse(token))
                .isInstanceOf(JwtException.class);
    }
}
