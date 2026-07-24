package com.albertchow.lifecompass.blog.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for posting a comment on a blog post. Can be either a
 * top-level comment or a threaded reply to an existing comment/answer.
 */
public record CreateCommentRequest(
        @NotBlank String content,
        /** ID of the comment being replied to; null (or 0) for a top-level comment. */
        Long parentId,
        /** ID of the top-level answer this reply belongs to; null (or 0) for a top-level comment. */
        Long answerId) {
}
