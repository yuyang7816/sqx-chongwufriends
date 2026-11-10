-- 用户端调试数据（MySQL 5.7）
-- 测试账号、社区动态；执行前请确保 pet-business-schema.sql 已执行

SET NAMES utf8mb4;

-- 测试账号 密码均为 123456（MD5: e10adc3949ba59abbe56e057f20f883e）
DELETE FROM tb_user WHERE phone IN ('13800138000', '13800138001');
INSERT INTO tb_user (user_name, phone, password, sex, status, create_time, update_time) VALUES
('测试用户A', '13800138000', 'e10adc3949ba59abbe56e057f20f883e', 1, 1, NOW(), NOW()),
('测试用户B', '13800138001', 'e10adc3949ba59abbe56e057f20f883e', 2, 1, NOW(), NOW());

-- 社区动态（需有用户ID，取刚插入的用户）
SET @uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);
SET @uid_b = (SELECT user_id FROM tb_user WHERE phone = '13800138001' LIMIT 1);

DELETE FROM community_post WHERE id >= 9001 AND id <= 9010;
INSERT INTO community_post (id, user_id, store_id, content, media_urls, media_type, like_count, comment_count, audit_status, create_time, update_time, is_deleted) VALUES
(9001, @uid_a, 1, '豆包今天特别乖，一直摇尾巴欢迎客人～', '/static/logo.png', 1, 128, 12, 1, NOW(), NOW(), 0),
(9002, @uid_b, 2, '花姐晒太阳的样子太治愈了，快来门店看她！', '', 1, 86, 8, 1, NOW(), NOW(), 0),
(9003, @uid_a, 3, '柯基小七学会新技能啦，会握手！', '/static/logo.png', 1, 203, 25, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0);
