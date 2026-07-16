package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A comment on a post, with optional threaded replies.
 */
@Data
@TableName("blog_comment")
public class BlogComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long blogId;

    /** Top-level comment id, 0 if this is a root comment. */
    private Long parentId;

    /** Comment id being replied to, 0 if none. */
    private Long answerId;

    private String content;

    private Integer liked;

    /** 1 visible, 0 deleted. */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Commenter display name, joined at query time (not persisted). */
    @TableField(exist = false)
    private String authorName;

    /** Commenter avatar, joined at query time (not persisted). */
    @TableField(exist = false)
    private String authorIcon;
}
