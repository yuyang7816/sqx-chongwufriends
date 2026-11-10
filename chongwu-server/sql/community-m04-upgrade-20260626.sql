-- M04 朋友圈升级：审核开关 + 社区菜单补丁（可重复执行，不影响商城 2097-2100）
SET NAMES utf8mb4;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'community_post' AND COLUMN_NAME = 'reject_reason'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE community_post ADD COLUMN reject_reason VARCHAR(255) DEFAULT NULL COMMENT ''审核拒绝原因'' AFTER audit_status',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'community_audit_switch', '1', '1', '社区发布审核开关1开0关', 'kaiguan', '社区审核开关'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'community_audit_switch');

UPDATE sys_config SET param_value = '1', status = '1' WHERE param_key = 'community_audit_switch';

INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'community_complaint_types');

-- 社区按钮（2090-2096）+ 投诉类型菜单（2101-2104）
DELETE FROM sys_role_menu WHERE menu_id IN (2090, 2091, 2092, 2093, 2094, 2095, 2096, 2101, 2102, 2103, 2104);
DELETE FROM sys_menu WHERE menu_id IN (2090, 2091, 2092, 2093, 2094, 2095, 2096, 2101, 2102, 2103, 2104);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2090, 2020, '动态审核', NULL, 'pet:community:audit', 2, NULL, 0),
(2091, 2020, '动态下架', NULL, 'pet:community:offline', 2, NULL, 1),
(2092, 2020, '动态删除', NULL, 'pet:community:delete', 2, NULL, 2),
(2093, 2020, '评论删除', NULL, 'pet:community:comment:delete', 2, NULL, 3),
(2094, 2020, '批量审核', NULL, 'pet:community:batchAudit', 2, NULL, 4),
(2095, 2021, '投诉处理', NULL, 'pet:community:complaint:handle', 2, NULL, 0),
(2096, 2021, '投诉配置', NULL, 'pet:community:complaint:config', 2, NULL, 1),
(2101, 2010, '投诉类型', 'pet/community/complaint-types', 'pet:community:complaint:type', 1, 'warning', 8),
(2102, 2101, '新增类型', NULL, 'pet:community:complaint:type:add', 2, NULL, 0),
(2103, 2101, '编辑类型', NULL, 'pet:community:complaint:type:update', 2, NULL, 1),
(2104, 2101, '删除类型', NULL, 'pet:community:complaint:type:delete', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2090, 2091, 2092, 2093, 2094, 2095, 2096, 2101, 2102, 2103, 2104)
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 恢复商城按钮（若被误删）
DELETE FROM sys_role_menu WHERE menu_id IN (2097, 2098, 2099, 2100);
DELETE FROM sys_menu WHERE menu_id IN (2097, 2098, 2099, 2100);
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2097, 2031, '新增商品', NULL, 'pet:mall:add', 2, NULL, 0),
(2098, 2031, '编辑商品', NULL, 'pet:mall:update', 2, NULL, 1),
(2099, 2031, '删除商品', NULL, 'pet:mall:delete', 2, NULL, 2),
(2100, 2031, '商品详情', NULL, 'pet:mall:info', 2, NULL, 3);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2097, 2098, 2099, 2100)
ON DUPLICATE KEY UPDATE role_id = role_id;
