const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const keys = [
    'sys_getui_appkey',
    'sys_getui_mastersecret',
    'file_ali_oss_secretAccessKey',
    'map_tencent_key',
    'sms_smsbao_username',
    'sms_smsbao_password',
  ];
  const [r] = await c.query(
    `SELECT param_key, IFNULL(param_value,'') v FROM sys_config WHERE param_key IN (${keys
      .map(() => '?')
      .join(',')})`,
    keys
  );
  console.log(r);
  const [n] = await c.query('SELECT COUNT(*) c FROM sys_config');
  console.log('total configs', n[0].c);
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
