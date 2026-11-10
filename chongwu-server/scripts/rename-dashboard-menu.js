const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const [rows] = await conn.query("SELECT menu_id, parent_id, name, url, perms, type, icon FROM sys_menu WHERE name = '控制台' OR menu_id = 2000");
  console.log('BEFORE:', JSON.stringify(rows, null, 2));
  const [res] = await conn.query("UPDATE sys_menu SET name = '数据中心' WHERE menu_id = 2000 AND name = '控制台'");
  console.log('UPDATE affected:', res.affectedRows);
  const [after] = await conn.query("SELECT menu_id, name, url FROM sys_menu WHERE menu_id = 2000");
  console.log('AFTER:', JSON.stringify(after, null, 2));
  await conn.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
