-- 宠物业务 V2 扩展（MySQL 5.7 兼容）
-- 执行前请先执行 pet-business-schema.sql
-- 本脚本使用存储过程安全添加列，可重复执行

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

-- community_post 扩展列
CALL pet_add_column('community_post', 'pet_id', 'BIGINT DEFAULT NULL COMMENT ''关联宠物''');
CALL pet_add_column('community_post', 'user_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''发布者昵称''');
CALL pet_add_column('community_post', 'view_count', 'INT DEFAULT 0 COMMENT ''浏览量''');
CALL pet_add_column('community_post', 'visibility', 'TINYINT DEFAULT 1 COMMENT ''1公开 2仅自己''');
CALL pet_add_column('community_post', 'allow_like', 'TINYINT DEFAULT 1');
CALL pet_add_column('community_post', 'allow_comment', 'TINYINT DEFAULT 1');
CALL pet_add_column('community_post', 'status', 'TINYINT DEFAULT 1 COMMENT ''1正常 2下架''');
CALL pet_add_column('community_comment', 'user_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''评论者昵称''');

-- island_pet 扩展列
CALL pet_add_column('island_pet', 'vitality', 'INT DEFAULT 80');
CALL pet_add_column('island_pet', 'intimacy', 'INT DEFAULT 0');
CALL pet_add_column('island_pet', 'resident_pet_id', 'BIGINT DEFAULT NULL');
CALL pet_add_column('island_pet', 'resident_pet_ids', 'VARCHAR(64) DEFAULT NULL');
CALL pet_add_column('island_pet', 'feed_cooldown_until', 'DATETIME DEFAULT NULL');

-- island_task 扩展列
CALL pet_add_column('island_task', 'task_group', 'VARCHAR(32) DEFAULT ''daily''');
CALL pet_add_column('island_task', 'reward_coins', 'INT DEFAULT 0');
CALL pet_add_column('island_task', 'reward_points', 'INT DEFAULT 0');

-- pet_strategy 扩展列
CALL pet_add_column('pet_strategy', 'category_id', 'BIGINT DEFAULT NULL');

-- mall_spu 扩展列
CALL pet_add_column('mall_spu', 'category_id', 'BIGINT DEFAULT NULL');
CALL pet_add_column('mall_spu', 'original_price', 'DECIMAL(10,2) DEFAULT 0');
CALL pet_add_column('mall_spu', 'activity_price', 'DECIMAL(10,2) DEFAULT NULL');
CALL pet_add_column('mall_spu', 'vip_price', 'DECIMAL(10,2) DEFAULT NULL');
CALL pet_add_column('mall_spu', 'biz_type', 'TINYINT DEFAULT 1 COMMENT ''1普通 2投喂 3次卡 4积分''');

-- mall_order 扩展列
CALL pet_add_column('mall_order', 'biz_type', 'TINYINT DEFAULT 1');
CALL pet_add_column('mall_order', 'pay_status', 'TINYINT DEFAULT 1');
CALL pet_add_column('mall_order', 'remark', 'VARCHAR(255) DEFAULT NULL');

CALL pet_add_column('adopt_application', 'form_json', 'TEXT DEFAULT NULL COMMENT ''领养登记表扩展JSON''');

DROP PROCEDURE IF EXISTS pet_add_column;

-- ========== 新建表 ==========

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
    item_type VARCHAR(32) DEFAULT 'prop',
    price_coins INT DEFAULT 0,
    price_points INT DEFAULT 0,
    effect_desc VARCHAR(255) DEFAULT NULL,
    effect_json VARCHAR(512) DEFAULT NULL,
    durability INT DEFAULT 100,
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

CREATE TABLE IF NOT EXISTS pay_callback_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) DEFAULT NULL,
    channel VARCHAR(32) DEFAULT NULL,
    trade_type VARCHAR(32) DEFAULT NULL,
    request_body TEXT,
    response_body TEXT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调日志';

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
