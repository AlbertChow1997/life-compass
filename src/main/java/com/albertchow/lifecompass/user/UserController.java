package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.user.dto.UpdateProfileRequest;
import com.albertchow.lifecompass.user.dto.UserStatsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The personal center: /api/user/** always requires auth (no permitAll entry matches it). */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/stats")
    public Result<UserStatsResponse> stats() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.getStats(userId));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/shops")
    public Result<List<Shop>> followedShops() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listFollowedShops(userId));
    }

    @GetMapping("/posts")
    public Result<List<Blog>> myPosts() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyPosts(userId));
    }

    @GetMapping("/comments")
    public Result<List<BlogComment>> myComments() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyComments(userId));
    }

    @GetMapping("/likes")
    public Result<List<Blog>> myLikes() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyLikes(userId));
    }

    @GetMapping("/orders")
    public Result<List<VoucherOrder>> myOrders() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyOrders(userId));
    }
}
