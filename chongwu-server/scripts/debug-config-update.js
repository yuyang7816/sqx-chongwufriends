const crypto = require('crypto');
const mysql = require('mysql2/promise');

async function main() {
  const c = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });
  const uuid = crypto.randomUUID();
  await c.query('INSERT INTO sys_captcha(uuid,code,expire_time) VALUES(?,?,DATE_ADD(NOW(),INTERVAL 5 MINUTE))', [uuid, 'test1']);
  const login = await fetch('http://192.168.0.118:9294/sqx_fast/sys/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '123456', uuid, captcha: 'test1' }),
  });
  const token = (await login.json()).data.token;
  const [rows] = await c.query("SELECT * FROM sys_config WHERE param_key='sys_server_name' LIMIT 1");
  const row = rows[0];
  const body = {
    id: row.id,
    paramKey: row.param_key,
    paramValue: row.param_value === '和它交个朋友-e2e' ? '和它交个朋友' : '和它交个朋友-e2e',
    status: row.status,
    remark: row.remark,
    paramType: row.param_type,
    paramTypeDesc: row.param_type_desc,
  };
  const nav = await fetch('http://192.168.0.118:9294/sqx_fast/sys/menu/nav', { headers: { token } }).then((r) => r.json());
  console.log('has update perm', (nav.data?.permissions || []).includes('sys:config:update'));
  const upd = await fetch('http://192.168.0.118:9294/sqx_fast/sys/config/update', {
    method: 'POST',
    headers: { token, 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  console.log(await upd.json());
  await c.end();
}

main();
