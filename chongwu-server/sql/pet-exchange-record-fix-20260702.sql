-- 修复历史积分兑换订单 biz_type，确保兑换明细可查询
UPDATE mall_order
SET biz_type = 4
WHERE pay_type = 2
  AND IFNULL(point_amount, 0) > 0
  AND IFNULL(biz_type, 0) <> 4;

-- 周边兑换管理菜单（幂等）
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 2147, 2030, '周边兑换', 'pet/exchange', 'pet:exchange:list', 1, 'goods', 6
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2147);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 2148, 2147, '兑换履约', NULL, 'pet:exchange:update', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2148);
