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
  const sql = fs.readFileSync(path.join(__dirname, '../sql/pet-store-archive-p01-upgrade-20260626.sql'), 'utf8');
  await conn.query(sql);
  const [rows] = await conn.query(
    `SELECT store_id,
            SUM(pet_type = 'dog') AS dogs,
            SUM(pet_type = 'cat') AS cats
     FROM pet_archive
     WHERE audit_status = 1 AND store_id BETWEEN 1 AND 4
     GROUP BY store_id
     ORDER BY store_id`,
  );
  console.log(JSON.stringify(rows, null, 2));
  await conn.end();
})().catch((error) => {
  console.error(error);
  process.exit(1);
});
