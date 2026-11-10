-- 管理端宠物业务菜单与按钮权限全量补丁（MySQL 5.7+）
-- 用途：补全优惠券模板/发券/兑换/投喂反馈等 M09 菜单，修复岛宅任务「新增」按钮权限
-- 可重复执行；执行前建议备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

-- ========== M09 商城运营菜单（优惠券模板 / 发券 / 周边兑换 / 投喂反馈） ==========
DELETE FROM sys_role_menu WHERE menu_id IN (2140, 2141, 2142, 2143, 2144, 2145, 2146, 2147, 2148);
DELETE FROM sys_menu WHERE menu_id IN (2140, 2141, 2142, 2143, 2144, 2145, 2146, 2147, 2148);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2140, 2030, '优惠券模板', 'pet/coupon', 'pet:coupon:list', 1, 'ticket', 4),
(2141, 2140, '新增模板', NULL, 'pet:coupon:add', 2, NULL, 0),
(2142, 2140, '编辑模板', NULL, 'pet:coupon:update', 2, NULL, 1),
(2143, 2140, '删除模板', NULL, 'pet:coupon:delete', 2, NULL, 2),
(2144, 2030, '优惠券发放', 'pet/coupon-grant', 'pet:coupon:grant', 1, 'present', 5),
(2145, 2144, '执行发券', NULL, 'pet:coupon:grant', 2, NULL, 0),
(2146, 2033, '上传反馈视频', NULL, 'pet:feed:feedback', 2, NULL, 0),
(2147, 2030, '周边兑换', 'pet/exchange', 'pet:exchange:list', 1, 'goods', 6),
(2148, 2147, '兑换履约', NULL, 'pet:exchange:update', 2, NULL, 0);

-- 商城管理子菜单排序
UPDATE sys_menu SET order_num = 0 WHERE menu_id = 2085;
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2031;
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2032;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2033;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 2140;
UPDATE sys_menu SET order_num = 5 WHERE menu_id = 2144;
UPDATE sys_menu SET order_num = 6 WHERE menu_id = 2147;

-- ========== 岛宅任务「新增任务」按钮（2121 已被领养回访占用，使用 2149） ==========
DELETE FROM sys_role_menu WHERE menu_id = 2149;
DELETE FROM sys_menu WHERE menu_id = 2149;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2149, 2051, '新增任务', NULL, 'pet:task:add', 2, NULL, 0);

-- 调整任务按钮排序
UPDATE sys_menu SET order_num = 1 WHERE menu_id = 2122;
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2123;

-- ========== 超级管理员(role_id=1) 授权全部宠物业务菜单 ==========
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id >= 2000
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 本次新增/补丁 menu_id 强制授权 role 1
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2140, 2141, 2142, 2143, 2144, 2145, 2146, 2147, 2148, 2149)
ON DUPLICATE KEY UPDATE role_id = role_id;
