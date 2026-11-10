-- 岛宅业务测试数据（MySQL 5.7）
-- 依赖：pet-business-schema.sql、pet-business-v2-mysql57.sql、pet-business-seed.sql、pet-debug-seed-mysql57.sql

SET NAMES utf8mb4;

-- 扩展岛宅常驻字段
DROP PROCEDURE IF EXISTS pet_add_column;
DELIMITER $$
CREATE PROCEDURE pet_add_column(IN tbl VARCHAR(64), IN col VARCHAR(64), IN def VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;
CALL pet_add_column('island_pet', 'resident_pet_ids', 'VARCHAR(64) DEFAULT NULL');

SET @uid_a = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);

UPDATE pet_archive SET avatar_url = '/static/logo.png' WHERE id IN (1, 2, 3);

-- 测试用户资产
DELETE FROM user_asset WHERE user_id = @uid_a;
INSERT INTO user_asset (user_id, points, coins, love_value, flowers, level, level_name, create_time, update_time, is_deleted)
VALUES (@uid_a, 520, 1280, 328, 66, 4, 'Lv.4', NOW(), NOW(), 0);

-- 岛宅状态（关联宠物档案 id=1 豆包）
DELETE FROM island_pet WHERE user_id = @uid_a;
INSERT INTO island_pet (user_id, hunger, clean, mood, vitality, intimacy, resident_pet_id, resident_pet_ids, feed_cooldown_until, create_time, update_time, is_deleted)
VALUES (@uid_a, 74, 71, 82, 67, 340, 1, '1', NULL, NOW(), NOW(), 0);

-- 岛宅任务（分组与前端 tab 一致）
DELETE FROM island_task WHERE id >= 3001 AND id <= 3010;
INSERT INTO island_task (id, title, description, reward_desc, task_group, reward_coins, reward_points, required_level, sort, status, create_time, update_time, is_deleted) VALUES
(3001, '每日投喂宠物', '完成一次岛宅投喂', '+50 金币', '每日待办', 50, 0, 1, 1, 1, NOW(), NOW(), 0),
(3002, '陪宠物玩耍 5 分钟', '与常驻宠物互动', '+30 金币 +20 花瓣', '家主任务', 30, 20, 1, 2, 1, NOW(), NOW(), 0),
(3003, '发布一条社群动态', '分享宠物日常', '+20 金币', '社群任务', 20, 0, 1, 3, 1, NOW(), NOW(), 0);

-- 集市道具
DELETE FROM island_market_item WHERE id >= 4001 AND id <= 4020;
INSERT INTO island_market_item (id, name, cover_url, item_type, price_coins, price_points, effect_desc, durability, favor, pet_type, status, sort, create_time, update_time, is_deleted) VALUES
(4001, '按摩梳', '/static/market/massage-comb.png', 'toy', 80, 0, '+15 亲密度', 100, 15, 'dog', 1, 1, NOW(), NOW(), 0),
(4002, '弹力球', '/static/market/bouncy-ball.png', 'toy', 50, 0, '+10 亲密度', 80, 10, 'dog', 1, 2, NOW(), NOW(), 0),
(4003, '磨牙棒', '/static/market/chew-bone.png', 'toy', 60, 0, '+12 亲密度', 60, 12, 'dog', 1, 3, NOW(), NOW(), 0),
(4004, '飞盘', '/static/market/frisbee.png', 'toy', 90, 0, '+18 亲密度', 100, 18, 'dog', 1, 4, NOW(), NOW(), 0),
(4005, '绳结玩具', '/static/market/rope-toy.png', 'toy', 45, 0, '+8 亲密度', 70, 8, 'dog', 1, 5, NOW(), NOW(), 0),
(4006, '蝴蝶结领结', '/static/market/bow-tie.png', 'decor', 120, 0, '+20 亲密度(装饰品)', -1, 20, 'dog', 1, 6, NOW(), NOW(), 0),
(4007, '逗猫棒', '/static/market/cat-teaser.png', 'toy', 55, 0, '+10 亲密度', 75, 10, 'cat', 1, 1, NOW(), NOW(), 0),
(4008, '猫薄荷球', '/static/market/catnip-ball.png', 'toy', 40, 0, '+8 亲密度', 60, 8, 'cat', 1, 2, NOW(), NOW(), 0),
(4009, '激光笔', '/static/market/laser-pointer.png', 'toy', 70, 0, '+14 亲密度', 90, 14, 'cat', 1, 3, NOW(), NOW(), 0),
(4010, '猫抓板', '/static/market/scratch-board.png', 'toy', 85, 0, '+16 亲密度', 100, 16, 'cat', 1, 4, NOW(), NOW(), 0),
(4011, '毛线球', '/static/market/yarn-ball.png', 'toy', 45, 0, '+9 亲密度', 70, 9, 'cat', 1, 5, NOW(), NOW(), 0),
(4012, '铃铛项圈', '/static/market/bell-collar.png', 'decor', 110, 0, '+18 亲密度(装饰品)', -1, 18, 'cat', 1, 6, NOW(), NOW(), 0);

-- 投喂商城商品（category=feed, biz_type=2）
DELETE FROM mall_spu WHERE category = 'feed';
INSERT INTO mall_spu (name, category, cover_url, description, price, point_price, biz_type, status, sort, create_time, update_time, is_deleted) VALUES
('鲜肉罐头套餐', 'feed', '/static/logo.png', '门店鲜食罐头，投喂后可见反馈视频', 29.90, 0, 2, 1, 1, NOW(), NOW(), 0),
('冻干零食礼包', 'feed', '/static/logo.png', '冻干鸡肉+三文鱼，毛孩子最爱', 19.90, 0, 2, 1, 2, NOW(), NOW(), 0),
('营养湿粮组合', 'feed', '/static/logo.png', '均衡营养湿粮，适合日常投喂', 39.90, 0, 2, 1, 3, NOW(), NOW(), 0);

-- 管理端菜单已合并至 pet-menu-sys-config-mysql57.sql / pet-full-mysql57.sql
