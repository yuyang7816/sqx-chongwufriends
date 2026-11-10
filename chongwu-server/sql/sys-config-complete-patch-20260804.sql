-- =============================================================================
-- 完整补齐脚本（生产库 chongwu）
-- 覆盖：短信宝账号密码、短信相关、邮件/地图/个推/OSS/协议、短信模板、岛宅字段
-- 可重复执行（INSERT ... WHERE NOT EXISTS / 条件 ALTER）
-- =============================================================================

-- ---------- 1) island_pet 字段（若不存在则补） ----------
SET @sql = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE island_pet ADD COLUMN food_bowl_filled TINYINT DEFAULT 0 COMMENT ''饭盆本轮已喂''',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='island_pet' AND COLUMN_NAME='food_bowl_filled');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE island_pet ADD COLUMN water_bowl_filled TINYINT DEFAULT 0 COMMENT ''水盆本轮已喂''',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='island_pet' AND COLUMN_NAME='water_bowl_filled');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE island_pet ADD COLUMN daily_progress INT DEFAULT 50 COMMENT ''今日进度0-100''',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='island_pet' AND COLUMN_NAME='daily_progress');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = (SELECT IF(COUNT(*)=0,
  'ALTER TABLE island_pet ADD COLUMN stat_date DATE DEFAULT NULL COMMENT ''属性结算日期''',
  'SELECT 1') FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='island_pet' AND COLUMN_NAME='stat_date');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------- 2) 短信配置（短信宝） ----------
-- 服务商：dxb / 3 / 短信宝 均可；代码已兼容
UPDATE sys_config
SET param_value = 'dxb',
    remark = '短信服务商：dxb(短信宝)/aliyun/tencent，也支持码值 3/2/1',
    param_type = 'duanxin',
    param_type_desc = '短信',
    status = '1'
WHERE param_key = 'sms_provider';

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_provider', 'dxb', '1', '短信服务商：dxb(短信宝)/aliyun/tencent，也支持码值 3/2/1', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_provider');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_smsbao_username', '', '1', '短信宝账号', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_smsbao_username');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_smsbao_password', '', '1', '短信宝密码（明文，发送时自动MD5）', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_smsbao_password');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_ali_access_key', '', '1', '阿里云短信AccessKey', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_ali_access_key');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_ali_access_secret', '', '1', '阿里云短信AccessSecret', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_ali_access_secret');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_tencent_sdk_appid', '', '1', '腾讯云短信SDK AppID', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_tencent_sdk_appid');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sms_tencent_appkey', '', '1', '腾讯云短信AppKey', 'duanxin', '短信'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sms_tencent_appkey');

-- ---------- 3) 邮件配置 ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'mail_host', '', '1', '邮件SMTP主机', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'mail_host');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'mail_port', '465', '1', '邮件SMTP端口', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'mail_port');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'mail_send_mail', '', '1', '发件邮箱账号', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'mail_send_mail');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'mail_auth_code', '', '1', '发件邮箱授权码', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'mail_auth_code');

-- ---------- 4) 地图 / 个推 / 域名 ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'map_tianditu_key', '', '1', '天地图Key', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'map_tianditu_key');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'google_map_key', '', '1', '谷歌地图Key', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'google_map_key');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'map_tencent_key', '', '1', '腾讯地图Key', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'map_tencent_key');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_getui_appid', '', '1', '个推AppID', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_getui_appid');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_getui_appkey', '', '1', '个推AppKey', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_getui_appkey');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_getui_mastersecret', '', '1', '个推MasterSecret', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_getui_mastersecret');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_admin_domain', 'https://cwjpyadmin.xianmxkj.com', '1', '管理端/文件域名', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_admin_domain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_h5_domain', 'https://cwjpy.xianmxkj.com', '1', 'H5/文件访问域名', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_h5_domain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'sys_server_name', '和它交个朋友', '1', '系统名称（短信签名文案）', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'sys_server_name');

-- ---------- 5) 文件 OSS / 宝塔（本地已有时不覆盖） ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_storage_type', 'local', '1', '存储类型 local/oss/bt', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_storage_type');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_local_upload_path', '/upload', '1', '本地上传目录', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_local_upload_path');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_cdn_domain', '', '1', '文件CDN域名', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_cdn_domain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_endpoint', '', '1', '阿里云OSS Endpoint', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_endpoint');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_bucket', '', '1', '阿里云OSS Bucket', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_bucket');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_ali_oss_accessKeyId', '', '1', '阿里云OSS AccessKeyId', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_ali_oss_accessKeyId');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_ali_oss_secretAccessKey', '', '1', '阿里云OSS SecretAccessKey', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_ali_oss_secretAccessKey');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'file_bt_endpoint', '', '1', '宝塔/BT 本地文件访问地址', 'oss', '文件上传'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'file_bt_endpoint');

-- ---------- 6) 协议配置（后台「协议配置」页） ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'user_agreement', '<p>用户服务协议（请在后台完善）</p>', '1', '用户服务协议', 'xieyi', '协议'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'user_agreement');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'privacy_policy', '<p>隐私政策（请在后台完善）</p>', '1', '隐私政策', 'xieyi', '协议'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'privacy_policy');

-- ---------- 7) 微信支付兜底字段（PayController 会读 pay_wx_app_id） ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'pay_wx_app_id', '', '1', '微信APP支付AppID', 'weixin', '微信支付'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'pay_wx_app_id');

-- ---------- 8) 岛宅每日进度相关（若缺则补） ----------
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_daily_stat_init', '50', '1', '岛宅每日初始进度/属性(%)', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_daily_stat_init');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_progress_gain', '6', '1', '岛宅喂饭/喂水进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_progress_gain', '2', '1', '岛宅打榜进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_progress_gain', '4', '1', '岛宅送东西/集市进度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_progress_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_hunger_gain', '12', '1', '岛宅喂饭/喂水饱食度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_hunger_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_mood_gain', '10', '1', '岛宅喂饭/喂水心情加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_mood_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_clean_gain', '6', '1', '岛宅打榜清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_ranking_vitality_gain', '6', '1', '岛宅打榜活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_ranking_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_market_clean_gain', '8', '1', '岛宅集市清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_market_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_market_vitality_gain', '8', '1', '岛宅集市活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_market_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_clean_gain', '8', '1', '岛宅送东西清洁度加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_clean_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_gift_vitality_gain', '8', '1', '岛宅送东西活力值加成', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_gift_vitality_gain');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'island_feed_cooldown_minutes', '30', '1', '岛宅饭水都喂后共用冷却(分钟)', 'xitong', '全局'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'island_feed_cooldown_minutes');

-- ---------- 9) 短信模板（login/register/reset/bind） ----------
INSERT INTO sms_template (biz_code, content)
SELECT 'login', '【%s】您的登录验证码为%s，10分钟内有效。'
WHERE NOT EXISTS (SELECT 1 FROM sms_template WHERE biz_code = 'login');

INSERT INTO sms_template (biz_code, content)
SELECT 'register', '【%s】您的注册验证码为%s，10分钟内有效。'
WHERE NOT EXISTS (SELECT 1 FROM sms_template WHERE biz_code = 'register');

INSERT INTO sms_template (biz_code, content)
SELECT 'reset', '【%s】您的重置密码验证码为%s，10分钟内有效。'
WHERE NOT EXISTS (SELECT 1 FROM sms_template WHERE biz_code = 'reset');

INSERT INTO sms_template (biz_code, content)
SELECT 'bind', '【%s】您的绑定手机验证码为%s，10分钟内有效。'
WHERE NOT EXISTS (SELECT 1 FROM sms_template WHERE biz_code = 'bind');

-- ---------- 10) 商城分类兜底 ----------
INSERT INTO mall_category (name, sort, status)
SELECT '全部商品', 0, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '全部商品');

INSERT INTO mall_category (name, sort, status)
SELECT '客户投喂', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '客户投喂');

INSERT INTO mall_category (name, sort, status)
SELECT '超值次卡', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '超值次卡');

INSERT INTO mall_category (name, sort, status)
SELECT '实物赠予', 3, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '实物赠予');
