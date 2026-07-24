package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/login. Email/password login, used by
 * merchants and admins only — regular users sign in with SMS or Google.
 */
public record CredentialLoginRequest(@NotBlank String email, @NotBlank String password) {
}
