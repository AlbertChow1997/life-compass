package com.albertchow.lifecompass.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Stamps createTime/updateTime on every insert and updateTime on every update,
 * so entities returned from a service already carry accurate timestamps
 * instead of relying on a follow-up SELECT to see the DB's CURRENT_TIMESTAMP default.
 */
@Component
public class TimestampMetaObjectHandler implements MetaObjectHandler {

    /** Called by MyBatis-Plus before every insert; fills createTime and updateTime with the current time. */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /** Called by MyBatis-Plus before every update; refreshes updateTime to the current time. */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
