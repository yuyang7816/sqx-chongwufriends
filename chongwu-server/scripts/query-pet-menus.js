const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const [rows] = await conn.query(`
    SELECT menu_id, parent_id, name, url, perms, type, order_num
    FROM sys_menu
    WHERE menu_id >= 2000 AND menu_id < 2100
    ORDER BY parent_id, order_num, menu_id
  `);
  console.table(rows);
  await conn.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
