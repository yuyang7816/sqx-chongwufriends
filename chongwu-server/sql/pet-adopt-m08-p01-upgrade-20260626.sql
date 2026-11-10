-- M08 P0/P1：回访权限、E2E 测试数据清理
SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id = 2121;
DELETE FROM sys_menu WHERE menu_id = 2121;
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2121, 2041, '新增回访', NULL, 'pet:adopt:visit', 2, NULL, 1);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2121 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2121);

-- 清理 E2E 测试用户宠物4申请（重复联调）
DELETE av FROM adopt_visit av
INNER JOIN adopt_application aa ON av.application_id = aa.id
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 4;

DELETE aa FROM adopt_application aa
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 4;

-- 宠物3用于完成态 E2E
UPDATE pet_archive SET adoptable = 1, audit_status = 1, status_label = '等待领养' WHERE id = 3;

DELETE av FROM adopt_visit av
INNER JOIN adopt_application aa ON av.application_id = aa.id
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 3;

DELETE aa FROM adopt_application aa
INNER JOIN tb_user u ON aa.user_id = u.user_id
WHERE u.phone = '13800138000' AND aa.pet_id = 3;
