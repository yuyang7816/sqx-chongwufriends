-- 宠物业务初始化数据（在 pet-business-schema.sql 执行后运行）

INSERT INTO pet_city (name, code, sort, status) VALUES
('上海', 'SH', 1, 1),
('杭州', 'HZ', 2, 1),
('南京', 'NJ', 3, 1);

INSERT INTO pet_store (city_id, name, address, tone, business_status, sort) VALUES
(1, '爱琴海店', '上海市闵行区', '#f6d57c', 1, 1),
(1, '百脑汇店', '上海市徐汇区', '#cfeedd', 1, 2),
(1, '大学路店', '上海市杨浦区', '#f7d5db', 1, 3),
(1, '淮海路店', '上海市黄浦区', '#efe6d8', 1, 4);

INSERT INTO pet_archive (store_id, name, pet_type, breed, gender, age_desc, pet_code, level_desc, status_label, description, like_count, adoptable, sort) VALUES
(1, '豆包', 'dog', '柴犬', '男', '2岁', 'SH-007', 'Lv.5 明星宠物', '优秀员工', '豆包性格友好，特别会活跃气氛。', 12800, 1, 1),
(2, '花姐', 'cat', '三花', '女', '7岁', 'SH-012', 'Lv.4 人气喵星', '优秀员工', '花姐是百脑汇店的元老级员工。', 6400, 0, 1),
(3, '小七', 'dog', '柯基', '女', '1岁', 'SH-028', 'Lv.4 新星萌宠', '优秀员工', '小七非常亲人，喜欢被夸奖。', 7200, 1, 1);

INSERT INTO operation_banner (name, image_url, jump_url, position_code, sort, status) VALUES
('理性养宠', '/static/logo.png', '/packageA/adopt/list', 'home', 1, 1);

INSERT INTO pet_strategy_category (name, sort, status) VALUES
('汪星人', 1, 1),
('喵星人', 2, 1),
('幼宠', 3, 1);

INSERT INTO pet_strategy (title, sub_title, tag, category_id, cover_url, content, view_count, like_count, sort, status) VALUES
('新手养狗必看清单', '从接狗狗回家第一天到养成健康习惯，一篇全搞定', '汪星人,幼宠', 1, NULL, '<h3>接狗回家前准备</h3><p>准备食盆水盆、牵引绳、宠物窝、尿垫、基础粮和驱虫药，提前规划好活动空间，把易咬物品收好。</p><h3>第一周适应期</h3><p>保持安静环境，固定喂食与如厕时间，减少频繁外出和陌生人刺激，让狗狗建立安全感。</p><h3>健康与训练</h3><p>按月龄完成疫苗和驱虫；从名字回应、坐下、定点排便开始，用正向反馈建立规则。</p>', 23000, 1200, 1, 1),
('猫咪的100种小表情解读', '读懂主子的每个眼神和尾巴语言', '喵星人', 2, NULL, '<h3>眼睛在说什么</h3><p>慢眨眼代表信任；瞳孔放大可能是兴奋或紧张。</p><h3>尾巴语言</h3><p>尾巴竖起微微晃动多是心情不错；快速甩尾往往是烦躁信号。</p>', 18000, 956, 2, 1),
('宠物自制鲜食食谱大全', '营养均衡的5道鲜食，比罐头更健康', '饮食', NULL, NULL, '<h3>鲜食原则</h3><p>蛋白质为主、适量蔬菜、控制油脂和调味，避开洋葱、葡萄、巧克力等危险食物。</p>', 15000, 823, 3, 1),
('如何判断宠物是否生病', '10个常见症状自查清单，建议收藏', '健康', 3, NULL, '<h3>需要警惕的信号</h3><p>拒食、持续呕吐腹泻、精神萎靡、呼吸急促、大量饮水却尿少等，请及时就医。</p>', 31000, 2000, 4, 1),
('柴犬饲养百科', '从性格特点到日常护理，全面了解柴犬', '汪星人', 1, NULL, '<h3>性格特点</h3><p>柴犬独立、警觉、忠诚。日常需足够运动、梳理被毛与正向训练。</p>', 27000, 1500, 5, 1);

INSERT INTO island_task (title, description, reward_desc, required_level, sort, status) VALUES
('每日投喂宠物', '已完成', '+50 金币', 1, 1, 1),
('陪宠物玩耍 5 分钟', '进行中', '+30 金币 +20 花瓣', 1, 2, 1);
