const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const mysql = require('mysql2/promise');

const API_BASE = process.env.API_BASE || 'http://192.168.0.118:9294/sqx_fast/';
const DB = {
  host: process.env.DB_HOST || 'shuju.xianmxkj.com',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '4A4h85myDa8fNic8',
  database: process.env.DB_NAME || 'chongwu',
  multipleStatements: true,
};

async function login(conn) {
  const uuid = crypto.randomUUID();
  const code = 'test1';
  await conn.query('DELETE FROM sys_captcha WHERE uuid = ?', [uuid]);
  await conn.query(
    'INSERT INTO sys_captcha (uuid, code, expire_time) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE))',
    [uuid, code]
  );
  const response = await fetch(`${API_BASE}sys/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: process.env.ADMIN_USER || 'admin',
      password: process.env.ADMIN_PASS || '123456',
      uuid,
      captcha: code,
    }),
  });
  const payload = await response.json();
  const token = payload.token || payload.data?.token;
  if (payload.code !== 0 || !token) {
    throw new Error(`login failed: ${JSON.stringify(payload)}`);
  }
  return token;
}

async function main() {
  const sql = fs.readFileSync(path.join(__dirname, '../sql/sys-user-auto-increment-upgrade-20260625.sql'), 'utf8');
  const conn = await mysql.createConnection(DB);
  try {
    await conn.query(sql);
    const [col] = await conn.query("SHOW COLUMNS FROM sys_user LIKE 'user_id'");
    if (!String(col[0]?.Extra || '').includes('auto_increment')) {
      throw new Error('sys_user.user_id is still not AUTO_INCREMENT');
    }

    const token = await login(conn);
    const username = `e2e_admin_${Date.now()}`;
    const saveRes = await fetch(`${API_BASE}sys/user/save`, {
      method: 'POST',
      headers: { token, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username,
        password: '123456',
        email: `${username}@test.com`,
        mobile: '13800138000',
        status: 1,
        roleIdList: [1],
      }),
    });
    const savePayload = await saveRes.json();
    if (savePayload.code !== 0) {
      throw new Error(`save failed: ${JSON.stringify(savePayload)}`);
    }

    const [rows] = await conn.query('SELECT user_id, username FROM sys_user WHERE username = ?', [username]);
    if (!rows.length) {
      throw new Error('created admin not found in database');
    }

    await conn.query('DELETE FROM sys_user_role WHERE user_id = ?', [rows[0].user_id]);
    await conn.query('DELETE FROM sys_user WHERE user_id = ?', [rows[0].user_id]);

    console.log('[PASS] sys/user/save works, created user_id =', rows[0].user_id);
  } finally {
    await conn.end();
  }
}

main().catch((error) => {
  console.error('[FAIL]', error.message || error);
  process.exit(1);
});
