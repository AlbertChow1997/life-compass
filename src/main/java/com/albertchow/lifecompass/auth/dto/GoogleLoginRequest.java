package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** The ID token returned by Google Sign-In on the frontend. */
public record GoogleLoginRequest(@NotBlank String idToken) {
}
