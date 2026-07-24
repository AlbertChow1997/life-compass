package com.albertchow.lifecompass.blog.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a blog post. A post can optionally be linked
 * to a shop (e.g. a review-style post), but doesn't have to be.
 */
public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        /** Comma-separated image URLs. */
        String images,
        /** Shop this post is about, or null if it isn't tied to a shop. */
        Long shopId) {
}
