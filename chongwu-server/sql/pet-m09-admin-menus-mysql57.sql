-- M09 个人中心运营菜单：发券 / 投喂反馈 / 周边兑换履约
DELETE FROM sys_role_menu WHERE menu_id IN (2144, 2145, 2146, 2147, 2148, 2149);
DELETE FROM sys_menu WHERE menu_id IN (2144, 2145, 2146, 2147, 2148, 2149);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2144, 2030, '优惠券发放', 'pet/coupon-grant', 'pet:coupon:grant', 1, 'present', 5),
(2145, 2144, '执行发券', NULL, 'pet:coupon:grant', 2, NULL, 0),
(2146, 2033, '上传反馈视频', NULL, 'pet:feed:feedback', 2, NULL, 0),
(2147, 2030, '周边兑换', 'pet/exchange', 'pet:exchange:list', 1, 'goods', 6),
(2148, 2147, '兑换履约', NULL, 'pet:exchange:update', 2, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2144, 2145, 2146, 2147, 2148);
