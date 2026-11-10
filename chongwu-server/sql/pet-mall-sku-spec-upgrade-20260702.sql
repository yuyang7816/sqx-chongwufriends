-- 商城规格模板库 + SPU 规格模式（MySQL 5.7+，可重复执行）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS mall_sku_spec (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spec_name VARCHAR(128) NOT NULL COMMENT '规格名称',
    price DECIMAL(10,2) DEFAULT 0 COMMENT '默认售价',
    point_price INT DEFAULT 0 COMMENT '默认积分价',
    default_stock INT DEFAULT 0 COMMENT '默认库存',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    KEY idx_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城规格模板';

-- spec_mode: 1单规格 2多规格
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_spu' AND COLUMN_NAME = 'spec_mode');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE mall_spu ADD COLUMN spec_mode TINYINT DEFAULT 1 COMMENT ''1单规格 2多规格'' AFTER sort',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_sku' AND COLUMN_NAME = 'spec_id');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE mall_sku ADD COLUMN spec_id BIGINT DEFAULT NULL COMMENT ''关联规格模板ID'' AFTER spu_id',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 规格模板种子
INSERT INTO mall_sku_spec (id, spec_name, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8001, '标准装', 29.90, 0, 999, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8001);

INSERT INTO mall_sku_spec (id, spec_name, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8002, '小份装', 19.90, 0, 999, 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8002);

INSERT INTO mall_sku_spec (id, spec_name, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8003, '大份装', 39.90, 0, 999, 3, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8003);

INSERT INTO mall_sku_spec (id, spec_name, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8004, '积分兑换装', 0, 200, 50, 4, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8004);

-- 菜单：规格管理（商城管理下）
DELETE FROM sys_role_menu WHERE menu_id IN (2150, 2151, 2152, 2153);
DELETE FROM sys_menu WHERE menu_id IN (2150, 2151, 2152, 2153);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2150, 2030, '规格管理', 'pet/mall/specs', 'pet:mall:spec:list', 1, 'list', 2),
(2151, 2150, '新增规格', NULL, 'pet:mall:spec:add', 2, NULL, 0),
(2152, 2150, '编辑规格', NULL, 'pet:mall:spec:update', 2, NULL, 1),
(2153, 2150, '删除规格', NULL, 'pet:mall:spec:delete', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2150, 2151, 2152, 2153)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 商品7001设为多规格示例
UPDATE mall_spu SET spec_mode = 2 WHERE id = 7001;
