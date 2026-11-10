-- 用户360：补齐用户管理按钮权限，并确保超管拥有详情 Tab/操作所需业务菜单
-- 可重复执行
-- 说明：详情页本身挂在「用户管理」下，不新增顶级菜单；Tab/按钮复用各业务 pet:* 权限

SET NAMES utf8mb4;

-- ========== 用户管理按钮：查看详情 / 编辑用户 ==========
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2174, 2001, '查看用户详情', NULL, 'userList:info', 2, NULL, 0),
(2175, 2001, '编辑用户', NULL, 'userList:update', 2, NULL, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  perms = VALUES(perms),
  type = VALUES(type),
  order_num = VALUES(order_num);

-- 删除用户按钮排序靠后
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2131;

-- ========== 超管：本次新增按钮 ==========
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id IN (2174, 2175);

-- ========== 超管：用户360一期 Tab / 快捷操作所需业务权限 ==========
-- 用户管理、商城订单、商品、投喂、资产、券模板/发券、岛宅、消息
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (
  2001, 2131, 2174, 2175,
  2031, 2032, 2033, 2080,
  2055, 2130,
  2140, 2141, 2142, 2143, 2144, 2145,
  2170, 2171, 2172, 2173
)
OR m.perms IN (
  'userList:list', 'userList:info', 'userList:update', 'userList:delete',
  'pet:order:list', 'pet:mall:list', 'pet:feed:list',
  'pet:asset:list', 'pet:coupon:list', 'pet:coupon:grant',
  'pet:island:list', 'pet:island:update',
  'pet:message:list', 'pet:message:send', 'pet:message:info', 'pet:message:delete'
);

-- ========== 兜底：超管补齐全部宠物业务菜单（含 >=2100 按钮权限） ==========
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m WHERE m.menu_id >= 2000;
