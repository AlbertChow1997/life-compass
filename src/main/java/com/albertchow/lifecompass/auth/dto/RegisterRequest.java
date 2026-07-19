package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Self-registration with email+password. {@code confirmPassword} is checked
 * client-side only — the backend only ever sees the final password once.
 * {@code role} is deliberately restricted to USER/MERCHANT; ADMIN accounts
 * are never self-service.
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank String nickName,
        String city,
        @NotBlank @Pattern(regexp = "USER|MERCHANT", message = "Role must be USER or MERCHANT") String role) {
}
