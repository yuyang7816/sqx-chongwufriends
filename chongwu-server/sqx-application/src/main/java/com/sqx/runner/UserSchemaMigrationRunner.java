package com.sqx.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 补齐 tb_user 表缺失字段，避免实体与库表不一致导致接口 500。
 */
@Slf4j
@Order(0)
@Component
public class UserSchemaMigrationRunner implements ApplicationRunner {

    @Resource
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            ensureColumn(connection, "birthday",
                    "ALTER TABLE tb_user ADD COLUMN birthday VARCHAR(16) DEFAULT NULL COMMENT '生日 yyyy-MM-dd'");
            ensureColumn(connection, "wechat",
                    "ALTER TABLE tb_user ADD COLUMN wechat VARCHAR(64) DEFAULT NULL COMMENT '微信号'");
        } catch (Exception e) {
            log.error("tb_user 字段补齐失败，请手动执行 sql/pet-user-birthday-wechat-mysql57.sql", e);
        }
    }

    private void ensureColumn(Connection connection, String columnName, String ddl) throws Exception {
        if (columnExists(connection, columnName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl);
            log.info("tb_user 已新增字段: {}", columnName);
        }
    }

    private boolean columnExists(Connection connection, String columnName) throws Exception {
        String sql = "SELECT COUNT(1) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_user' AND COLUMN_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
