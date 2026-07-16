package com.albertchow.lifecompass.shop;

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

import java.util.List;

/** Requirement 3: users rate shops 1..5; the shop's average score/comment count stay in sync. */
@Service
@RequiredArgsConstructor
public class ShopRatingService {

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

        ShopRating existing = shopRatingMapper.selectOne(new LambdaQueryWrapper<ShopRating>()
                .eq(ShopRating::getShopId, shopId)
                .eq(ShopRating::getUserId, userId));
        if (existing == null) {
            existing = new ShopRating();
            existing.setShopId(shopId);
            existing.setUserId(userId);
            existing.setScore(request.score());
            existing.setContent(request.content() != null ? request.content() : "");
            shopRatingMapper.insert(existing);
        } else {
            existing.setScore(request.score());
            existing.setContent(request.content() != null ? request.content() : "");
            shopRatingMapper.updateById(existing);
        }
        recomputeShopScore(shopId);
        return existing;
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
