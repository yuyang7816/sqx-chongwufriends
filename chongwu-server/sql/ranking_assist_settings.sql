-- 人气打榜：每日免费次数 / 积分消耗 / 金币消耗
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'ranking_daily_free_limit', '10', '1', '每人每日免费打榜次数', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'ranking_daily_free_limit');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'ranking_assist_points_cost', '10', '1', '付费打榜积分消耗', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'ranking_assist_points_cost');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'ranking_assist_coins_cost', '10', '1', '付费打榜金币消耗', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'ranking_assist_coins_cost');
