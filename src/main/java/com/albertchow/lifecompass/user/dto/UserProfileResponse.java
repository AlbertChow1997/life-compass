package com.albertchow.lifecompass.user.dto;

/** Public profile of one user, returned by GET /api/users/{id}. */
public record UserProfileResponse(
        Long id,
        String nickName,
        String icon,
        String city,
        /** Short personal description, shown on the profile. */
        String bio,
        /** Accumulated experience points, used to determine level/Pro eligibility. */
        long experience,
        /** Experience points required to reach Pro status (and the top level), for rendering a progress bar. */
        long proThreshold,
        long following,
        long followers) {
}
