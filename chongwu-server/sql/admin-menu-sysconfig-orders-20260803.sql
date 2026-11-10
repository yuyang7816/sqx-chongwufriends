-- 菜单与配置调整 2026-08-03
-- 1) 系统配置提到顶级（与支付管理同级，不在支付管理下）
-- 2) 去掉支付单
-- 3) 商城管理下：商城订单 / 投喂订单 同级
-- 4) 打榜次数/规则配置归入全局配置(xitong)
-- 说明：管理端侧栏仅支持两级，故订单不再套第三级目录

SET NAMES utf8mb4;

-- ========== 1. 系统配置 → 顶级菜单（与支付管理同级） ==========
UPDATE sys_menu
SET parent_id = 0,
    order_num = 10,
    icon = 'edit',
    type = 1
WHERE menu_id = 2073
  AND name = '系统配置';

-- ========== 2. 去掉支付单 ==========
DELETE FROM sys_role_menu WHERE menu_id = 2061;
DELETE FROM sys_menu WHERE menu_id = 2061;

-- 支付管理下仅保留支付回调日志
UPDATE sys_menu SET order_num = 0 WHERE menu_id = 2062;

-- ========== 3. 商城管理下订单同级：商城订单 / 投喂订单 ==========
UPDATE sys_menu
SET name = '商城订单',
    url = 'pet/orders',
    perms = 'pet:order:list',
    type = 1,
    icon = 'order',
    parent_id = 2030,
    order_num = 2
WHERE menu_id = 2032;

UPDATE sys_menu
SET name = '投喂订单',
    url = 'pet/feed',
    perms = 'pet:feed:list',
    type = 1,
    icon = 'list',
    parent_id = 2030,
    order_num = 3
WHERE menu_id = 2033;

-- 若此前误建了三级「订单」目录下的商城订单，清理并回收权限
DELETE FROM sys_role_menu WHERE menu_id = 2190;
DELETE FROM sys_menu WHERE menu_id = 2190;

-- ========== 4. 打榜次数/规则 → 全局配置 ==========
UPDATE sys_config
SET param_type = 'xitong',
    param_type_desc = '全局'
WHERE param_key IN (
  'ranking_rules',
  'ranking_daily_free_limit',
  'ranking_assist_points_cost',
  'ranking_assist_coins_cost'
);
