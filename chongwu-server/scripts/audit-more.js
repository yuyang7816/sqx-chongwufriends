const mysql = require('mysql2/promise');

async function main() {
  const c = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const [cols] = await c.query('SHOW COLUMNS FROM island_pet');
  console.log('island_pet:', cols.map((x) => x.Field).join(','));
  const [t] = await c.query("SHOW TABLES LIKE '%home%'");
  console.log('home tables', t);
  const [b] = await c.query("SHOW TABLES LIKE '%banner%'");
  console.log('banner', b);
  const [x] = await c.query(
    "SELECT param_key, param_type FROM sys_config WHERE param_type IN ('xieyi','image') OR param_key LIKE '%agree%' OR param_key LIKE '%protocol%' OR param_key LIKE '%xieyi%'"
  );
  console.log('xieyi/image', x);
  const [allTypes] = await c.query(
    'SELECT param_type, COUNT(*) c FROM sys_config GROUP BY param_type'
  );
  console.log('types', allTypes);
  await c.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
