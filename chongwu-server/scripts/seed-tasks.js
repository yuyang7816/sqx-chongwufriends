const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com', port: 3306,
    user: 'root', password: '4A4h85myDa8fNic8', database: 'chongwu'
  });

  // 1) 更新现有 3001 投喂任务：花瓣奖励 +30
  await conn.query("UPDATE island_task SET reward_points=30, reward_desc='+50金币 +30花瓣' WHERE id=3001;");

  // 2) 新增任务
  const tasks = [
    // 每日待办
    ['给动态点个赞', '在朋友圈给任意动态点赞1次', '+10金币 +5花瓣', '每日待办', 'like', 10, 5, 1, 2],
    ['评论一条动态', '在朋友圈给任意动态评论1次', '+15金币 +5花瓣', '每日待办', 'comment', 15, 5, 1, 3],
    // 家主任务
    ['设置常驻宠物', '在岛宅设置1只常驻宠物', '+60金币 +20花瓣', '家主任务', 'resident', 60, 20, 1, 1],
    ['集市兑换道具', '在岛宅集市兑换1次道具', '+40金币', '家主任务', 'buy', 40, 0, 1, 3],
    ['解锁新玩具', '达到 Lv.3 后解锁高级玩具', '+100金币', '家主任务', 'none', 100, 0, 3, 5],
    // 社群任务
    ['分享一条动态', '分享任意动态到好友/朋友圈', '+15金币 +10花瓣', '社群任务', 'share', 15, 10, 1, 2],
    ['完善宠物档案', '在档案里完善宠物资料', '+25金币 +10花瓣', '社群任务', 'none', 25, 10, 1, 3],
  ];
  for (const t of tasks) {
    await conn.query(
      "INSERT INTO island_task (title, description, reward_desc, task_group, action_type, reward_coins, reward_points, required_level, sort, status) VALUES (?,?,?,?,?,?,?,?,?,1);",
      t
    );
  }

  const [rows] = await conn.query("SELECT id, title, task_group, action_type, reward_coins, reward_points, required_level, sort, status FROM island_task ORDER BY task_group, sort, id;");
  console.log('=== 任务列表 ===');
  console.table(rows);

  await conn.end();
}
main().catch(e => { console.error('ERROR:', e.message); process.exit(1); });
