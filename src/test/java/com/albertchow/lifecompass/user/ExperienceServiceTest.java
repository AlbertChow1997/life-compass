package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogLikeMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExperienceService#compute}, the derived-XP formula
 * described in the technical report's Section 2.3.3: 10 XP/post (capped at
 * 3/day), 2 XP/comment (capped at 10/day), 1 XP/like received (capped at
 * 20/day). The daily capping itself happens inside the mapper SQL, so
 * these tests mock the mapper's already-capped-and-summed return value and
 * assert only the weighting/aggregation this service is responsible for.
 */
@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private BlogMapper blogMapper;
    @Mock
    private BlogCommentMapper commentMapper;
    @Mock
    private BlogLikeMapper likeMapper;

    private ExperienceService experienceService;

    @BeforeEach
    void setUp() {
        experienceService = new ExperienceService(blogMapper, commentMapper, likeMapper);
    }

    @Test
    void compute_weightsAndSumsAllThreeSources() {
        when(blogMapper.sumCappedDailyCount(USER_ID, 3)).thenReturn(5L);       // capped days already summed
        when(commentMapper.sumCappedDailyCount(USER_ID, 10)).thenReturn(7L);
        when(likeMapper.sumCappedDailyLikesReceived(USER_ID, 20)).thenReturn(12L);

        long xp = experienceService.compute(USER_ID);

        // 5*10 + 7*2 + 12*1 = 50 + 14 + 12 = 76
        assertThat(xp).isEqualTo(76);
    }

    @Test
    void compute_returnsZero_whenTheUserHasNoActivityAtAll() {
        when(blogMapper.sumCappedDailyCount(USER_ID, 3)).thenReturn(0L);
        when(commentMapper.sumCappedDailyCount(USER_ID, 10)).thenReturn(0L);
        when(likeMapper.sumCappedDailyLikesReceived(USER_ID, 20)).thenReturn(0L);

        assertThat(experienceService.compute(USER_ID)).isZero();
    }

    @Test
    void compute_reachesTheProThreshold_atExactlyFiftyCappedPostDaysWorthOfXp() {
        // A concrete sanity check that PRO_THRESHOLD (500) is reachable and that the
        // "MAX" level shown in the frontend badge (see App.css/UserMenu.tsx) lines up
        // with a real combination of activity rather than an unreachable number.
        when(blogMapper.sumCappedDailyCount(USER_ID, 3)).thenReturn(50L);  // 50 * 10 = 500
        when(commentMapper.sumCappedDailyCount(USER_ID, 10)).thenReturn(0L);
        when(likeMapper.sumCappedDailyLikesReceived(USER_ID, 20)).thenReturn(0L);

        assertThat(experienceService.compute(USER_ID)).isEqualTo(ExperienceService.PRO_THRESHOLD);
    }
}
