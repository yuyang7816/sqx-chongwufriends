const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: process.env.DB_HOST || 'shuju.xianmxkj.com',
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '4A4h85myDa8fNic8',
    database: process.env.DB_NAME || 'chongwu',
    multipleStatements: true,
  });
  const sql = fs.readFileSync(
    path.join(__dirname, '../sql/pet-menu-missing-upgrade-20260626.sql'),
    'utf8',
  );
  await conn.query(sql);
  const [rows] = await conn.query(
    `SELECT menu_id, parent_id, name, url, perms, type, order_num
     FROM sys_menu
     WHERE menu_id BETWEEN 2105 AND 2131 OR url IN ('pet/cities', 'pet/home-social')
     ORDER BY menu_id`,
  );
  console.log(JSON.stringify(rows, null, 2));
  await conn.end();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
