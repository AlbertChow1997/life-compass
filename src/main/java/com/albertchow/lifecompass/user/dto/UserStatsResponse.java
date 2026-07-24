package com.albertchow.lifecompass.user.dto;

/**
 * Response for GET /api/user/stats, powering the current user's profile
 * summary (follow counts and progress towards Pro status).
 */
public record UserStatsResponse(
        long following,
        long followers,
        /** Accumulated experience points, used to determine Pro eligibility. */
        long experience,
        /** Experience points required to reach Pro status, for rendering a progress bar. */
        long proThreshold) {
}
