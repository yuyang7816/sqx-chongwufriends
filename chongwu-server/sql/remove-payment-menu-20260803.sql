-- 去掉管理端「支付管理」菜单及其子菜单（支付回调日志等）
SET NAMES utf8mb4;

-- 收集支付管理目录及子项
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id = 2060 OR parent_id = 2060
  ) t
);

DELETE FROM sys_menu WHERE menu_id = 2060 OR parent_id = 2060;
