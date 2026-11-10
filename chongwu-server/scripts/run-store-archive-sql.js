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

(async () => {
  const conn = await mysql.createConnection({ ...parseDbFromYaml(), multipleStatements: true });
  const sql = fs.readFileSync(path.join(__dirname, '../sql/pet-store-archive-upgrade-20260626.sql'), 'utf8');
  await conn.query(sql);
  const [rows] = await conn.query(
    'SELECT id, store_id, name, pet_type, audit_status FROM pet_archive ORDER BY id LIMIT 10',
  );
  console.log(JSON.stringify(rows, null, 2));
  await conn.end();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
