-- 朋友圈投诉管理菜单 + 社区按钮权限补丁（MySQL 5.7+）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

-- ========== 新增菜单与按钮（menu_id 2023、2034-2037、2090-2096） ==========
DELETE FROM sys_role_menu WHERE menu_id IN (2023, 2034, 2035, 2036, 2037, 2090, 2091, 2092, 2093, 2094, 2095, 2096);
DELETE FROM sys_menu WHERE menu_id IN (2023, 2034, 2035, 2036, 2037, 2090, 2091, 2092, 2093, 2094, 2095, 2096);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2023, 2020, '投诉管理', 'pet/community/complaints', 'pet:community:complaint:list', 1, 'warning', 1),
(2034, 0, '首页装修', 'content/decorations', 'bannerList:list', 1, 'picture', 2),
(2035, 2034, '新增轮播', NULL, 'bannerList:add', 2, NULL, 0),
(2036, 2034, '编辑轮播', NULL, 'bannerList:update', 2, NULL, 1),
(2037, 2034, '删除轮播', NULL, 'bannerList:delete', 2, NULL, 2),
(2090, 2021, '动态审核', NULL, 'pet:community:audit', 2, NULL, 0),
(2091, 2021, '动态下架', NULL, 'pet:community:offline', 2, NULL, 1),
(2092, 2021, '动态删除', NULL, 'pet:community:delete', 2, NULL, 2),
(2093, 2021, '评论删除', NULL, 'pet:community:comment:delete', 2, NULL, 3),
(2094, 2021, '批量审核', NULL, 'pet:community:batchAudit', 2, NULL, 4),
(2095, 2023, '投诉处理', NULL, 'pet:community:complaint:handle', 2, NULL, 0),
(2096, 2023, '投诉类型配置', NULL, 'pet:community:complaint:config', 2, NULL, 1);

-- 敏感词排序调整到投诉管理之后
UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2022;

-- 系统配置页路由权限兼容（前端 meta.permission = allocationList:list）
UPDATE sys_menu SET perms = 'sys:config:list,allocationList:list' WHERE menu_id = 2073;

-- 超级管理员角色(1)授权新增菜单，如角色ID不同请改 role_id
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2023, 2034, 2035, 2036, 2037, 2090, 2091, 2092, 2093, 2094, 2095, 2096)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ========== 投诉类型配置项（若不存在则插入） ==========
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'community_complaint_types');
