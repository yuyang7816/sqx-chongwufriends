-- 商城轮播图表 + 初始数据（MySQL 5.7+）
-- 可重复执行

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS operation_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    image_url VARCHAR(512) DEFAULT NULL,
    jump_url VARCHAR(512) DEFAULT NULL,
    position_code VARCHAR(32) DEFAULT 'home' COMMENT 'home/adopt/shop',
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营位';

INSERT INTO operation_banner (name, image_url, jump_url, position_code, sort, status)
SELECT '春季萌宠市集', 'https://mydz.xianmxkj.com/file/uploadPath/2026/06/25/demo-shop-banner.png', '/packageA/product/detail?id=7001', 'shop', 1, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM operation_banner WHERE position_code = 'shop' AND name = '春季萌宠市集'
);

-- 确保商城装修菜单与按钮权限存在
DELETE FROM sys_role_menu WHERE menu_id IN (2034, 2035, 2036, 2037);
DELETE FROM sys_menu WHERE menu_id IN (2034, 2035, 2036, 2037);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
(2034, 0, '首页装修', 'content/decorations', 'bannerList:list', 1, 'picture', 2),
(2035, 2034, '新增轮播', NULL, 'bannerList:add', 2, NULL, 0),
(2036, 2034, '编辑轮播', NULL, 'bannerList:update', 2, NULL, 1),
(2037, 2034, '删除轮播', NULL, 'bannerList:delete', 2, NULL, 2);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (2034, 2035, 2036, 2037)
ON DUPLICATE KEY UPDATE role_id = role_id;
