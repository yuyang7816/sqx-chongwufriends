-- M09 优惠券模板管理菜单
DELETE FROM sys_role_menu WHERE menu_id IN (2140, 2141, 2142, 2143);
DELETE FROM sys_menu WHERE menu_id IN (2140, 2141, 2142, 2143);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2140, 2030, '优惠券模板', 'pet/coupon', 'pet:coupon:list', 1, 'ticket', 4),
(2141, 2140, '新增模板', NULL, 'pet:coupon:add', 2, NULL, 0),
(2142, 2140, '编辑模板', NULL, 'pet:coupon:update', 2, NULL, 1),
(2143, 2140, '删除模板', NULL, 'pet:coupon:delete', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2140, 2141, 2142, 2143);
