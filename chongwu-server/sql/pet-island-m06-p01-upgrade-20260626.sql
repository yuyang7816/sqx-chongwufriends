-- M06 完善：集市道具封面、双常驻测试档案、任务种子
SET NAMES utf8mb4;

-- 集市道具封面图
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_market_item' AND COLUMN_NAME = 'cover_url'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE island_market_item ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT ''封面图'' AFTER name',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE island_market_item SET cover_url = 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png'
WHERE id = 4001 AND (cover_url IS NULL OR cover_url = '');
UPDATE island_market_item SET cover_url = 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png'
WHERE id = 4002 AND (cover_url IS NULL OR cover_url = '');
UPDATE island_market_item SET cover_url = 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png'
WHERE id = 4003 AND (cover_url IS NULL OR cover_url = '');

-- 第二只已审核宠物（双常驻 E2E）
UPDATE pet_archive SET audit_status = 1, pet_type = 'cat', name = COALESCE(NULLIF(name, ''), '奶茶')
WHERE id = 2;
UPDATE pet_archive SET avatar_url = COALESCE(NULLIF(avatar_url, ''), 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png')
WHERE id = 2;

-- 重置测试用户任务记录（便于重复 E2E）
DELETE FROM island_task_record WHERE user_id = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);
