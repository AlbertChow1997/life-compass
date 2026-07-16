package com.albertchow.lifecompass.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ShopUpsertRequest(
        @NotBlank String name,
        @NotNull Long typeId,
        Long ownerId,
        String images,
        String area,
        String address,
        BigDecimal x,
        BigDecimal y,
        Long avgPrice,
        String openHours) {
}
