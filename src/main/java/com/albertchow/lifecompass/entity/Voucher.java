package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A purchasable coupon offered by a shop. Money fields are in euro cents.
 * Merchants toggle {@code status} between on-shelf (1) and off-shelf (2).
 */
@Data
@TableName("voucher")
public class Voucher {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    private String title;

    private String subTitle;

    private String rules;

    /** Price paid by the user (euro cents). */
    private Long payValue;

    /** Face value (euro cents). */
    private Long actualValue;

    /** 0 regular, 1 limited (stock-controlled). */
    private Integer type;

    /** Remaining stock, for limited vouchers. */
    private Integer stock;

    /** 1 on-shelf, 2 off-shelf, 3 expired. */
    private Integer status;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
