-- M07 商城与支付：商品种子、商城 Banner、测试用户积分、沙箱支付开关
SET NAMES utf8mb4;

-- 商城分类
INSERT INTO mall_category (id, name, sort, status)
SELECT 1, '全部商品', 0, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE id = 1);
INSERT INTO mall_category (id, name, sort, status)
SELECT 2, '客户投喂', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE id = 2);
INSERT INTO mall_category (id, name, sort, status)
SELECT 3, '超值次卡', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE id = 3);
INSERT INTO mall_category (id, name, sort, status)
SELECT 4, '实物赠予', 3, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE id = 4);

-- 商城 SPU（普通商品 + 积分兑换品）
INSERT INTO mall_spu (id, name, category, category_id, cover_url, description, price, original_price, point_price, biz_type, status, sort, is_deleted)
SELECT 7001, '鲜肉罐头套餐', 'feed', 2, 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png',
       '门店鲜食罐头，投喂后可见反馈视频', 29.90, 39.90, 0, 1, 1, 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_spu WHERE id = 7001);

INSERT INTO mall_spu (id, name, category, category_id, cover_url, description, price, original_price, point_price, biz_type, status, sort, is_deleted)
SELECT 7002, '冻干零食礼包', 'feed', 2, 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png',
       '冻干鸡肉+三文鱼，毛孩子最爱', 19.90, 25.90, 0, 1, 1, 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_spu WHERE id = 7002);

INSERT INTO mall_spu (id, name, category, category_id, cover_url, description, price, original_price, point_price, biz_type, status, sort, is_deleted)
SELECT 7005, '宠物零食礼盒', 'gift', 4, 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png',
       '精选零食礼盒，可积分兑换', 0.00, 0.00, 500, 4, 1, 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_spu WHERE id = 7005);

-- SKU
INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7101, 7001, '标准装', 29.90, 0, 999, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE id = 7101);

INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7102, 7002, '标准装', 19.90, 0, 999, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE id = 7102);

INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, is_deleted)
SELECT 7104, 7005, '积分兑换', 0.00, 500, 50, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_sku WHERE id = 7104);

-- 商城 Tab Banner
INSERT INTO operation_banner (name, image_url, jump_url, position_code, sort, status, sub_title)
SELECT '春季萌宠市集', 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png',
       '/packageA/product/detail?id=7001', 'shop', 1, 1, '门店同款好物限时优惠'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM operation_banner WHERE position_code = 'shop' AND status = 1);

-- 测试用户积分（确保可兑换 500 积分商品）
UPDATE user_asset SET points = GREATEST(COALESCE(points, 0), 800)
WHERE user_id = (SELECT user_id FROM tb_user WHERE phone = '13800138000' LIMIT 1);

-- 沙箱支付开关（未配置微信商户号时自动走 mock 渠道）
INSERT INTO sys_config (param_key, param_value, status, remark, param_type, param_type_desc)
SELECT 'pet_pay_mock_enabled', '1', '1', '商城沙箱支付开关1开0关', 'kaiguan', '开关'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE param_key = 'pet_pay_mock_enabled');
