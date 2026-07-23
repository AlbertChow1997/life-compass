package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreateCommentRequest;
import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read and write endpoints for comments on a single blog post. Nested under
 * /api/blog/{blogId}/comments so every request is already scoped to one post.
 */
@RestController
@RequestMapping("/api/blog/{blogId}/comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService commentService;

    /** Lists the visible (non-deleted) comments on a post, oldest first. */
    @GetMapping
    public Result<List<BlogComment>> list(@PathVariable Long blogId) {
        return Result.ok(commentService.list(blogId));
    }

    /** Adds a new comment (or reply) to a post on behalf of the logged-in user. */
    @PostMapping
    public Result<BlogComment> add(@PathVariable Long blogId, @Valid @RequestBody CreateCommentRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(commentService.add(blogId, userId, request));
    }
}
