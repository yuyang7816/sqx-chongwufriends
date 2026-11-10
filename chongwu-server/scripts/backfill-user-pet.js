const mysql = require('mysql2/promise');

(async () => {
  const c = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  const sql = `
    INSERT INTO user_pet (user_id, pet_id, application_id, source, status)
    SELECT aa.user_id, aa.pet_id, aa.id, 'adopt', 1
    FROM adopt_application aa
    WHERE aa.status = 6
      AND aa.is_deleted = 0
      AND aa.user_id IS NOT NULL
      AND aa.pet_id IS NOT NULL
      AND NOT EXISTS (
        SELECT 1 FROM user_pet up
        WHERE up.user_id = aa.user_id
          AND up.pet_id = aa.pet_id
          AND up.is_deleted = 0
      )
  `;
  const [r] = await c.query(sql);
  console.log('inserted', r.affectedRows);

  const [pets] = await c.query('SELECT * FROM user_pet WHERE user_id = 5');
  console.log('user_pet for 5', pets);

  const [arch] = await c.query(
    'SELECT id, name, avatar_url, audit_status, adoptable, is_deleted FROM pet_archive WHERE id = 1'
  );
  console.log('archive', arch);

  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
