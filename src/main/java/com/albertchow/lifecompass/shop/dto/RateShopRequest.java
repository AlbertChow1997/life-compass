package com.albertchow.lifecompass.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request body for a user submitting a star rating (and optional review text) for a shop. */
public record RateShopRequest(
        /** Star rating from 1 to 5. */
        @NotNull @Min(1) @Max(5) Integer score,
        String content) {
}
