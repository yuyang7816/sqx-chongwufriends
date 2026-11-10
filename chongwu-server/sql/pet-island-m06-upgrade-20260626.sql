-- M06 岛宅子模块联调数据（任务/集市/投喂商品）
SET NAMES utf8mb4;

SET @uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);

-- 重置测试用户任务记录，便于 E2E 重复执行
DELETE FROM island_task_record WHERE user_id = @uid_a;

-- 岛宅任务
DELETE FROM island_task WHERE id BETWEEN 3001 AND 3010;
INSERT INTO island_task (id, title, description, reward_desc, task_group, reward_coins, reward_points, required_level, sort, status, create_time, update_time, is_deleted) VALUES
(3001, '每日投喂宠物', '完成一次岛宅投喂', '+50 金币', '每日待办', 50, 0, 1, 1, 1, NOW(), NOW(), 0),
(3002, '陪宠物玩耍 5 分钟', '与常驻宠物互动', '+30 金币 +20 花瓣', '家主任务', 30, 20, 1, 2, 1, NOW(), NOW(), 0),
(3003, '发布一条社群动态', '分享宠物日常', '+20 金币', '社群任务', 20, 0, 1, 3, 1, NOW(), NOW(), 0);

-- 集市道具
DELETE FROM island_market_item WHERE id BETWEEN 4001 AND 4010;
INSERT INTO island_market_item (id, name, item_type, price_coins, price_points, effect_desc, durability, favor, pet_type, status, sort, create_time, update_time, is_deleted) VALUES
(4001, '磨牙棒', 'toy', 80, 0, '提升亲密度', 80, 5, 'dog', 1, 1, NOW(), NOW(), 0),
(4002, '逗猫棒', 'toy', 60, 0, '提升亲密度', 70, 4, 'cat', 1, 2, NOW(), NOW(), 0),
(4003, '宠物床垫', 'bed', 200, 0, '提升心情', 90, 8, 'dog', 1, 3, NOW(), NOW(), 0);

-- 确保测试用户有足够金币购买集市道具
UPDATE user_asset SET coins = GREATEST(coins, 1500), points = GREATEST(points, 520), flowers = GREATEST(flowers, 66), love_value = GREATEST(love_value, 328)
WHERE user_id = @uid_a;

-- 投喂商城商品
DELETE FROM mall_spu WHERE category = 'feed';
INSERT INTO mall_spu (name, category, cover_url, description, price, point_price, biz_type, status, sort, create_time, update_time, is_deleted) VALUES
('鲜肉罐头套餐', 'feed', '/static/logo.png', '门店鲜食罐头，投喂后可见反馈视频', 29.90, 0, 2, 1, 1, NOW(), NOW(), 0),
('冻干零食礼包', 'feed', '/static/logo.png', '冻干鸡肉+三文鱼，毛孩子最爱', 19.90, 0, 2, 1, 2, NOW(), NOW(), 0);

-- 岛宅无冷却，常驻宠物 id=1
UPDATE island_pet SET feed_cooldown_until = NULL, resident_pet_id = 1, resident_pet_ids = '1', intimacy = 340
WHERE user_id = @uid_a;
