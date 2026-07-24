package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.ShopRating;
import com.albertchow.lifecompass.security.UserContext;
import com.albertchow.lifecompass.shop.dto.RateShopRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints for viewing and submitting star ratings/reviews on a single shop. */
@RestController
@RequestMapping("/api/shop/{shopId}/ratings")
@RequiredArgsConstructor
public class ShopRatingController {

    private final ShopRatingService ratingService;

    /** Lists all ratings left on this shop. */
    @GetMapping
    public Result<List<ShopRating>> list(@PathVariable Long shopId) {
        return Result.ok(ratingService.list(shopId));
    }

    /** Submits a new rating; capped at 50/month per user, with a 30-day cooldown per shop. */
    @PostMapping
    public Result<ShopRating> rate(@PathVariable Long shopId, @Valid @RequestBody RateShopRequest request) {
        Long userId = UserContext.require().id();
        return Result.ok(ratingService.rate(shopId, userId, request));
    }
}
