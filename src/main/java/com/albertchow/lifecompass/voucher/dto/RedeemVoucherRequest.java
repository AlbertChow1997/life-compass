package com.albertchow.lifecompass.voucher.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for a merchant redeeming a customer's voucher by the code shown on their order. */
public record RedeemVoucherRequest(@NotBlank String verifyCode) {
}
