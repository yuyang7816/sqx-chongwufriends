-- =============================================================================
-- 宠物业务完整初始化脚本（MySQL 5.7 兼容）
-- 包含：框架表（sys_* / tb_user / verify_code / sms_template 等）、
--       业务表结构、V2 扩展、管理端菜单、系统配置、业务与测试数据
--
-- 适用场景：全新空库一键初始化 / 开发测试环境重复重置
-- 可重复执行：固定 ID 数据先删后插；表结构 IF NOT EXISTS；列扩展走存储过程
--
-- 使用前请先选择或创建数据库，例如：
--   CREATE DATABASE IF NOT EXISTS pet_app DEFAULT CHARSET utf8mb4;
--   USE pet_app;
--
-- 第零部分框架表清单：
--   tb_user, sys_user, sys_role, sys_user_role, sys_menu, sys_role_menu, sys_config,
--   sys_captcha, sys_log, sys_dict, sys_user_token,
--   verify_code, sms_template, messages, user_wallet, referral_user_commission
-- 未纳入（旧羽毛球/会员业务，宠物版暂不需要）：banner, vip_*, withdraw_*, referral_commission 等
--

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 第零部分：基础框架表（空库可独立运行）
-- =============================================================================

CREATE TABLE IF NOT EXISTS tb_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(64) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    avatar VARCHAR(512) DEFAULT NULL,
    sex TINYINT DEFAULT NULL,
    age INT DEFAULT NULL,
    birthday VARCHAR(16) DEFAULT NULL COMMENT '生日 yyyy-MM-dd',
    wechat VARCHAR(64) DEFAULT NULL COMMENT '微信号',
    ma_open_id VARCHAR(64) DEFAULT NULL,
    mp_open_id VARCHAR(64) DEFAULT NULL,
    password VARCHAR(128) DEFAULT NULL,
    apple_id VARCHAR(64) DEFAULT NULL,
    sys_phone TINYINT DEFAULT NULL,
    status TINYINT DEFAULT 1,
    platform VARCHAR(32) DEFAULT NULL,
    cid VARCHAR(64) DEFAULT NULL,
    user_type TINYINT DEFAULT 1,
    is_vip TINYINT DEFAULT 0,
    invitation_code VARCHAR(32) DEFAULT NULL,
    superior_invitation_code VARCHAR(32) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App用户';

CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id BIGINT NOT NULL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    name VARCHAR(64) DEFAULT NULL,
    url VARCHAR(256) DEFAULT NULL,
    perms VARCHAR(256) DEFAULT NULL,
    type INT DEFAULT NULL,
    icon VARCHAR(64) DEFAULT NULL,
    order_num INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    param_key VARCHAR(128) NOT NULL,
    param_value TEXT,
    status VARCHAR(8) DEFAULT '1',
    remark VARCHAR(256) DEFAULT NULL,
    param_type VARCHAR(64) DEFAULT NULL,
    param_type_desc VARCHAR(64) DEFAULT NULL,
    UNIQUE KEY uk_param_key (param_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT NOT NULL PRIMARY KEY,
    role_name VARCHAR(64) DEFAULT NULL,
    remark VARCHAR(256) DEFAULT NULL,
    create_user_id BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单';

CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) DEFAULT NULL,
    salt VARCHAR(32) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    mobile VARCHAR(20) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_user_id BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT NULL,
    inviter_code VARCHAR(32) DEFAULT NULL,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

CREATE TABLE IF NOT EXISTS sys_captcha (
    uuid VARCHAR(64) NOT NULL PRIMARY KEY,
    code VARCHAR(16) DEFAULT NULL,
    expire_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图形验证码';

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) DEFAULT NULL,
    operation VARCHAR(128) DEFAULT NULL,
    method VARCHAR(256) DEFAULT NULL,
    params TEXT,
    time BIGINT DEFAULT NULL COMMENT '耗时ms',
    ip VARCHAR(64) DEFAULT NULL,
    create_date DATETIME DEFAULT NULL,
    KEY idx_create_date (create_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(128) NOT NULL,
    code VARCHAR(128) NOT NULL,
    value VARCHAR(512) NOT NULL,
    order_num INT DEFAULT 0,
    remark VARCHAR(256) DEFAULT NULL,
    del_flag TINYINT DEFAULT 0 COMMENT '0正常 -1删除',
    KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典';

CREATE TABLE IF NOT EXISTS sys_user_token (
    user_id BIGINT NOT NULL PRIMARY KEY,
    token VARCHAR(128) DEFAULT NULL,
    expire_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户Token';

CREATE TABLE IF NOT EXISTS verify_code (
    id BIGINT NOT NULL PRIMARY KEY,
    phone VARCHAR(64) DEFAULT NULL,
    code VARCHAR(16) DEFAULT NULL,
    status TINYINT DEFAULT 1 COMMENT '1未使用 2已使用',
    expire_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信/邮箱验证码';

CREATE TABLE IF NOT EXISTS sms_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_code VARCHAR(64) NOT NULL,
    content VARCHAR(512) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_biz_code (biz_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信模板';

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) DEFAULT NULL,
    content TEXT,
    image_urls VARCHAR(512) DEFAULT NULL,
    url VARCHAR(512) DEFAULT NULL,
    type TINYINT DEFAULT 1 COMMENT '1系统公告 2用户消息',
    user_id BIGINT DEFAULT NULL,
    data_id BIGINT DEFAULT NULL,
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';

CREATE TABLE IF NOT EXISTS user_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包';

CREATE TABLE IF NOT EXISTS referral_user_commission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    commission DECIMAL(10,2) DEFAULT 0,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户佣金汇总';

INSERT IGNORE INTO sys_role (role_id, role_name, remark, create_user_id, create_time) VALUES
(1, '超级管理员', '拥有全部权限', 1, NOW());

-- admin / admin（Shiro SHA256，salt=YzcmCZNvbXhcTaQ37Uhp8OA）
INSERT IGNORE INTO sys_user (user_id, username, password, salt, email, mobile, status, create_user_id, create_time) VALUES
(1, 'admin', '79248caa45e60d46d0e628edd1050bc4297d05bdaf387a08276b58d35f0e5cbb', 'YzcmCZNvbXhcTaQ37Uhp8OA', 'admin@localhost', '13900000000', 1, 1, NOW());

INSERT IGNORE INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 短信模板（login/register/reset，content 格式：String.format(template, 系统名, 验证码)）
DELETE FROM sms_template WHERE biz_code IN ('login', 'register', 'reset');
INSERT INTO sms_template (biz_code, content) VALUES
('login', '【%s】您的登录验证码为%s，10分钟内有效。'),
('register', '【%s】您的注册验证码为%s，10分钟内有效。'),
('reset', '【%s】您的重置密码验证码为%s，10分钟内有效。');

-- 系统字典示例（可按需扩展）
DELETE FROM sys_dict WHERE type = 'pet_common' AND code IN ('pet_type_dog', 'pet_type_cat');
INSERT INTO sys_dict (name, type, code, value, order_num, remark, del_flag) VALUES
('宠物类型-狗', 'pet_common', 'pet_type_dog', 'dog', 1, '宠物类型', 0),
('宠物类型-猫', 'pet_common', 'pet_type_cat', 'cat', 2, '宠物类型', 0);

-- =============================================================================
-- 第一部分：基础表结构
-- =============================================================================

CREATE TABLE IF NOT EXISTS pet_city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT '城市名称',
    code VARCHAR(32) DEFAULT NULL COMMENT '城市编码',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市';

CREATE TABLE IF NOT EXISTS pet_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT DEFAULT NULL,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    tone VARCHAR(16) DEFAULT '#f6d57c' COMMENT '展示色',
    cover_url VARCHAR(512) DEFAULT NULL,
    business_status TINYINT DEFAULT 1 COMMENT '1营业 0休息',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_city (city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店';

CREATE TABLE IF NOT EXISTS pet_archive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    pet_type VARCHAR(16) DEFAULT 'dog' COMMENT 'dog/cat',
    breed VARCHAR(64) DEFAULT NULL,
    gender VARCHAR(8) DEFAULT NULL,
    age_desc VARCHAR(32) DEFAULT NULL,
    weight_desc VARCHAR(32) DEFAULT NULL,
    pet_code VARCHAR(32) DEFAULT NULL,
    level_desc VARCHAR(64) DEFAULT NULL,
    status_label VARCHAR(64) DEFAULT NULL,
    description TEXT,
    good_at VARCHAR(512) DEFAULT NULL COMMENT '擅长JSON',
    avoid_items VARCHAR(512) DEFAULT NULL COMMENT '忌讳JSON',
    tags VARCHAR(512) DEFAULT NULL COMMENT '标签JSON',
    avatar_url VARCHAR(512) DEFAULT NULL,
    like_count INT DEFAULT 0,
    adoptable TINYINT DEFAULT 0 COMMENT '0否 1可领养',
    audit_status TINYINT DEFAULT 1 COMMENT '1通过 0待审 2拒绝',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_store (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物档案';

CREATE TABLE IF NOT EXISTS pet_album (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    media_url VARCHAR(512) NOT NULL,
    media_type TINYINT DEFAULT 1 COMMENT '1图片 2视频',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_pet (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物相册';

CREATE TABLE IF NOT EXISTS community_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT DEFAULT NULL,
    content TEXT,
    media_urls TEXT COMMENT '媒体URL逗号分隔',
    media_type TINYINT DEFAULT 1 COMMENT '1图文 2视频',
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    audit_status TINYINT DEFAULT 0 COMMENT '0待审 1通过 2拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区动态';

CREATE TABLE IF NOT EXISTS community_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(512) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论';

CREATE TABLE IF NOT EXISTS community_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区点赞';

CREATE TABLE IF NOT EXISTS adopt_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city VARCHAR(64) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    reason TEXT,
    status TINYINT DEFAULT 1 COMMENT '1待审核 2初审通过 3复审通过 4面谈 5签约 6完成 7拒绝',
    reject_reason VARCHAR(255) DEFAULT NULL,
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id),
    KEY idx_pet (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养申请';

CREATE TABLE IF NOT EXISTS adopt_visit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    visit_time DATETIME DEFAULT NULL,
    content TEXT,
    operator_id BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_app (application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领养回访';

CREATE TABLE IF NOT EXISTS mall_spu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    category VARCHAR(64) DEFAULT NULL,
    cover_url VARCHAR(512) DEFAULT NULL,
    description TEXT,
    price DECIMAL(10,2) DEFAULT 0,
    point_price INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城SPU';

CREATE TABLE IF NOT EXISTS mall_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spu_id BIGINT NOT NULL,
    sku_name VARCHAR(128) DEFAULT NULL,
    price DECIMAL(10,2) DEFAULT 0,
    point_price INT DEFAULT 0,
    stock INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_spu (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城SKU';

CREATE TABLE IF NOT EXISTS mall_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    spu_id BIGINT DEFAULT NULL,
    sku_id BIGINT DEFAULT NULL,
    quantity INT DEFAULT 1,
    pay_amount DECIMAL(10,2) DEFAULT 0,
    point_amount INT DEFAULT 0,
    pay_type TINYINT DEFAULT 1 COMMENT '1现金 2积分',
    status TINYINT DEFAULT 1 COMMENT '1待支付 2已支付 3已取消 4已退款',
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单';

CREATE TABLE IF NOT EXISTS user_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INT DEFAULT 0,
    coins INT DEFAULT 0,
    love_value INT DEFAULT 0,
    flowers INT DEFAULT 0,
    level INT DEFAULT 1,
    level_name VARCHAR(64) DEFAULT 'Lv.1',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产';

CREATE TABLE IF NOT EXISTS user_asset_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_type VARCHAR(16) NOT NULL COMMENT 'points/coins/love/flowers',
    change_amount INT NOT NULL,
    direction TINYINT NOT NULL COMMENT '1收入 2支出',
    biz_type VARCHAR(32) DEFAULT NULL,
    biz_id BIGINT DEFAULT NULL,
    title VARCHAR(128) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产流水';

CREATE TABLE IF NOT EXISTS island_pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hunger INT DEFAULT 80,
    clean INT DEFAULT 80,
    mood INT DEFAULT 80,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岛宅宠物状态';

CREATE TABLE IF NOT EXISTS island_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    reward_desc VARCHAR(128) DEFAULT NULL,
    required_level INT DEFAULT 1,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岛宅任务';

CREATE TABLE IF NOT EXISTS island_task_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '1完成 2进行中 3未开始',
    complete_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user_task (user_id, task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务完成记录';

CREATE TABLE IF NOT EXISTS feed_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    amount DECIMAL(10,2) DEFAULT 0,
    quantity INT DEFAULT 1,
    message VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1 COMMENT '1待支付 2已支付 3已反馈',
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投喂订单';

CREATE TABLE IF NOT EXISTS operation_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    image_url VARCHAR(512) DEFAULT NULL,
    jump_url VARCHAR(512) DEFAULT NULL,
    position_code VARCHAR(32) DEFAULT 'home' COMMENT 'home/adopt/shop',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营位';

CREATE TABLE IF NOT EXISTS pet_strategy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    sub_title VARCHAR(255) DEFAULT NULL,
    tag VARCHAR(32) DEFAULT NULL,
    cover_url VARCHAR(512) DEFAULT NULL,
    content TEXT,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养宠攻略';

CREATE TABLE IF NOT EXISTS api_idempotent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotent_key VARCHAR(64) NOT NULL,
    user_id BIGINT DEFAULT NULL,
    api_path VARCHAR(128) NOT NULL,
    result_snapshot TEXT,
    expire_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_key_path (idempotent_key, api_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口幂等记录';

CREATE TABLE IF NOT EXISTS pay_callback_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) DEFAULT NULL,
    channel VARCHAR(32) DEFAULT NULL,
    trade_type VARCHAR(32) DEFAULT NULL,
    request_body TEXT,
    response_body TEXT,
    status TINYINT DEFAULT 1 COMMENT '1成功 2失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志';

-- =============================================================================
-- 第二部分：V2 扩展（列 + 新表）
-- =============================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS pet_add_column$$
CREATE PROCEDURE pet_add_column(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL pet_add_column('community_post', 'pet_id', 'BIGINT DEFAULT NULL COMMENT ''关联宠物''');
CALL pet_add_column('community_post', 'user_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''发布者昵称''');
CALL pet_add_column('community_post', 'view_count', 'INT DEFAULT 0 COMMENT ''浏览量''');
CALL pet_add_column('community_post', 'visibility', 'TINYINT DEFAULT 1 COMMENT ''1公开 2仅自己''');
CALL pet_add_column('community_post', 'allow_like', 'TINYINT DEFAULT 1');
CALL pet_add_column('community_post', 'allow_comment', 'TINYINT DEFAULT 1');
CALL pet_add_column('community_post', 'status', 'TINYINT DEFAULT 1 COMMENT ''1正常 2下架''');
CALL pet_add_column('community_comment', 'user_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''评论者昵称''');

CALL pet_add_column('island_pet', 'vitality', 'INT DEFAULT 80');
CALL pet_add_column('island_pet', 'intimacy', 'INT DEFAULT 0');
CALL pet_add_column('island_pet', 'resident_pet_id', 'BIGINT DEFAULT NULL');
CALL pet_add_column('island_pet', 'resident_pet_ids', 'VARCHAR(64) DEFAULT NULL');
CALL pet_add_column('island_pet', 'feed_cooldown_until', 'DATETIME DEFAULT NULL');

CALL pet_add_column('island_task', 'task_group', 'VARCHAR(32) DEFAULT ''daily''');
CALL pet_add_column('island_task', 'reward_coins', 'INT DEFAULT 0');
CALL pet_add_column('island_task', 'reward_points', 'INT DEFAULT 0');

CALL pet_add_column('pet_strategy', 'category_id', 'BIGINT DEFAULT NULL');

CALL pet_add_column('mall_spu', 'category_id', 'BIGINT DEFAULT NULL');
CALL pet_add_column('mall_spu', 'original_price', 'DECIMAL(10,2) DEFAULT 0');
CALL pet_add_column('mall_spu', 'activity_price', 'DECIMAL(10,2) DEFAULT NULL');
CALL pet_add_column('mall_spu', 'vip_price', 'DECIMAL(10,2) DEFAULT NULL');
CALL pet_add_column('mall_spu', 'biz_type', 'TINYINT DEFAULT 1 COMMENT ''1普通 2投喂 3次卡 4积分''');

CALL pet_add_column('mall_order', 'biz_type', 'TINYINT DEFAULT 1');
CALL pet_add_column('mall_order', 'pay_status', 'TINYINT DEFAULT 1');
CALL pet_add_column('mall_order', 'remark', 'VARCHAR(255) DEFAULT NULL');

DROP PROCEDURE IF EXISTS pet_add_column;

CREATE TABLE IF NOT EXISTS community_post_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    media_url VARCHAR(512) NOT NULL,
    media_type TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态媒体';

CREATE TABLE IF NOT EXISTS community_view_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT DEFAULT NULL,
    device_id VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_view (post_id, user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览去重';

CREATE TABLE IF NOT EXISTS community_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type TINYINT NOT NULL,
    target_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报';

CREATE TABLE IF NOT EXISTS island_market_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    cover_url VARCHAR(512) DEFAULT NULL COMMENT '封面图',
    item_type VARCHAR(32) DEFAULT 'prop',
    price_coins INT DEFAULT 0,
    price_points INT DEFAULT 0,
    effect_desc VARCHAR(255) DEFAULT NULL,
    effect_json VARCHAR(512) DEFAULT NULL,
    durability INT DEFAULT 100 COMMENT '耐久，-1表示永久/装饰',
    favor INT DEFAULT 0,
    pet_type VARCHAR(16) DEFAULT 'all',
    status TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集市道具';

CREATE TABLE IF NOT EXISTS island_market_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    pay_coins INT DEFAULT 0,
    pay_points INT DEFAULT 0,
    favor INT DEFAULT 0,
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集市购买记录';

CREATE TABLE IF NOT EXISTS marketing_rank_period (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打榜周期';

CREATE TABLE IF NOT EXISTS marketing_rank_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    like_count INT DEFAULT 0,
    rank_no INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_period_pet (period_id, pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='榜单记录';

CREATE TABLE IF NOT EXISTS marketing_assist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assist_type VARCHAR(32) DEFAULT 'like',
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user_day (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='助力记录';

CREATE TABLE IF NOT EXISTS pet_strategy_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='攻略分类';

CREATE TABLE IF NOT EXISTS strategy_read_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_read (strategy_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读去重';

CREATE TABLE IF NOT EXISTS mall_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城分类';

CREATE TABLE IF NOT EXISTS coupon_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    amount_desc VARCHAR(64) DEFAULT NULL,
    min_amount DECIMAL(10,2) DEFAULT 0,
    coupon_type VARCHAR(32) DEFAULT 'store',
    valid_days INT DEFAULT 30,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    order_id BIGINT DEFAULT NULL,
    expire_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

CREATE TABLE IF NOT EXISTS feed_order_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    message VARCHAR(255) DEFAULT NULL,
    signature VARCHAR(128) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投喂留言';

CREATE TABLE IF NOT EXISTS feed_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    video_url VARCHAR(512) NOT NULL,
    feedback_status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投喂视频反馈';

CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pay_no VARCHAR(64) NOT NULL,
    biz_order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    terminal VARCHAR(16) DEFAULT 'mini',
    pay_channel VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 1,
    pay_time DATETIME DEFAULT NULL,
    idempotent_key VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_pay_no (pay_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一支付单';

CREATE TABLE IF NOT EXISTS pay_refund_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pay_no VARCHAR(64) NOT NULL,
    refund_no VARCHAR(64) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 1,
    request_body TEXT,
    response_body TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款日志';

CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(64) NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词';

CREATE TABLE IF NOT EXISTS user_notification_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    push_enabled TINYINT DEFAULT 1,
    sms_enabled TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置';

CREATE TABLE IF NOT EXISTS wecom_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qr_url VARCHAR(512) DEFAULT NULL,
    wecom_id VARCHAR(64) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微配置';

CREATE TABLE IF NOT EXISTS user_cancel_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注销申请';

-- =============================================================================
-- 第三部分：管理端菜单 + 系统配置
-- =============================================================================

DELETE FROM sys_role_menu WHERE menu_id >= 2000 AND menu_id < 2100;
DELETE FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2100;

INSERT IGNORE INTO sys_role (role_id, role_name, remark, create_user_id, create_time) VALUES
(1, '超级管理员', '拥有全部权限', 1, NOW());

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2000, 0, '控制台', 'dashboard', NULL, 1, 'dashboard', 0),
(2001, 0, '用户管理', 'users', 'userList:list', 1, 'user', 1),
(2174, 2001, '查看用户详情', NULL, 'userList:info', 2, NULL, 0),
(2175, 2001, '编辑用户', NULL, 'userList:update', 2, NULL, 1),
(2131, 2001, '删除用户', NULL, 'userList:delete', 2, NULL, 2),
(2034, 0, '首页装修', 'content/decorations', 'bannerList:list', 1, 'picture', 2),
(2109, 0, '首页社群入口', 'pet/home-social', 'pet:home:social:list', 1, 'link', 3),
(2010, 0, '宠物管理', NULL, NULL, 0, 'peoples', 4),
(2105, 2010, '宠物城市', 'pet/cities', 'pet:city:list', 1, 'location', 0),
(2106, 2105, '新增城市', NULL, 'pet:city:add', 2, NULL, 0),
(2107, 2105, '编辑城市', NULL, 'pet:city:update', 2, NULL, 1),
(2108, 2105, '删除城市', NULL, 'pet:city:delete', 2, NULL, 2),
(2011, 2010, '门店管理', 'pet/stores', 'pet:store:list', 1, 'shop', 1),
(2111, 2011, '新增门店', NULL, 'pet:store:add', 2, NULL, 0),
(2112, 2011, '编辑门店', NULL, 'pet:store:update', 2, NULL, 1),
(2113, 2011, '删除门店', NULL, 'pet:store:delete', 2, NULL, 2),
(2012, 2010, '宠物档案', 'pet/archives', 'pet:archive:list', 1, 'documentation', 2),
(2114, 2012, '新增档案', NULL, 'pet:archive:add', 2, NULL, 0),
(2115, 2012, '编辑档案', NULL, 'pet:archive:update', 2, NULL, 1),
(2116, 2012, '删除档案', NULL, 'pet:archive:delete', 2, NULL, 2),
(2080, 2001, '查看用户资产', NULL, 'pet:asset:list', 2, NULL, 3),
(2020, 0, '社区管理', NULL, NULL, 0, 'message', 5),
(2021, 2020, '动态管理', 'pet/community', 'pet:community:list', 1, 'message', 0),
(2023, 2020, '投诉管理', 'pet/community/complaints', 'pet:community:complaint:list', 1, 'warning', 1),
(2022, 2020, '敏感词', 'pet/sensitive', 'pet:sensitive:list', 1, 'edit', 3),
(2117, 2022, '新增敏感词', NULL, 'pet:sensitive:add', 2, NULL, 0),
(2118, 2022, '编辑敏感词', NULL, 'pet:sensitive:update', 2, NULL, 1),
(2119, 2022, '删除敏感词', NULL, 'pet:sensitive:delete', 2, NULL, 2),
(2090, 2021, '动态审核', NULL, 'pet:community:audit', 2, NULL, 0),
(2091, 2021, '动态下架', NULL, 'pet:community:offline', 2, NULL, 1),
(2092, 2021, '动态删除', NULL, 'pet:community:delete', 2, NULL, 2),
(2093, 2021, '评论删除', NULL, 'pet:community:comment:delete', 2, NULL, 3),
(2094, 2021, '批量审核', NULL, 'pet:community:batchAudit', 2, NULL, 4),
(2095, 2023, '投诉处理', NULL, 'pet:community:complaint:handle', 2, NULL, 0),
(2096, 2023, '投诉类型配置', NULL, 'pet:community:complaint:config', 2, NULL, 1),
(2030, 0, '商城管理', NULL, NULL, 0, 'shopping', 6),
(2031, 2030, '商品管理', 'pet/mall', 'pet:mall:list', 1, 'goods', 0),
(2032, 2030, '订单管理', 'pet/orders', 'pet:order:list', 1, 'order', 1),
(2184, 0, '投喂模块', NULL, NULL, 0, 'present', 7),
(2160, 2184, '投喂商品', 'pet/feed/products', 'pet:mall:list', 1, 'goods', 0),
(2033, 2184, '投喂订单', 'pet/feed', 'pet:feed:list', 1, 'list', 1),
(2110, 2109, '保存社群配置', NULL, 'pet:home:social:update', 2, NULL, 0),
(2035, 2034, '新增轮播', NULL, 'bannerList:add', 2, NULL, 0),
(2036, 2034, '编辑轮播', NULL, 'bannerList:update', 2, NULL, 1),
(2037, 2034, '删除轮播', NULL, 'bannerList:delete', 2, NULL, 2),
(2040, 0, '领养管理', NULL, NULL, 0, 'star', 7),
(2041, 2040, '领养申请', 'pet/adopt', 'pet:adopt:list', 1, 'form', 0),
(2120, 2041, '领养状态变更', NULL, 'pet:adopt:update', 2, NULL, 0),
(2050, 0, '营销管理', NULL, NULL, 0, 'chart', 8),
(2051, 2050, '岛宅任务', 'pet/tasks', 'pet:task:list', 1, 'skill', 0),
(2121, 2051, '新增任务', NULL, 'pet:task:add', 2, NULL, 0),
(2122, 2051, '编辑任务', NULL, 'pet:task:update', 2, NULL, 1),
(2123, 2051, '删除任务', NULL, 'pet:task:delete', 2, NULL, 2),
(2052, 2050, '打榜排行', 'pet/ranking', 'pet:ranking:list', 1, 'rank', 1),
(2180, 2050, '攻略分类', 'pet/strategy/categories', 'pet:strategy:category:list', 1, 'category', 2),
(2181, 2180, '新增分类', NULL, 'pet:strategy:category:add', 2, NULL, 0),
(2182, 2180, '编辑分类', NULL, 'pet:strategy:category:update', 2, NULL, 1),
(2183, 2180, '删除分类', NULL, 'pet:strategy:category:delete', 2, NULL, 2),
(2053, 2050, '养宠攻略', 'pet/strategy', 'pet:strategy:list', 1, 'guide', 3),
(2124, 2053, '新增攻略', NULL, 'pet:strategy:add', 2, NULL, 0),
(2125, 2053, '编辑攻略', NULL, 'pet:strategy:update', 2, NULL, 1),
(2126, 2053, '删除攻略', NULL, 'pet:strategy:delete', 2, NULL, 2),
(2054, 2050, '集市道具', 'pet/market', 'pet:market:list', 1, 'shopping', 4),
(2127, 2054, '新增道具', NULL, 'pet:market:add', 2, NULL, 0),
(2128, 2054, '编辑道具', NULL, 'pet:market:update', 2, NULL, 1),
(2129, 2054, '删除道具', NULL, 'pet:market:delete', 2, NULL, 2),
(2055, 2050, '岛宅状态', 'pet/island-pets', 'pet:island:list', 1, 'peoples', 5),
(2130, 2055, '编辑岛宅', NULL, 'pet:island:update', 2, NULL, 0),
(2060, 0, '支付管理', NULL, NULL, 0, 'money', 9),
(2061, 2060, '支付单', 'pet/payment-orders', 'pet:payment:order:list', 1, 'money', 0),
(2062, 2060, '支付回调日志', 'pet/payment-log', 'pet:payment:list', 1, 'log', 1),
(2070, 0, '系统管理', NULL, NULL, 0, 'system', 10),
(2071, 2070, '菜单管理', 'system/menus', 'sys:menu:list', 1, 'tree-table', 0),
(2072, 2070, '角色权限', 'system/roles', 'sys:role:list', 1, 'peoples', 1),
(2073, 2070, '系统配置', 'system/configs', 'sys:config:list,allocationList:list', 1, 'edit', 2),
(2074, 2070, '管理员', 'system/admins', 'sys:user:list', 1, 'user', 3),
(2081, 2071, '菜单操作', NULL, 'sys:menu:save,sys:menu:update,sys:menu:delete,sys:menu:info,sys:menu:select', 2, NULL, 0),
(2082, 2072, '角色操作', NULL, 'sys:role:save,sys:role:update,sys:role:delete,sys:role:info,sys:role:select', 2, NULL, 0),
(2083, 2073, '配置操作', NULL, 'sys:config:update,sys:config:save,allocationList:update', 2, NULL, 0),
(2084, 2074, '管理员操作', NULL, 'sys:user:save,sys:user:update,sys:user:delete,sys:user:info', 2, NULL, 0);

-- 超管授权全部宠物业务菜单（含 >=2100 的按钮权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 2000;

DELETE FROM sys_config WHERE param_key IN (
  'sys_user_ma_appid','sys_user_ma_secret','pay_wx_jsapi_app_id','pay_wx_jsapi_mch_id','pay_wx_jsapi_api_key',
  'pay_wx_notify_url','pay_wx_h5_app_id','pay_wx_mch_id','pay_wx_mch_key','pay_wx_key_path',
  'pay_ali_appid','pay_ali_private_key','pay_ali_public_key','pay_ali_notify_url','pay_ali_mode','pay_ali_sandbox',
  'sms_provider','sms_smsbao_username','sms_smsbao_password','sms_ali_access_key','sms_ali_access_secret','sms_tencent_sdk_appid','sms_tencent_appkey','wecom_qr_url','wecom_service_link',
  'sys_h5_domain','file_storage_type','file_local_upload_path','file_cdn_domain',
  'community_audit_switch','community_complaint_types','mall_order_audit_switch','sys_server_name',
  'sys_withdraw_max','sys_withdraw_min','sys_withdraw_rate','map_platform','map_tencent_key'
);

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES
('sys_user_ma_appid', '', '1', '用户端微信小程序AppID', 'weixin', '微信配置'),
('sys_user_ma_secret', '', '1', '用户端微信小程序AppSecret', 'weixin', '微信配置'),
('pay_wx_jsapi_app_id', '', '1', '微信小程序支付AppID', 'weixin', '微信支付'),
('pay_wx_jsapi_mch_id', '', '1', '微信小程序支付商户号', 'weixin', '微信支付'),
('pay_wx_jsapi_api_key', '', '1', '微信小程序支付API密钥', 'weixin', '微信支付'),
('pay_wx_notify_url', '', '1', '微信支付回调地址', 'weixin', '微信支付'),
('pay_wx_h5_app_id', '', '1', '微信H5支付AppID', 'weixin', '微信支付'),
('pay_wx_mch_id', '', '1', '微信支付商户号', 'weixin', '微信支付'),
('pay_wx_mch_key', '', '1', '微信支付商户密钥', 'weixin', '微信支付'),
('pay_wx_key_path', '', '1', '微信支付证书路径', 'weixin', '微信支付'),
('pay_ali_appid', '', '1', '支付宝AppID', 'zhifubao', '支付宝'),
('pay_ali_private_key', '', '1', '支付宝应用私钥', 'zhifubao', '支付宝'),
('pay_ali_public_key', '', '1', '支付宝公钥', 'zhifubao', '支付宝'),
('pay_ali_notify_url', '', '1', '支付宝回调地址', 'zhifubao', '支付宝'),
('pay_ali_mode', '1', '1', '支付宝模式1公钥2证书', 'zhifubao', '支付宝'),
('pay_ali_sandbox', '2', '1', '支付宝沙箱1是2否', 'zhifubao', '支付宝'),
('sms_provider', 'dxb', '1', '短信服务商：dxb(短信宝)/aliyun/tencent，也支持码值 3/2/1', 'duanxin', '短信'),
('sms_smsbao_username', '', '1', '短信宝账号', 'duanxin', '短信'),
('sms_smsbao_password', '', '1', '短信宝密码（明文，发送时自动MD5）', 'duanxin', '短信'),
('sms_ali_access_key', '', '1', '阿里云短信AccessKey', 'duanxin', '短信'),
('sms_ali_access_secret', '', '1', '阿里云短信AccessSecret', 'duanxin', '短信'),
('sms_tencent_sdk_appid', '', '1', '腾讯云短信SDK AppID', 'duanxin', '短信'),
('sms_tencent_appkey', '', '1', '腾讯云短信AppKey', 'duanxin', '短信'),
('wecom_qr_url', '', '1', '企业微信客服二维码URL', 'weixin', '企微'),
('wecom_service_link', '', '1', '客服链接', 'weixin', '企微'),
('sys_h5_domain', '', '1', 'H5/文件访问域名', 'oss', '文件上传'),
('file_storage_type', 'local', '1', '存储类型 local/oss/bt', 'oss', '文件上传'),
('file_local_upload_path', '/upload', '1', '本地上传目录', 'oss', '文件上传'),
('file_cdn_domain', '', '1', '文件CDN域名', 'oss', '文件上传'),
('community_audit_switch', '1', '1', '社区发布审核开关1开0关', 'kaiguan', '开关'),
('community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉'),
('mall_order_audit_switch', '0', '1', '商城订单审核开关', 'kaiguan', '开关'),
('sys_server_name', '和它交个朋友', '1', '系统名称', 'xitong', '全局'),
('sys_withdraw_max', '5000', '1', '最大提现金额', 'fuwufei', '服务费'),
('sys_withdraw_min', '10', '1', '最小提现金额', 'fuwufei', '服务费'),
('sys_withdraw_rate', '0.01', '1', '提现手续费率', 'fuwufei', '服务费'),
('map_platform', 'tencent', '1', '地图平台 tencent/google/tianditu', 'xitong', '全局'),
('map_tencent_key', '', '1', '腾讯地图Key', 'xitong', '全局');

-- =============================================================================
-- 第四部分：业务基础数据
-- =============================================================================

DELETE FROM pet_album WHERE pet_id BETWEEN 1 AND 4;
DELETE FROM marketing_rank_record WHERE period_id = 5001;
DELETE FROM marketing_rank_period WHERE id = 5001;
DELETE FROM island_market_item WHERE id BETWEEN 4001 AND 4020;
DELETE FROM island_task WHERE id BETWEEN 3001 AND 3010;
DELETE FROM mall_sku WHERE id BETWEEN 7101 AND 7110 OR spu_id BETWEEN 7001 AND 7010;
DELETE FROM mall_spu WHERE id BETWEEN 7001 AND 7010;
DELETE FROM user_coupon WHERE template_id BETWEEN 1001 AND 1010;
DELETE FROM coupon_template WHERE id BETWEEN 1001 AND 1010;
DELETE FROM pet_strategy WHERE id BETWEEN 1 AND 10;
DELETE FROM pet_strategy_category WHERE id BETWEEN 1 AND 10;
DELETE FROM operation_banner WHERE id BETWEEN 1 AND 10;
DELETE FROM pet_archive WHERE id BETWEEN 1 AND 4;
DELETE FROM pet_store WHERE id BETWEEN 1 AND 4;
DELETE FROM pet_city WHERE id BETWEEN 1 AND 3;
DELETE FROM mall_category WHERE id BETWEEN 1 AND 4;
DELETE FROM sensitive_word WHERE id BETWEEN 1 AND 10;
DELETE FROM wecom_config WHERE id = 1;

INSERT INTO pet_city (id, name, code, sort, status) VALUES
(1, '上海', 'SH', 1, 1),
(2, '杭州', 'HZ', 2, 1),
(3, '南京', 'NJ', 3, 1);

INSERT INTO pet_store (id, city_id, name, address, phone, tone, business_status, sort) VALUES
(1, 1, '爱琴海店', '上海市闵行区', '021-88880001', '#f6d57c', 1, 1),
(2, 1, '百脑汇店', '上海市徐汇区', '021-88880002', '#cfeedd', 1, 2),
(3, 1, '大学路店', '上海市杨浦区', '021-88880003', '#f7d5db', 1, 3),
(4, 1, '淮海路店', '上海市黄浦区', '021-88880004', '#efe6d8', 1, 4);

INSERT INTO pet_archive (id, store_id, name, pet_type, breed, gender, age_desc, weight_desc, pet_code, level_desc, status_label, description, good_at, avoid_items, tags, avatar_url, like_count, adoptable, sort) VALUES
(1, 1, '豆包', 'dog', '柴犬', '男', '2岁', '9kg', 'SH-007', 'Lv.5 明星宠物', '优秀员工', '豆包性格友好，是店里的气氛担当，特别喜欢和客人互动。', '["抚摸","玩球","晒太阳"]', '["鞭炮","洗澡"]', '["活泼","爱笑","亲人"]', '/static/logo.png', 12800, 1, 1),
(2, 2, '花姐', 'cat', '三花', '女', '7岁', '4.5kg', 'SH-012', 'Lv.4 人气喵星', '优秀员工', '花姐是百脑汇店的元老级员工，安静又亲人。', '["被撸","晒太阳","卖萌"]', '["狗","噪音"]', '["温柔","粘人"]', '/static/logo.png', 6400, 0, 1),
(3, 3, '小七', 'dog', '柯基', '女', '1岁', '8kg', 'SH-028', 'Lv.4 新星萌宠', '优秀员工', '小七非常亲人，喜欢被夸奖，是大学路店的小明星。', '["握手","奔跑","撒娇"]', '["独处太久"]', '["活泼","亲人"]', '/static/logo.png', 7200, 1, 1),
(4, 4, '球球', 'dog', '金毛', '男', '3岁', '28kg', 'SH-031', 'Lv.3', '营业中', '球球温顺大方，喜欢和小朋友一起玩。', '["游泳","接飞盘"]', '["辣椒"]', '["贪吃","爱笑"]', '/static/logo.png', 3200, 1, 1);

INSERT INTO pet_album (pet_id, media_url, media_type, sort) VALUES
(1, '/static/logo.png', 1, 1),
(2, '/static/logo.png', 1, 1),
(3, '/static/logo.png', 1, 1),
(4, '/static/logo.png', 1, 1);

INSERT INTO operation_banner (id, name, image_url, jump_url, position_code, sort, status) VALUES
(1, '理性养宠', '/static/logo.png', '/packageA/adopt/list', 'home', 1, 1),
(2, '门店投喂', '/static/logo.png', '/packageA/feed/index', 'home', 2, 1),
(3, '领养专区', '/static/logo.png', '/packageA/adopt/list', 'adopt', 1, 1),
(4, '春季萌宠市集', '/static/logo.png', '/packageA/product/detail?id=7001', 'shop', 1, 1);

INSERT INTO pet_strategy_category (id, name, sort, status) VALUES
(1, '汪星人', 1, 1),
(2, '喵星人', 2, 1),
(3, '幼宠', 3, 1);

INSERT INTO pet_strategy (id, title, sub_title, tag, category_id, cover_url, content, view_count, like_count, sort, status) VALUES
(1, '新手养狗必看清单', '从接狗狗回家第一天到养成健康习惯，一篇全搞定', '汪星人,幼宠', 1, NULL, '<h3>接狗回家前准备</h3><p>准备食盆水盆、牵引绳、宠物窝、尿垫、基础粮和驱虫药，提前规划好活动空间，把易咬物品收好。</p><h3>第一周适应期</h3><p>保持安静环境，固定喂食与如厕时间，减少频繁外出和陌生人刺激，让狗狗建立安全感。</p><h3>健康与训练</h3><p>按月龄完成疫苗和驱虫；从名字回应、坐下、定点排便开始，用正向反馈建立规则。</p><h3>常见误区</h3><p>不要频繁更换粮品牌，不要用人类食物奖励，不要在狗狗害怕时强行安抚过度。</p>', 23000, 1200, 1, 1),
(2, '猫咪的100种小表情解读', '读懂主子的每个眼神和尾巴语言', '喵星人', 2, NULL, '<h3>眼睛在说什么</h3><p>慢眨眼代表信任；瞳孔放大可能是兴奋或紧张；长时间盯着你通常是在观察，不一定是挑衅。</p><h3>耳朵与胡须</h3><p>耳朵向前表示好奇，向后贴平多为不适或防御；胡须前伸是探索，后收则说明它想拉开距离。</p><h3>尾巴语言</h3><p>尾巴竖起微微晃动多是心情不错；尾巴炸开说明受到惊吓；快速甩尾往往是烦躁信号。</p><h3>相处建议</h3><p>尊重猫咪边界，用慢眨眼回应信任，不要强行抱抱或突然摸肚子。</p>', 18000, 956, 2, 1),
(3, '宠物自制鲜食食谱大全', '营养均衡的5道鲜食，比罐头更健康', '饮食', NULL, NULL, '<h3>鲜食原则</h3><p>蛋白质为主、适量蔬菜、控制油脂和调味。人吃的盐、糖、洋葱、葡萄、巧克力等对宠物危险，务必避开。</p><h3>推荐食谱</h3><p>1. 鸡胸肉南瓜泥：低脂易消化，适合肠胃敏感宠物。<br/>2. 牛肉西兰花饭：补充铁质与纤维。<br/>3. 三文鱼蔬菜粥：优质脂肪，注意去刺彻底加热。<br/>4. 鸭肉山药糊：温和适口，换粮过渡友好。<br/>5. 鸡肝蔬菜小份餐：补铁，每周不超过 1~2 次。</p><h3>注意事项</h3><p>鲜食需当日制作当日食用，分装冷藏不超过 24 小时；如宠物有肾病、胰腺炎等疾病，请先咨询兽医。</p>', 15000, 823, 3, 1),
(4, '如何判断宠物是否生病', '10个常见症状自查清单，建议收藏', '健康', 3, NULL, '<h3>需要警惕的信号</h3><p>1. 连续两餐以上拒食；2. 呕吐或腹泻超过 24 小时；3. 精神萎靡不愿活动；4. 呼吸急促或张口呼吸；5. 大量饮水却尿少；6. 皮肤红肿掉毛抓挠；7. 眼睛分泌物增多；8. 突然跛行；9. 牙龈苍白或发黄；10. 体温异常（犬猫通常约 38~39.5℃）。</p><h3>居家观察</h3><p>记录发病时间、频率、是否接触新粮/新环境，就诊时把信息告诉医生，有助于更快判断。</p><h3>就医建议</h3><p>幼宠、老年宠、慢性病宠物出现上述症状应尽快就医；出现抽搐、持续出血、无法站立等紧急情况请立即送医。</p>', 31000, 2000, 4, 1),
(5, '柴犬饲养百科', '从性格特点到日常护理，全面了解柴犬', '汪星人', 1, NULL, '<h3>性格特点</h3><p>柴犬独立、警觉、忠诚，占有欲相对明显。适合有耐心、愿意坚持训练与社会化的家庭。</p><h3>日常护理</h3><p>每周梳理被毛，换毛季加强吸尘与梳毛；注意趾甲修剪和耳道清洁，避免潮湿环境导致皮肤问题。</p><h3>运动与训练</h3><p>每天保证足够散步和嗅闻时间。训练以短时多次、正向奖励为主，避免粗暴呵斥。</p><h3>健康关注</h3><p>关注髋关节、过敏性皮炎与眼部问题，定期体检，保持体重管理，减少关节负担。</p>', 27000, 1500, 5, 1);

INSERT INTO mall_category (id, name, sort, status) VALUES
(1, '全部商品', 0, 1),
(2, '客户投喂', 1, 1),
(3, '超值次卡', 2, 1),
(4, '实物赠予', 3, 1);

INSERT INTO mall_spu (id, name, category, category_id, cover_url, description, price, original_price, point_price, biz_type, status, sort) VALUES
(7001, '鲜肉罐头套餐', 'feed', 2, '/static/logo.png', '门店鲜食罐头，投喂后可见反馈视频', 29.90, 39.90, 0, 2, 1, 1),
(7002, '冻干零食礼包', 'feed', 2, '/static/logo.png', '冻干鸡肉+三文鱼，毛孩子最爱', 19.90, 25.90, 0, 2, 1, 2),
(7003, '营养湿粮组合', 'feed', 2, '/static/logo.png', '均衡营养湿粮，适合日常投喂', 39.90, 49.90, 0, 2, 1, 3),
(7004, '洗护次卡(5次)', 'card', 3, '/static/logo.png', '门店洗护服务次卡', 199.00, 299.00, 0, 3, 1, 4),
(7005, '宠物零食礼盒', 'gift', 4, '/static/logo.png', '精选零食礼盒，可积分兑换', 0.00, 0.00, 500, 4, 1, 5);

INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock) VALUES
(7101, 7001, '标准装', 29.90, 0, 999),
(7102, 7002, '标准装', 19.90, 0, 999),
(7103, 7004, '5次卡', 199.00, 0, 100),
(7104, 7005, '积分兑换', 0.00, 500, 50);

INSERT INTO island_task (id, title, description, reward_desc, task_group, reward_coins, reward_points, required_level, sort, status) VALUES
(3001, '每日投喂宠物', '完成一次岛宅投喂', '+50 金币', '每日待办', 50, 0, 1, 1, 1),
(3002, '陪宠物玩耍 5 分钟', '与常驻宠物互动', '+30 金币 +20 花瓣', '家主任务', 30, 20, 1, 2, 1),
(3003, '发布一条社群动态', '分享宠物日常', '+20 金币', '社群任务', 20, 0, 1, 3, 1),
(3004, '完成一次门店投喂', '为门店宠物下单投喂', '+80 金币', '每日待办', 80, 0, 2, 4, 1),
(3005, '集市购买道具', '在岛宅集市兑换任意道具', '+15 金币', '家主任务', 15, 0, 1, 5, 1);

INSERT INTO island_market_item (id, name, cover_url, item_type, price_coins, price_points, effect_desc, durability, favor, pet_type, status, sort) VALUES
(4001, '按摩梳', '/static/market/massage-comb.png', 'toy', 80, 80, '+15 亲密度', 100, 15, 'dog', 1, 1),
(4002, '弹力球', '/static/market/bouncy-ball.png', 'toy', 50, 50, '+10 亲密度', 80, 10, 'dog', 1, 2),
(4003, '磨牙棒', '/static/market/chew-bone.png', 'toy', 60, 60, '+12 亲密度', 60, 12, 'dog', 1, 3),
(4004, '飞盘', '/static/market/frisbee.png', 'toy', 90, 90, '+18 亲密度', 100, 18, 'dog', 1, 4),
(4005, '绳结玩具', '/static/market/rope-toy.png', 'toy', 45, 45, '+8 亲密度', 70, 8, 'dog', 1, 5),
(4006, '蝴蝶结领结', '/static/market/bow-tie.png', 'decor', 120, 120, '+20 亲密度(装饰品)', -1, 20, 'dog', 1, 6),
(4007, '逗猫棒', '/static/market/cat-teaser.png', 'toy', 55, 55, '+10 亲密度', 75, 10, 'cat', 1, 1),
(4008, '猫薄荷球', '/static/market/catnip-ball.png', 'toy', 40, 40, '+8 亲密度', 60, 8, 'cat', 1, 2),
(4009, '激光笔', '/static/market/laser-pointer.png', 'toy', 70, 70, '+14 亲密度', 90, 14, 'cat', 1, 3),
(4010, '猫抓板', '/static/market/scratch-board.png', 'toy', 85, 85, '+16 亲密度', 100, 16, 'cat', 1, 4),
(4011, '毛线球', '/static/market/yarn-ball.png', 'toy', 45, 45, '+9 亲密度', 70, 9, 'cat', 1, 5),
(4012, '铃铛项圈', '/static/market/bell-collar.png', 'decor', 110, 110, '+18 亲密度(装饰品)', -1, 18, 'cat', 1, 6);

INSERT INTO marketing_rank_period (id, name, start_time, end_time, status) VALUES
(5001, '2026春季萌宠榜', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

INSERT INTO marketing_rank_record (period_id, pet_id, like_count, rank_no) VALUES
(5001, 1, 12800, 1),
(5001, 3, 7200, 2),
(5001, 2, 6400, 3),
(5001, 4, 3200, 4);

INSERT INTO coupon_template (id, title, amount_desc, min_amount, coupon_type, valid_days, status) VALUES
(1001, '新用户满50减10', '满50减10', 50.00, 'store', 30, 1),
(1002, '投喂专享9折券', '9折', 0.00, 'feed', 15, 1);

INSERT INTO sensitive_word (id, word, status) VALUES
(1, '违禁词示例', 1),
(2, '广告引流', 1),
(3, '辱骂词汇', 1);

INSERT INTO wecom_config (id, qr_url, wecom_id, status) VALUES
(1, '/static/logo.png', 'pet-service', 1);

-- =============================================================================
-- 第五部分：测试用户与联调数据
-- =============================================================================

SET @old_uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);
SET @old_uid_b = (SELECT user_id FROM tb_user WHERE phone = '13800138001' LIMIT 1);

DELETE FROM community_like WHERE post_id BETWEEN 9001 AND 9010;
DELETE FROM community_comment WHERE post_id BETWEEN 9001 AND 9010;
DELETE FROM community_post WHERE id BETWEEN 9001 AND 9010;
DELETE FROM user_asset_ledger WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM user_wallet WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM referral_user_commission WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM user_coupon WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM user_asset WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM island_pet WHERE user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM island_task_record WHERE user_id = @old_uid_a OR user_id = @old_uid_b OR task_id BETWEEN 3001 AND 3010;
DELETE FROM island_market_record WHERE user_id = @old_uid_a OR user_id = @old_uid_b OR item_id BETWEEN 4001 AND 4020;
DELETE FROM marketing_assist WHERE period_id = 5001 OR user_id = @old_uid_a OR user_id = @old_uid_b;
DELETE FROM adopt_visit WHERE application_id = 6001;
DELETE FROM adopt_application WHERE id = 6001;
DELETE FROM feed_feedback WHERE order_id = 8001;
DELETE FROM feed_order_message WHERE order_id = 8001;
DELETE FROM feed_order WHERE id = 8001 OR order_no = 'FO20250604001';
DELETE FROM pay_order WHERE pay_no IN ('PAY20250604001', 'PAY20250604002');
DELETE FROM mall_order WHERE order_no IN ('MO20250604001', 'MO20250604002');
DELETE FROM pay_callback_log WHERE order_no = 'MO20250604001';
DELETE FROM tb_user WHERE phone IN ('13800138000', '13800138001');

INSERT INTO tb_user (user_name, phone, password, sex, status, create_time, update_time) VALUES
('测试用户A', '13800138000', 'e10adc3949ba59abbe56e057f20f883e', 1, 1, NOW(), NOW()),
('测试用户B', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', 2, 1, NOW(), NOW());

SET @uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);
SET @uid_b = (SELECT user_id FROM tb_user WHERE phone = '13800138001' LIMIT 1);

INSERT INTO community_post (id, user_id, store_id, content, media_urls, media_type, like_count, comment_count, audit_status, visibility, allow_like, allow_comment, status, create_time, update_time, is_deleted) VALUES
(9001, @uid_a, 1, '豆包今天特别乖，一直摇尾巴欢迎客人～', '/static/logo.png', 1, 0, 2, 1, 1, 1, 1, 1, NOW(), NOW(), 0),
(9002, @uid_b, 2, '花姐晒太阳的样子太治愈了，快来门店看她！', '', 1, 1, 0, 1, 1, 1, 1, 1, NOW(), NOW(), 0),
(9003, @uid_a, 3, '柯基小七学会新技能啦，会握手！', '/static/logo.png', 1, 1, 1, 1, 1, 1, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0);

INSERT INTO community_comment (post_id, user_id, content) VALUES
(9001, @uid_b, '豆包太可爱了！'),
(9001, @uid_a, '下次带朋友一起来～'),
(9003, @uid_b, '小七好聪明！');

INSERT INTO community_like (post_id, user_id) VALUES
(9002, @uid_a),
(9003, @uid_b);

INSERT INTO user_asset (user_id, points, coins, love_value, flowers, level, level_name, create_time, update_time, is_deleted) VALUES
(@uid_a, 520, 1280, 328, 66, 4, 'Lv.4', NOW(), NOW(), 0),
(@uid_b, 180, 360, 120, 20, 2, 'Lv.2', NOW(), NOW(), 0);

INSERT INTO user_wallet (user_id, amount) VALUES
(@uid_a, 0.00),
(@uid_b, 0.00);

INSERT INTO referral_user_commission (user_id, commission) VALUES
(@uid_a, 0.00),
(@uid_b, 0.00);

INSERT INTO island_pet (user_id, hunger, clean, mood, vitality, intimacy, resident_pet_id, resident_pet_ids, feed_cooldown_until, create_time, update_time, is_deleted) VALUES
(@uid_a, 74, 71, 82, 67, 340, 1, '1', NULL, NOW(), NOW(), 0),
(@uid_b, 88, 85, 90, 75, 120, 2, '2', NULL, NOW(), NOW(), 0);

INSERT INTO island_task_record (user_id, task_id, status, complete_time) VALUES
(@uid_a, 3001, 1, NOW()),
(@uid_a, 3002, 2, NULL),
(@uid_b, 3001, 3, NULL);

INSERT INTO adopt_application (id, user_id, pet_id, real_name, phone, city, address, reason, status) VALUES
(6001, @uid_a, 1, '张三', '13800138000', '上海', '上海市闵行区XX路100号', '非常喜欢豆包，有稳定工作和养宠经验。', 2);

INSERT INTO adopt_visit (application_id, visit_time, content, operator_id) VALUES
(6001, DATE_SUB(NOW(), INTERVAL 3 DAY), '电话回访：申请人条件良好，建议进入复审。', 1);

INSERT INTO feed_order (id, order_no, user_id, pet_id, product_name, amount, quantity, message, status) VALUES
(8001, 'FO20250604001', @uid_a, 1, '鲜肉罐头套餐', 29.90, 1, '豆包加油！', 3);

INSERT INTO feed_order_message (order_id, message, signature) VALUES
(8001, '豆包加油！', '测试用户A');

INSERT INTO feed_feedback (order_id, video_url, feedback_status) VALUES
(8001, '/static/logo.png', 1);

INSERT INTO mall_order (order_no, user_id, spu_id, sku_id, quantity, pay_amount, pay_type, biz_type, pay_status, status) VALUES
('MO20250604001', @uid_a, 7001, 7101, 1, 29.90, 1, 2, 2, 2),
('MO20250604002', @uid_b, 7005, 7104, 1, 0.00, 2, 4, 2, 2);

INSERT INTO pay_order (pay_no, biz_order_no, user_id, business_type, terminal, pay_channel, amount, status, pay_time) VALUES
('PAY20250604001', 'MO20250604001', @uid_a, 'mall', 'mini', 'wx_jsapi', 29.90, 2, NOW()),
('PAY20250604002', 'FO20250604001', @uid_a, 'feed', 'mini', 'wx_jsapi', 29.90, 2, NOW());

INSERT INTO user_coupon (user_id, template_id, status, expire_time) VALUES
(@uid_a, 1001, 1, DATE_ADD(NOW(), INTERVAL 30 DAY)),
(@uid_a, 1002, 1, DATE_ADD(NOW(), INTERVAL 15 DAY));

INSERT INTO marketing_assist (period_id, pet_id, user_id, assist_type, idempotent_key) VALUES
(5001, 1, @uid_a, 'like', CONCAT('assist-', @uid_a, '-1')),
(5001, 3, @uid_b, 'like', CONCAT('assist-', @uid_b, '-3'));

INSERT INTO island_market_record (user_id, item_id, pay_coins, pay_points, idempotent_key) VALUES
(@uid_a, 4001, 80, 0, CONCAT('market-', @uid_a, '-4001'));

INSERT INTO pay_callback_log (order_no, channel, trade_type, request_body, response_body, status) VALUES
('MO20250604001', 'wx_jsapi', 'pay', '{"out_trade_no":"MO20250604001"}', '{"return_code":"SUCCESS"}', 1);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 执行完毕
-- =============================================================================
