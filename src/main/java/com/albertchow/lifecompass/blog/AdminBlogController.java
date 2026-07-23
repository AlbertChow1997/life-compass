package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.common.Result;
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
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminBlogService adminBlogService;

    /** Marks (or unmarks) a post as featured so it can be highlighted on the site. */
    @PutMapping("/{id}/feature")
    public Result<Void> setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        adminBlogService.setFeatured(id, featured);
        return Result.ok();
    }

    /** Soft-deletes (takes down) a blog post. */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        adminBlogService.deletePost(id);
        return Result.ok();
    }

    /** Soft-deletes a comment and updates the parent post's comment count. */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        adminBlogService.deleteComment(commentId);
        return Result.ok();
    }
}
