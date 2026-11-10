const crypto = require('crypto');
const mysql = require('mysql2/promise');

const API_BASE = process.env.API_BASE || 'http://192.168.0.118:9294/sqx_fast/';
const DB = {
  host: process.env.DB_HOST || 'shuju.xianmxkj.com',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '4A4h85myDa8fNic8',
  database: process.env.DB_NAME || 'chongwu',
};

function walkMenus(items, acc = []) {
  (items || []).forEach((item) => {
    acc.push(item);
    walkMenus(item.list || item.children || [], acc);
  });
  return acc;
}

async function loginWithCaptcha(conn) {
  const uuid = crypto.randomUUID();
  const code = 'test1';
  const expire = new Date(Date.now() + 5 * 60 * 1000);
  await conn.query('DELETE FROM sys_captcha WHERE uuid = ?', [uuid]);
  await conn.query('INSERT INTO sys_captcha (uuid, code, expire_time) VALUES (?, ?, ?)', [uuid, code, expire]);

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
  const conn = await mysql.createConnection(DB);
  try {
    const token = await loginWithCaptcha(conn);
    const navRes = await fetch(`${API_BASE}sys/menu/nav`, {
      headers: { token },
    });
    const navPayload = await navRes.json();
    if (navPayload.code !== 0) {
      throw new Error(`nav failed: ${JSON.stringify(navPayload)}`);
    }
    const navData = navPayload.data || navPayload;

    const flat = walkMenus(navData.menuList || []);
    const complaint = flat.find((item) => item.url === 'pet/community/complaints');
    const complaintType = flat.find((item) => item.url === 'pet/community/complaint-types');
    const perms = Array.isArray(navData.permissions) ? navData.permissions : [...(navData.permissions || [])];
    const requiredPerms = [
      'pet:community:complaint:list',
      'pet:community:complaint:handle',
      'pet:community:complaint:config',
      'pet:community:complaint:type',
      'pet:community:complaint:type:add',
      'pet:community:complaint:type:update',
      'pet:community:complaint:type:delete',
      'pet:community:audit',
      'bannerList:list',
    ];
    const missingPerms = requiredPerms.filter((perm) => !perms.includes(perm));

    console.log('=== Nav API test ===');
    console.log(`Menu count: ${flat.length}`);
    console.log(`Complaint menu: ${complaint ? complaint.name : 'MISSING'}`);
    console.log(`Complaint type menu: ${complaintType ? complaintType.name : 'MISSING'}`);
    console.log(`Permissions count: ${perms.length}`);
    console.log(`Missing required perms: ${missingPerms.length ? missingPerms.join(', ') : 'none'}`);

    if (!complaint || !complaintType || missingPerms.length) {
      process.exitCode = 1;
      console.error('[FAIL] nav API test failed');
      return;
    }
    console.log('[PASS] nav API test passed');
  } finally {
    await conn.end();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
