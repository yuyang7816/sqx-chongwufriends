const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const sqlPath = path.join(__dirname, '../sql/pet-mall-spec-keep-four-20260803.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');

  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });

  console.log('Executing pet-mall-spec-keep-four SQL...');
  const [result] = await conn.query(sql);
  const results = Array.isArray(result) ? result : [result];
  console.log('Statements executed:', results.length);
  for (const r of results) {
    if (r && typeof r.affectedRows === 'number') {
      console.log(`  affectedRows=${r.affectedRows} changedRows=${r.changedRows ?? '-'}`);
    }
  }

  const [specs] = await conn.query(
    `SELECT id, spec_name, status, is_deleted FROM mall_sku_spec WHERE IFNULL(is_deleted,0)=0 ORDER BY id`
  );
  console.log('\n=== active specs (should be 4) ===');
  console.table(specs);

  const [skus] = await conn.query(
    `SELECT spu_id, sku_name, spec_id, price, point_price, stock
     FROM mall_sku
     WHERE spu_id IN (7201,7202,7203,7204) AND IFNULL(is_deleted,0)=0
     ORDER BY spu_id, id`
  );
  console.log('\n=== multi-spec skus ===');
  console.table(skus);

  const [spus] = await conn.query(
    `SELECT id, name, spec_mode, price FROM mall_spu WHERE IFNULL(is_deleted,0)=0 ORDER BY id`
  );
  console.log('\n=== active spus ===');
  console.table(spus);

  const [bad] = await conn.query(
    `SELECT COUNT(*) AS cnt FROM mall_sku_spec WHERE IFNULL(is_deleted,0)=0 AND id NOT IN (8101,8102,8103,8104)`
  );
  console.log('\nextra active specs:', bad[0].cnt);

  await conn.end();
  console.log('Done.');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
