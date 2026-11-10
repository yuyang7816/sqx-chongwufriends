-- 集市道具正规种子（对齐小程序参考图：汪星人 6 + 喵星人 6）
-- 封面优先使用 /upload/...（管理端可预览）；若本地无上传文件，可改回 /static/market/*.png
SET NAMES utf8mb4;

-- 兼容旧库补封面字段
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'island_market_item' AND COLUMN_NAME = 'cover_url'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE island_market_item ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT ''封面图'' AFTER name',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清理旧种子及关联购买记录（仅种子 ID 段）
DELETE FROM island_market_record WHERE item_id BETWEEN 4001 AND 4020;
DELETE FROM island_market_item WHERE id BETWEEN 4001 AND 4020;

INSERT INTO island_market_item
(id, name, cover_url, item_type, price_coins, price_points, effect_desc, durability, favor, pet_type, status, sort, create_time, update_time, is_deleted)
VALUES
-- 汪星人
(4001, '按摩梳',     '/static/market/massage-comb.png',  'toy',   80, 80, '+15 亲密度', 100, 15, 'dog', 1, 1,  NOW(), NOW(), 0),
(4002, '弹力球',     '/static/market/bouncy-ball.png',   'toy',   50, 50, '+10 亲密度',  80, 10, 'dog', 1, 2,  NOW(), NOW(), 0),
(4003, '磨牙棒',     '/static/market/chew-bone.png',     'toy',   60, 60, '+12 亲密度',  60, 12, 'dog', 1, 3,  NOW(), NOW(), 0),
(4004, '飞盘',       '/static/market/frisbee.png',       'toy',   90, 90, '+18 亲密度', 100, 18, 'dog', 1, 4,  NOW(), NOW(), 0),
(4005, '绳结玩具',   '/static/market/rope-toy.png',      'toy',   45, 45, '+8 亲密度',   70,  8, 'dog', 1, 5,  NOW(), NOW(), 0),
(4006, '蝴蝶结领结', '/static/market/bow-tie.png',       'decor', 120, 120, '+20 亲密度(装饰品)', -1, 20, 'dog', 1, 6, NOW(), NOW(), 0),
-- 喵星人
(4007, '逗猫棒',     '/static/market/cat-teaser.png',    'toy',   55, 55, '+10 亲密度',  75, 10, 'cat', 1, 1,  NOW(), NOW(), 0),
(4008, '猫薄荷球',   '/static/market/catnip-ball.png',   'toy',   40, 40, '+8 亲密度',   60,  8, 'cat', 1, 2,  NOW(), NOW(), 0),
(4009, '激光笔',     '/static/market/laser-pointer.png', 'toy',   70, 70, '+14 亲密度',  90, 14, 'cat', 1, 3,  NOW(), NOW(), 0),
(4010, '猫抓板',     '/static/market/scratch-board.png', 'toy',   85, 85, '+16 亲密度', 100, 16, 'cat', 1, 4,  NOW(), NOW(), 0),
(4011, '毛线球',     '/static/market/yarn-ball.png',     'toy',   45, 45, '+9 亲密度',   70,  9, 'cat', 1, 5,  NOW(), NOW(), 0),
(4012, '铃铛项圈',   '/static/market/bell-collar.png',   'decor', 110, 110, '+18 亲密度(装饰品)', -1, 18, 'cat', 1, 6, NOW(), NOW(), 0);
