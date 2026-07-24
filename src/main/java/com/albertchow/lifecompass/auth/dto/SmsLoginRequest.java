package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /api/auth/sms/login. Logs the user in (creating an
 * account on first use) by verifying the code previously sent via
 * SmsCodeRequest. Also used for phone-based registration.
 */
public record SmsLoginRequest(
        @NotBlank String phone,
        /** The 6-digit one-time code sent to {@code phone}. */
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Code must be 6 digits") String code) {
}
