package com.albertchow.lifecompass.support.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for asking the support FAQ bot a question. The backend
 * matches this against stored FAQ entries and returns a SupportAnswerResponse.
 */
public record AskSupportRequest(@NotBlank String question) {
}
