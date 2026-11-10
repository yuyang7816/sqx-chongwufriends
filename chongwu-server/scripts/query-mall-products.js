const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  console.log('=== mall_spu ===');
  const [spus] = await conn.query(`
    SELECT id, name, category, category_id, price, original_price, point_price, biz_type, spec_mode, status, sort
    FROM mall_spu
    WHERE is_deleted = 0
    ORDER BY id
  `);
  console.table(spus);

  console.log('=== mall_sku ===');
  const [skus] = await conn.query(`
    SELECT id, spu_id, sku_name, spec_id, price, point_price, stock
    FROM mall_sku
    WHERE is_deleted = 0
    ORDER BY spu_id, id
  `);
  console.table(skus);

  console.log('=== mall_sku_spec ===');
  const [specs] = await conn.query(`
    SELECT id, spec_name, price, point_price, default_stock, status, sort
    FROM mall_sku_spec
    WHERE is_deleted = 0
    ORDER BY id
  `);
  console.table(specs);

  await conn.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
