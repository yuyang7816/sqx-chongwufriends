const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu'
  });

  const [cols] = await conn.query("SHOW COLUMNS FROM sys_config;");
  console.log('=== sys_config 字段 ===');
  console.table(cols.map(c => ({ Field: c.Field, Type: c.Type })));

  const [cfg] = await conn.query("SELECT * FROM sys_config WHERE param_key LIKE '%file%' OR param_key LIKE '%cdn%' OR param_key LIKE '%upload%' LIMIT 20;");
  console.log('=== 文件相关配置 ===');
  console.table(cfg);

  await conn.end();
}

main().catch(e => {
  console.error('ERROR:', e.message);
  process.exit(1);
});
