-- 用户表新增生日、微信号字段（可逐条执行，已存在则跳过）
ALTER TABLE tb_user ADD COLUMN birthday VARCHAR(16) DEFAULT NULL COMMENT '生日 yyyy-MM-dd';
ALTER TABLE tb_user ADD COLUMN wechat VARCHAR(64) DEFAULT NULL COMMENT '微信号';
