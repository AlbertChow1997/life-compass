package com.albertchow.lifecompass.blog.dto;

import jakarta.validation.constraints.NotBlank;

/** parentId/answerId are null (or 0) for a top-level comment, set for a threaded reply. */
public record CreateCommentRequest(
        @NotBlank String content,
        Long parentId,
        Long answerId) {
}
