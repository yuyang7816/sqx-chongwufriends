-- 商城商品规格管理菜单 + 多规格种子数据（MySQL 5.7+）
-- 可重复执行

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2101, 2102, 2103, 2104);
DELETE FROM sys_menu WHERE menu_id IN (2101, 2102, 2103, 2104);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2101, 2031, '规格列表', NULL, 'pet:mall:sku:list', 2, NULL, 4),
(2102, 2031, '新增规格', NULL, 'pet:mall:sku:add', 2, NULL, 5),
(2103, 2031, '编辑规格', NULL, 'pet:mall:sku:update', 2, NULL, 6),
(2104, 2031, '删除规格', NULL, 'pet:mall:sku:delete', 2, NULL, 7);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2101, 2102, 2103, 2104)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 商品 7001 增加多规格示例（小份/大份）
INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7105, 7001, '小份装', 19.90, 0, 999, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE id = 7105);

INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7106, 7001, '大份装', 39.90, 0, 999, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE id = 7106);

UPDATE mall_sku SET sku_name = '标准装' WHERE id = 7101 AND spu_id = 7001;
