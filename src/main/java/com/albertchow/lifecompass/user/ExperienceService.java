package com.albertchow.lifecompass.user;

import com.albertchow.lifecompass.mapper.BlogCommentMapper;
import com.albertchow.lifecompass.mapper.BlogLikeMapper;
import com.albertchow.lifecompass.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Experience points: derived live from posts/comments/likes-received, each
 * capped per calendar day before being weighted and summed, so the number
 * can't be farmed by spamming one action. Nothing is cached — recomputed on
 * every read, which keeps it always correct with no sync bugs.
 */
@Service
@RequiredArgsConstructor
public class ExperienceService {

    private static final int POST_DAILY_CAP = 3;
    private static final int COMMENT_DAILY_CAP = 10;
    private static final int LIKE_RECEIVED_DAILY_CAP = 20;

    private static final int XP_PER_POST = 10;
    private static final int XP_PER_COMMENT = 2;
    private static final int XP_PER_LIKE_RECEIVED = 1;

    /** XP at or above this shows a colorful "PRO" badge; below it, a gray one. */
    public static final long PRO_THRESHOLD = 500;

    private final BlogMapper blogMapper;
    private final BlogCommentMapper commentMapper;
    private final BlogLikeMapper likeMapper;

    public long compute(Long userId) {
        long fromPosts = blogMapper.sumCappedDailyCount(userId, POST_DAILY_CAP) * XP_PER_POST;
        long fromComments = commentMapper.sumCappedDailyCount(userId, COMMENT_DAILY_CAP) * XP_PER_COMMENT;
        long fromLikes = likeMapper.sumCappedDailyLikesReceived(userId, LIKE_RECEIVED_DAILY_CAP) * XP_PER_LIKE_RECEIVED;
        return fromPosts + fromComments + fromLikes;
    }
}
