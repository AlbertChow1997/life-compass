package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only moderation endpoints for the blog: featuring posts, taking down
 * posts, and removing comments. All paths are under /api/admin/**, which
 * Spring Security restricts to ROLE_ADMIN (see SecurityConfig).
 */
@Tag(name = "Admin - Blog", description = "Admin-only post/comment moderation (requires ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminBlogService adminBlogService;

    /** Marks (or unmarks) a post as featured so it can be highlighted on the site. */
    @Operation(summary = "Feature or unfeature a post")
    @PutMapping("/{id}/feature")
    public Result<Void> setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        adminBlogService.setFeatured(id, featured);
        return Result.ok();
    }

    /** Soft-deletes (takes down) a blog post. */
    @Operation(summary = "Take down a post")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        adminBlogService.deletePost(id);
        return Result.ok();
    }

    /** Soft-deletes a comment and updates the parent post's comment count. */
    @Operation(summary = "Remove a comment")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        adminBlogService.deleteComment(commentId);
        return Result.ok();
    }
}
