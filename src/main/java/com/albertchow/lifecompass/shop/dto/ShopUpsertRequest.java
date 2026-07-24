package com.albertchow.lifecompass.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for creating or updating a shop listing (admin/merchant
 * only). The same shape is used for both create and update.
 */
public record ShopUpsertRequest(
        @NotBlank String name,
        /** FK -> shop_type.id. */
        @NotNull Long typeId,
        /** FK -> user.id, the managing merchant; null if unassigned. */
        Long ownerId,
        /** Comma-separated image URLs. */
        String images,
        /** City / district, e.g. Dublin, Cork. */
        String area,
        String address,
        /** Longitude. */
        BigDecimal x,
        /** Latitude. */
        BigDecimal y,
        /** Average price per person, in euro cents. */
        Long avgPrice,
        String openHours) {
}
