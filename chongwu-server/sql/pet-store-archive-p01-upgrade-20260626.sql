-- M03 P0：各门店补齐犬猫档案测试数据（每店至少 1 狗 + 1 猫）
-- 可重复执行

SET NAMES utf8mb4;

DELETE FROM pet_album WHERE pet_id IN (5, 6, 7, 8);
DELETE FROM pet_archive WHERE id IN (5, 6, 7, 8);

INSERT INTO pet_archive (
  id, store_id, name, pet_type, breed, gender, age_desc, weight_desc, pet_code,
  level_desc, status_label, description, good_at, avoid_items, tags, avatar_url,
  like_count, adoptable, audit_status, sort
) VALUES
(5, 1, '奶糖', 'cat', '英短', '女', '1岁', '4kg', 'SH-101', 'Lv.3 新星萌宠', '优秀员工',
 '奶糖是爱琴海店的喵星人代表，安静黏人。', '["被撸","晒太阳"]', '["洗澡"]', '["温柔","粘人"]',
 '/static/logo.png', 5200, 0, 1, 2),
(6, 2, '皮蛋', 'dog', '泰迪', '男', '3岁', '6kg', 'SH-102', 'Lv.3', '营业中',
 '皮蛋活泼好动，喜欢和客人玩球。', '["玩球","握手"]', '["独处"]', '["活泼","爱笑"]',
 '/static/logo.png', 4100, 1, 1, 2),
(7, 3, '橘橘', 'cat', '橘猫', '男', '2岁', '5.5kg', 'SH-103', 'Lv.4', '优秀员工',
 '橘橘是大学路店的喵星人，贪吃可爱。', '["吃罐头","晒太阳"]', '["狗"]', '["贪吃","亲人"]',
 '/static/logo.png', 3800, 0, 1, 2),
(8, 4, '咪咪', 'cat', '布偶', '女', '1岁', '4.2kg', 'SH-104', 'Lv.3', '营业中',
 '咪咪温顺大方，适合安静陪伴。', '["被撸","卖萌"]', '["噪音"]', '["温柔","爱睡觉"]',
 '/static/logo.png', 2900, 1, 1, 2);

INSERT INTO pet_album (pet_id, media_url, media_type, sort) VALUES
(5, '/static/logo.png', 1, 1),
(6, '/static/logo.png', 1, 1),
(7, '/static/logo.png', 1, 1),
(8, '/static/logo.png', 1, 1);

UPDATE pet_archive SET audit_status = 1 WHERE audit_status IS NULL OR audit_status = 0;
