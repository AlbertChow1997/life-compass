package com.albertchow.lifecompass.shop.dto;

/**
 * Response telling the frontend whether the current user already follows
 * a given shop, e.g. to render the follow button's initial state.
 */
public record FollowStatusResponse(boolean followed) {
}
