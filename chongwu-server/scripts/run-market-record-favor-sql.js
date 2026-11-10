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
    host: urlMatch[1],
    port: Number(urlMatch[2]),
    database: urlMatch[3],
    user: userMatch?.[1] || 'root',
    password: passMatch?.[1] || '',
  };
}

async function run() {
  const db = parseDbFromYaml();
  const conn = await mysql.createConnection({ ...db, multipleStatements: true });
  const [cols] = await conn.query("SHOW COLUMNS FROM island_market_record LIKE 'favor'");
  if (!cols.length) {
    await conn.query(
      "ALTER TABLE island_market_record ADD COLUMN favor INT DEFAULT 0 COMMENT '兑换时增加的亲密度快照' AFTER pay_points"
    );
    console.log('added favor column');
  } else {
    console.log('favor column already exists');
  }
  const [result] = await conn.query(
    `UPDATE island_market_record r
     INNER JOIN island_market_item i ON i.id = r.item_id
     SET r.favor = IFNULL(i.favor, 0)
     WHERE r.favor IS NULL OR r.favor = 0`
  );
  console.log('backfill affectedRows', result.affectedRows);
  const [sample] = await conn.query(
    'SELECT id, item_id, pay_coins, pay_points, favor FROM island_market_record ORDER BY id DESC LIMIT 5'
  );
  console.log('sample', sample);
  await conn.end();
}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
