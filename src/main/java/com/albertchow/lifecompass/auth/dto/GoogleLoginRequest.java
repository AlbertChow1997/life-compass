package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/google. Wraps the ID token returned by
 * Google Sign-In on the frontend, which the backend verifies with Google
 * before issuing our own session token.
 */
public record GoogleLoginRequest(@NotBlank String idToken) {
}
