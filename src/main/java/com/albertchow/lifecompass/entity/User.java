package com.albertchow.lifecompass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * An account: regular user (Google/SMS login), merchant, or admin.
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** E.164 phone number for Twilio SMS login. */
    private String phone;

    /** Email address for Google login. */
    private String email;

    /** Google subject id (sub claim). */
    private String googleId;

    /** BCrypt hash for merchant/admin credential login; never serialized. */
    @JsonIgnore
    private String password;

    private String nickName;

    private String icon;

    /** USER | MERCHANT | ADMIN. */
    private String role;

    /** 1 active, 0 banned. */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
