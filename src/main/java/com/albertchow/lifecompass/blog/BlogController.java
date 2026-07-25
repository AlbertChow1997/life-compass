package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreatePostRequest;
import com.albertchow.lifecompass.blog.dto.LikeResponse;
import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Blog Posts", description = "Browse, publish, and like posts")
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    /** Lists visible posts, optionally filtered to featured-only, to posts by people the current user follows, or to one author. */
    @Operation(summary = "List posts, optionally featured-only, following-only, or by one author")
    @GetMapping
    public Result<List<Blog>> list(
            @Parameter(description = "Only return admin-featured posts") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Only return posts by users the current user follows (requires auth; empty list otherwise)")
            @RequestParam(required = false) Boolean followedOnly,
            @Parameter(description = "Only return posts by this author id, e.g. for a public profile page")
            @RequestParam(required = false) Long authorId) {
        return Result.ok(blogService.list(featured, followedOnly, authorId));
    }

    /** Fetches a single post by ID. */
    @Operation(summary = "Get one post's detail")
    @GetMapping("/{id}")
    public Result<Blog> detail(@PathVariable Long id) {
        return Result.ok(blogService.getById(id));
    }

    /** Publishes a new post on behalf of the logged-in user, optionally linked to a shop. */
    @Operation(summary = "Create a post")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public Result<Blog> create(@Valid @RequestBody CreatePostRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.create(userId, request));
    }

    /** Likes the post if the current user hasn't liked it yet, otherwise un-likes it. */
    @Operation(summary = "Toggle like on a post")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/like")
    public Result<LikeResponse> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.toggleLike(id, userId));
    }
}
