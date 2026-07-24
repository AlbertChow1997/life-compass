package com.albertchow.lifecompass.blog.dto;

/**
 * Response returned after liking/unliking a post or comment, so the
 * frontend can update its counter and button state without re-fetching.
 */
public record LikeResponse(
        /** Total like count after the action. */
        int liked,
        /** Whether the current user is now among the likers. */
        boolean likedByMe) {
}
