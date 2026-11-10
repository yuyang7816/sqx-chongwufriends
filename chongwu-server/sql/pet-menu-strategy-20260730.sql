-- 养宠攻略菜单及按钮权限（可重复执行）
SET NAMES utf8mb4;

-- 父级「营销管理」
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2050, 0, '营销管理', NULL, NULL, 0, 'chart', 8)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  type = VALUES(type),
  icon = VALUES(icon),
  order_num = VALUES(order_num);

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

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2053, 2050, '养宠攻略', 'pet/strategy', 'pet:strategy:list', 1, 'guide', 3)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  url = VALUES(url),
  perms = VALUES(perms),
  type = VALUES(type),
  icon = VALUES(icon),
  order_num = VALUES(order_num);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2181, 2180, '新增分类', NULL, 'pet:strategy:category:add', 2, NULL, 0),
(2182, 2180, '编辑分类', NULL, 'pet:strategy:category:update', 2, NULL, 1),
(2183, 2180, '删除分类', NULL, 'pet:strategy:category:delete', 2, NULL, 2),
(2124, 2053, '新增攻略', NULL, 'pet:strategy:add', 2, NULL, 0),
(2125, 2053, '编辑攻略', NULL, 'pet:strategy:update', 2, NULL, 1),
(2126, 2053, '删除攻略', NULL, 'pet:strategy:delete', 2, NULL, 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  perms = VALUES(perms),
  type = VALUES(type),
  order_num = VALUES(order_num);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id IN (2050, 2053, 2124, 2125, 2126, 2180, 2181, 2182, 2183);
