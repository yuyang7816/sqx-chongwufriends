-- 投喂模块独立菜单 + 6 条正规投喂商品
-- 可重复执行
SET NAMES utf8mb4;

-- ========== 1) 新建顶级菜单：投喂模块 ==========
INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (2184, 0, '投喂模块', NULL, NULL, 0, 'present', 7)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  name = VALUES(name),
  url = NULL,
  perms = NULL,
  type = 0,
  icon = VALUES(icon),
  order_num = VALUES(order_num);

-- 投喂商品 / 投喂订单 从商城管理挪到投喂模块
UPDATE sys_menu SET parent_id = 2184, order_num = 0, icon = 'goods' WHERE menu_id = 2160;
UPDATE sys_menu SET parent_id = 2184, order_num = 1, icon = 'list'  WHERE menu_id = 2033;

-- 按钮权限仍挂在对应菜单下
UPDATE sys_menu SET parent_id = 2160 WHERE menu_id IN (2161, 2162, 2163);
UPDATE sys_menu SET parent_id = 2033 WHERE menu_id = 2146;

-- 超管授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_id IN (2184, 2160, 2033, 2161, 2162, 2163, 2146);

-- ========== 2) 投喂商品种子：清理旧数据，写入 6 条正规商品 ==========
UPDATE mall_spu
SET is_deleted = 1, update_time = NOW()
WHERE (biz_type = 2 OR category = 'feed' OR category LIKE '%投喂%')
  AND (is_deleted = 0 OR is_deleted IS NULL);

UPDATE mall_sku k
JOIN mall_spu s ON s.id = k.spu_id
SET k.is_deleted = 1, k.update_time = NOW()
WHERE (s.biz_type = 2 OR s.category = 'feed' OR s.category LIKE '%投喂%')
  AND (k.is_deleted = 0 OR k.is_deleted IS NULL);

-- 确保「客户投喂」分类存在
SET @feed_cat_id = (SELECT id FROM mall_category WHERE name = '客户投喂' ORDER BY id DESC LIMIT 1);
INSERT INTO mall_category (name, sort, status)
SELECT '客户投喂', 1, 1 FROM DUAL WHERE @feed_cat_id IS NULL;
SET @feed_cat_id = (SELECT id FROM mall_category WHERE name = '客户投喂' ORDER BY id DESC LIMIT 1);

DELETE FROM mall_sku WHERE spu_id BETWEEN 7201 AND 7206;
DELETE FROM mall_spu WHERE id BETWEEN 7201 AND 7206;

INSERT INTO mall_spu (
  id, name, category, category_id, cover_url, description,
  price, original_price, point_price, biz_type, status, sort,
  create_time, update_time, is_deleted
) VALUES
(7201, '手工鲜食包(牛肉味)', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/0883703dd0004b0f886e197ae815b129.png', '新鲜牛肉+南瓜+西兰花，营养均衡', 15.00, 18.00, 0, 2, 1, 1, NOW(), NOW(), 0),
(7202, '进口主食罐头(金枪鱼)', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png', '高蛋白，适口性极佳', 28.00, 32.00, 0, 2, 1, 2, NOW(), NOW(), 0),
(7203, '混合冻干零食包', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/0883703dd0004b0f886e197ae815b129.png', '鸡肉、鸭肉、鹌鹑，磨牙必备', 12.00, 15.00, 0, 2, 1, 3, NOW(), NOW(), 0),
(7204, '宠物专供羊奶', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/c0497df08c53480d83810dbeeaa4fa43.png', '温和易吸收，补钙滋养肠胃', 18.00, 22.00, 0, 2, 1, 4, NOW(), NOW(), 0),
(7205, '鲜肉软罐头礼盒', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/c0497df08c53480d83810dbeeaa4fa43.png', '多口味软罐头组合，门店直供鲜食', 22.00, 26.00, 0, 2, 1, 5, NOW(), NOW(), 0),
(7206, '营养化毛膏套装', 'feed', @feed_cat_id, 'https://tu.mianyazuju.com/20260626/c0497df08c53480d83810dbeeaa4fa43.png', '化毛护肠胃，日常养护好帮手', 16.00, 19.90, 0, 2, 1, 6, NOW(), NOW(), 0);

INSERT INTO mall_sku (id, spu_id, sku_name, price, point_price, stock, create_time, update_time, is_deleted) VALUES
(7301, 7201, '标准装', 15.00, 0, 999, NOW(), NOW(), 0),
(7302, 7202, '标准装', 28.00, 0, 999, NOW(), NOW(), 0),
(7303, 7203, '标准装', 12.00, 0, 999, NOW(), NOW(), 0),
(7304, 7204, '标准装', 18.00, 0, 999, NOW(), NOW(), 0),
(7305, 7205, '标准装', 22.00, 0, 999, NOW(), NOW(), 0),
(7306, 7206, '标准装', 16.00, 0, 999, NOW(), NOW(), 0);
