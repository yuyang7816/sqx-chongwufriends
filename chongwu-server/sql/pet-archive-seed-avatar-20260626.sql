-- P0：宠物档案封面与随拍相册种子数据（可重复执行）
SET NAMES utf8mb4;

-- 封面图（CDN 可访问）
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png' WHERE id = 1;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/a687bbd2bc5e426ab8df84db6c9ce0af.png' WHERE id = 2;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png' WHERE id = 3;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png' WHERE id = 4;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png' WHERE id = 5;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png' WHERE id = 6;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/a687bbd2bc5e426ab8df84db6c9ce0af.png' WHERE id = 7;
UPDATE pet_archive SET avatar_url = 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png' WHERE id = 8;

-- 随拍相册：每只宠物 2 张（封面 + 补充图）
DELETE FROM pet_album WHERE pet_id BETWEEN 1 AND 8;

INSERT INTO pet_album (pet_id, media_url, media_type, sort) VALUES
(1, 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png', 1, 1),
(1, 'https://tu.mianyazuju.com/20260626/2b90c86749ab43afb4a3b4ef7402c1d7.png', 1, 2),
(2, 'https://tu.mianyazuju.com/20260626/a687bbd2bc5e426ab8df84db6c9ce0af.png', 1, 1),
(2, 'https://tu.mianyazuju.com/20260626/8c0e780e15c745a5b77ba8fdd36b22c9.png', 1, 2),
(3, 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png', 1, 1),
(3, 'https://tu.mianyazuju.com/20260626/c0497df08c53480d83810dbeeaa4fa43.png', 1, 2),
(4, 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png', 1, 1),
(4, 'https://tu.mianyazuju.com/20260626/1a69e484c7574ef7a519ceeb471625ac.png', 1, 2),
(5, 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png', 1, 1),
(5, 'https://tu.mianyazuju.com/20260626/dcf11d725a3b42298fa46c6e69206d4c.png', 1, 2),
(6, 'https://tu.mianyazuju.com/20260626/b1cf3f3c49414cb9b348ceb5752c7076.png', 1, 1),
(6, 'https://tu.mianyazuju.com/20260626/2d43e381d90e4154b0c894639359798c.png', 1, 2),
(7, 'https://tu.mianyazuju.com/20260626/a687bbd2bc5e426ab8df84db6c9ce0af.png', 1, 1),
(7, 'https://tu.mianyazuju.com/20260626/0883703dd0004b0f886e197ae815b129.png', 1, 2),
(8, 'https://tu.mianyazuju.com/20260626/2841865290584c849d241db375e4798d.png', 1, 1),
(8, 'https://tu.mianyazuju.com/20260626/8d353c28a7904c2ca3323d27f66db0ba.png', 1, 2);

-- 确保已审核通过才在用户端展示
UPDATE pet_archive SET audit_status = 1 WHERE id BETWEEN 1 AND 8 AND (audit_status IS NULL OR audit_status = 0);
