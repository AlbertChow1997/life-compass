package com.albertchow.lifecompass.mapper;

import com.albertchow.lifecompass.entity.BlogLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BlogLikeMapper extends BaseMapper<BlogLike> {

    /**
     * Sum of likes received per calendar day by {@code authorUserId}'s posts,
     * each day capped at {@code cap} before summing — the experience-point
     * formula's anti-farming rule.
     */
    @Select("""
            SELECT COALESCE(SUM(LEAST(daily_count, #{cap})), 0) FROM (
                SELECT COUNT(*) AS daily_count
                FROM blog_like bl
                JOIN blog b ON bl.blog_id = b.id
                WHERE b.user_id = #{authorUserId}
                GROUP BY DATE(bl.create_time)
            ) t
            """)
    long sumCappedDailyLikesReceived(@Param("authorUserId") Long authorUserId, @Param("cap") int cap);
}
