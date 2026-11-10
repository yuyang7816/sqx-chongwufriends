-- 微信小程序授权登录配置说明升级
-- 确保用户端小程序 AppId/Secret 配置项存在，供微信授权登录使用

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_user_ma_appid', '', '1', '用户端微信小程序AppID（微信授权登录必填）', 'weixin', '微信配置'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_user_ma_appid');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_user_ma_secret', '', '1', '用户端微信小程序AppSecret（微信授权登录必填）', 'weixin', '微信配置'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_user_ma_secret');

UPDATE sys_config SET remark = '用户端微信小程序AppID（微信授权登录必填）', param_type = 'weixin', param_type_desc = '微信配置'
WHERE param_key = 'sys_user_ma_appid';

UPDATE sys_config SET remark = '用户端微信小程序AppSecret（微信授权登录必填）', param_type = 'weixin', param_type_desc = '微信配置'
WHERE param_key = 'sys_user_ma_secret';
