const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com', port: 3306,
    user: 'root', password: '4A4h85myDa8fNic8', database: 'chongwu'
  });

  const [cols] = await conn.query("SHOW COLUMNS FROM island_task;");
  console.log('=== island_task 字段 ===');
  console.table(cols.map(c => ({ Field: c.Field, Type: c.Type })));

  const [tasks] = await conn.query("SELECT * FROM island_task ORDER BY sort, id;");
  console.log('=== 现有任务 ===');
  console.table(tasks);

  const [cnt] = await conn.query("SELECT COUNT(*) c FROM island_task;");
  console.log('任务总数:', cnt[0].c);

  await conn.end();
}
main().catch(e => { console.error('ERROR:', e.message); process.exit(1); });
