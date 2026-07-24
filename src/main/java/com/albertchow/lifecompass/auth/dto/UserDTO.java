package com.albertchow.lifecompass.auth.dto;

/**
 * The current user's public profile, returned by GET /api/auth/me and
 * embedded wherever an authenticated user's identity needs to be shown.
 */
public record UserDTO(
        Long id,
        String nickName,
        /** URL of the profile picture, or null if not set. */
        String icon,
        String city,
        /** USER, MERCHANT, or ADMIN. */
        String role) {
}
