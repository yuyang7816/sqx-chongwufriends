const mysql = require('mysql2/promise');

(async () => {
  const c = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  const [users] = await c.query(
    'SELECT user_id, phone, user_name FROM tb_user WHERE phone = ? OR sys_phone = ? LIMIT 10',
    ['18740331034', '18740331034']
  );
  console.log('users', users);

  if (!users.length) {
    const [appsByPhone] = await c.query(
      'SELECT * FROM adopt_application WHERE phone = ? ORDER BY id DESC LIMIT 20',
      ['18740331034']
    );
    console.log('appsByPhone', appsByPhone);
  }

  const userIds = users.map((u) => u.user_id);
  if (userIds.length) {
    const [apps] = await c.query(
      'SELECT id, user_id, pet_id, phone, status, reject_reason, create_time, update_time, is_deleted FROM adopt_application WHERE user_id IN (?) OR phone = ? ORDER BY id DESC',
      [userIds, '18740331034']
    );
    console.log('apps', apps);

    const [pets] = await c.query(
      'SELECT * FROM user_pet WHERE user_id IN (?)',
      [userIds]
    );
    console.log('user_pet', pets);

    const [island] = await c.query(
      'SELECT id, user_id, resident_pet_id, resident_pet_ids FROM island_pet WHERE user_id IN (?)',
      [userIds]
    );
    console.log('island_pet', island);
  }

  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
