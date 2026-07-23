package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.ShopFollow;
import com.albertchow.lifecompass.mapper.ShopFollowMapper;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.shop.dto.ShopUpsertRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles shop listings: searching/browsing, admin create/update, and
 * users following (saving) shops. Ratings are handled separately by
 * {@link ShopRatingService}.
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopMapper shopMapper;
    private final ShopFollowMapper shopFollowMapper;

    /** Searches shops, optionally filtered by category (typeId) and/or a name keyword, ranked by score. */
    public List<Shop> search(Long typeId, String name) {
        LambdaQueryWrapper<Shop> query = new LambdaQueryWrapper<>();
        if (typeId != null) {
            query.eq(Shop::getTypeId, typeId);
        }
        if (name != null && !name.isBlank()) {
            query.like(Shop::getName, name.trim());
        }
        query.orderByDesc(Shop::getScore);
        return shopMapper.selectList(query);
    }

    /** Fetches a shop by ID, or throws NotFoundException if it doesn't exist. */
    public Shop getById(Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new NotFoundException("Shop not found");
        }
        return shop;
    }

    /** Creates a new shop listing with its sold/comment/score counters reset to zero. */
    public Shop create(ShopUpsertRequest request) {
        Shop shop = new Shop();
        applyRequest(shop, request);
        shop.setSold(0);
        shop.setComments(0);
        shop.setScore(0);
        shopMapper.insert(shop);
        return shop;
    }

    /** Updates an existing shop's editable fields, leaving its sold/comment/score counters untouched. */
    public Shop update(Long id, ShopUpsertRequest request) {
        Shop shop = getById(id);
        applyRequest(shop, request);
        shopMapper.updateById(shop);
        return shop;
    }

    /** Checks whether the given user (if any) follows/has saved this shop. */
    public boolean isFollowedBy(Long shopId, Long userId) {
        if (userId == null) {
            return false;
        }
        return shopFollowMapper.exists(new LambdaQueryWrapper<ShopFollow>()
                .eq(ShopFollow::getShopId, shopId)
                .eq(ShopFollow::getUserId, userId));
    }

    /** Adds the shop to the user's followed list, unless they already follow it. */
    public void follow(Long shopId, Long userId) {
        getById(shopId);
        if (isFollowedBy(shopId, userId)) {
            return;
        }
        ShopFollow follow = new ShopFollow();
        follow.setShopId(shopId);
        follow.setUserId(userId);
        shopFollowMapper.insert(follow);
    }

    /** Removes the shop from the user's followed list, if present. */
    public void unfollow(Long shopId, Long userId) {
        shopFollowMapper.delete(new LambdaQueryWrapper<ShopFollow>()
                .eq(ShopFollow::getShopId, shopId)
                .eq(ShopFollow::getUserId, userId));
    }

    /** Copies the editable fields from the request DTO onto the entity, defaulting nullable text fields to empty strings. */
    private void applyRequest(Shop shop, ShopUpsertRequest request) {
        shop.setName(request.name());
        shop.setTypeId(request.typeId());
        shop.setOwnerId(request.ownerId());
        shop.setImages(request.images() != null ? request.images() : "");
        shop.setArea(request.area() != null ? request.area() : "");
        shop.setAddress(request.address() != null ? request.address() : "");
        shop.setX(request.x());
        shop.setY(request.y());
        shop.setAvgPrice(request.avgPrice());
        shop.setOpenHours(request.openHours() != null ? request.openHours() : "");
    }
}
