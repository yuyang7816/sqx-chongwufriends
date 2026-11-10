const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });

  const sqlPath = path.join(__dirname, '..', 'sql', 'pet-mall-points-spec-upgrade-20260728.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');
  await conn.query(sql);
  console.log('upgrade sql executed.');

  console.log('=== mall_spu after ===');
  const [spus] = await conn.query(`
    SELECT id, name, price, point_price, spec_mode, status
    FROM mall_spu WHERE is_deleted = 0 ORDER BY id
  `);
  console.table(spus);

  console.log('=== mall_sku after ===');
  const [skus] = await conn.query(`
    SELECT id, spu_id, sku_name, spec_id, price, point_price, stock
    FROM mall_sku WHERE is_deleted = 0 ORDER BY spu_id, id
  `);
  console.table(skus);

  await conn.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
