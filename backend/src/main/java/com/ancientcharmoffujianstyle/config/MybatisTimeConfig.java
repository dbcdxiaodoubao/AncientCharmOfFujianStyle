package com.ancientcharmoffujianstyle.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 古韵闽风 - MyBatis自动时间填充处理器
 * 在非遗数据和用户记录插入/更新时自动维护createTime和updateTime字段。
 * 确保打卡记录和非遗项目的时间戳在服务端统一生成，不依赖数据库默认值。
 */
@Component
public class MybatisTimeConfig implements MetaObjectHandler {

    private static final String FIELD_CREATE_TIME = "createTime";
    private static final String FIELD_UPDATE_TIME = "updateTime";
    private static final String FIELD_BROWSE_TIME = "browseTime";

    /**
     * 新增记录时同时填充创建时间和更新时间
     * 适用于非遗项目录入、用户注册、打卡上传等场景
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, FIELD_BROWSE_TIME, LocalDateTime.class, now);
    }

    /**
     * 更新记录时仅刷新更新时间
     * 适用于非遗信息编辑、用户资料修改等场景
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, FIELD_UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
