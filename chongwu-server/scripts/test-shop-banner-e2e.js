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
      token,
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
  });
  return response.json();
}

async function ensureShopBannerSeed(conn) {
  const [rows] = await conn.query(
    "SELECT COUNT(*) AS cnt FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'operation_banner'"
  );
  if (!rows[0]?.cnt) {
    const ddl = fs.readFileSync(path.join(__dirname, '../sql/operation-banner-upgrade-20260625.sql'), 'utf8');
    await conn.query(ddl);
    return;
  }
  const [count] = await conn.query("SELECT COUNT(*) AS cnt FROM operation_banner WHERE position_code = 'shop'");
  if (!count[0]?.cnt) {
    await conn.query(`
      INSERT INTO operation_banner (name, image_url, jump_url, position_code, sort, status)
      VALUES ('春季萌宠市集', 'https://mydz.xianmxkj.com/file/uploadPath/2026/06/25/demo-shop-banner.png', '/packageA/product/detail?id=7001', 'shop', 1, 1)
    `);
  }
}

async function main() {
  const conn = await mysql.createConnection(DB);
  const results = [];
  const testName = `e2e_shop_banner_${Date.now()}`;
  let createdId = null;

  try {
    await ensureShopBannerSeed(conn);
    results.push('db ready');

    const token = await login(conn);
    const nav = await apiJson('sys/menu/nav', token);
    const flat = walkMenus(nav.data?.menuList || []);
    const decorationMenu = flat.find((item) => item.url === 'content/decorations');
    if (!decorationMenu) {
      throw new Error('menu content/decorations missing');
    }
    const perms = [...(nav.data?.permissions || [])];
    ['bannerList:list', 'bannerList:add', 'bannerList:update', 'bannerList:delete'].forEach((perm) => {
      if (!perms.includes(perm)) {
        throw new Error(`missing permission ${perm}`);
      }
    });
    results.push('admin menu + permissions');

    const createRes = await apiJson('admin/pet/banner/save', token, {
      method: 'POST',
      body: JSON.stringify({
        name: testName,
        imageUrl: 'https://mydz.xianmxkj.com/file/uploadPath/2026/06/25/demo-shop-banner.png',
        jumpUrl: '/packageA/product/detail?id=7001',
        sort: 99,
        status: 1,
        positionCode: 'shop',
      }),
    });
    if (createRes.code !== 0) {
      throw new Error(`create banner failed: ${JSON.stringify(createRes)}`);
    }
    results.push('admin create');

    const pageRes = await apiJson('admin/pet/banner/page', token, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 20, positionCode: 'shop', name: testName }),
    });
    const record = (pageRes.data?.records || []).find((item) => item.name === testName);
    if (!record?.id) {
      throw new Error(`created banner not found: ${JSON.stringify(pageRes)}`);
    }
    createdId = record.id;
    results.push('admin page');

    const infoRes = await apiJson('admin/pet/banner/info', token, {
      method: 'POST',
      body: JSON.stringify({ id: createdId }),
    });
    if (infoRes.code !== 0 || !infoRes.data?.id) {
      throw new Error(`banner info failed: ${JSON.stringify(infoRes)}`);
    }
    results.push('admin info');

    const updateRes = await apiJson('admin/pet/banner/update', token, {
      method: 'POST',
      body: JSON.stringify({
        id: createdId,
        name: `${testName}_updated`,
        imageUrl: record.imageUrl,
        jumpUrl: record.jumpUrl,
        sort: 100,
        status: 1,
        positionCode: 'shop',
      }),
    });
    if (updateRes.code !== 0) {
      throw new Error(`update banner failed: ${JSON.stringify(updateRes)}`);
    }
    results.push('admin update');

    const appRes = await fetch(`${API_BASE}app/pet/home/banners?positionCode=shop`);
    const appPayload = await appRes.json();
    const appList = appPayload.data || [];
    const found = appList.find((item) => item.id === createdId || item.name === `${testName}_updated`);
    if (!found) {
      throw new Error(`app banners missing created item: ${JSON.stringify(appPayload)}`);
    }
    results.push('app shop banners');

    const deleteRes = await apiJson('admin/pet/banner/delete', token, {
      method: 'POST',
      body: JSON.stringify({ id: createdId }),
    });
    if (deleteRes.code !== 0) {
      throw new Error(`delete banner failed: ${JSON.stringify(deleteRes)}`);
    }
    createdId = null;
    results.push('admin delete');

    console.log('\n=== Shop banner e2e ===');
    console.log(`Passed: ${results.join(', ')}`);
    console.log('[PASS] shop banner e2e passed');
  } finally {
    if (createdId) {
      await conn.query('DELETE FROM operation_banner WHERE id = ?', [createdId]).catch(() => {});
    }
    await conn.end();
  }
}

main().catch((error) => {
  console.error('\n[FAIL]', error.message || error);
  process.exit(1);
});
