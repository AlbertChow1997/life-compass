package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.shop.dto.FollowStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public endpoints for browsing/searching shops and for following/unfollowing
 * them (saving a shop to your list). Ratings live separately in
 * {@link ShopRatingController}.
 */
@Tag(name = "Shops", description = "Browse/search shops, and follow/unfollow one")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /** Searches shops, optionally filtered by category (typeId) and/or a name keyword. */
    @Operation(summary = "Search shops by category and/or name")
    @GetMapping
    public Result<List<Shop>> list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String name) {
        return Result.ok(shopService.search(typeId, name));
    }

    /** Lists shops within radiusKm of (lat, lng), nearest first, each with its distance set. */
    @Operation(summary = "Find shops near a location, nearest first")
    @GetMapping("/nearby")
    public Result<List<Shop>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm) {
        return Result.ok(shopService.searchNearby(lat, lng, radiusKm));
    }

    /** Fetches full details for a single shop. */
    @Operation(summary = "Get one shop's detail")
    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        return Result.ok(shopService.getById(id));
    }

    /** Reports whether the current user follows this shop; signed-out visitors simply get followed=false rather than a 401. */
    @Operation(summary = "Check whether the current user follows this shop")
    @GetMapping("/{id}/follow")
    public Result<FollowStatusResponse> followStatus(@PathVariable Long id) {
        LoginUser loginUser = UserContext.get();
        Long userId = loginUser != null ? loginUser.id() : null;
        return Result.ok(new FollowStatusResponse(shopService.isFollowedBy(id, userId)));
    }

    /** Adds this shop to the current user's followed/saved list. */
    @Operation(summary = "Follow (save) this shop")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/follow")
    public Result<Void> follow(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        shopService.follow(id, userId);
        return Result.ok();
    }

    /** Removes this shop from the current user's followed/saved list. */
    @Operation(summary = "Unfollow this shop")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollow(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        shopService.unfollow(id, userId);
        return Result.ok();
    }
}
