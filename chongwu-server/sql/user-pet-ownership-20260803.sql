-- 用户自有宠物（领养审核完成后写入）
CREATE TABLE IF NOT EXISTS user_pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pet_id BIGINT NOT NULL COMMENT '宠物档案ID',
    application_id BIGINT DEFAULT NULL COMMENT '来源领养申请ID',
    source VARCHAR(32) DEFAULT 'adopt' COMMENT '来源：adopt',
    status TINYINT DEFAULT 1 COMMENT '1有效 0失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_pet (user_id, pet_id),
    KEY idx_user (user_id),
    KEY idx_pet (pet_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自有宠物';

-- 回填：历史已完成领养申请写入归属
INSERT INTO user_pet (user_id, pet_id, application_id, source, status)
SELECT aa.user_id, aa.pet_id, aa.id, 'adopt', 1
FROM adopt_application aa
WHERE aa.status = 6
  AND aa.is_deleted = 0
  AND aa.user_id IS NOT NULL
  AND aa.pet_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_pet up
      WHERE up.user_id = aa.user_id AND up.pet_id = aa.pet_id AND up.is_deleted = 0
  );
