-- 用户资产：从「宠物管理」独立菜单并入「用户管理」按钮权限
-- 可重复执行
SET NAMES utf8mb4;

UPDATE sys_menu
SET parent_id = 2001,
    name = '查看用户资产',
    url = NULL,
    perms = 'pet:asset:list',
    type = 2,
    icon = NULL,
    order_num = 3
WHERE menu_id = 2080;

-- 兜底：若不存在则插入为用户管理下的按钮
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 2080, 2001, '查看用户资产', NULL, 'pet:asset:list', 2, NULL, 3
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2080);

-- 超管保留该权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2080);
