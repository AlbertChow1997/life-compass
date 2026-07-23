package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.shop.dto.FollowStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public shop browsing and search (requirements 2 & 5). */
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    public Result<List<Shop>> list(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String name) {
        return Result.ok(shopService.search(typeId, name));
    }

    @GetMapping("/{id}")
    public Result<Shop> detail(@PathVariable Long id) {
        return Result.ok(shopService.getById(id));
    }

    /** Public: falls back to followed=false when signed out (no permitAll special-casing needed). */
    @GetMapping("/{id}/follow")
    public Result<FollowStatusResponse> followStatus(@PathVariable Long id) {
        LoginUser loginUser = UserContext.get();
        Long userId = loginUser != null ? loginUser.id() : null;
        return Result.ok(new FollowStatusResponse(shopService.isFollowedBy(id, userId)));
    }

    @PostMapping("/{id}/follow")
    public Result<Void> follow(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        shopService.follow(id, userId);
        return Result.ok();
    }

    @DeleteMapping("/{id}/follow")
    public Result<Void> unfollow(@PathVariable Long id) {
        Long userId = UserContext.require().id();
        shopService.unfollow(id, userId);
        return Result.ok();
    }
}
