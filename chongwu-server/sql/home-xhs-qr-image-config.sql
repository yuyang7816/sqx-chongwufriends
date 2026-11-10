-- 首页小红书二维码（系统配置 → 图片配置）
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'home_xhs_qr_url', '', '1', '首页关注小红书二维码图片', 'image', '图片配置'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'home_xhs_qr_url');
