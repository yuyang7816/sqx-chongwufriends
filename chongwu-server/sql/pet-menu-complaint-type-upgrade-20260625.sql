-- 社区管理-投诉类型菜单补丁（MySQL 5.7+）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2101, 2102);
DELETE FROM sys_menu WHERE menu_id IN (2101, 2102);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2101, 2020, '投诉类型', 'pet/community/complaint-types', 'pet:community:complaint:type', 1, 'edit', 2),
(2102, 2101, '保存类型', NULL, 'pet:community:complaint:config', 2, NULL, 0);

-- 敏感词排序后移
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2022;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2101, 2102)
ON DUPLICATE KEY UPDATE role_id = role_id;
