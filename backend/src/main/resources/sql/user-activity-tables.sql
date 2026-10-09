CREATE TABLE IF NOT EXISTS user_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录id',
    user_id BIGINT NOT NULL COMMENT '用户id',
    fy_id BIGINT NOT NULL COMMENT '非遗项目id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_favorite_user_fy (user_id, fy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏记录';

CREATE TABLE IF NOT EXISTS user_preference (
    user_id BIGINT NOT NULL COMMENT '用户id',
    city BIGINT DEFAULT NULL COMMENT '偏好城市',
    category VARCHAR(255) DEFAULT NULL COMMENT '偏好非遗类别',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好';

CREATE TABLE IF NOT EXISTS user_browse_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '浏览记录id',
    user_id BIGINT NOT NULL COMMENT '用户id',
    fy_id BIGINT NOT NULL COMMENT '非遗项目id',
    browse_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (id),
    KEY idx_user_browse_history_user_time (user_id, browse_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览足迹';
