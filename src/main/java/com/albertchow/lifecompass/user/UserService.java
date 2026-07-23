package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.entity.BlogLike;
import com.albertchow.lifecompass.entity.Follow;
import com.albertchow.lifecompass.entity.Shop;
import com.albertchow.lifecompass.entity.ShopFollow;
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
import com.albertchow.lifecompass.user.dto.UpdateProfileRequest;
import com.albertchow.lifecompass.user.dto.UserStatsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The "personal center": stats, profile editing, and every "my X" list. */
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

    public UserStatsResponse getStats(Long userId) {
        long following = followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
        long followers = followMapper.selectCount(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowUserId, userId));
        long experience = experienceService.compute(userId);
        return new UserStatsResponse(following, followers, experience, ExperienceService.PRO_THRESHOLD);
    }

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

    public List<Shop> listFollowedShops(Long userId) {
        List<Long> shopIds = shopFollowMapper.selectList(new LambdaQueryWrapper<ShopFollow>().eq(ShopFollow::getUserId, userId))
                .stream().map(ShopFollow::getShopId).toList();
        if (shopIds.isEmpty()) {
            return List.of();
        }
        return shopMapper.selectByIds(shopIds);
    }

    public List<Blog> listMyPosts(Long userId) {
        var query = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, 1)
                .orderByDesc(Blog::getCreateTime);
        return blogMapper.selectList(query);
    }

    public List<BlogComment> listMyComments(Long userId) {
        var query = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getUserId, userId)
                .eq(BlogComment::getStatus, 1)
                .orderByDesc(BlogComment::getCreateTime);
        return commentMapper.selectList(query);
    }

    public List<Blog> listMyLikes(Long userId) {
        List<Long> blogIds = likeMapper.selectList(new LambdaQueryWrapper<BlogLike>().eq(BlogLike::getUserId, userId))
                .stream().map(BlogLike::getBlogId).toList();
        if (blogIds.isEmpty()) {
            return List.of();
        }
        return blogMapper.selectByIds(blogIds);
    }

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
}
