package com.albertchow.lifecompass.support.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for an admin creating or updating an FAQ entry used by the support bot. */
public record SupportFaqRequest(
        /** Comma-separated keywords/phrases that trigger this answer. */
        @NotBlank String keywords,
        @NotBlank String answer) {
}
