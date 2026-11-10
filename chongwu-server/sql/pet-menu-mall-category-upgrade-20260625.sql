-- 商城分类管理菜单 + 商品管理按钮权限补丁（MySQL 5.7+）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2085, 2086, 2087, 2088, 2097, 2098, 2099, 2100);
DELETE FROM sys_menu WHERE menu_id IN (2085, 2086, 2087, 2088, 2097, 2098, 2099, 2100);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2085, 2030, '分类管理', 'pet/mall/categories', 'pet:mall:category:list', 1, 'category', 0),
(2086, 2085, '新增分类', NULL, 'pet:mall:category:add', 2, NULL, 0),
(2087, 2085, '编辑分类', NULL, 'pet:mall:category:update', 2, NULL, 1),
(2088, 2085, '删除分类', NULL, 'pet:mall:category:delete', 2, NULL, 2),
(2097, 2031, '新增商品', NULL, 'pet:mall:add', 2, NULL, 0),
(2098, 2031, '编辑商品', NULL, 'pet:mall:update', 2, NULL, 1),
(2099, 2031, '删除商品', NULL, 'pet:mall:delete', 2, NULL, 2),
(2100, 2031, '商品详情', NULL, 'pet:mall:info', 2, NULL, 3);

-- 分类管理排在商品管理之前
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2031;
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2032;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2033;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2085, 2086, 2087, 2088, 2097, 2098, 2099, 2100)
ON DUPLICATE KEY UPDATE role_id = role_id;
