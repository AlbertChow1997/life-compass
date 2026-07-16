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

@RestController
@RequestMapping("/api/blog/{blogId}/comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService commentService;

    @GetMapping
    public Result<List<BlogComment>> list(@PathVariable Long blogId) {
        return Result.ok(commentService.list(blogId));
    }

    @PostMapping
    public Result<BlogComment> add(@PathVariable Long blogId, @Valid @RequestBody CreateCommentRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(commentService.add(blogId, userId, request));
    }
}
