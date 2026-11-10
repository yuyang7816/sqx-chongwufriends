-- 管理端「投喂商品」菜单：挂在商城管理下，复用 pet:mall:* 增删改权限
-- 可重复执行

SET NAMES utf8mb4;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2160, 2030, '投喂商品', 'pet/feed/products', 'pet:mall:list', 1, 'goods', 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  url = VALUES(url),
  perms = VALUES(perms),
  type = VALUES(type),
  icon = VALUES(icon),
  order_num = VALUES(order_num);

-- 按钮权限复用商品管理按钮文案（便于角色勾选展示）
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2161, 2160, '新增投喂商品', NULL, 'pet:mall:add', 2, NULL, 0),
(2162, 2160, '编辑投喂商品', NULL, 'pet:mall:update', 2, NULL, 1),
(2163, 2160, '删除投喂商品', NULL, 'pet:mall:delete', 2, NULL, 2)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  perms = VALUES(perms),
  type = VALUES(type),
  order_num = VALUES(order_num);

-- 超级管理员补齐菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id IN (2160, 2161, 2162, 2163);

-- 调整投喂订单排序，避免与投喂商品冲突
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2033;
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2031;
