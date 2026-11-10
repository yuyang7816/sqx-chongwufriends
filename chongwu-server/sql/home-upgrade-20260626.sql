-- M02 首页完善：Banner 副标题/点击统计、门店推荐位、社群配置、多城门店 seed

-- operation_banner
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_banner' AND COLUMN_NAME = 'sub_title');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE operation_banner ADD COLUMN sub_title VARCHAR(255) NULL COMMENT ''副标题'' AFTER name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_banner' AND COLUMN_NAME = 'click_count');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE operation_banner ADD COLUMN click_count INT NOT NULL DEFAULT 0 COMMENT ''点击次数'' AFTER status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pet_store featured
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_store' AND COLUMN_NAME = 'featured');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE pet_store ADD COLUMN featured TINYINT NOT NULL DEFAULT 0 COMMENT ''首页推荐 1是'' AFTER business_status', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- wecom_config social fields
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wecom_config' AND COLUMN_NAME = 'xhs_title');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE wecom_config ADD COLUMN xhs_title VARCHAR(64) NULL COMMENT ''小红书标题'' AFTER wecom_id', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wecom_config' AND COLUMN_NAME = 'xhs_subtitle');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE wecom_config ADD COLUMN xhs_subtitle VARCHAR(128) NULL COMMENT ''小红书副标题'' AFTER xhs_title', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wecom_config' AND COLUMN_NAME = 'xhs_jump_url');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE wecom_config ADD COLUMN xhs_jump_url VARCHAR(255) NULL COMMENT ''小红书跳转'' AFTER xhs_subtitle', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wecom_config' AND COLUMN_NAME = 'community_title');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE wecom_config ADD COLUMN community_title VARCHAR(64) NULL COMMENT ''社群标题'' AFTER xhs_jump_url', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'wecom_config' AND COLUMN_NAME = 'community_subtitle');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE wecom_config ADD COLUMN community_subtitle VARCHAR(128) NULL COMMENT ''社群副标题'' AFTER community_title', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- seed data
UPDATE operation_banner SET sub_title = '我们主张降低遗弃，用爱与责任给它一个温暖的家。'
WHERE position_code = 'home' AND (sub_title IS NULL OR sub_title = '');

UPDATE pet_store SET featured = 1 WHERE id = 1 AND featured = 0;

INSERT INTO pet_store (city_id, name, address, phone, tone, cover_url, business_status, featured, sort)
SELECT 2, '西湖银泰店', '杭州市西湖区银泰城', '0571-88881001', '#cfeedd', '/static/logo.png', 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM pet_store WHERE city_id = 2 LIMIT 1);

INSERT INTO pet_store (city_id, name, address, phone, tone, cover_url, business_status, featured, sort)
SELECT 2, '钱江新城店', '杭州市上城区钱江路', '0571-88881002', '#f6d57c', '/static/logo.png', 1, 0, 2
FROM DUAL WHERE (SELECT COUNT(*) FROM pet_store WHERE city_id = 2) < 2;

INSERT INTO pet_store (city_id, name, address, phone, tone, cover_url, business_status, featured, sort)
SELECT 3, '新街口店', '南京市秦淮区新街口', '025-88882001', '#f7d5db', '/static/logo.png', 1, 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM pet_store WHERE city_id = 3 LIMIT 1);

INSERT INTO pet_store (city_id, name, address, phone, tone, cover_url, business_status, featured, sort)
SELECT 3, '河西龙湖店', '南京市建邺区河西', '025-88882002', '#efe6d8', '/static/logo.png', 1, 0, 2
FROM DUAL WHERE (SELECT COUNT(*) FROM pet_store WHERE city_id = 3) < 2;

UPDATE wecom_config SET
  xhs_title = COALESCE(xhs_title, '关注小红书'),
  xhs_subtitle = COALESCE(xhs_subtitle, '获取最新动态'),
  xhs_jump_url = COALESCE(xhs_jump_url, '/pages/msg/msg'),
  community_title = COALESCE(community_title, '加入社群'),
  community_subtitle = COALESCE(community_subtitle, '获取养宠咨询')
WHERE status = 1;
