-- 用户管理改为顶级直达菜单：点击即进用户列表，不再展开子菜单
-- 可重复执行

SET NAMES utf8mb4;

-- 将「用户管理」目录提升为页面菜单
UPDATE sys_menu
SET url = 'users',
    perms = 'userList:list',
    type = 1,
    icon = IFNULL(NULLIF(icon, ''), 'user'),
    name = '用户管理'
WHERE menu_id = 2001;

-- 删除按钮挂到「用户管理」下
UPDATE sys_menu
SET parent_id = 2001
WHERE menu_id = 2131;

-- 角色原先勾了「用户列表」的，补上「用户管理」
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role_id, 2001
FROM sys_role_menu
WHERE menu_id = 2002;

-- 移除多余的「用户列表」子菜单
DELETE FROM sys_role_menu WHERE menu_id = 2002;
DELETE FROM sys_menu WHERE menu_id = 2002;
