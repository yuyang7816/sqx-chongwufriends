-- M08 领养：可领养档案、测试数据清理、菜单权限补丁
SET NAMES utf8mb4;

-- 领养申请表扩展字段
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'adopt_application' AND COLUMN_NAME = 'form_json'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE adopt_application ADD COLUMN form_json TEXT DEFAULT NULL COMMENT ''领养登记表扩展JSON'' AFTER reason',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'adopt_application' AND COLUMN_NAME = 'idempotent_key'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE adopt_application ADD COLUMN idempotent_key VARCHAR(64) DEFAULT NULL COMMENT ''幂等键'' AFTER reject_reason',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 可领养退役宠物（已审核）
UPDATE pet_archive SET adoptable = 1, audit_status = 1, status_label = COALESCE(NULLIF(status_label, ''), '等待领养')
WHERE id IN (1, 3, 4);
UPDATE pet_archive SET adoptable = 0, audit_status = 1 WHERE id = 2;

-- 领养专区 Banner
INSERT INTO operation_banner (name, image_url, jump_url, position_code, sort, status, sub_title)
SELECT '领养专区', 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png',
       '/packageA/adopt/list', 'adopt', 1, 1, '给退役毛孩子一个温暖的家'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM operation_banner WHERE position_code = 'adopt' AND status = 1);

-- 领养审核按钮权限
DELETE FROM sys_role_menu WHERE menu_id = 2120;
DELETE FROM sys_menu WHERE menu_id = 2120;
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2120, 2041, '领养状态变更', NULL, 'pet:adopt:update', 2, NULL, 0);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2120 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2120);

-- E2E 测试用户：清理宠物4的进行中申请（便于重复联调）
DELETE av FROM adopt_visit av
INNER JOIN adopt_application aa ON av.application_id = aa.id
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 4;

DELETE aa FROM adopt_application aa
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 4;
