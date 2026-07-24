package com.albertchow.lifecompass.user.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for the current user editing their own profile. */
public record UpdateProfileRequest(
        @NotBlank String nickName,
        String city,
        /** URL previously returned by POST /api/upload; blank clears the current icon. */
        String icon) {
}
