const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const sqlPath = path.join(__dirname, '../sql/island-care-daily-stat-20260804.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });
  console.log('Executing island-care SQL...');
  await conn.query(sql);
  const [cols] = await conn.query(
    "SHOW COLUMNS FROM island_pet WHERE Field IN ('food_bowl_filled','water_bowl_filled','daily_progress','stat_date')"
  );
  console.log(
    'columns:',
    cols.map((c) => `${c.Field}:${c.Type}`).join(', ')
  );
  const [cfgs] = await conn.query(
    "SELECT param_key, param_value FROM sys_config WHERE param_key LIKE 'island_%' ORDER BY param_key"
  );
  console.log('configs:', cfgs.length);
  cfgs.forEach((r) => console.log(' ', r.param_key, '=', r.param_value));
  await conn.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
