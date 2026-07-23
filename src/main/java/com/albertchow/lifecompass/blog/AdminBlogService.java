package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogComment;
import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the admin moderation actions for the blog: featuring posts,
 * taking down posts, and deleting comments. Deletions here are soft deletes
 * (status flipped to 0) rather than removing rows from the database.
 */
@Service
@RequiredArgsConstructor
public class AdminBlogService {

    private final BlogMapper blogMapper;
    private final BlogCommentMapper commentMapper;

    /** Toggles whether a post is featured; throws if the post doesn't exist. */
    public void setFeatured(Long blogId, boolean featured) {
        requireBlog(blogId);
        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setFeatured(featured ? 1 : 0);
        blogMapper.updateById(patch);
    }

    /** Soft-deletes a post by flipping its status to 0 instead of removing the row. */
    public void deletePost(Long blogId) {
        requireBlog(blogId);
        Blog patch = new Blog();
        patch.setId(blogId);
        patch.setStatus(0);
        blogMapper.updateById(patch);
    }

    /** Soft-deletes a comment and decrements the parent post's comment counter (never below zero). */
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

    /** Fetches a post by ID or throws NotFoundException if it doesn't exist. */
    private Blog requireBlog(Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new NotFoundException("Post not found");
        }
        return blog;
    }
}
