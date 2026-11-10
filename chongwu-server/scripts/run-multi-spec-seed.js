const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const sqlPath = path.join(__dirname, '../sql/pet-mall-multi-spec-seed-20260803.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');

  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });

  console.log('Executing multi-spec seed SQL...');
  const [result] = await conn.query(sql);
  const results = Array.isArray(result) ? result : [result];
  console.log('Statements executed:', results.length);
  for (const r of results) {
    if (r && typeof r.affectedRows === 'number') {
      console.log(`  affectedRows=${r.affectedRows} changedRows=${r.changedRows ?? '-'}`);
    }
  }

  const [specs] = await conn.query(
    `SELECT id, spec_name, rule_value FROM mall_sku_spec WHERE id IN (8101,8102,8103,8104) ORDER BY id`
  );
  console.log('\n=== specs ===');
  for (const s of specs) {
    console.log(s.id, s.spec_name, s.rule_value);
  }

  const [skus] = await conn.query(
    `SELECT spu_id, sku_name, spec_id, price, point_price, stock
     FROM mall_sku
     WHERE spu_id IN (7201,7202,7203,7204) AND IFNULL(is_deleted,0)=0
     ORDER BY spu_id, id`
  );
  console.log('\n=== skus ===');
  console.table(skus);

  const [spus] = await conn.query(
    `SELECT id, name, spec_mode, price FROM mall_spu WHERE id IN (7201,7202,7203,7204)`
  );
  console.log('\n=== spus ===');
  console.table(spus);

  await conn.end();
  console.log('Done.');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
