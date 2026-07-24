package com.albertchow.lifecompass.user.dto;

/** One entry in the public user directory (GET /api/users). */
public record UserSummaryResponse(
        Long id,
        String nickName,
        String icon,
        String city,
        /** Whether the current caller follows this user; always false when signed out. */
        boolean followedByCurrentUser) {
}
