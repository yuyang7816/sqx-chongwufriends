-- M10 运营与内容：打榜规则、企微配置、菜单按钮权限
SET NAMES utf8mb4;

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'ranking_rules',
       '每周一 0 点重置榜单；每日最多打榜 10 次；每月 TOP3 宠物将获得专属周边礼包。',
       '1',
       '打榜规则说明（用户端展示）',
       'marketing',
       '打榜运营'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'ranking_rules');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'wecom_service_link', '', '1', '企微客服链接', 'weixin', '企微'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'wecom_service_link');

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'wecom_benefit_text',
       '福利早知道|第一时间获取门店优惠、新品信息\n养宠顾问|专业养宠师在线答疑，随时咨询\n宠友社群|和 2000+ 宠友交流养宠心得',
       '1',
       '企微页权益说明，格式：标题|描述，每行一条',
       'weixin',
       '企微'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'wecom_benefit_text');

UPDATE wecom_config
SET community_title = COALESCE(NULLIF(community_title, ''), '和它交个朋友'),
    community_subtitle = COALESCE(NULLIF(community_subtitle, ''), '加入我们的宠物大家庭')
WHERE status = 1;

-- 打榜规则编辑按钮（挂打榜排行 2052）
DELETE FROM sys_role_menu WHERE menu_id = 2150;
DELETE FROM sys_menu WHERE menu_id = 2150;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2150, 2052, '编辑打榜规则', NULL, 'pet:ranking:rules', 2, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2150 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2150);
