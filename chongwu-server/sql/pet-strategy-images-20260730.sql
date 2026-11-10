-- 养宠小课堂：正文穿插配图（2026-07-30）
-- 依赖本地静态资源 /static/strategy/*.jpg

UPDATE pet_strategy SET
  cover_url = '/static/strategy/dog-home.jpg',
  content = CONCAT(
    '<img class="article-img" src="/static/strategy/dog-home.jpg" alt="新手养狗" style="width:100%;border-radius:16px;margin:8px 0 16px;display:block;"/>',
    '<h3>接狗回家前准备</h3>',
    '<p>准备食盆水盆、牵引绳、宠物窝、尿垫、基础粮和驱虫药，提前规划好活动空间，把易咬物品收好。</p>',
    '<img class="article-img" src="/static/strategy/dog-puppy.jpg" alt="幼犬适应" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>第一周适应期</h3>',
    '<p>保持安静环境，固定喂食与如厕时间，减少频繁外出和陌生人刺激，让狗狗建立安全感。</p>',
    '<img class="article-img" src="/static/strategy/dog-train.jpg" alt="狗狗训练" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>健康与训练</h3>',
    '<p>按月龄完成疫苗和驱虫；从名字回应、坐下、定点排便开始，用正向反馈建立规则。</p>',
    '<h3>常见误区</h3>',
    '<p>不要频繁更换粮品牌，不要用人类食物奖励，不要在狗狗害怕时强行安抚过度。</p>'
  )
WHERE id = 1;

UPDATE pet_strategy SET
  cover_url = '/static/strategy/cat-face.jpg',
  content = CONCAT(
    '<img class="article-img" src="/static/strategy/cat-face.jpg" alt="猫咪表情" style="width:100%;border-radius:16px;margin:8px 0 16px;display:block;"/>',
    '<h3>眼睛在说什么</h3>',
    '<p>慢眨眼代表信任；瞳孔放大可能是兴奋或紧张；长时间盯着你通常是在观察，不一定是挑衅。</p>',
    '<img class="article-img" src="/static/strategy/cat-eyes.jpg" alt="猫咪眼神" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>耳朵与胡须</h3>',
    '<p>耳朵向前表示好奇，向后贴平多为不适或防御；胡须前伸是探索，后收则说明它想拉开距离。</p>',
    '<img class="article-img" src="/static/strategy/cat-rest.jpg" alt="猫咪放松" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>尾巴语言</h3>',
    '<p>尾巴竖起微微晃动多是心情不错；尾巴炸开说明受到惊吓；快速甩尾往往是烦躁信号。</p>',
    '<h3>相处建议</h3>',
    '<p>尊重猫咪边界，用慢眨眼回应信任，不要强行抱抱或突然摸肚子。</p>'
  )
WHERE id = 2;

UPDATE pet_strategy SET
  cover_url = '/static/strategy/pet-food.jpg',
  content = CONCAT(
    '<img class="article-img" src="/static/strategy/pet-food.jpg" alt="宠物鲜食" style="width:100%;border-radius:16px;margin:8px 0 16px;display:block;"/>',
    '<h3>鲜食原则</h3>',
    '<p>蛋白质为主、适量蔬菜、控制油脂和调味。人吃的盐、糖、洋葱、葡萄、巧克力等对宠物危险，务必避开。</p>',
    '<img class="article-img" src="/static/strategy/fresh-bowl.jpg" alt="自制鲜食" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>推荐食谱</h3>',
    '<p>1. 鸡胸肉南瓜泥：低脂易消化，适合肠胃敏感宠物。<br/>2. 牛肉西兰花饭：补充铁质与纤维。<br/>3. 三文鱼蔬菜粥：优质脂肪，注意去刺彻底加热。<br/>4. 鸭肉山药糊：温和适口，换粮过渡友好。<br/>5. 鸡肝蔬菜小份餐：补铁，每周不超过 1~2 次。</p>',
    '<h3>注意事项</h3>',
    '<p>鲜食需当日制作当日食用，分装冷藏不超过 24 小时；如宠物有肾病、胰腺炎等疾病，请先咨询兽医。</p>'
  )
WHERE id = 3;

UPDATE pet_strategy SET
  cover_url = '/static/strategy/vet-care.jpg',
  content = CONCAT(
    '<img class="article-img" src="/static/strategy/vet-care.jpg" alt="宠物健康检查" style="width:100%;border-radius:16px;margin:8px 0 16px;display:block;"/>',
    '<h3>需要警惕的信号</h3>',
    '<p>1. 连续两餐以上拒食；2. 呕吐或腹泻超过 24 小时；3. 精神萎靡不愿活动；4. 呼吸急促或张口呼吸；5. 大量饮水却尿少；6. 皮肤红肿掉毛抓挠；7. 眼睛分泌物增多；8. 突然跛行；9. 牙龈苍白或发黄；10. 体温异常（犬猫通常约 38~39.5℃）。</p>',
    '<img class="article-img" src="/static/strategy/pet-rest.jpg" alt="居家观察宠物" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>居家观察</h3>',
    '<p>记录发病时间、频率、是否接触新粮/新环境，就诊时把信息告诉医生，有助于更快判断。</p>',
    '<h3>就医建议</h3>',
    '<p>幼宠、老年宠、慢性病宠物出现上述症状应尽快就医；出现抽搐、持续出血、无法站立等紧急情况请立即送医。</p>'
  )
WHERE id = 4;

UPDATE pet_strategy SET
  cover_url = '/static/strategy/shiba-main.jpg',
  content = CONCAT(
    '<img class="article-img" src="/static/strategy/shiba-main.jpg" alt="柴犬" style="width:100%;border-radius:16px;margin:8px 0 16px;display:block;"/>',
    '<h3>性格特点</h3>',
    '<p>柴犬独立、警觉、忠诚，占有欲相对明显。适合有耐心、愿意坚持训练与社会化的家庭。</p>',
    '<img class="article-img" src="/static/strategy/shiba-walk.jpg" alt="柴犬日常运动" style="width:100%;border-radius:16px;margin:16px 0;display:block;"/>',
    '<h3>日常护理</h3>',
    '<p>每周梳理被毛，换毛季加强吸尘与梳毛；注意趾甲修剪和耳道清洁，避免潮湿环境导致皮肤问题。</p>',
    '<h3>运动与训练</h3>',
    '<p>每天保证足够散步和嗅闻时间。训练以短时多次、正向奖励为主，避免粗暴呵斥。</p>',
    '<img class="article-img" src="/static/pets/island-pet-shiba.png" alt="柴犬插画" style="width:100%;max-width:320px;margin:16px auto;display:block;border-radius:16px;background:#fff8f0;"/>',
    '<h3>健康关注</h3>',
    '<p>关注髋关节、过敏性皮炎与眼部问题，定期体检，保持体重管理，减少关节负担。</p>'
  )
WHERE id = 5;
