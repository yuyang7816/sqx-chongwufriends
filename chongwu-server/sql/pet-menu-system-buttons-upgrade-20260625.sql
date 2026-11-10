-- 系统管理模块按钮权限补丁（菜单/角色/配置/管理员）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2081, 2082, 2083, 2084);
DELETE FROM sys_menu WHERE menu_id IN (2081, 2082, 2083, 2084);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2081, 2071, '菜单操作', NULL, 'sys:menu:save,sys:menu:update,sys:menu:delete,sys:menu:info,sys:menu:select', 2, NULL, 0),
(2082, 2072, '角色操作', NULL, 'sys:role:save,sys:role:update,sys:role:delete,sys:role:info,sys:role:select', 2, NULL, 0),
(2083, 2073, '配置操作', NULL, 'sys:config:update,sys:config:save,allocationList:update', 2, NULL, 0),
(2084, 2074, '管理员操作', NULL, 'sys:user:save,sys:user:update,sys:user:delete,sys:user:info', 2, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2081, 2082, 2083, 2084)
ON DUPLICATE KEY UPDATE role_id = role_id;
