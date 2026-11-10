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

function walkMenus(items, acc = []) {
  (items || []).forEach((item) => {
    acc.push(item);
    walkMenus(item.list || item.children || [], acc);
  });
  return acc;
}

async function apiJson(url, token, options = {}) {
  const response = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      ...(token ? { token } : {}),
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });
  return response.json();
}

async function apiGet(url, token) {
  const response = await fetch(`${API_BASE}${url}`, {
    headers: { token },
  });
  return response.json();
}

function normalizeMenuList(payload) {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.data)) return payload.data;
  return payload?.data?.menuList || payload?.menuList || [];
}

async function main() {
  const conn = await mysql.createConnection(DB);
  const results = [];
  const stamp = Date.now();
  const roleName = `e2e_role_all_${stamp}`;
  let createdRoleId = null;

  try {
    const token = await login(conn);
    results.push('login');

    const nav = await apiJson('sys/menu/nav', token);
    const flat = walkMenus(nav.data?.menuList || []);
    const complaintTypeMenu = flat.find((item) => item.url === 'pet/community/complaint-types');
    if (!complaintTypeMenu) {
      throw new Error('menu pet/community/complaint-types missing');
    }

    const perms = [...(nav.data?.permissions || [])];
    [
      'pet:community:complaint:type',
      'pet:community:complaint:type:add',
      'pet:community:complaint:type:update',
      'pet:community:complaint:type:delete',
      'pet:community:complaint:config',
    ].forEach((perm) => {
      if (!perms.includes(perm)) {
        throw new Error(`missing permission ${perm}`);
      }
    });
    results.push('complaint type menu + permissions');

    const menuListRes = await apiGet('sys/menu/list', token);
    const allMenus = normalizeMenuList(menuListRes);
    const allMenuIds = allMenus.map((item) => item.menuId).filter((id) => id != null && id > 0);
    if (!allMenuIds.length) {
      throw new Error(`menu list empty: ${JSON.stringify(menuListRes).slice(0, 200)}`);
    }

    const [maxRoleRows] = await conn.query('SELECT COALESCE(MAX(role_id), 0) + 1 AS nextId FROM sys_role');
    createdRoleId = maxRoleRows[0].nextId;
    await conn.query(
      'INSERT INTO sys_role (role_id, role_name, remark, create_user_id, create_time) VALUES (?, ?, ?, 1, NOW())',
      [createdRoleId, roleName, 'e2e select all menus']
    );
    results.push('role seed');

    const updateRoleRes = await apiJson('sys/role/update', token, {
      method: 'POST',
      body: JSON.stringify({
        roleId: createdRoleId,
        roleName,
        remark: 'e2e select all menus',
        menuIdList: allMenuIds,
      }),
    });
    if (updateRoleRes.code !== 0) {
      throw new Error(`update role failed: ${JSON.stringify(updateRoleRes)}`);
    }
    results.push('role update with all menus');

    const roleInfoRes = await apiGet(`sys/role/info/${createdRoleId}`, token);
    const roleInfo = roleInfoRes.data?.role || roleInfoRes.role || {};
    const savedMenuIds = (roleInfo.menuIdList || []).filter((id) => Number(id) > 0);
    if (savedMenuIds.length < allMenuIds.length) {
      throw new Error(`role menus incomplete: saved=${savedMenuIds.length}, expected>=${allMenuIds.length}`);
    }
    const missing = allMenuIds.filter((id) => !savedMenuIds.includes(id));
    if (missing.length) {
      throw new Error(`role missing menu ids: ${missing.join(',')}`);
    }
    results.push('role select-all persistence');

    const deleteRes = await apiJson('sys/role/delete', token, {
      method: 'POST',
      body: JSON.stringify([createdRoleId]),
    });
    if (deleteRes.code !== 0) {
      throw new Error(`delete role failed: ${JSON.stringify(deleteRes)}`);
    }
    createdRoleId = null;
    results.push('role cleanup');

    console.log('\n=== Complaint type menu + role select-all e2e ===');
    console.log(`Passed: ${results.join(', ')}`);
    console.log('[PASS] complaint type menu and role select-all e2e passed');
  } finally {
    if (createdRoleId) {
      await conn.query('DELETE FROM sys_role_menu WHERE role_id = ?', [createdRoleId]).catch(() => {});
      await conn.query('DELETE FROM sys_role WHERE role_id = ?', [createdRoleId]).catch(() => {});
    }
    await conn.end();
  }
}

main().catch((error) => {
  console.error('\n[FAIL]', error.message || error);
  process.exit(1);
});
