-- 消息管理菜单及按钮权限（可重复执行）
SET NAMES utf8mb4;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2170, 0, '消息管理', 'pet/messages', 'pet:message:list', 1, 'message', 20)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  url = VALUES(url),
  perms = VALUES(perms),
  type = VALUES(type),
  icon = VALUES(icon),
  order_num = VALUES(order_num);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2171, 2170, '发送公告', NULL, 'pet:message:send', 2, NULL, 0),
(2172, 2170, '查看详情', NULL, 'pet:message:info', 2, NULL, 1),
(2173, 2170, '删除消息', NULL, 'pet:message:delete', 2, NULL, 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  perms = VALUES(perms),
  type = VALUES(type),
  order_num = VALUES(order_num);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id IN (2170, 2171, 2172, 2173);
