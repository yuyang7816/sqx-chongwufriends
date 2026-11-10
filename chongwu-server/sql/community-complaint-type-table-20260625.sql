CREATE TABLE IF NOT EXISTS community_complaint_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1启用 2禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_complaint_type_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='朋友圈投诉类型';

INSERT INTO community_complaint_type (name, sort, status)
SELECT '垃圾广告', 0, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM community_complaint_type WHERE name = '垃圾广告');

INSERT INTO community_complaint_type (name, sort, status)
SELECT '违法违规', 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM community_complaint_type WHERE name = '违法违规');

INSERT INTO community_complaint_type (name, sort, status)
SELECT '人身攻击', 2, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM community_complaint_type WHERE name = '人身攻击');

INSERT INTO community_complaint_type (name, sort, status)
SELECT '不实信息', 3, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM community_complaint_type WHERE name = '不实信息');

INSERT INTO community_complaint_type (name, sort, status)
SELECT '其他', 4, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM community_complaint_type WHERE name = '其他');

UPDATE community_complaint_type SET is_deleted = 0, sort = 0, status = 1 WHERE name = '垃圾广告' AND is_deleted <> 0;
UPDATE community_complaint_type SET is_deleted = 0, sort = 1, status = 1 WHERE name = '违法违规' AND is_deleted <> 0;
UPDATE community_complaint_type SET is_deleted = 0, sort = 2, status = 1 WHERE name = '人身攻击' AND is_deleted <> 0;
UPDATE community_complaint_type SET is_deleted = 0, sort = 3, status = 1 WHERE name = '不实信息' AND is_deleted <> 0;
UPDATE community_complaint_type SET is_deleted = 0, sort = 4, status = 1 WHERE name = '其他' AND is_deleted <> 0;
