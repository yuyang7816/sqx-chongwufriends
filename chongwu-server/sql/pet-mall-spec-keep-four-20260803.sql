-- 规格管理只保留四个多规格模板（8101~8104），清理其余规格；
-- 对齐 7201~7204 商品 SKU；其余商品保持/改为单规格。
-- 可重复执行。逻辑删除值与 MyBatis-Plus 一致：is_deleted = -1
SET NAMES utf8mb4;

-- ========== 1) 确保四条规格模板存在且正确 ==========
INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8101, '鲜食多规格',
'[{"value":"份量","detail":"100g,200g,300g"},{"value":"口味","detail":"原味,牛肉"}]',
15.00, 0, 200, 10, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8101);

UPDATE mall_sku_spec
SET spec_name = '鲜食多规格',
    rule_value = '[{"value":"份量","detail":"100g,200g,300g"},{"value":"口味","detail":"原味,牛肉"}]',
    price = 15.00, point_price = 0, default_stock = 200, sort = 10, status = 1, is_deleted = 0
WHERE id = 8101;

INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8102, '罐头多规格',
'[{"value":"净含量","detail":"85g,170g,370g"},{"value":"口味","detail":"金枪鱼,鸡肉"}]',
12.80, 0, 300, 11, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8102);

UPDATE mall_sku_spec
SET spec_name = '罐头多规格',
    rule_value = '[{"value":"净含量","detail":"85g,170g,370g"},{"value":"口味","detail":"金枪鱼,鸡肉"}]',
    price = 12.80, point_price = 0, default_stock = 300, sort = 11, status = 1, is_deleted = 0
WHERE id = 8102;

INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8103, '零食多规格',
'[{"value":"包装","detail":"小包,中包,大包"},{"value":"口味","detail":"混合,纯肉"}]',
9.90, 80, 400, 12, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8103);

UPDATE mall_sku_spec
SET spec_name = '零食多规格',
    rule_value = '[{"value":"包装","detail":"小包,中包,大包"},{"value":"口味","detail":"混合,纯肉"}]',
    price = 9.90, point_price = 80, default_stock = 400, sort = 12, status = 1, is_deleted = 0
WHERE id = 8103;

INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8104, '羊奶多规格',
'[{"value":"份量","detail":"100g,200g"},{"value":"口味","detail":"原味,牛肉"}]',
15.00, 0, 200, 13, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8104);

UPDATE mall_sku_spec
SET spec_name = '羊奶多规格',
    rule_value = '[{"value":"份量","detail":"100g,200g"},{"value":"口味","detail":"原味,牛肉"}]',
    price = 15.00, point_price = 0, default_stock = 200, sort = 13, status = 1, is_deleted = 0
WHERE id = 8104;

-- ========== 2) 软删引用旧规格的 SKU（含已删 E2E 商品残留） ==========
UPDATE mall_sku
SET is_deleted = -1
WHERE IFNULL(is_deleted, 0) = 0
  AND (
    spec_id IN (8001, 8002, 8003, 8004, 8005)
    OR spu_id IN (7012, 7013)
  );

-- ========== 3) 软删非四条模板的规格 ==========
UPDATE mall_sku_spec
SET is_deleted = -1
WHERE id NOT IN (8101, 8102, 8103, 8104)
  AND IFNULL(is_deleted, 0) <> -1;

-- ========== 4) 多规格商品 7201~7204：模式 + 重建笛卡尔积 SKU ==========
UPDATE mall_spu SET spec_mode = 2, price = 15.00 WHERE id = 7201 AND IFNULL(is_deleted, 0) = 0;
UPDATE mall_spu SET spec_mode = 2, price = 12.80 WHERE id = 7202 AND IFNULL(is_deleted, 0) = 0;
UPDATE mall_spu SET spec_mode = 2, price = 9.90 WHERE id = 7203 AND IFNULL(is_deleted, 0) = 0;
UPDATE mall_spu SET spec_mode = 2, price = 15.00 WHERE id = 7204 AND IFNULL(is_deleted, 0) = 0;

UPDATE mall_sku SET is_deleted = -1
WHERE spu_id IN (7201, 7202, 7203, 7204) AND IFNULL(is_deleted, 0) = 0;

-- 7201 × 8101
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '100g-原味', 15.00, 0, 200, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '100g-原味' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '100g-牛肉', 18.00, 80, 180, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '100g-牛肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '200g-原味', 28.00, 150, 160, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '200g-原味' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '200g-牛肉', 32.00, 200, 140, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '200g-牛肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '300g-原味', 39.00, 220, 120, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '300g-原味' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '300g-牛肉', 45.00, 280, 100, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '300g-牛肉' AND IFNULL(is_deleted, 0) = 0);

UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 15.00, point_price = 0, stock = 200 WHERE spu_id = 7201 AND sku_name = '100g-原味';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 18.00, point_price = 80, stock = 180 WHERE spu_id = 7201 AND sku_name = '100g-牛肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 28.00, point_price = 150, stock = 160 WHERE spu_id = 7201 AND sku_name = '200g-原味';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 32.00, point_price = 200, stock = 140 WHERE spu_id = 7201 AND sku_name = '200g-牛肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 39.00, point_price = 220, stock = 120 WHERE spu_id = 7201 AND sku_name = '300g-原味';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 45.00, point_price = 280, stock = 100 WHERE spu_id = 7201 AND sku_name = '300g-牛肉';

-- 7202 × 8102
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '85g-金枪鱼', 12.80, 0, 300, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '85g-金枪鱼' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '85g-鸡肉', 13.80, 50, 280, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '85g-鸡肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '170g-金枪鱼', 22.80, 180, 220, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '170g-金枪鱼' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '170g-鸡肉', 24.80, 200, 200, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '170g-鸡肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '370g-金枪鱼', 39.90, 320, 150, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '370g-金枪鱼' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '370g-鸡肉', 42.90, 360, 130, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '370g-鸡肉' AND IFNULL(is_deleted, 0) = 0);

UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 12.80, point_price = 0, stock = 300 WHERE spu_id = 7202 AND sku_name = '85g-金枪鱼';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 13.80, point_price = 50, stock = 280 WHERE spu_id = 7202 AND sku_name = '85g-鸡肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 22.80, point_price = 180, stock = 220 WHERE spu_id = 7202 AND sku_name = '170g-金枪鱼';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 24.80, point_price = 200, stock = 200 WHERE spu_id = 7202 AND sku_name = '170g-鸡肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 39.90, point_price = 320, stock = 150 WHERE spu_id = 7202 AND sku_name = '370g-金枪鱼';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 42.90, point_price = 360, stock = 130 WHERE spu_id = 7202 AND sku_name = '370g-鸡肉';

-- 7203 × 8103
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '小包-混合', 9.90, 80, 400, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '小包-混合' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '小包-纯肉', 11.90, 100, 360, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '小包-纯肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '中包-混合', 18.80, 160, 260, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '中包-混合' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '中包-纯肉', 21.80, 190, 230, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '中包-纯肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '大包-混合', 29.90, 260, 180, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '大包-混合' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '大包-纯肉', 34.90, 300, 150, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '大包-纯肉' AND IFNULL(is_deleted, 0) = 0);

UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 9.90, point_price = 80, stock = 400 WHERE spu_id = 7203 AND sku_name = '小包-混合';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 11.90, point_price = 100, stock = 360 WHERE spu_id = 7203 AND sku_name = '小包-纯肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 18.80, point_price = 160, stock = 260 WHERE spu_id = 7203 AND sku_name = '中包-混合';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 21.80, point_price = 190, stock = 230 WHERE spu_id = 7203 AND sku_name = '中包-纯肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 29.90, point_price = 260, stock = 180 WHERE spu_id = 7203 AND sku_name = '大包-混合';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 34.90, point_price = 300, stock = 150 WHERE spu_id = 7203 AND sku_name = '大包-纯肉';

-- 7204 × 8104
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7204, 8104, '100g-原味', 15.00, 0, 200, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7204 AND sku_name = '100g-原味' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7204, 8104, '100g-牛肉', 18.00, 80, 180, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7204 AND sku_name = '100g-牛肉' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7204, 8104, '200g-原味', 28.00, 150, 160, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7204 AND sku_name = '200g-原味' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7204, 8104, '200g-牛肉', 32.00, 200, 140, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7204 AND sku_name = '200g-牛肉' AND IFNULL(is_deleted, 0) = 0);

UPDATE mall_sku SET is_deleted = 0, spec_id = 8104, price = 15.00, point_price = 0, stock = 200 WHERE spu_id = 7204 AND sku_name = '100g-原味';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8104, price = 18.00, point_price = 80, stock = 180 WHERE spu_id = 7204 AND sku_name = '100g-牛肉';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8104, price = 28.00, point_price = 150, stock = 160 WHERE spu_id = 7204 AND sku_name = '200g-原味';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8104, price = 32.00, point_price = 200, stock = 140 WHERE spu_id = 7204 AND sku_name = '200g-牛肉';

-- 同商品同 SKU 名仅保留最小 id
UPDATE mall_sku s
JOIN (
  SELECT spu_id, sku_name, MIN(id) keep_id
  FROM mall_sku
  WHERE spu_id IN (7201, 7202, 7203, 7204) AND IFNULL(is_deleted, 0) = 0
  GROUP BY spu_id, sku_name
  HAVING COUNT(*) > 1
) d ON s.spu_id = d.spu_id AND s.sku_name = d.sku_name AND s.id <> d.keep_id
SET s.is_deleted = -1
WHERE IFNULL(s.is_deleted, 0) = 0;

-- ========== 5) 其余在售商品改为单规格，清空 spec_id ==========
UPDATE mall_spu
SET spec_mode = 1
WHERE IFNULL(is_deleted, 0) = 0
  AND id NOT IN (7201, 7202, 7203, 7204)
  AND IFNULL(spec_mode, 1) <> 1;

UPDATE mall_sku k
JOIN mall_spu p ON p.id = k.spu_id
SET k.spec_id = NULL
WHERE IFNULL(k.is_deleted, 0) = 0
  AND IFNULL(p.is_deleted, 0) = 0
  AND p.id NOT IN (7201, 7202, 7203, 7204)
  AND k.spec_id IS NOT NULL;

-- 单规格商品若有多条有效 SKU，仅保留最早一条
UPDATE mall_sku k
JOIN (
  SELECT spu_id, MIN(id) keep_id
  FROM mall_sku
  WHERE IFNULL(is_deleted, 0) = 0
    AND spu_id NOT IN (7201, 7202, 7203, 7204)
  GROUP BY spu_id
  HAVING COUNT(*) > 1
) d ON k.spu_id = d.spu_id AND k.id <> d.keep_id
SET k.is_deleted = -1
WHERE IFNULL(k.is_deleted, 0) = 0;
