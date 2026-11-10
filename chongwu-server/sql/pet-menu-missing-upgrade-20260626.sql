-- 补全宠物业务缺失菜单与按钮权限（宠物城市、首页社群入口及 CRUD 按钮）
-- 可重复执行：先删新增 menu_id 再插入
-- 执行前请备份 sys_menu、sys_role_menu

SET NAMES utf8mb4;

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2105 AND 2131;
DELETE FROM sys_menu WHERE menu_id BETWEEN 2105 AND 2131;

-- 顶级菜单腾出「首页社群入口」排序位（紧跟首页装修之后）
UPDATE sys_menu
SET order_num = order_num + 1
WHERE parent_id = 0
  AND order_num >= 3;

-- 宠物管理下腾出「宠物城市」排序位（排在门店管理之前）
UPDATE sys_menu
SET order_num = order_num + 1
WHERE parent_id = 2010;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES
-- 宠物城市
(2105, 2010, '宠物城市', 'pet/cities', 'pet:city:list', 1, 'location', 0),
(2106, 2105, '新增城市', NULL, 'pet:city:add', 2, NULL, 0),
(2107, 2105, '编辑城市', NULL, 'pet:city:update', 2, NULL, 1),
(2108, 2105, '删除城市', NULL, 'pet:city:delete', 2, NULL, 2),
-- 首页社群入口（与用户管理、首页装修同级）
(2109, 0, '首页社群入口', 'pet/home-social', 'pet:home:social:list', 1, 'link', 3),
(2110, 2109, '保存社群配置', NULL, 'pet:home:social:update', 2, NULL, 0),
-- 门店管理按钮
(2111, 2011, '新增门店', NULL, 'pet:store:add', 2, NULL, 0),
(2112, 2011, '编辑门店', NULL, 'pet:store:update', 2, NULL, 1),
(2113, 2011, '删除门店', NULL, 'pet:store:delete', 2, NULL, 2),
-- 宠物档案按钮
(2114, 2012, '新增档案', NULL, 'pet:archive:add', 2, NULL, 0),
(2115, 2012, '编辑档案', NULL, 'pet:archive:update', 2, NULL, 1),
(2116, 2012, '删除档案', NULL, 'pet:archive:delete', 2, NULL, 2),
-- 敏感词按钮
(2117, 2022, '新增敏感词', NULL, 'pet:sensitive:add', 2, NULL, 0),
(2118, 2022, '编辑敏感词', NULL, 'pet:sensitive:update', 2, NULL, 1),
(2119, 2022, '删除敏感词', NULL, 'pet:sensitive:delete', 2, NULL, 2),
-- 领养审核
(2120, 2041, '领养状态变更', NULL, 'pet:adopt:update', 2, NULL, 0),
-- 岛宅任务按钮
(2121, 2051, '新增任务', NULL, 'pet:task:add', 2, NULL, 0),
(2122, 2051, '编辑任务', NULL, 'pet:task:update', 2, NULL, 1),
(2123, 2051, '删除任务', NULL, 'pet:task:delete', 2, NULL, 2),
-- 养宠攻略按钮
(2124, 2053, '新增攻略', NULL, 'pet:strategy:add', 2, NULL, 0),
(2125, 2053, '编辑攻略', NULL, 'pet:strategy:update', 2, NULL, 1),
(2126, 2053, '删除攻略', NULL, 'pet:strategy:delete', 2, NULL, 2),
-- 集市道具按钮
(2127, 2054, '新增道具', NULL, 'pet:market:add', 2, NULL, 0),
(2128, 2054, '编辑道具', NULL, 'pet:market:update', 2, NULL, 1),
(2129, 2054, '删除道具', NULL, 'pet:market:delete', 2, NULL, 2),
-- 岛宅状态按钮
(2130, 2055, '编辑岛宅', NULL, 'pet:island:update', 2, NULL, 0),
-- 用户列表删除
(2131, 2001, '删除用户', NULL, 'userList:delete', 2, NULL, 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2105 AND 2131
ON DUPLICATE KEY UPDATE role_id = role_id;
