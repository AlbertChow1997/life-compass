package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.ShopRating;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.user.dto.FollowStatusResponse;
import com.albertchow.lifecompass.user.dto.UpdateProfileRequest;
import com.albertchow.lifecompass.user.dto.UserStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The "personal center": a signed-in user's own profile, stats, activity
 * history (posts/comments/likes/orders/ratings), and following other users.
 * Every path here is under /api/user/** and always requires authentication
 * (there is no permitAll entry that matches it).
 */
@Tag(name = "Personal Centre", description = "The current user's own profile, stats, activity history, and following other users")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Returns the current user's activity stats (e.g. XP, post/comment counts). */
    @Operation(summary = "Get follower/following counts and XP")
    @GetMapping("/stats")
    public Result<UserStatsResponse> stats() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.getStats(userId));
    }

    /** Updates the current user's editable profile fields (nickname, avatar, city, etc.). */
    @Operation(summary = "Update nickname/city/avatar")
    @PutMapping("/profile")
    public Result<User> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(userService.updateProfile(userId, request));
    }

    /** Lists the shops the current user follows/has saved. */
    @Operation(summary = "List shops the current user follows")
    @GetMapping("/shops")
    public Result<List<Shop>> followedShops() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listFollowedShops(userId));
    }

    /** Lists the blog posts the current user has published. */
    @Operation(summary = "List the current user's own posts")
    @GetMapping("/posts")
    public Result<List<Blog>> myPosts() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyPosts(userId));
    }

    /** Lists the comments the current user has left on blog posts. */
    @Operation(summary = "List the current user's own comments")
    @GetMapping("/comments")
    public Result<List<BlogComment>> myComments() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyComments(userId));
    }

    /** Lists the blog posts the current user has liked. */
    @Operation(summary = "List posts the current user has liked")
    @GetMapping("/likes")
    public Result<List<Blog>> myLikes() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyLikes(userId));
    }

    /** Lists the voucher orders the current user has placed. */
    @Operation(summary = "List the current user's voucher orders")
    @GetMapping("/orders")
    public Result<List<VoucherOrder>> myOrders() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyOrders(userId));
    }

    /** Lists the shop ratings the current user has left. */
    @Operation(summary = "List the current user's own shop ratings")
    @GetMapping("/ratings")
    public Result<List<ShopRating>> myRatings() {
        Long userId = UserContext.require().id();
        return Result.ok(userService.listMyRatings(userId));
    }

    /** Deletes one of the current user's own ratings. */
    @Operation(summary = "Delete one of the current user's own ratings")
    @DeleteMapping("/ratings/{id}")
    public Result<Void> deleteRating(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        userService.deleteRating(id, userId);
        return Result.ok();
    }

    /** Makes the current user follow another user. */
    @Operation(summary = "Follow another user")
    @PostMapping("/{targetUserId}/follow")
    public Result<Void> followUser(@PathVariable Long targetUserId) {
        Long userId = UserContext.require().id();
        userService.followUser(userId, targetUserId);
        return Result.ok();
    }

    /** Makes the current user unfollow another user. */
    @Operation(summary = "Unfollow another user")
    @DeleteMapping("/{targetUserId}/follow")
    public Result<Void> unfollowUser(@PathVariable Long targetUserId) {
        Long userId = UserContext.require().id();
        userService.unfollowUser(userId, targetUserId);
        return Result.ok();
    }

    /** Reports whether the current user follows the given user; unlike shop-follow status, this always requires auth since there's no anonymous "who am I following" concept. */
    @Operation(summary = "Check whether the current user follows another user")
    @GetMapping("/{targetUserId}/follow")
    public Result<FollowStatusResponse> followUserStatus(@PathVariable Long targetUserId) {
        LoginUser loginUser = UserContext.require();
        return Result.ok(new FollowStatusResponse(userService.isFollowingUser(loginUser.id(), targetUserId)));
    }
}
