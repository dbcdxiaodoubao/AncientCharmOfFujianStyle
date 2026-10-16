-- Core tables are included so an empty database can start without a private SQL dump.
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(80) NOT NULL,
    password VARCHAR(100) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_sys_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fyinfo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    dtl TEXT,
    picture_url VARCHAR(512),
    city BIGINT NOT NULL,
    level BIGINT,
    type VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS check_in (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    fy_id BIGINT NOT NULL,
    picture_url VARCHAR(512) NOT NULL,
    txt VARCHAR(1000),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_check_in_user (user_id),
    KEY idx_check_in_fy (fy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auth_session (
    token_hash CHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    PRIMARY KEY (token_hash),
    KEY idx_auth_session_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS check_in_like (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞id',
    checkin_id BIGINT NOT NULL COMMENT '打卡记录id',
    user_id BIGINT NOT NULL COMMENT '点赞用户id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_check_in_like (checkin_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡点赞';

CREATE TABLE IF NOT EXISTS check_in_comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论id',
    checkin_id BIGINT NOT NULL COMMENT '打卡记录id',
    user_id BIGINT NOT NULL COMMENT '评论用户id',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (id),
    KEY idx_check_in_comment (checkin_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡评论';
