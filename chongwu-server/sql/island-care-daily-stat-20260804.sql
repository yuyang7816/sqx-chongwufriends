-- 岛宅：双碗独立投喂 + 共用冷却；每日属性/进度从系统配置初始化
-- 执行库：shouban

-- island_pet 扩展字段
SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE island_pet ADD COLUMN food_bowl_filled TINYINT DEFAULT 0 COMMENT ''饭盆本轮已喂 1是0否''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_pet' AND COLUMN_NAME = 'food_bowl_filled'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE island_pet ADD COLUMN water_bowl_filled TINYINT DEFAULT 0 COMMENT ''水盆本轮已喂 1是0否''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_pet' AND COLUMN_NAME = 'water_bowl_filled'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE island_pet ADD COLUMN daily_progress INT DEFAULT 50 COMMENT ''今日进度0-100''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_pet' AND COLUMN_NAME = 'daily_progress'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE island_pet ADD COLUMN stat_date DATE DEFAULT NULL COMMENT ''属性结算日期''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_pet' AND COLUMN_NAME = 'stat_date'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 系统配置：每日进度与各行为加成（可在后台「全局配置」修改）
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_daily_stat_init', '50', '1', '岛宅每日初始进度/属性(%)', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_daily_stat_init');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_progress_gain', '6', '1', '岛宅喂饭/喂水进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_progress_gain', '2', '1', '岛宅打榜进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_progress_gain', '4', '1', '岛宅送东西/集市进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_hunger_gain', '12', '1', '岛宅喂饭/喂水饱食度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_hunger_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_mood_gain', '10', '1', '岛宅喂饭/喂水心情加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_mood_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_clean_gain', '6', '1', '岛宅打榜清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_vitality_gain', '6', '1', '岛宅打榜活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_market_clean_gain', '8', '1', '岛宅集市清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_market_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_market_vitality_gain', '8', '1', '岛宅集市活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_market_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_clean_gain', '8', '1', '岛宅送东西清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_vitality_gain', '8', '1', '岛宅送东西活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_cooldown_minutes', '30', '1', '岛宅饭水都喂后共用冷却(分钟)', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_cooldown_minutes');
