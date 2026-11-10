package com.sqx.app.pet.community.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class CommunitySchemaInitializer {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void upgradeSchema() {
        ensureRejectReasonColumn();
        ensureAuditSwitchConfig();
        ensureComplaintTypeTable();
    }

    private void ensureRejectReasonColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'community_post' AND COLUMN_NAME = 'reject_reason'",
                Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute(
                "ALTER TABLE community_post ADD COLUMN reject_reason VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因' AFTER audit_status");
    }

    private void ensureAuditSwitchConfig() {
        try {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES "
                            + "('community_audit_switch', '1', '1', '社区发布审核开关1开0关', 'kaiguan', '社区审核开关')");
            jdbcTemplate.update(
                    "INSERT IGNORE INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc) VALUES "
                            + "('community_complaint_types', '垃圾广告,违法违规,人身攻击,不实信息,其他', '1', '朋友圈投诉类型，逗号分隔', 'community', '朋友圈投诉')");
        } catch (Exception ignored) {
            // 配置表结构差异时不阻断启动
        }
    }

    private void ensureComplaintTypeTable() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS community_complaint_type ("
                            + "id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                            + "name VARCHAR(32) NOT NULL,"
                            + "sort INT DEFAULT 0,"
                            + "status TINYINT DEFAULT 1 COMMENT '1启用 2禁用',"
                            + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                            + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                            + "is_deleted TINYINT DEFAULT 0,"
                            + "UNIQUE KEY uk_complaint_type_name (name)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈投诉类型'");
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM community_complaint_type WHERE is_deleted = 0",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            String raw = jdbcTemplate.query(
                    "SELECT param_value FROM sys_config WHERE param_key = 'community_complaint_types' LIMIT 1",
                    rs -> rs.next() ? rs.getString(1) : null);
            if (StringUtils.isBlank(raw)) {
                raw = "垃圾广告,违法违规,人身攻击,不实信息,其他";
            }
            String[] items = raw.split(",");
            int sort = 0;
            for (String item : items) {
                String name = item == null ? "" : item.trim();
                if (name.isEmpty()) {
                    continue;
                }
                jdbcTemplate.update(
                        "INSERT INTO community_complaint_type (name, sort, status) VALUES (?, ?, 1)",
                        name, sort++);
            }
        } catch (Exception ignored) {
            // 表结构差异时不阻断启动
        }
    }
}
