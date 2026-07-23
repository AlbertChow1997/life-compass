package com.albertchow.lifecompass.mapper;

import com.albertchow.lifecompass.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
