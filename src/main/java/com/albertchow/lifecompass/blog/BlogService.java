package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreatePostRequest;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.BlogMapper;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Requirement 4: user posts, optionally linking a shop. */
@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogMapper blogMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;

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

    public List<Blog> list(Boolean featuredOnly) {
        var query = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getStatus, 1)
                .orderByDesc(Blog::getFeatured)
                .orderByDesc(Blog::getCreateTime);
        if (Boolean.TRUE.equals(featuredOnly)) {
            query.eq(Blog::getFeatured, 1);
        }
        return enrich(blogMapper.selectList(query));
    }

    public Blog getById(Long id) {
        Blog blog = blogMapper.selectById(id);
        if (!isVisible(blog)) {
            throw new NotFoundException("Post not found");
        }
        return enrich(List.of(blog)).get(0);
    }

    private boolean isVisible(Blog blog) {
        return blog != null && blog.getStatus() != null && blog.getStatus() == 1;
    }

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
        return blogs;
    }
}
