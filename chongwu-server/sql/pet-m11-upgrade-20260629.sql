-- M11 消息通知：管理端菜单与按钮权限
SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id IN (2151, 2152, 2153, 2154);
DELETE FROM sys_menu WHERE menu_id IN (2151, 2152, 2153, 2154);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2151, 2020, '消息管理', 'pet/messages', 'pet:message:list', 1, 'bell', 5),
(2152, 2151, '发送公告', NULL, 'pet:message:send', 2, NULL, 0),
(2153, 2151, '删除消息', NULL, 'pet:message:delete', 2, NULL, 1),
(2154, 2151, '查看详情', NULL, 'pet:message:info', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2151, 2152, 2153, 2154);
