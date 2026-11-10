-- 朋友圈模块升级 2026-06-25

USE shouban;

-- reject_reason on community_post
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

-- ensure community_audit_switch
INSERT IGNORE INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES
('community_audit_switch', '1', '1', '社区发布审核开关1开0关', 'kaiguan', '社区审核开关');

INSERT IGNORE INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES
('community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉');
