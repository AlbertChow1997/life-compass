package com.albertchow.lifecompass.security;

import com.albertchow.lifecompass.common.enums.Role;

/**
 * The identity carried inside a JWT, resolved once per request by
 * {@link JwtAuthenticationFilter} and exposed via {@link UserContext}.
 */
public record LoginUser(Long id, Role role) {
}
