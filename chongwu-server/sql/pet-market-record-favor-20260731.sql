-- 集市兑换记录补充亲密度快照字段，便于「我的兑换」展示当时增加的亲密度
ALTER TABLE island_market_record
    ADD COLUMN favor INT DEFAULT 0 COMMENT '兑换时增加的亲密度快照' AFTER pay_points;

-- 回填旧记录：按道具当前 favor
UPDATE island_market_record r
    INNER JOIN island_market_item i ON i.id = r.item_id
SET r.favor = IFNULL(i.favor, 0)
WHERE r.favor IS NULL OR r.favor = 0;
