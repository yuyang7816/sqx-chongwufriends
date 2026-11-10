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

  const [before] = await conn.query(`
    SELECT spu_id, sku_name, COUNT(*) cnt
    FROM mall_sku
    WHERE spu_id IN (7201,7202,7203,7204) AND IFNULL(is_deleted,0)=0
    GROUP BY spu_id, sku_name
    HAVING COUNT(*) > 1
  `);
  console.log('duplicates before:', before);

  const [r] = await conn.query(`
    UPDATE mall_sku s
    JOIN (
      SELECT spu_id, sku_name, MIN(id) keep_id
      FROM mall_sku
      WHERE spu_id IN (7201,7202,7203,7204) AND IFNULL(is_deleted,0)=0
      GROUP BY spu_id, sku_name
      HAVING COUNT(*) > 1
    ) d ON s.spu_id = d.spu_id AND s.sku_name = d.sku_name AND s.id <> d.keep_id
    SET s.is_deleted = 1
    WHERE IFNULL(s.is_deleted,0)=0
  `);
  console.log('dedupe affectedRows:', r.affectedRows);

  const [skus] = await conn.query(`
    SELECT spu_id, sku_name, spec_id, price, point_price, stock
    FROM mall_sku
    WHERE spu_id IN (7201,7202,7203,7204) AND IFNULL(is_deleted,0)=0
    ORDER BY spu_id, id
  `);
  console.table(skus);
  console.log('total active skus:', skus.length);

  await conn.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
