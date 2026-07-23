package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** A user following another user. */
@Data
@TableName("follow")
public class Follow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK -&gt; user.id, the follower. */
    private Long userId;

    /** FK -&gt; user.id, the user being followed. */
    private Long followUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
