package com.sky.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入的自动填充");

        this.strictInsertFill(metaObject, AutoFillConstant.CREATE_TIME, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, AutoFillConstant.CREATE_USER, BaseContext::getCurrentId, Long.class);

        this.strictInsertFill(metaObject, AutoFillConstant.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, AutoFillConstant.UPDATE_USER, BaseContext::getCurrentId, Long.class);

    }

    @Override
    public void updateFill(MetaObject metaObject) {

        log.info("修改的自动填充");
        this.strictUpdateFill(metaObject, AutoFillConstant.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, AutoFillConstant.UPDATE_USER, BaseContext::getCurrentId, Long.class);

    }
}
