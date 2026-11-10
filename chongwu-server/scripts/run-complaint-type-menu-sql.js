const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });
  const sql = fs.readFileSync(path.join(__dirname, '../sql/pet-menu-complaint-type-upgrade-20260625.sql'), 'utf8');
  await conn.query(sql);
  const [rows] = await conn.query('SELECT menu_id,name,url,perms FROM sys_menu WHERE menu_id IN (2101,2102) ORDER BY menu_id');
  console.log(JSON.stringify(rows, null, 2));
  await conn.end();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
