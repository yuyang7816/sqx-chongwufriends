const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const [cols] = await conn.query("SHOW COLUMNS FROM sys_user LIKE 'user_id'");
  const [max] = await conn.query('SELECT MAX(user_id) AS maxId FROM sys_user');
  console.log(cols[0]);
  console.log(max[0]);
  await conn.end();
}

main();
