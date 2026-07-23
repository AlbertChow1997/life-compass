package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreatePostRequest;
import com.albertchow.lifecompass.blog.dto.LikeResponse;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogLike;
import com.albertchow.lifecompass.entity.Follow;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.BlogLikeMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import com.albertchow.lifecompass.mapper.FollowMapper;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.albertchow.lifecompass.security.LoginUser;
import com.albertchow.lifecompass.security.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements blog post browsing, creation, and liking. A post can optionally
 * be linked to a shop. Also enriches posts with author details and
 * current-user-specific flags (liked / following author) before returning them.
 */
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogMapper blogMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final BlogLikeMapper likeMapper;
    private final FollowMapper followMapper;

    /** Creates a new post for the given user, validating the linked shop (if any) exists. */
    public Blog create(Long userId, CreatePostRequest request) {
        if (request.shopId() != null && shopMapper.selectById(request.shopId()) == null) {
            throw new NotFoundException("Linked shop not found");
        }
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setShopId(request.shopId());
        blog.setTitle(request.title());
        blog.setContent(request.content());
        blog.setImages(request.images() != null ? request.images() : "");
        blog.setLiked(0);
        blog.setComments(0);
        blog.setFeatured(0);
        blog.setStatus(1);
        blogMapper.insert(blog);
        return enrich(List.of(blog)).get(0);
    }

    /** Lists visible posts (featured ones first), optionally narrowed to featured-only or to authors the current user follows. */
    public List<Blog> list(Boolean featuredOnly, Boolean followedOnly) {
        var query = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, 1)
                .orderByDesc(Blog::getFeatured)
                .orderByDesc(Blog::getCreateTime);
        if (Boolean.TRUE.equals(featuredOnly)) {
            query.eq(Blog::getFeatured, 1);
        }
        if (Boolean.TRUE.equals(followedOnly)) {
            LoginUser loginUser = UserContext.get();
            if (loginUser == null) {
                return List.of();
            }
            List<Long> followedIds = followMapper.selectList(
                            new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, loginUser.id()))
                    .stream().map(Follow::getFollowUserId).toList();
            if (followedIds.isEmpty()) {
                return List.of();
            }
            query.in(Blog::getUserId, followedIds);
        }
        return enrich(blogMapper.selectList(query));
    }

    /** Fetches one post by ID, treating soft-deleted or missing posts as not found. */
    public Blog getById(Long id) {
        Blog blog = blogMapper.selectById(id);
        if (!isVisible(blog)) {
            throw new NotFoundException("Post not found");
        }
        return enrich(List.of(blog)).get(0);
    }

    /** Toggles the current user's like on a post and keeps blog.liked in sync with the real count. */
    @Transactional
    public LikeResponse toggleLike(Long blogId, Long userId) {
        if (!isVisible(blogMapper.selectById(blogId))) {
            throw new NotFoundException("Post not found");
        }
        BlogLike existing = likeMapper.selectOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId));
        boolean nowLiked = existing == null;
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
        } else {
            BlogLike like = new BlogLike();
            like.setBlogId(blogId);
            like.setUserId(userId);
            likeMapper.insert(like);
        }

        long count = likeMapper.selectCount(new LambdaQueryWrapper<BlogLike>().eq(BlogLike::getBlogId, blogId));
        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setLiked((int) count);
        blogMapper.updateById(patch);

        return new LikeResponse((int) count, nowLiked);
    }

    /** A post counts as visible only if it exists and hasn't been soft-deleted (status == 1). */
    private boolean isVisible(Blog blog) {
        return blog != null && blog.getStatus() != null && blog.getStatus() == 1;
    }

    /** Fills in each post's author name/avatar, and — if someone is logged in — whether they liked the post or follow its author. */
    private List<Blog> enrich(List<Blog> blogs) {
        if (blogs.isEmpty()) {
            return blogs;
        }
        Set<Long> userIds = blogs.stream().map(Blog::getUserId).collect(Collectors.toSet());
        Map<Long, User> usersById = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (Blog blog : blogs) {
            User author = usersById.get(blog.getUserId());
            if (author != null) {
                blog.setAuthorName(author.getNickName());
                blog.setAuthorIcon(author.getIcon());
            }
        }

        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            Set<Long> blogIds = blogs.stream().map(Blog::getId).collect(Collectors.toSet());
            Set<Long> likedBlogIds = likeMapper.selectList(new LambdaQueryWrapper<BlogLike>()
                            .eq(BlogLike::getUserId, loginUser.id())
                            .in(BlogLike::getBlogId, blogIds))
                    .stream().map(BlogLike::getBlogId).collect(Collectors.toSet());

            Set<Long> followedAuthorIds = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, loginUser.id())
                            .in(Follow::getFollowUserId, userIds))
                    .stream().map(Follow::getFollowUserId).collect(Collectors.toSet());

            for (Blog blog : blogs) {
                blog.setLikedByCurrentUser(likedBlogIds.contains(blog.getId()));
                blog.setAuthorFollowedByCurrentUser(followedAuthorIds.contains(blog.getUserId()));
            }
        }
        return blogs;
    }
}
