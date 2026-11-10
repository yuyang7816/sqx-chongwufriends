-- P1：品种字典、档案审核拒绝原因、菜单与按钮权限
SET NAMES utf8mb4;

-- 档案拒绝原因
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pet_archive' AND COLUMN_NAME = 'reject_reason'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE pet_archive ADD COLUMN reject_reason VARCHAR(255) DEFAULT NULL COMMENT ''审核拒绝原因'' AFTER audit_status',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 品种字典
CREATE TABLE IF NOT EXISTS pet_breed_dict (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pet_type VARCHAR(16) NOT NULL COMMENT 'dog/cat',
  name VARCHAR(64) NOT NULL COMMENT '品种名称',
  sort INT DEFAULT 0,
  status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_type_name (pet_type, name),
  KEY idx_pet_type (pet_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物品种字典';

DELETE FROM pet_breed_dict WHERE id BETWEEN 1 AND 30;
INSERT INTO pet_breed_dict (id, pet_type, name, sort, status) VALUES
(1, 'dog', '柴犬', 1, 1),
(2, 'dog', '柯基', 2, 1),
(3, 'dog', '泰迪', 3, 1),
(4, 'dog', '金毛', 4, 1),
(5, 'dog', '拉布拉多', 5, 1),
(6, 'dog', '哈士奇', 6, 1),
(7, 'dog', '萨摩耶', 7, 1),
(8, 'dog', '边境牧羊犬', 8, 1),
(9, 'cat', '英短', 1, 1),
(10, 'cat', '美短', 2, 1),
(11, 'cat', '布偶', 3, 1),
(12, 'cat', '橘猫', 4, 1),
(13, 'cat', '三花', 5, 1),
(14, 'cat', '狸花猫', 6, 1),
(15, 'cat', '暹罗', 7, 1);

-- 菜单：品种字典 + 档案审核按钮（menu_id 2132-2138）
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2132 AND 2138;
DELETE FROM sys_menu WHERE menu_id BETWEEN 2132 AND 2138;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2132, 2010, '品种字典', 'pet/breeds', 'pet:breed:list', 1, 'collection-tag', 3),
(2133, 2132, '新增品种', NULL, 'pet:breed:add', 2, NULL, 0),
(2134, 2132, '编辑品种', NULL, 'pet:breed:update', 2, NULL, 1),
(2135, 2132, '删除品种', NULL, 'pet:breed:delete', 2, NULL, 2),
(2136, 2012, '审核档案', NULL, 'pet:archive:audit', 2, NULL, 3),
(2137, 2012, '批量审核档案', NULL, 'pet:archive:batchAudit', 2, NULL, 4);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2132 AND 2138
ON DUPLICATE KEY UPDATE role_id = role_id;
