-- 规格模板改为「规格标题 + 多组规格值」结构（对齐酒吧后台添加规格）
SET NAMES utf8mb4;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_sku_spec' AND COLUMN_NAME = 'rule_value');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE mall_sku_spec ADD COLUMN rule_value TEXT NULL COMMENT ''规格值JSON [{value,detail}]'' AFTER spec_name',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 兼容旧数据：把原规格名迁移为单组规格值
UPDATE mall_sku_spec
SET rule_value = CONCAT('[{"value":"规格","detail":"', REPLACE(IFNULL(spec_name, ''), '"', ''), '"}]')
WHERE (rule_value IS NULL OR rule_value = '' OR rule_value = 'null')
  AND IFNULL(is_deleted, 0) = 0
  AND IFNULL(spec_name, '') <> '';
