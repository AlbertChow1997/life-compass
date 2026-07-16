package com.albertchow.lifecompass.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateShopRequest(@NotNull @Min(1) @Max(5) Integer score, String content) {
}
