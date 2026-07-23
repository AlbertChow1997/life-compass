package com.albertchow.lifecompass.support.dto;

import jakarta.validation.constraints.NotBlank;

public record SupportFaqRequest(@NotBlank String keywords, @NotBlank String answer) {
}
