package com.albertchow.lifecompass.mapper;

import com.albertchow.lifecompass.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * Sum of posts created per calendar day by {@code userId}, each day capped
     * at {@code cap} before summing — the experience-point formula's
     * anti-farming rule.
     */
    @Select("""
            SELECT COALESCE(SUM(LEAST(daily_count, #{cap})), 0) FROM (
                SELECT COUNT(*) AS daily_count
                FROM blog
                WHERE user_id = #{userId} AND status = 1
                GROUP BY DATE(create_time)
            ) t
            """)
    long sumCappedDailyCount(@Param("userId") Long userId, @Param("cap") int cap);

    /**
     * Every visible post's id/author/title by the given authors, newest first.
     * Callers (see {@code UserService.listDirectory}) keep only the first row
     * seen per author to get "their most recent post" — simplest correct way
     * to do a per-group "latest row" without a window function.
     */
    @Select("""
            <script>
            SELECT user_id AS userId, title
            FROM blog
            WHERE status = 1 AND user_id IN
            <foreach item="id" collection="userIds" open="(" separator="," close=")">
                #{id}
            </foreach>
            ORDER BY create_time DESC
            </script>
            """)
    List<Map<String, Object>> selectRecentTitlesByAuthors(@Param("userIds") Collection<Long> userIds);
}
