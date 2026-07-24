package com.albertchow.lifecompass.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Request body for a merchant creating a new voucher for one of their shops. */
public record CreateVoucherRequest(
        @NotNull Long shopId,
        @NotBlank String title,
        String subTitle,
        String rules,
        /** Price the user pays, in euro cents. */
        @NotNull Long payValue,
        /** Face value of the voucher, in euro cents. */
        @NotNull Long actualValue,
        /** 0 regular, 1 limited (stock-controlled). */
        Integer type,
        /** Starting stock; only meaningful when {@code type} is limited. */
        Integer stock,
        LocalDateTime beginTime,
        LocalDateTime endTime) {
}
