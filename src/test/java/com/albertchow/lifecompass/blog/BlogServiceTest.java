package com.albertchow.lifecompass.blog;

import com.albertchow.lifecompass.blog.dto.LikeResponse;
import com.albertchow.lifecompass.common.exception.NotFoundException;
import com.albertchow.lifecompass.entity.Blog;
import com.albertchow.lifecompass.entity.BlogLike;
import com.albertchow.lifecompass.mapper.BlogLikeMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import com.albertchow.lifecompass.mapper.FollowMapper;
import com.albertchow.lifecompass.mapper.ShopMapper;
import com.albertchow.lifecompass.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression tests for the like-count bug fixed earlier in this project:
 * seed posts were given a decorative baseline "liked" count with no
 * backing blog_like rows, so the very first real like recomputed the
 * count from scratch and reset it to 1 instead of adding to the
 * baseline. {@link BlogService#toggleLike} now stores that baseline
 * separately as {@code likedBase} and adds the real row count on top of
 * it — these tests assert that behaviour directly, plus the ordinary
 * not-found/soft-deleted rejection path.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BlogServiceTest {

    @Mock
    private BlogMapper blogMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private BlogLikeMapper likeMapper;
    @Mock
    private FollowMapper followMapper;

    private BlogService blogService;

    @BeforeEach
    void setUp() {
        blogService = new BlogService(blogMapper, userMapper, shopMapper, likeMapper, followMapper);
    }

    private Blog visiblePost(int likedBase) {
        Blog blog = new Blog();
        blog.setId(1L);
        blog.setStatus(1);
        blog.setLikedBase(likedBase);
        return blog;
    }

    @Test
    void toggleLike_addsOnTopOfTheSeededBaseline_insteadOfResettingToOne() {
        // Regression test: this post was seeded with 24 decorative "liked" but zero
        // real blog_like rows, exactly the scenario that used to reset the count to 1.
        when(blogMapper.selectById(1L)).thenReturn(visiblePost(24));
        when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // not yet liked
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L); // one real like now exists

        LikeResponse response = blogService.toggleLike(1L, 99L);

        assertThat(response.liked()).isEqualTo(25); // 24 (base) + 1 (real), NOT reset to 1
        assertThat(response.likedByMe()).isTrue();
        verify(likeMapper).insert(any(BlogLike.class));

        ArgumentCaptor<Blog> captor = ArgumentCaptor.forClass(Blog.class);
        verify(blogMapper).updateById(captor.capture());
        assertThat(captor.getValue().getLiked()).isEqualTo(25);
    }

    @Test
    void toggleLike_removesTheLike_andKeepsTheBaseline() {
        BlogLike existing = new BlogLike();
        existing.setId(555L);
        when(blogMapper.selectById(1L)).thenReturn(visiblePost(24));
        when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // back to zero real likes

        LikeResponse response = blogService.toggleLike(1L, 99L);

        assertThat(response.liked()).isEqualTo(24); // base preserved, not zeroed out
        assertThat(response.likedByMe()).isFalse();
        verify(likeMapper).deleteById(555L);
    }

    @Test
    void toggleLike_treatsANullLikedBase_asZero() {
        Blog blog = visiblePost(0);
        blog.setLikedBase(null); // e.g. a row from before the likedBase column existed
        when(blogMapper.selectById(1L)).thenReturn(blog);
        when(likeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        LikeResponse response = blogService.toggleLike(1L, 99L);

        assertThat(response.liked()).isEqualTo(1);
    }

    @Test
    void toggleLike_throwsNotFound_whenThePostDoesNotExist() {
        when(blogMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> blogService.toggleLike(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }

    @Test
    void toggleLike_throwsNotFound_whenThePostIsSoftDeleted() {
        Blog deleted = visiblePost(24);
        deleted.setStatus(0);
        when(blogMapper.selectById(1L)).thenReturn(deleted);

        assertThatThrownBy(() -> blogService.toggleLike(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Post not found");
    }
}
