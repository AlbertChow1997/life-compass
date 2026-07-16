package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A business listing. Money fields are in euro cents; {@code score} is the
 * average rating x10 (e.g. 46 = 4.6 stars).
 */
@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** FK -&gt; shop_type.id. */
    private Long typeId;

    /** FK -&gt; user.id, the managing merchant (nullable). */
    private Long ownerId;

    /** Comma-separated image URLs. */
    private String images;

    /** City / district, e.g. Dublin, Cork. */
    private String area;

    private String address;

    /** Longitude. */
    private BigDecimal x;

    /** Latitude. */
    private BigDecimal y;

    /** Average price per person (euro cents). */
    private Long avgPrice;

    private Integer sold;

    private Integer comments;

    /** Average rating x10 (0..50). */
    private Integer score;

    private String openHours;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
