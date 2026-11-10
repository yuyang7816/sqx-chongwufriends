-- M03 门店小伙伴：确保门店宠物档案可在用户端展示
-- 可重复执行

SET NAMES utf8mb4;

UPDATE pet_archive
SET audit_status = 1
WHERE audit_status IS NULL OR audit_status = 0;
