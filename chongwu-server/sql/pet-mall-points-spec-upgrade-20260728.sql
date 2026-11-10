-- =============================================================================
-- 商城商品补齐规格(SKU)与积分(point_price)
-- 日期: 2026-07-28
-- 说明:
--   1. 修正 7001 多规格但 SKU 无 spec_id 且重名 -> 改为单规格
--   2. 为缺少 SKU 的商品补一条"标准装" SKU
--   3. 为 point_price 不足的商品按 price*10 设置积分(最低100, 0元商品保持原值)
--   4. 单规格 SKU 积分与 SPU 同步
--   5. 多规格 SKU 按各自价格补积分
-- =============================================================================

USE chongwu;

-- 1. 修正 7001: 多规格但 SKU 无 spec_id 且重名, 改为单规格
UPDATE mall_spu SET spec_mode = 1, point_price = 299 WHERE id = 7001;
-- 软删除多余的 SKU, 仅保留 7101 标准装
UPDATE mall_sku SET is_deleted = 1 WHERE spu_id = 7001 AND id IN (7105, 7106);
UPDATE mall_sku SET point_price = 299, price = 29.90, sku_name = '标准装', stock = 999
WHERE id = 7101 AND is_deleted = 0;

-- 2. 为缺少 SKU 的商品补一条"标准装" SKU
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock)
SELECT s.id, NULL, '标准装', s.price, GREATEST(100, ROUND(s.price * 10)), 999
FROM mall_spu s
WHERE s.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM mall_sku k
    WHERE k.spu_id = s.id AND k.is_deleted = 0
  );

-- 3. 为所有 point_price 不足的商品设置合理积分(price*10, 最低100; 0元商品保持原值)
UPDATE mall_spu
SET point_price = GREATEST(100, ROUND(price * 10))
WHERE is_deleted = 0
  AND price > 0
  AND (point_price IS NULL OR point_price < 100);

-- 4. 单规格 SKU 的积分与 SPU 同步
UPDATE mall_sku k
JOIN mall_spu s ON s.id = k.spu_id
SET k.point_price = s.point_price
WHERE k.is_deleted = 0
  AND s.is_deleted = 0
  AND s.spec_mode = 1
  AND k.spec_id IS NULL;

-- 5. 多规格 SKU(spec_id 非空)按各自价格补积分
UPDATE mall_sku
SET point_price = GREATEST(100, ROUND(price * 10))
WHERE is_deleted = 0
  AND spec_id IS NOT NULL
  AND (point_price IS NULL OR point_price = 0);
