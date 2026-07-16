package com.albertchow.lifecompass.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsCodeRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must be in E.164 format, e.g. +353851234567")
        String phone) {
}
