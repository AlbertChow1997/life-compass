package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreatePostRequest;
import com.albertchow.lifecompass.blog.dto.LikeResponse;
import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public-facing endpoints for browsing and publishing blog posts, and for
 * liking them. Comment endpoints live separately in {@link BlogCommentController}.
 */
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /** Lists visible posts, optionally filtered to featured-only or to posts by people the current user follows. */
    @GetMapping
    public Result<List<Blog>> list(
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean followedOnly) {
        return Result.ok(blogService.list(featured, followedOnly));
    }

    /** Fetches a single post by ID. */
    @GetMapping("/{id}")
    public Result<Blog> detail(@PathVariable Long id) {
        return Result.ok(blogService.getById(id));
    }

    /** Publishes a new post on behalf of the logged-in user, optionally linked to a shop. */
    @PostMapping
    public Result<Blog> create(@Valid @RequestBody CreatePostRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.create(userId, request));
    }

    /** Likes the post if the current user hasn't liked it yet, otherwise un-likes it. */
    @PostMapping("/{id}/like")
    public Result<LikeResponse> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.toggleLike(id, userId));
    }
}
