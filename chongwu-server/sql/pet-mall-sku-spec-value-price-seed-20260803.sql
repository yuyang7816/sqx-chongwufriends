-- 规格值各自售价/积分/库存示例数据 + 绑定商品
SET NAMES utf8mb4;

-- 1) 升级旧规格：把 detail 补成 items（若尚无 items）
UPDATE mall_sku_spec
SET rule_value = JSON_ARRAY(
  JSON_OBJECT(
    'value', '规格',
    'detail', spec_name,
    'items', JSON_ARRAY(
      JSON_OBJECT(
        'name', spec_name,
        'price', IFNULL(price, 0),
        'pointPrice', IFNULL(point_price, 0),
        'stock', IFNULL(default_stock, 0)
      )
    )
  )
)
WHERE IFNULL(is_deleted, 0) = 0
  AND (rule_value IS NULL OR rule_value = '' OR rule_value NOT LIKE '%"items"%');

-- 2) 鲜食份量模板
INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8101, '鲜食份量',
'[{"value":"份量","detail":"100g,200g,300g","items":[{"name":"100g","price":15.00,"pointPrice":0,"stock":200},{"name":"200g","price":28.00,"pointPrice":150,"stock":160},{"name":"300g","price":39.00,"pointPrice":220,"stock":120}]}]',
15.00, 0, 200, 10, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8101);

UPDATE mall_sku_spec
SET spec_name = '鲜食份量',
    rule_value = '[{"value":"份量","detail":"100g,200g,300g","items":[{"name":"100g","price":15.00,"pointPrice":0,"stock":200},{"name":"200g","price":28.00,"pointPrice":150,"stock":160},{"name":"300g","price":39.00,"pointPrice":220,"stock":120}]}]',
    price = 15.00, point_price = 0, default_stock = 200, sort = 10, status = 1, is_deleted = 0
WHERE id = 8101;

-- 3) 罐头规格模板
INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8102, '罐头规格',
'[{"value":"净含量","detail":"85g,170g,370g","items":[{"name":"85g","price":12.80,"pointPrice":0,"stock":300},{"name":"170g","price":22.80,"pointPrice":180,"stock":220},{"name":"370g","price":39.90,"pointPrice":320,"stock":150}]}]',
12.80, 0, 300, 11, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8102);

UPDATE mall_sku_spec
SET spec_name = '罐头规格',
    rule_value = '[{"value":"净含量","detail":"85g,170g,370g","items":[{"name":"85g","price":12.80,"pointPrice":0,"stock":300},{"name":"170g","price":22.80,"pointPrice":180,"stock":220},{"name":"370g","price":39.90,"pointPrice":320,"stock":150}]}]',
    price = 12.80, point_price = 0, default_stock = 300, sort = 11, status = 1, is_deleted = 0
WHERE id = 8102;

-- 4) 零食规格模板
INSERT INTO mall_sku_spec (id, spec_name, rule_value, price, point_price, default_stock, sort, status, is_deleted)
SELECT 8103, '零食规格',
'[{"value":"包装","detail":"小包,中包,大包","items":[{"name":"小包","price":9.90,"pointPrice":80,"stock":400},{"name":"中包","price":18.80,"pointPrice":160,"stock":260},{"name":"大包","price":29.90,"pointPrice":260,"stock":180}]}]',
9.90, 80, 400, 12, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM mall_sku_spec WHERE id = 8103);

UPDATE mall_sku_spec
SET spec_name = '零食规格',
    rule_value = '[{"value":"包装","detail":"小包,中包,大包","items":[{"name":"小包","price":9.90,"pointPrice":80,"stock":400},{"name":"中包","price":18.80,"pointPrice":160,"stock":260},{"name":"大包","price":29.90,"pointPrice":260,"stock":180}]}]',
    price = 9.90, point_price = 80, default_stock = 400, sort = 12, status = 1, is_deleted = 0
WHERE id = 8103;

-- 5) 把示例商品改为多规格，并重建 SKU
UPDATE mall_spu SET spec_mode = 2, price = 15.00 WHERE id = 7201; -- 手工鲜食包
UPDATE mall_spu SET spec_mode = 2, price = 12.80 WHERE id = 7202; -- 进口主食罐头
UPDATE mall_spu SET spec_mode = 2, price = 9.90 WHERE id = 7203; -- 混合冻干零食包

-- 清理旧 SKU
UPDATE mall_sku SET is_deleted = 1 WHERE spu_id IN (7201, 7202, 7203) AND IFNULL(is_deleted, 0) = 0;

-- 鲜食包 SKU
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '100g', 15.00, 0, 200, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '100g' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '200g', 28.00, 150, 160, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '200g' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7201, 8101, '300g', 39.00, 220, 120, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7201 AND sku_name = '300g' AND IFNULL(is_deleted, 0) = 0);

-- 罐头 SKU
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '85g', 12.80, 0, 300, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '85g' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '170g', 22.80, 180, 220, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '170g' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7202, 8102, '370g', 39.90, 320, 150, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7202 AND sku_name = '370g' AND IFNULL(is_deleted, 0) = 0);

-- 零食 SKU
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '小包', 9.90, 80, 400, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '小包' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '中包', 18.80, 160, 260, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '中包' AND IFNULL(is_deleted, 0) = 0);
INSERT INTO mall_sku (spu_id, spec_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7203, 8103, '大包', 29.90, 260, 180, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE spu_id = 7203 AND sku_name = '大包' AND IFNULL(is_deleted, 0) = 0);

-- 若旧 SKU 是软删后同名仍在，恢复并更新价格
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 15.00, point_price = 0, stock = 200 WHERE spu_id = 7201 AND sku_name = '100g';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 28.00, point_price = 150, stock = 160 WHERE spu_id = 7201 AND sku_name = '200g';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8101, price = 39.00, point_price = 220, stock = 120 WHERE spu_id = 7201 AND sku_name = '300g';

UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 12.80, point_price = 0, stock = 300 WHERE spu_id = 7202 AND sku_name = '85g';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 22.80, point_price = 180, stock = 220 WHERE spu_id = 7202 AND sku_name = '170g';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8102, price = 39.90, point_price = 320, stock = 150 WHERE spu_id = 7202 AND sku_name = '370g';

UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 9.90, point_price = 80, stock = 400 WHERE spu_id = 7203 AND sku_name = '小包';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 18.80, point_price = 160, stock = 260 WHERE spu_id = 7203 AND sku_name = '中包';
UPDATE mall_sku SET is_deleted = 0, spec_id = 8103, price = 29.90, point_price = 260, stock = 180 WHERE spu_id = 7203 AND sku_name = '大包';
