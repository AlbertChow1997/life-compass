package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Email/password login, used by merchants and admins only. */
public record CredentialLoginRequest(@NotBlank String email, @NotBlank String password) {
}
