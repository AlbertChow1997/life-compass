package com.albertchow.lifecompass.auth.dto;

/**
 * Response for GET /api/auth/config. Tells the frontend which login
 * methods are actually enabled on the server, without exposing any secrets.
 */
public record AuthConfigResponse(
        /** True if SMS provider credentials are set, so phone login/registration can be offered. */
        boolean smsConfigured) {
}
