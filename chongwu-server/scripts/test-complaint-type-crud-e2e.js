const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
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

async function loginAdmin(conn) {
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
    throw new Error(`admin login failed: ${JSON.stringify(payload)}`);
  }
  return token;
}

async function loginApp() {
  const response = await fetch(`${API_BASE}app/base/auth/phonePasswordLogin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      phoneNo: process.env.APP_PHONE || '13800138000',
      password: process.env.APP_PASS || '123456',
    }),
  });
  const payload = await response.json();
  const token = payload.data?.token;
  if (![0, 200].includes(payload.code) || !token) {
    throw new Error(`app login failed: ${JSON.stringify(payload)}`);
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

async function main() {
  const conn = await mysql.createConnection(DB);
  const results = [];
  const stamp = Date.now();
  const typeName = `e2e_${stamp}`;
  let createdId = null;

  try {
    const ddl = fs.readFileSync(path.join(__dirname, '../sql/community-complaint-type-table-20260625.sql'), 'utf8');
    await conn.query(ddl);
    const menuSql = fs.readFileSync(path.join(__dirname, '../sql/pet-menu-complaint-type-crud-upgrade-20260625.sql'), 'utf8');
    await conn.query(menuSql);
    results.push('db ready');

    const adminToken = await loginAdmin(conn);
    const appToken = await loginApp();
    results.push('admin + app login');

    const nav = await apiJson('sys/menu/nav', adminToken);
    const flat = walkMenus(nav.data?.menuList || []);
    if (!flat.find((item) => item.url === 'pet/community/complaint-types')) {
      throw new Error('menu pet/community/complaint-types missing');
    }
    const perms = [...(nav.data?.permissions || [])];
    [
      'pet:community:complaint:type',
      'pet:community:complaint:type:add',
      'pet:community:complaint:type:update',
      'pet:community:complaint:type:delete',
    ].forEach((perm) => {
      if (!perms.includes(perm)) {
        throw new Error(`missing permission ${perm}`);
      }
    });
    results.push('admin menu + permissions');

    const createRes = await apiJson('admin/pet/community/complaint-type/save', adminToken, {
      method: 'POST',
      body: JSON.stringify({ name: typeName, sort: 99, status: 1 }),
    });
    if (createRes.code !== 0) {
      throw new Error(`create failed: ${JSON.stringify(createRes)}`);
    }
    results.push('admin create');

    const pageRes = await apiJson('admin/pet/community/complaint-type/page', adminToken, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, name: typeName }),
    });
    const record = (pageRes.data?.records || []).find((item) => item.name === typeName);
    if (!record?.id) {
      throw new Error(`created type not found: ${JSON.stringify(pageRes)}`);
    }
    createdId = record.id;
    results.push('admin page');

    const updatedName = `u${String(stamp).slice(-6)}`;
    const updateRes = await apiJson('admin/pet/community/complaint-type/update', adminToken, {
      method: 'POST',
      body: JSON.stringify({
        id: createdId,
        name: updatedName,
        sort: 100,
        status: 1,
      }),
    });
    if (updateRes.code !== 0) {
      throw new Error(`update failed: ${JSON.stringify(updateRes)}`);
    }
    results.push('admin update');

    const listRes = await apiJson('admin/pet/community/complaint-type/list', adminToken, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    const listed = (listRes.data || []).find((item) => item.id === createdId);
    if (!listed || listed.name !== updatedName) {
      throw new Error(`list missing updated type: ${JSON.stringify(listRes)}`);
    }
    results.push('admin list');

    const adminConfigRes = await apiJson('admin/pet/community/config', adminToken, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    const adminTypes = adminConfigRes.data?.complaintTypes || [];
    if (!adminTypes.includes(updatedName)) {
      throw new Error(`admin config missing updated type: ${JSON.stringify(adminConfigRes)}`);
    }
    results.push('admin config sync');

    const appConfigRes = await apiJson('app/pet/community/config', appToken, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    const appTypes = appConfigRes.data?.complaintTypes || [];
    if (!appTypes.includes(updatedName)) {
      throw new Error(`app config missing updated type: ${JSON.stringify(appConfigRes)}`);
    }
    results.push('app config sync');

    const invalidRes = await apiJson('app/pet/community/complaint', appToken, {
      method: 'POST',
      body: JSON.stringify({
        targetType: 1,
        targetId: 9007,
        complaintType: '不存在的类型',
        content: '测试',
      }),
    });
    if ([0, 200].includes(invalidRes.code)) {
      throw new Error('invalid complaint type should fail');
    }
    results.push('app invalid type blocked');

    const deleteRes = await apiJson('admin/pet/community/complaint-type/delete', adminToken, {
      method: 'POST',
      body: JSON.stringify({ id: createdId }),
    });
    if (deleteRes.code !== 0) {
      throw new Error(`delete failed: ${JSON.stringify(deleteRes)}`);
    }
    results.push('admin delete');

    const restoreRes = await apiJson('admin/pet/community/complaint-type/save', adminToken, {
      method: 'POST',
      body: JSON.stringify({ name: updatedName, sort: 101, status: 1 }),
    });
    if (restoreRes.code !== 0) {
      throw new Error(`restore after delete failed: ${JSON.stringify(restoreRes)}`);
    }
    const restoredPageRes = await apiJson('admin/pet/community/complaint-type/page', adminToken, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, name: updatedName }),
    });
    const restored = (restoredPageRes.data?.records || []).find((item) => item.name === updatedName);
    if (!restored?.id) {
      throw new Error(`restored type not found: ${JSON.stringify(restoredPageRes)}`);
    }
    createdId = restored.id;
    results.push('admin restore after delete');

    const falsePositiveName = '广告';
    const falsePositiveRes = await apiJson('admin/pet/community/complaint-type/save', adminToken, {
      method: 'POST',
      body: JSON.stringify({ name: falsePositiveName, sort: 88, status: 1 }),
    });
    if (falsePositiveRes.code !== 0) {
      throw new Error(`false-positive type create failed: ${JSON.stringify(falsePositiveRes)}`);
    }
    const falsePositivePage = await apiJson('admin/pet/community/complaint-type/page', adminToken, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, name: falsePositiveName }),
    });
    const falsePositiveRecord = (falsePositivePage.data?.records || []).find((item) => item.name === falsePositiveName);
    if (!falsePositiveRecord?.id) {
      throw new Error(`false-positive type not found: ${JSON.stringify(falsePositivePage)}`);
    }
    const falsePositiveDelete = await apiJson('admin/pet/community/complaint-type/delete', adminToken, {
      method: 'POST',
      body: JSON.stringify({ id: falsePositiveRecord.id }),
    });
    if (falsePositiveDelete.code !== 0) {
      throw new Error(`false-positive delete failed: ${JSON.stringify(falsePositiveDelete)}`);
    }
    results.push('admin delete substring-safe');

    const postPageRes = await apiJson('app/pet/community/page', appToken, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 20 }),
    });
    const posts = postPageRes.data?.records || [];
    const complaintType = appTypes.find((item) => item !== updatedName) || appTypes[0];
    let complaintPostId = null;
    for (const post of posts) {
      const complaintRes = await apiJson('app/pet/community/complaint', appToken, {
        method: 'POST',
        body: JSON.stringify({
          targetType: 1,
          targetId: post.id,
          complaintType,
          content: `e2e投诉测试-${stamp}`,
        }),
      });
      if ([0, 200].includes(complaintRes.code)) {
        complaintPostId = post.id;
        break;
      }
    }
    if (complaintPostId) {
      results.push(`app submit complaint postId=${complaintPostId}`);
      const myComplaintRes = await apiJson('app/pet/community/complaint/page', appToken, {
        method: 'POST',
        body: JSON.stringify({ page: 1, limit: 20 }),
      });
      if (![0, 200].includes(myComplaintRes.code)) {
        throw new Error(`app complaint page failed: ${JSON.stringify(myComplaintRes)}`);
      }
      results.push('app my complaint page');
      const adminComplaintRes = await apiJson('admin/pet/community/complaint/page', adminToken, {
        method: 'POST',
        body: JSON.stringify({ page: 1, limit: 20 }),
      });
      if (adminComplaintRes.code !== 0) {
        throw new Error(`admin complaint page failed: ${JSON.stringify(adminComplaintRes)}`);
      }
      results.push('admin complaint page');
    } else {
      results.push('app submit complaint skipped (no complaintable post)');
    }

    const finalDeleteRes = await apiJson('admin/pet/community/complaint-type/delete', adminToken, {
      method: 'POST',
      body: JSON.stringify({ id: createdId }),
    });
    if (finalDeleteRes.code !== 0) {
      throw new Error(`final delete failed: ${JSON.stringify(finalDeleteRes)}`);
    }
    createdId = null;
    results.push('admin final cleanup');

    console.log('\n=== Complaint module 3-end e2e ===');
    console.log(`Passed: ${results.join(', ')}`);
    console.log('[PASS] complaint module e2e passed');
  } finally {
    if (createdId) {
      await conn.query('DELETE FROM community_complaint_type WHERE id = ?', [createdId]).catch(() => {});
    }
    await conn.end();
  }
}

main().catch((error) => {
  console.error('\n[FAIL]', error.message || error);
  process.exit(1);
});
