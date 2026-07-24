package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A question asked via the support widget, logged whether or not a keyword
 * matched — lets admins see what customers are actually asking, and is the
 * natural place to plug in an AI responder later.
 */
@Data
@TableName("support_message")
public class SupportMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK -> user.id, null if asked while signed out. */
    private Long userId;

    private String question;

    /** FK -> support_faq.id, null if no keyword matched. */
    private Long matchedFaqId;

    /** Snapshot of the answer shown, if any. */
    private String answerGiven;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
