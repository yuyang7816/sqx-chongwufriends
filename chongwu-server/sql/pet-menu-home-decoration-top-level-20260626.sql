-- 将「首页装修」从「商城管理」子菜单提升为顶级菜单（与用户管理同级）
-- 执行前请备份 sys_menu

-- 先为顶级菜单腾出排序位（插在用户管理之后）
UPDATE sys_menu
SET order_num = order_num + 1
WHERE parent_id = 0
  AND order_num >= 2
  AND menu_id != 2034;

-- 提升首页装修为顶级菜单
UPDATE sys_menu
SET parent_id = 0,
    order_num = 2
WHERE menu_id = 2034;
