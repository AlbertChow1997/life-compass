package com.albertchow.lifecompass.support.dto;

import jakarta.validation.constraints.NotBlank;

public record AskSupportRequest(@NotBlank String question) {
}
