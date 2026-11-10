-- 养宠攻略分类管理菜单及按钮权限（可重复执行）
SET NAMES utf8mb4;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2180, 2050, '攻略分类', 'pet/strategy/categories', 'pet:strategy:category:list', 1, 'category', 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  url = VALUES(url),
  perms = VALUES(perms),
  type = VALUES(type),
  icon = VALUES(icon),
  order_num = VALUES(order_num);

-- 养宠攻略排在分类之后
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2053 AND parent_id = 2050;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2181, 2180, '新增分类', NULL, 'pet:strategy:category:add', 2, NULL, 0),
(2182, 2180, '编辑分类', NULL, 'pet:strategy:category:update', 2, NULL, 1),
(2183, 2180, '删除分类', NULL, 'pet:strategy:category:delete', 2, NULL, 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  perms = VALUES(perms),
  type = VALUES(type),
  order_num = VALUES(order_num);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id IN (2180, 2181, 2182, 2183);
