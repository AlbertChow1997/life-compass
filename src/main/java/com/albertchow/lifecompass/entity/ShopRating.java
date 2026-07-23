package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A user's star rating (1..5) and optional review text for a shop. A user
 * may rate the same shop again after a cooldown, so this is a history of
 * ratings, not a single record per (shop, user).
 */
@Data
@TableName("shop_rating")
public class ShopRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private Long userId;

    /** Star rating 1..5. */
    private Integer score;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Shop name, joined for "my ratings" responses. */
    @TableField(exist = false)
    private String shopName;
}
