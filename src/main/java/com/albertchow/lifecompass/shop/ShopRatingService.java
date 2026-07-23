package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.ShopRating;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.ShopRatingMapper;
import com.albertchow.lifecompass.shop.dto.RateShopRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Requirement 3: users rate shops 1..5; the shop's average score/comment
 * count stay in sync. Ratings form a history (a user may rate the same shop
 * again after a cooldown), bounded by two anti-spam rules:
 * - at most {@link #MONTHLY_CAP} ratings per user per calendar month
 * - a 30-day cooldown before re-rating the same shop
 */
@Service
@RequiredArgsConstructor
public class ShopRatingService {

    private static final int MONTHLY_CAP = 50;
    private static final int SAME_SHOP_COOLDOWN_DAYS = 30;

    private final ShopRatingMapper shopRatingMapper;
    private final ShopMapper shopMapper;

    public List<ShopRating> list(Long shopId) {
        var query = new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getShopId, shopId)
                .orderByDesc(ShopRating::getCreateTime);
        return shopRatingMapper.selectList(query);
    }

    @Transactional
    public ShopRating rate(Long shopId, Long userId, RateShopRequest request) {
        if (shopMapper.selectById(shopId) == null) {
            throw new NotFoundException("Shop not found");
        }

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long ratedThisMonth = shopRatingMapper.selectCount(new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getUserId, userId)
                .ge(ShopRating::getCreateTime, startOfMonth));
        if (ratedThisMonth >= MONTHLY_CAP) {
            throw new BusinessException("You've reached the monthly limit of " + MONTHLY_CAP + " shop ratings.");
        }

        LocalDateTime cooldownCutoff = LocalDateTime.now().minusDays(SAME_SHOP_COOLDOWN_DAYS);
        boolean ratedRecently = shopRatingMapper.exists(new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getShopId, shopId)
                .eq(ShopRating::getUserId, userId)
                .ge(ShopRating::getCreateTime, cooldownCutoff));
        if (ratedRecently) {
            throw new BusinessException(
                    "You can rate this shop again " + SAME_SHOP_COOLDOWN_DAYS + " days after your last rating.");
        }

        ShopRating rating = new ShopRating();
        rating.setShopId(shopId);
        rating.setUserId(userId);
        rating.setScore(request.score());
        rating.setContent(request.content() != null ? request.content() : "");
        shopRatingMapper.insert(rating);

        recomputeShopScore(shopId);
        return rating;
    }

    public List<ShopRating> listMine(Long userId) {
        var query = new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getUserId, userId)
                .orderByDesc(ShopRating::getCreateTime);
        List<ShopRating> ratings = shopRatingMapper.selectList(query);
        if (ratings.isEmpty()) {
            return ratings;
        }
        List<Long> shopIds = ratings.stream().map(ShopRating::getShopId).distinct().toList();
        Map<Long, Shop> shopsById = shopMapper.selectByIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s));
        for (ShopRating rating : ratings) {
            Shop shop = shopsById.get(rating.getShopId());
            if (shop != null) {
                rating.setShopName(shop.getName());
            }
        }
        return ratings;
    }

    @Transactional
    public void deleteMine(Long ratingId, Long userId) {
        ShopRating rating = shopRatingMapper.selectById(ratingId);
        if (rating == null || !rating.getUserId().equals(userId)) {
            throw new NotFoundException("Rating not found");
        }
        shopRatingMapper.deleteById(ratingId);
        recomputeShopScore(rating.getShopId());
    }

    private void recomputeShopScore(Long shopId) {
        List<ShopRating> ratings = shopRatingMapper.selectList(
                new LambdaQueryWrapper<ShopRating>().eq(ShopRating::getShopId, shopId));
        int count = ratings.size();
        int scoreX10 = count == 0
                ? 0
                : (int) Math.round(ratings.stream().mapToInt(ShopRating::getScore).average().orElse(0) * 10);

        Shop patch = new Shop();
        patch.setId(shopId);
        patch.setComments(count);
        patch.setScore(scoreX10);
        shopMapper.updateById(patch);
    }
}
