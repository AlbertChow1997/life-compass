package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Secured by /api/admin/** -> ROLE_ADMIN. */
@RestController
@RequestMapping("/api/admin/blog")
@RequiredArgsConstructor
public class AdminBlogController {

    private final AdminBlogService adminBlogService;

    @PutMapping("/{id}/feature")
    public Result<Void> setFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        adminBlogService.setFeatured(id, featured);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        adminBlogService.deletePost(id);
        return Result.ok();
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        adminBlogService.deleteComment(commentId);
        return Result.ok();
    }
}
