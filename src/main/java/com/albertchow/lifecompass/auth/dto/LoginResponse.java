package com.albertchow.lifecompass.auth.dto;

/**
 * Response returned by any successful login endpoint (credential, SMS, or
 * Google). Carries the session token plus enough user info for the
 * frontend to render immediately without a follow-up request.
 */
public record LoginResponse(
        /** Bearer token to send in the Authorization header on subsequent requests. */
        String token,
        Long userId,
        String nickName,
        /** USER, MERCHANT, or ADMIN. */
        String role) {
}
