package com.albertchow.lifecompass.user.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code icon} is a URL previously returned by POST /api/upload; blank clears it. */
public record UpdateProfileRequest(@NotBlank String nickName, String city, String icon) {
}
