const crypto = require('crypto');
const mysql = require('mysql2/promise');

const API_BASE = (process.env.API_BASE || 'http://192.168.0.120:9294/sqx_fast/').replace(/\/?$/, '/');
const DB = {
  host: process.env.DB_HOST || 'shuju.xianmxkj.com',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '4A4h85myDa8fNic8',
  database: process.env.DB_NAME || 'chongwu',
};

const EXPECTED_MENU_URLS = [
  'dashboard',
  'users',
  'content/decorations',
  'pet/cities',
  'pet/home-social',
  'pet/stores',
  'pet/archives',
  'pet/breeds',
  'pet/community',
  'pet/community/complaints',
  'pet/community/complaint-types',
  'pet/sensitive',
  'pet/mall/categories',
  'pet/mall',
  'pet/orders',
  'pet/feed',
  'pet/adopt',
  'pet/tasks',
  'pet/ranking',
  'pet/strategy',
  'pet/market',
  'pet/island-pets',
  'pet/payment-orders',
  'pet/payment-log',
  'system/menus',
  'system/roles',
  'system/configs',
  'system/admins',
];

const EXPECTED_BUTTON_PERMS = [
  'bannerList:add',
  'bannerList:update',
  'bannerList:delete',
  'pet:city:add',
  'pet:city:update',
  'pet:city:delete',
  'pet:home:social:update',
  'pet:store:add',
  'pet:store:update',
  'pet:store:delete',
  'pet:archive:add',
  'pet:archive:update',
  'pet:archive:delete',
  'pet:archive:audit',
  'pet:archive:batchAudit',
  'pet:asset:list',
  'pet:breed:add',
  'pet:breed:update',
  'pet:breed:delete',
  'pet:sensitive:add',
  'pet:sensitive:update',
  'pet:sensitive:delete',
  'pet:adopt:update',
  'pet:adopt:visit',
  'pet:task:add',
  'pet:task:update',
  'pet:task:delete',
  'pet:strategy:add',
  'pet:strategy:update',
  'pet:strategy:delete',
  'pet:market:add',
  'pet:market:update',
  'pet:market:delete',
  'pet:island:update',
  'pet:mall:add',
  'pet:mall:update',
  'pet:mall:delete',
  'pet:mall:category:add',
  'pet:mall:category:update',
  'pet:mall:category:delete',
  'pet:community:audit',
  'pet:community:offline',
  'pet:community:delete',
  'pet:community:comment:delete',
  'pet:community:batchAudit',
  'pet:community:complaint:handle',
  'pet:community:complaint:config',
  'pet:community:complaint:type:add',
  'pet:community:complaint:type:update',
  'pet:community:complaint:type:delete',
  'userList:delete',
];

function walkMenus(items, acc = []) {
  (items || []).forEach((item) => {
    acc.push(item);
    walkMenus(item.list || item.children || [], acc);
  });
  return acc;
}

function collectPerms(items, acc = new Set()) {
  items.forEach((item) => {
    String(item.perms || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .forEach((perm) => acc.add(perm));
    collectPerms(item.list || item.children || [], acc);
  });
  return acc;
}

async function loginWithCaptcha(conn) {
  const uuid = crypto.randomUUID();
  const code = 'test1';
  await conn.query('DELETE FROM sys_captcha WHERE uuid = ?', [uuid]);
  await conn.query(
    'INSERT INTO sys_captcha (uuid, code, expire_time) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE))',
    [uuid, code],
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
  return response.json();
}

async function main() {
  const conn = await mysql.createConnection(DB);
  try {
    const [menus] = await conn.query(
      'SELECT menu_id, parent_id, name, url, perms, type FROM sys_menu WHERE menu_id >= 2000 ORDER BY menu_id',
    );
    const menuByUrl = new Map(menus.filter((row) => row.url).map((row) => [row.url, row]));
    const buttonPerms = new Set(
      menus.filter((row) => Number(row.type) === 2 && row.perms).map((row) => row.perms),
    );

    const missingMenus = EXPECTED_MENU_URLS.filter((url) => !menuByUrl.has(url));
    const missingButtons = EXPECTED_BUTTON_PERMS.filter((perm) => !buttonPerms.has(perm));

    console.log('=== DB menu audit ===');
    console.log(`Menus in DB: ${menus.length}`);
    console.log(`Missing menu URLs: ${missingMenus.length ? missingMenus.join(', ') : 'none'}`);
    console.log(`Missing button perms: ${missingButtons.length ? missingButtons.join(', ') : 'none'}`);

    const cityMenu = menuByUrl.get('pet/cities');
    const socialMenu = menuByUrl.get('pet/home-social');
    console.log(`宠物城市 parent_id=${cityMenu?.parent_id ?? 'MISSING'} (expect 2010)`);
    console.log(`首页社群 parent_id=${socialMenu?.parent_id ?? 'MISSING'} (expect 0)`);

    const token = await loginWithCaptcha(conn);
    const navPayload = await apiJson('sys/menu/nav', token);
    if (navPayload.code !== 0) {
      throw new Error(`nav failed: ${JSON.stringify(navPayload)}`);
    }
    const navData = navPayload.data || navPayload;
    const flat = walkMenus(navData.menuList || []);
    const navUrls = new Set(flat.filter((item) => item.url).map((item) => item.url));
    const navPerms = collectPerms(navData.menuList || []);
    if (Array.isArray(navData.permissions)) {
      navData.permissions.forEach((perm) => navPerms.add(perm));
    }

    const missingNavMenus = EXPECTED_MENU_URLS.filter((url) => !navUrls.has(url));
    const missingNavButtons = EXPECTED_BUTTON_PERMS.filter((perm) => !navPerms.has(perm));

    console.log('\n=== Nav API audit ===');
    console.log(`Nav flat menu count: ${flat.length}`);
    console.log(`Missing nav menu URLs: ${missingNavMenus.length ? missingNavMenus.join(', ') : 'none'}`);
    console.log(`Missing nav button perms: ${missingNavButtons.length ? missingNavButtons.join(', ') : 'none'}`);

    const cityPage = await apiJson('admin/pet/city/page', token, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 5 }),
    });
    if (cityPage.code !== 0) throw new Error(`city page failed: ${JSON.stringify(cityPage)}`);
    console.log(`\nAPI pet city page: records=${cityPage.data?.records?.length ?? 0}`);

    const socialInfo = await apiJson('admin/pet/home/social/info', token, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    if (socialInfo.code !== 0) throw new Error(`social info failed: ${JSON.stringify(socialInfo)}`);
    console.log(`API home social info: ${socialInfo.data?.communityTitle ? 'ok' : 'empty-config-ok'}`);

    const failed =
      missingMenus.length
      || missingButtons.length
      || missingNavMenus.length
      || missingNavButtons.length
      || Number(cityMenu?.parent_id) !== 2010
      || Number(socialMenu?.parent_id) !== 0;

    if (failed) {
      process.exitCode = 1;
      console.error('\n[FAIL] pet menu audit failed');
      return;
    }
    console.log('\n[PASS] pet menu audit passed');
  } finally {
    await conn.end();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
