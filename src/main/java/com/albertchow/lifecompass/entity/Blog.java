package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A user post. May optionally reference a shop ({@code shopId}) as a shop link.
 */
@Data
@TableName("blog")
public class Blog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK -&gt; user.id (author). */
    private Long userId;

    /** FK -&gt; shop.id, the optional linked shop (nullable). */
    private Long shopId;

    private String title;

    /** Comma-separated image URLs. */
    private String images;

    private String content;

    private Integer liked;

    private Integer comments;

    /** 0 normal, 1 featured by admin. */
    private Integer featured;

    /** 1 published, 0 deleted. */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ---- transient fields populated at query time (not persisted) ----

    /** Author display name, joined for list/detail responses. */
    @TableField(exist = false)
    private String authorName;

    /** Author avatar, joined for list/detail responses. */
    @TableField(exist = false)
    private String authorIcon;

    /** Whether the current viewer has liked this post. */
    @TableField(exist = false)
    private Boolean likedByCurrentUser;

    /** Whether the current viewer follows this post's author. */
    @TableField(exist = false)
    private Boolean authorFollowedByCurrentUser;
}
