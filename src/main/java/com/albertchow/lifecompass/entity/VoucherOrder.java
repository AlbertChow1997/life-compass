package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A user's purchase of a voucher.
 */
@Data
@TableName("voucher_order")
public class VoucherOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long voucherId;

    /** 1 card, 2 balance, 3 other. */
    private Integer payType;

    /** 1 unpaid, 2 paid, 3 used, 4 cancelled, 5 refunded. */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime useTime;

    private LocalDateTime refundTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ---- transient fields populated at query time (not persisted) ----

    /** Voucher title, joined for "my orders" responses. */
    @TableField(exist = false)
    private String voucherTitle;

    /** Owning shop's name, joined for "my orders" responses. */
    @TableField(exist = false)
    private String shopName;
}
