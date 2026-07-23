package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.common.exception.BusinessException;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.entity.BlogLike;
import com.albertchow.lifecompass.entity.Follow;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.ShopFollow;
import com.albertchow.lifecompass.entity.ShopRating;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.entity.Voucher;
import com.albertchow.lifecompass.entity.VoucherOrder;
import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogLikeMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import com.albertchow.lifecompass.mapper.FollowMapper;
import com.albertchow.lifecompass.mapper.ShopFollowMapper;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.albertchow.lifecompass.mapper.VoucherMapper;
import com.albertchow.lifecompass.mapper.VoucherOrderMapper;
import com.albertchow.lifecompass.shop.ShopRatingService;
import com.albertchow.lifecompass.user.dto.UpdateProfileRequest;
import com.albertchow.lifecompass.user.dto.UserStatsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Backs the "personal center" screens: user stats, profile editing, every
 * "my X" activity list (shops, posts, comments, likes, orders, ratings),
 * and user-to-user follow relationships.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final ShopFollowMapper shopFollowMapper;
    private final ShopMapper shopMapper;
    private final BlogMapper blogMapper;
    private final BlogCommentMapper commentMapper;
    private final BlogLikeMapper likeMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final VoucherMapper voucherMapper;
    private final ExperienceService experienceService;
    private final ShopRatingService shopRatingService;

    /** Assembles a user's follower/following counts plus their live-computed XP and the XP threshold for the "PRO" badge. */
    public UserStatsResponse getStats(Long userId) {
        long following = followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
        long followers = followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowUserId, userId));
        long experience = experienceService.compute(userId);
        return new UserStatsResponse(following, followers, experience, ExperienceService.PRO_THRESHOLD);
    }

    /** Updates nickname and city (always) and avatar icon (only if a new one was provided). */
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        user.setNickName(request.nickName());
        user.setCity(request.city() != null ? request.city() : "");
        if (request.icon() != null) {
            user.setIcon(request.icon());
        }
        userMapper.updateById(user);
        return user;
    }

    /** Fetches the shops the user follows/has saved. */
    public List<Shop> listFollowedShops(Long userId) {
        List<Long> shopIds = shopFollowMapper.selectList(new LambdaQueryWrapper<ShopFollow>().eq(ShopFollow::getUserId, userId))
                .stream().map(ShopFollow::getShopId).toList();
        if (shopIds.isEmpty()) {
            return List.of();
        }
        return shopMapper.selectByIds(shopIds);
    }

    /** Lists the user's own visible (non-deleted) posts, newest first. */
    public List<Blog> listMyPosts(Long userId) {
        var query = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, 1)
                .orderByDesc(Blog::getCreateTime);
        return blogMapper.selectList(query);
    }

    /** Lists the user's own visible (non-deleted) comments, newest first. */
    public List<BlogComment> listMyComments(Long userId) {
        var query = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getUserId, userId)
                .eq(BlogComment::getStatus, 1)
                .orderByDesc(BlogComment::getCreateTime);
        return commentMapper.selectList(query);
    }

    /** Fetches the posts the user has liked. */
    public List<Blog> listMyLikes(Long userId) {
        List<Long> blogIds = likeMapper.selectList(new LambdaQueryWrapper<BlogLike>().eq(BlogLike::getUserId, userId))
                .stream().map(BlogLike::getBlogId).toList();
        if (blogIds.isEmpty()) {
            return List.of();
        }
        return blogMapper.selectByIds(blogIds);
    }

    /** Lists the user's voucher orders, newest first, with each order's voucher title and shop name filled in. */
    public List<VoucherOrder> listMyOrders(Long userId) {
        var query = new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .orderByDesc(VoucherOrder::getCreateTime);
        List<VoucherOrder> orders = voucherOrderMapper.selectList(query);
        if (orders.isEmpty()) {
            return orders;
        }

        List<Long> voucherIds = orders.stream().map(VoucherOrder::getVoucherId).distinct().toList();
        Map<Long, Voucher> vouchersById = voucherMapper.selectByIds(voucherIds).stream()
                .collect(Collectors.toMap(Voucher::getId, v -> v));

        List<Long> shopIds = vouchersById.values().stream().map(Voucher::getShopId).distinct().toList();
        Map<Long, Shop> shopsById = shopMapper.selectByIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s));

        for (VoucherOrder order : orders) {
            Voucher voucher = vouchersById.get(order.getVoucherId());
            if (voucher != null) {
                order.setVoucherTitle(voucher.getTitle());
                Shop shop = shopsById.get(voucher.getShopId());
                if (shop != null) {
                    order.setShopName(shop.getName());
                }
            }
        }
        return orders;
    }

    /** Delegates to {@link ShopRatingService} to fetch the user's own shop ratings. */
    public List<ShopRating> listMyRatings(Long userId) {
        return shopRatingService.listMine(userId);
    }

    /** Delegates to {@link ShopRatingService} to delete one of the user's own ratings. */
    public void deleteRating(Long ratingId, Long userId) {
        shopRatingService.deleteMine(ratingId, userId);
    }

    /** Makes one user follow another, after rejecting self-follows and confirming the target account exists. */
    public void followUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException("You can't follow yourself");
        }
        if (userMapper.selectById(targetUserId) == null) {
            throw new NotFoundException("User not found");
        }
        boolean alreadyFollowing = followMapper.exists(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, targetUserId));
        if (alreadyFollowing) {
            return;
        }
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        followMapper.insert(follow);
    }

    /** Removes the follow relationship between two users, if it exists. */
    public void unfollowUser(Long userId, Long targetUserId) {
        followMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, targetUserId));
    }

    /** Checks whether one user follows another. */
    public boolean isFollowingUser(Long userId, Long targetUserId) {
        return followMapper.exists(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, targetUserId));
    }
}
