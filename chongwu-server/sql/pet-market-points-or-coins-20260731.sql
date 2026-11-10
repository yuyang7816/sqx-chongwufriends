-- 集市道具：补齐积分价，支持金币/积分二选一兑换
-- price_points 与 price_coins 同值，便于两端等价兑换；管理端可单独调整
SET NAMES utf8mb4;

UPDATE island_market_item
SET price_points = price_coins,
    update_time = NOW()
WHERE is_deleted = 0
  AND status = 1
  AND (price_points IS NULL OR price_points = 0)
  AND price_coins IS NOT NULL
  AND price_coins > 0;
