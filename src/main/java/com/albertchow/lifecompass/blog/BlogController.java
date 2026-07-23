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

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public Result<List<Blog>> list(
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean followedOnly) {
        return Result.ok(blogService.list(featured, followedOnly));
    }

    @GetMapping("/{id}")
    public Result<Blog> detail(@PathVariable Long id) {
        return Result.ok(blogService.getById(id));
    }

    @PostMapping
    public Result<Blog> create(@Valid @RequestBody CreatePostRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.create(userId, request));
    }

    @PostMapping("/{id}/like")
    public Result<LikeResponse> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        return Result.ok(blogService.toggleLike(id, userId));
    }
}
