-- M05 岛宅主场景联调数据（MySQL 5.7+，无 DELIMITER，兼容 node mysql2）
SET NAMES utf8mb4;

SET @uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);

-- 测试用户资产（与岛宅 info 资产字段对齐）
DELETE FROM user_asset WHERE user_id = @uid_a;
INSERT INTO user_asset (user_id, points, coins, love_value, flowers, level, level_name, create_time, update_time, is_deleted)
VALUES (@uid_a, 520, 1280, 328, 66, 4, 'Lv.4', NOW(), NOW(), 0);

-- 确保档案 1 已审核且有头像
UPDATE pet_archive
SET audit_status = 1,
    avatar_url = COALESCE(NULLIF(avatar_url, ''), 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png')
WHERE id = 1;

-- 岛宅状态：关联已审核档案 id=1，无投喂冷却
DELETE FROM island_pet WHERE user_id = @uid_a;
INSERT INTO island_pet (user_id, hunger, clean, mood, vitality, intimacy, resident_pet_id, resident_pet_ids, feed_cooldown_until, create_time, update_time, is_deleted)
VALUES (@uid_a, 74, 71, 82, 67, 340, 1, '1', NULL, NOW(), NOW(), 0);

-- 岛宅任务种子
DELETE FROM island_task WHERE id BETWEEN 3001 AND 3010;
INSERT INTO island_task (id, title, description, reward_desc, task_group, reward_coins, reward_points, required_level, sort, status, create_time, update_time, is_deleted) VALUES
(3001, '每日投喂宠物', '完成一次岛宅投喂', '+50 金币', '每日待办', 50, 0, 1, 1, 1, NOW(), NOW(), 0),
(3002, '陪宠物玩耍 5 分钟', '与常驻宠物互动', '+30 金币 +20 花瓣', '家主任务', 30, 20, 1, 2, 1, NOW(), NOW(), 0),
(3003, '发布一条社群动态', '分享宠物日常', '+20 金币', '社群任务', 20, 0, 1, 3, 1, NOW(), NOW(), 0);

-- 集市道具种子
DELETE FROM island_market_item WHERE id BETWEEN 4001 AND 4010;
INSERT INTO island_market_item (id, name, item_type, price_coins, price_points, effect_desc, durability, favor, pet_type, status, sort, create_time, update_time, is_deleted) VALUES
(4001, '磨牙棒', 'toy', 80, 0, '提升亲密度', 80, 5, 'dog', 1, 1, NOW(), NOW(), 0),
(4002, '逗猫棒', 'toy', 60, 0, '提升亲密度', 70, 4, 'cat', 1, 2, NOW(), NOW(), 0);
