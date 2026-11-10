-- 投诉类型列表增删改按钮权限补丁（MySQL 5.7+）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2102, 2103, 2104);
DELETE FROM sys_menu WHERE menu_id IN (2102, 2103, 2104);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2102, 2101, '新增类型', NULL, 'pet:community:complaint:type:add', 2, NULL, 0),
(2103, 2101, '编辑类型', NULL, 'pet:community:complaint:type:update', 2, NULL, 1),
(2104, 2101, '删除类型', NULL, 'pet:community:complaint:type:delete', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2102, 2103, 2104)
ON DUPLICATE KEY UPDATE role_id = role_id;
