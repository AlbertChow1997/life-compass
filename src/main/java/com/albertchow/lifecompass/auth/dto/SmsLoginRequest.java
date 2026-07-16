package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsLoginRequest(
        @NotBlank String phone,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Code must be 6 digits") String code) {
}
