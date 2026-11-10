const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com', port: 3306,
    user: 'root', password: '4A4h85myDa8fNic8', database: 'chongwu'
  });

  // 加 action_type 字段（如已存在则跳过）
  const [cols] = await conn.query("SHOW COLUMNS FROM island_task LIKE 'action_type';");
  if (cols.length === 0) {
    await conn.query("ALTER TABLE island_task ADD COLUMN action_type VARCHAR(32) NULL DEFAULT NULL COMMENT '触发完成的动作类型: feed/post/like/comment/share/buy/resident/login/none' AFTER task_group;");
    console.log('已添加 action_type 字段');
  } else {
    console.log('action_type 字段已存在，跳过');
  }

  // 给现有任务补默认 action_type
  await conn.query("UPDATE island_task SET action_type='feed' WHERE id=3001 AND action_type IS NULL;");
  await conn.query("UPDATE island_task SET action_type='post' WHERE id=3003 AND action_type IS NULL;");
  // 3002 陪宠物玩耍 -> play（暂无对应动作，设为 none 手动）
  await conn.query("UPDATE island_task SET action_type='none' WHERE id=3002 AND action_type IS NULL;");

  const [rows] = await conn.query("SELECT id, title, task_group, action_type, reward_coins, reward_points FROM island_task ORDER BY sort, id;");
  console.log('=== 现有任务（含 action_type）===');
  console.table(rows);

  await conn.end();
}
main().catch(e => { console.error('ERROR:', e.message); process.exit(1); });
