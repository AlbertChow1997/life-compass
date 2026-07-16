package com.albertchow.lifecompass.blog.dto;

import jakarta.validation.constraints.NotBlank;

/** shopId is optional: requirement 4 allows a post to link a shop but doesn't require it. */
public record CreatePostRequest(
        @NotBlank String title,
        @NotBlank String content,
        String images,
        Long shopId) {
}
