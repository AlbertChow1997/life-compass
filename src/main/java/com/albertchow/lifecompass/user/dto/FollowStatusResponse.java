package com.albertchow.lifecompass.user.dto;

/**
 * Response telling the frontend whether the current user already follows
 * a given other user, e.g. to render the follow button's initial state.
 */
public record FollowStatusResponse(boolean followed) {
}
