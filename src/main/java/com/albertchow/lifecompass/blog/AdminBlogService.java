package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Requirement 8: admin moderation of posts and comments. */
@Service
@RequiredArgsConstructor
public class AdminBlogService {

    private final BlogMapper blogMapper;
    private final BlogCommentMapper commentMapper;

    public void setFeatured(Long blogId, boolean featured) {
        requireBlog(blogId);
        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setFeatured(featured ? 1 : 0);
        blogMapper.updateById(patch);
    }

    public void deletePost(Long blogId) {
        requireBlog(blogId);
        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setStatus(0);
        blogMapper.updateById(patch);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        BlogComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new NotFoundException("Comment not found");
        }
        BlogComment patch = new BlogComment();
        patch.setId(commentId);
        patch.setStatus(0);
        commentMapper.updateById(patch);

        Blog blog = blogMapper.selectById(comment.getBlogId());
        if (blog != null) {
            Blog blogPatch = new Blog();
            blogPatch.setId(blog.getId());
            blogPatch.setComments(Math.max(0, blog.getComments() - 1));
            blogMapper.updateById(blogPatch);
        }
    }

    private Blog requireBlog(Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new NotFoundException("Post not found");
        }
        return blog;
    }
}
