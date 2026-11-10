const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const sqlPath = path.join(__dirname, '../sql/community-upgrade-20260625.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');
  const conn = await mysql.createConnection({
    host: '127.0.0.1',
    user: 'root',
    password: '123456',
    database: 'shouban',
    multipleStatements: true,
  });
  await conn.query(sql);
  await conn.end();
  console.log('SQL executed successfully');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
