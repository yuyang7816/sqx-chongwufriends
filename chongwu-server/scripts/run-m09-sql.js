const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

function parseDbFromYaml() {
  const yamlPath = path.join(__dirname, '../sqx-application/src/main/resources/application-prod.yml');
  const text = fs.readFileSync(yamlPath, 'utf8');
  const urlMatch = text.match(/url:\s*jdbc:mysql:\/\/([^:/]+):(\d+)\/([^?]+)/);
  const userMatch = text.match(/username:\s*(\S+)/);
  const passMatch = text.match(/password:\s*(\S+)/);
  if (!urlMatch) throw new Error('cannot parse DB config');
  return {
    host: process.env.DB_HOST || urlMatch[1],
    port: Number(process.env.DB_PORT || urlMatch[2]),
    database: process.env.DB_NAME || urlMatch[3],
    user: process.env.DB_USER || userMatch?.[1] || 'root',
    password: process.env.DB_PASSWORD || passMatch?.[1] || '',
  };
}

async function run() {
  const db = parseDbFromYaml();
  const conn = await mysql.createConnection({ ...db, multipleStatements: true });
  const files = [
    'pet-m09-coupon-menu-mysql57.sql',
    'pet-m09-admin-menus-mysql57.sql',
  ];
  for (const file of files) {
    const sql = fs.readFileSync(path.join(__dirname, '../sql', file), 'utf8');
    await conn.query(sql);
    console.log('executed', file);
  }
  const [rows] = await conn.query(
    'SELECT menu_id, name, perms FROM sys_menu WHERE menu_id BETWEEN 2140 AND 2148 ORDER BY menu_id'
  );
  console.log('menus', rows);
  await conn.end();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
