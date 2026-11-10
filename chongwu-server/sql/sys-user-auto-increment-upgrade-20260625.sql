-- sys_user 主键自增补丁（修复管理端新增管理员 500）
-- 错误：Field 'user_id' doesn't have a default value
-- 可重复执行

SET NAMES utf8mb4;

ALTER TABLE sys_user
  MODIFY COLUMN user_id BIGINT NOT NULL AUTO_INCREMENT;

SELECT MAX(user_id) AS current_max_user_id FROM sys_user;
