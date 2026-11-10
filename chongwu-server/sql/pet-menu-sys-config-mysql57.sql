-- 宠物业务管理端菜单 + 系统配置（MySQL 5.7）
-- 执行前请备份 sys_menu、sys_config、sys_role_menu
-- 可重复执行：先删宠物菜单再插入；配置项按 param_key 更新

SET NAMES utf8mb4;

-- ========== 清理旧宠物菜单（menu_id 2000-2099） ==========
DELETE FROM sys_role_menu WHERE menu_id >= 2000 AND menu_id < 2100;
DELETE FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2100;

-- ========== sys_menu 宠物业务完整菜单 ==========
-- type: 0目录 1菜单 2按钮

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2000, 0, '控制台', 'dashboard', NULL, 1, 'dashboard', 0),
(2001, 0, '用户管理', 'users', 'userList:list', 1, 'user', 1),
(2034, 0, '首页装修', 'content/decorations', 'bannerList:list', 1, 'picture', 2),
(2010, 0, '宠物管理', NULL, NULL, 0, 'peoples', 3),
(2011, 2010, '门店管理', 'pet/stores', 'pet:store:list', 1, 'shop', 0),
(2012, 2010, '宠物档案', 'pet/archives', 'pet:archive:list', 1, 'documentation', 1),
(2020, 0, '社区管理', NULL, NULL, 0, 'message', 4),
(2021, 2020, '动态管理', 'pet/community', 'pet:community:list', 1, 'message', 0),
(2023, 2020, '投诉管理', 'pet/community/complaints', 'pet:community:complaint:list', 1, 'warning', 1),
(2022, 2020, '敏感词', 'pet/sensitive', 'pet:sensitive:list', 1, 'edit', 2),
(2090, 2021, '动态审核', NULL, 'pet:community:audit', 2, NULL, 0),
(2091, 2021, '动态下架', NULL, 'pet:community:offline', 2, NULL, 1),
(2092, 2021, '动态删除', NULL, 'pet:community:delete', 2, NULL, 2),
(2093, 2021, '评论删除', NULL, 'pet:community:comment:delete', 2, NULL, 3),
(2094, 2021, '批量审核', NULL, 'pet:community:batchAudit', 2, NULL, 4),
(2095, 2023, '投诉处理', NULL, 'pet:community:complaint:handle', 2, NULL, 0),
(2096, 2023, '投诉类型配置', NULL, 'pet:community:complaint:config', 2, NULL, 1),
(2030, 0, '商城管理', NULL, NULL, 0, 'shopping', 5),
(2031, 2030, '商品管理', 'pet/mall', 'pet:mall:list', 1, 'goods', 0),
(2032, 2030, '订单管理', 'pet/orders', 'pet:order:list', 1, 'order', 1),
(2184, 0, '投喂模块', NULL, NULL, 0, 'present', 7),
(2160, 2184, '投喂商品', 'pet/feed/products', 'pet:mall:list', 1, 'goods', 0),
(2033, 2184, '投喂订单', 'pet/feed', 'pet:feed:list', 1, 'list', 1),
(2035, 2034, '新增轮播', NULL, 'bannerList:add', 2, NULL, 0),
(2036, 2034, '编辑轮播', NULL, 'bannerList:update', 2, NULL, 1),
(2037, 2034, '删除轮播', NULL, 'bannerList:delete', 2, NULL, 2),
(2040, 0, '领养管理', NULL, NULL, 0, 'star', 6),
(2041, 2040, '领养申请', 'pet/adopt', 'pet:adopt:list', 1, 'form', 0),
(2050, 0, '营销管理', NULL, NULL, 0, 'chart', 7),
(2051, 2050, '岛宅任务', 'pet/tasks', 'pet:task:list', 1, 'skill', 0),
(2052, 2050, '打榜排行', 'pet/ranking', 'pet:ranking:list', 1, 'rank', 1),
(2053, 2050, '养宠攻略', 'pet/strategy', 'pet:strategy:list', 1, 'guide', 2),
(2054, 2050, '集市道具', 'pet/market', 'pet:market:list', 1, 'shopping', 3),
(2055, 2050, '岛宅状态', 'pet/island-pets', 'pet:island:list', 1, 'peoples', 4),
(2060, 0, '支付管理', NULL, NULL, 0, 'money', 8),
(2061, 2060, '支付单', 'pet/payment-orders', 'pet:payment:order:list', 1, 'money', 0),
(2062, 2060, '支付回调日志', 'pet/payment-log', 'pet:payment:list', 1, 'log', 1),
(2070, 0, '系统管理', NULL, NULL, 0, 'system', 9),
(2071, 2070, '菜单管理', 'system/menus', 'sys:menu:list', 1, 'tree-table', 0),
(2072, 2070, '角色权限', 'system/roles', 'sys:role:list', 1, 'peoples', 1),
(2073, 2070, '系统配置', 'system/configs', 'sys:config:list,allocationList:list', 1, 'edit', 2),
(2074, 2070, '管理员', 'system/admins', 'sys:user:list', 1, 'user', 3),
(2081, 2071, '菜单操作', NULL, 'sys:menu:save,sys:menu:update,sys:menu:delete,sys:menu:info,sys:menu:select', 2, NULL, 0),
(2082, 2072, '角色操作', NULL, 'sys:role:save,sys:role:update,sys:role:delete,sys:role:info,sys:role:select', 2, NULL, 0),
(2083, 2073, '配置操作', NULL, 'sys:config:update,sys:config:save,allocationList:update', 2, NULL, 0),
(2084, 2074, '管理员操作', NULL, 'sys:user:save,sys:user:update,sys:user:delete,sys:user:info', 2, NULL, 0),
(2080, 2001, '查看用户资产', NULL, 'pet:asset:list', 2, NULL, 3);

-- 超级管理员角色(1)授权宠物菜单，如角色ID不同请改 role_id
DELETE FROM sys_role_menu WHERE menu_id >= 2000 AND menu_id < 2100;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2100;

-- ========== sys_config 业务配置项 ==========
-- status: 1启用

DELETE FROM sys_config WHERE param_key IN (
  'sys_user_ma_appid','sys_user_ma_secret','pay_wx_jsapi_app_id','pay_wx_jsapi_mch_id','pay_wx_jsapi_api_key',
  'pay_wx_notify_url','pay_wx_h5_app_id','pay_wx_mch_id','pay_wx_mch_key','pay_wx_key_path',
  'pay_ali_appid','pay_ali_private_key','pay_ali_public_key','pay_ali_notify_url','pay_ali_mode','pay_ali_sandbox',
  'sms_provider','sms_smsbao_username','sms_smsbao_password','sms_ali_access_key','sms_ali_access_secret','sms_tencent_sdk_appid','sms_tencent_appkey','wecom_qr_url','wecom_service_link',
  'sys_h5_domain','file_storage_type','file_local_upload_path','file_cdn_domain',
  'community_audit_switch','community_complaint_types','mall_order_audit_switch','sys_server_name',
  'sys_withdraw_max','sys_withdraw_min','sys_withdraw_rate','map_platform','map_tencent_key'
);

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES
('sys_user_ma_appid', '', '1', '用户端微信小程序AppID', 'weixin', '微信配置'),
('sys_user_ma_secret', '', '1', '用户端微信小程序AppSecret', 'weixin', '微信配置'),
('pay_wx_jsapi_app_id', '', '1', '微信小程序支付AppID', 'weixin', '微信支付'),
('pay_wx_jsapi_mch_id', '', '1', '微信小程序支付商户号', 'weixin', '微信支付'),
('pay_wx_jsapi_api_key', '', '1', '微信小程序支付API密钥', 'weixin', '微信支付'),
('pay_wx_notify_url', '', '1', '微信支付回调地址', 'weixin', '微信支付'),
('pay_wx_h5_app_id', '', '1', '微信H5支付AppID', 'weixin', '微信支付'),
('pay_wx_mch_id', '', '1', '微信支付商户号', 'weixin', '微信支付'),
('pay_wx_mch_key', '', '1', '微信支付商户密钥', 'weixin', '微信支付'),
('pay_wx_key_path', '', '1', '微信支付证书路径', 'weixin', '微信支付'),
('pay_ali_appid', '', '1', '支付宝AppID', 'zhifubao', '支付宝'),
('pay_ali_private_key', '', '1', '支付宝应用私钥', 'zhifubao', '支付宝'),
('pay_ali_public_key', '', '1', '支付宝公钥', 'zhifubao', '支付宝'),
('pay_ali_notify_url', '', '1', '支付宝回调地址', 'zhifubao', '支付宝'),
('pay_ali_mode', '1', '1', '支付宝模式1公钥2证书', 'zhifubao', '支付宝'),
('pay_ali_sandbox', '2', '1', '支付宝沙箱1是2否', 'zhifubao', '支付宝'),
('sms_provider', 'dxb', '1', '短信服务商：dxb(短信宝)/aliyun/tencent，也支持码值 3/2/1', 'duanxin', '短信'),
('sms_smsbao_username', '', '1', '短信宝账号', 'duanxin', '短信'),
('sms_smsbao_password', '', '1', '短信宝密码（明文，发送时自动MD5）', 'duanxin', '短信'),
('sms_ali_access_key', '', '1', '阿里云短信AccessKey', 'duanxin', '短信'),
('sms_ali_access_secret', '', '1', '阿里云短信AccessSecret', 'duanxin', '短信'),
('sms_tencent_sdk_appid', '', '1', '腾讯云短信SDK AppID', 'duanxin', '短信'),
('sms_tencent_appkey', '', '1', '腾讯云短信AppKey', 'duanxin', '短信'),
('wecom_qr_url', '', '1', '企业微信客服二维码URL', 'weixin', '企微'),
('wecom_service_link', '', '1', '客服链接', 'weixin', '企微'),
('sys_h5_domain', '', '1', 'H5/文件访问域名', 'oss', '文件上传'),
('file_storage_type', 'local', '1', '存储类型 local/oss/bt', 'oss', '文件上传'),
('file_local_upload_path', '/upload', '1', '本地上传目录', 'oss', '文件上传'),
('file_cdn_domain', '', '1', '文件CDN域名', 'oss', '文件上传'),
('community_audit_switch', '1', '1', '社区发布审核开关1开0关', 'kaiguan', '开关'),
('community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉'),
('mall_order_audit_switch', '0', '1', '商城订单审核开关', 'kaiguan', '开关'),
('sys_server_name', '和它交个朋友', '1', '系统名称', 'xitong', '全局'),
('sys_withdraw_max', '5000', '1', '最大提现金额', 'fuwufei', '服务费'),
('sys_withdraw_min', '10', '1', '最小提现金额', 'fuwufei', '服务费'),
('sys_withdraw_rate', '0.01', '1', '提现手续费率', 'fuwufei', '服务费'),
('map_platform', 'tencent', '1', '地图平台 tencent/google/tianditu', 'xitong', '全局'),
('map_tencent_key', '', '1', '腾讯地图Key', 'xitong', '全局');

-- 商城分类初始数据（表 mall_category 需已存在）
DELETE FROM mall_category WHERE name IN ('全部商品', '客户投喂', '超值次卡', '实物赠予');
INSERT INTO mall_category (name, sort, status) VALUES
('全部商品', 0, 1),
('客户投喂', 1, 1),
('超值次卡', 2, 1),
('实物赠予', 3, 1);
