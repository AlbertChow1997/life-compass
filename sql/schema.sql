-- =============================================================================
-- LifeCompass — Database Schema
-- Local entertainment & dining directory for the Irish market.
-- Engine: MySQL 8+, charset utf8mb4. All data stored/displayed in English.
--
-- Import order: run this file first, then data.sql for seed data.
--   mysql -u root -p lifecompass < sql/schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `lifecompass`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `lifecompass`;

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- user — accounts for regular users, merchants and admins.
-- Any role may register with email+password; regular users can additionally
-- sign in via Google (email/google_id) or Twilio SMS (phone).
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `phone`       VARCHAR(20)     DEFAULT NULL COMMENT 'Phone number (E.164, e.g. +353...) — Twilio SMS login',
    `email`       VARCHAR(128)    DEFAULT NULL COMMENT 'Email address — Google login / credential login',
    `google_id`   VARCHAR(64)     DEFAULT NULL COMMENT 'Google subject id (sub claim)',
    `password`    VARCHAR(128)    DEFAULT NULL COMMENT 'BCrypt hash — set for any self-registered account',
    `nick_name`   VARCHAR(32)     NOT NULL DEFAULT '' COMMENT 'Display name',
    `icon`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'Avatar URL',
    `city`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT 'City, collected at registration',
    `role`        VARCHAR(16)     NOT NULL DEFAULT 'USER' COMMENT 'USER | MERCHANT | ADMIN',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 active, 0 banned',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_google_id` (`google_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Users, merchants and admins';

-- -----------------------------------------------------------------------------
-- shop_type — business categories (Restaurant, Pub, Cinema, ...).
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `shop_type`;
CREATE TABLE `shop_type` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name`        VARCHAR(32)     NOT NULL COMMENT 'Category name',
    `icon`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'Category icon URL',
    `sort`        INT             NOT NULL DEFAULT 0 COMMENT 'Display order (ascending)',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Business categories';

-- -----------------------------------------------------------------------------
-- shop — a business listing. owner_id links to the MERCHANT user that manages it.
-- score is the average rating x10 (e.g. 46 => 4.6 stars) to avoid floats.
-- Money fields are stored in euro cents.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name`        VARCHAR(128)    NOT NULL COMMENT 'Shop name',
    `type_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> shop_type.id',
    `owner_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT 'FK -> user.id (managing merchant)',
    `images`      VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT 'Comma-separated image URLs',
    `area`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT 'City / district, e.g. Dublin, Cork',
    `address`     VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'Street address',
    `x`           DECIMAL(10, 7)  DEFAULT NULL COMMENT 'Longitude',
    `y`           DECIMAL(10, 7)  DEFAULT NULL COMMENT 'Latitude',
    `avg_price`   BIGINT          DEFAULT NULL COMMENT 'Average price per person (euro cents)',
    `sold`        INT             NOT NULL DEFAULT 0 COMMENT 'Vouchers sold',
    `comments`    INT             NOT NULL DEFAULT 0 COMMENT 'Number of ratings',
    `score`       INT             NOT NULL DEFAULT 0 COMMENT 'Average rating x10 (0..50)',
    `open_hours`  VARCHAR(64)     NOT NULL DEFAULT '' COMMENT 'Opening hours, e.g. 10:00-22:00',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type_id`),
    KEY `idx_owner` (`owner_id`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Business listings';

-- -----------------------------------------------------------------------------
-- shop_rating — a user's star rating (1..5) + optional text for a shop.
-- One rating per user per shop; shop.score/comments are derived from this table.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `shop_rating`;
CREATE TABLE `shop_rating` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `shop_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> shop.id',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> user.id',
    `score`       TINYINT         NOT NULL COMMENT 'Star rating 1..5',
    `content`     VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT 'Optional review text',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_user` (`shop_id`, `user_id`),
    KEY `idx_shop` (`shop_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'User star ratings for shops';

-- -----------------------------------------------------------------------------
-- blog — a user post. May optionally reference a shop (shop_id) as a "shop link".
-- featured flag is toggled by admins ("精选" / highlight). status supports soft delete.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> user.id (author)',
    `shop_id`     BIGINT UNSIGNED DEFAULT NULL COMMENT 'FK -> shop.id (optional linked shop)',
    `title`       VARCHAR(128)    NOT NULL COMMENT 'Post title',
    `images`      VARCHAR(2048)   NOT NULL DEFAULT '' COMMENT 'Comma-separated image URLs',
    `content`     VARCHAR(4096)   NOT NULL COMMENT 'Post body',
    `liked`       INT             NOT NULL DEFAULT 0 COMMENT 'Like count',
    `comments`    INT             NOT NULL DEFAULT 0 COMMENT 'Comment count',
    `featured`    TINYINT         NOT NULL DEFAULT 0 COMMENT '0 normal, 1 featured by admin',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 published, 0 deleted',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_shop` (`shop_id`),
    KEY `idx_featured` (`featured`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'User posts / recommendations';

-- -----------------------------------------------------------------------------
-- blog_comment — comments on a post, with optional threaded replies.
-- Admins can moderate via status.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> user.id (author)',
    `blog_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> blog.id',
    `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Top-level comment id, 0 if root',
    `answer_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Comment id being replied to, 0 if none',
    `content`     VARCHAR(1024)   NOT NULL COMMENT 'Comment text',
    `liked`       INT             NOT NULL DEFAULT 0 COMMENT 'Like count',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 visible, 0 deleted',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_blog` (`blog_id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Comments on posts';

-- -----------------------------------------------------------------------------
-- voucher — a purchasable coupon offered by a shop. Merchants toggle status
-- between on-shelf (1) and off-shelf (2). Money in euro cents.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `shop_id`      BIGINT UNSIGNED NOT NULL COMMENT 'FK -> shop.id',
    `title`        VARCHAR(128)    NOT NULL COMMENT 'Voucher title',
    `sub_title`    VARCHAR(255)    NOT NULL DEFAULT '' COMMENT 'Subtitle / tagline',
    `rules`        VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT 'Terms & conditions',
    `pay_value`    BIGINT          NOT NULL COMMENT 'Price paid by user (euro cents)',
    `actual_value` BIGINT          NOT NULL COMMENT 'Face value (euro cents)',
    `type`         TINYINT         NOT NULL DEFAULT 0 COMMENT '0 regular, 1 limited (stock-controlled)',
    `stock`        INT             NOT NULL DEFAULT 0 COMMENT 'Remaining stock (for limited vouchers)',
    `status`       TINYINT         NOT NULL DEFAULT 1 COMMENT '1 on-shelf, 2 off-shelf, 3 expired',
    `begin_time`   DATETIME        DEFAULT NULL COMMENT 'Redeemable from',
    `end_time`     DATETIME        DEFAULT NULL COMMENT 'Redeemable until',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_shop` (`shop_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Purchasable shop vouchers/coupons';

-- -----------------------------------------------------------------------------
-- voucher_order — a user's purchase of a voucher.
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `voucher_order`;
CREATE TABLE `voucher_order` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key / order id',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT 'FK -> user.id (buyer)',
    `voucher_id`  BIGINT UNSIGNED NOT NULL COMMENT 'FK -> voucher.id',
    `pay_type`    TINYINT         NOT NULL DEFAULT 1 COMMENT '1 card, 2 balance, 3 other',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 unpaid, 2 paid, 3 used, 4 cancelled, 5 refunded',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `pay_time`    DATETIME        DEFAULT NULL,
    `use_time`    DATETIME        DEFAULT NULL,
    `refund_time` DATETIME        DEFAULT NULL,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_voucher` (`voucher_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Voucher purchase orders';

SET FOREIGN_KEY_CHECKS = 1;
