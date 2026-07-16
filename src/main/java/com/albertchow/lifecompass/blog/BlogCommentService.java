package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.CreateCommentRequest;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.entity.User;
import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogMapper blogMapper;
    private final UserMapper userMapper;

    public List<BlogComment> list(Long blogId) {
        var query = new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getBlogId, blogId)
                .eq(BlogComment::getStatus, 1)
                .orderByAsc(BlogComment::getCreateTime);
        return enrich(commentMapper.selectList(query));
    }

    @Transactional
    public BlogComment add(Long blogId, Long userId, CreateCommentRequest request) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getStatus() == null || blog.getStatus() == 0) {
            throw new NotFoundException("Post not found");
        }

        BlogComment comment = new BlogComment();
        comment.setUserId(userId);
        comment.setBlogId(blogId);
        comment.setParentId(request.parentId() != null ? request.parentId() : 0L);
        comment.setAnswerId(request.answerId() != null ? request.answerId() : 0L);
        comment.setContent(request.content());
        comment.setLiked(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setComments(blog.getComments() + 1);
        blogMapper.updateById(patch);

        return enrich(List.of(comment)).get(0);
    }

    private List<BlogComment> enrich(List<BlogComment> comments) {
        if (comments.isEmpty()) {
            return comments;
        }
        Set<Long> userIds = comments.stream().map(BlogComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> usersById = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (BlogComment comment : comments) {
            User author = usersById.get(comment.getUserId());
            if (author != null) {
                comment.setAuthorName(author.getNickName());
                comment.setAuthorIcon(author.getIcon());
            }
        }
        return comments;
    }
}
