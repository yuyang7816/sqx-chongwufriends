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

const SYSTEM_BUTTON_PERMS = [
  'sys:menu:save',
  'sys:menu:update',
  'sys:menu:delete',
  'sys:menu:info',
  'sys:menu:select',
  'sys:role:save',
  'sys:role:update',
  'sys:role:delete',
  'sys:role:info',
  'sys:role:select',
  'sys:config:update',
  'sys:config:save',
  'allocationList:update',
  'sys:user:save',
  'sys:user:update',
  'sys:user:delete',
  'sys:user:info',
];

async function runSql(conn, fileName) {
  const sql = fs.readFileSync(path.join(__dirname, '../sql', fileName), 'utf8');
  await conn.query(sql);
  console.log(`[OK] executed ${fileName}`);
}

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

async function apiJson(url, token, options = {}) {
  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      token,
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });
  const payload = await response.json();
  return payload;
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

async function main() {
  const conn = await mysql.createConnection(DB);
  const results = [];
  try {
    await runSql(conn, 'pet-menu-system-buttons-upgrade-20260625.sql');
    const token = await login(conn);

    const nav = await apiJson('sys/menu/nav', token);
    assert(nav.code === 0, `nav failed: ${JSON.stringify(nav)}`);
    const perms = [...(nav.data?.permissions || [])];
    const missingPerms = SYSTEM_BUTTON_PERMS.filter((perm) => !perms.includes(perm));
    assert(!missingPerms.length, `missing perms: ${missingPerms.join(', ')}`);
    results.push('nav permissions');

    const menuList = await fetch(`${API_BASE}sys/menu/list`, { headers: { token } }).then((res) => res.json());
    assert(Array.isArray(menuList) && menuList.length > 0, 'menu list empty');
    results.push('menu list');

    const menuSelect = await apiJson('sys/menu/select', token);
    assert(menuSelect.code === 0 && Array.isArray(menuSelect.data?.menuList), 'menu select failed');
    results.push('menu select');

    const rolePage = await apiJson('sys/role/list?page=1&limit=10', token);
    assert(rolePage.code === 0, `role list failed: ${JSON.stringify(rolePage)}`);
    results.push('role list');

    const configList = await apiJson('sys/config/list', token);
    assert(configList.code === 0 && Array.isArray(configList.data), 'config list failed');
    const xitongGroup = configList.data.find((item) => item.paramType === 'xitong');
    assert(xitongGroup?.sysConfigEntityList?.length, 'xitong config missing');
    const target = xitongGroup.sysConfigEntityList.find((item) => item.paramKey === 'sys_server_name');
    assert(target, 'sys_server_name config missing');
    results.push('config list');

    const originalValue = target.paramValue;
    const nextValue = originalValue === '和它交个朋友-e2e' ? '和它交个朋友' : '和它交个朋友-e2e';
    const update = await apiJson('sys/config/update', token, {
      method: 'POST',
      body: JSON.stringify({
        id: target.id,
        paramKey: target.paramKey,
        paramValue: nextValue,
        status: '1',
        remark: target.remark,
        paramType: target.paramType,
        paramTypeDesc: target.paramTypeDesc,
      }),
    });
    assert(update.code === 0, `config update failed: ${JSON.stringify(update)}`);
    results.push('config update');

    const restore = await apiJson('sys/config/update', token, {
      method: 'POST',
      body: JSON.stringify({
        id: target.id,
        paramKey: target.paramKey,
        paramValue: originalValue,
        status: '1',
        remark: target.remark,
        paramType: target.paramType,
        paramTypeDesc: target.paramTypeDesc,
      }),
    });
    assert(restore.code === 0, `config restore failed: ${JSON.stringify(restore)}`);
    results.push('config restore');

    const userList = await apiJson('sys/user/list?page=1&limit=10', token);
    assert(userList.code === 0, `user list failed: ${JSON.stringify(userList)}`);
    results.push('admin list');

    console.log('\n=== System module e2e ===');
    console.log(`Passed: ${results.join(', ')}`);
    console.log('[PASS] system module e2e passed');
  } finally {
    await conn.end();
  }
}

main().catch((error) => {
  console.error('\n[FAIL]', error.message || error);
  process.exit(1);
});
