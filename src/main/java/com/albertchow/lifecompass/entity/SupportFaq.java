package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * An admin-managed keyword -> auto-reply entry for the support widget.
 * A user's question is matched against {@code keywords} (comma-separated,
 * case-insensitive substring match).
 */
@Data
@TableName("support_faq")
public class SupportFaq {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keywords;

    private String answer;

    /** 1 active, 0 inactive. */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
