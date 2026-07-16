package com.albertchow.lifecompass.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateVoucherRequest(
        @NotNull Long shopId,
        @NotBlank String title,
        String subTitle,
        String rules,
        @NotNull Long payValue,
        @NotNull Long actualValue,
        /** 0 regular, 1 limited (stock-controlled). */
        Integer type,
        Integer stock,
        LocalDateTime beginTime,
        LocalDateTime endTime) {
}
