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

function assertPerm(perms, perm) {
  if (!perms.includes(perm)) {
    throw new Error(`missing permission ${perm}`);
  }
}

async function main() {
  const conn = await mysql.createConnection(DB);
  const results = [];
  const stamp = Date.now();
  const categoryName = `e2e_cat_${stamp}`;
  const productName = `e2e_spu_${stamp}`;
  let createdCategoryId = null;
  let createdSpuId = null;

  try {
    const token = await login(conn);
    results.push('login');

    const nav = await apiJson('sys/menu/nav', token);
    const flat = walkMenus(nav.data?.menuList || []);
    const categoryMenu = flat.find((item) => item.url === 'pet/mall/categories');
    const spuMenu = flat.find((item) => item.url === 'pet/mall');
    if (!categoryMenu) throw new Error('menu pet/mall/categories missing');
    if (!spuMenu) throw new Error('menu pet/mall missing');

    const perms = [...(nav.data?.permissions || [])];
    [
      'pet:mall:category:list',
      'pet:mall:category:add',
      'pet:mall:category:update',
      'pet:mall:category:delete',
      'pet:mall:list',
      'pet:mall:add',
      'pet:mall:update',
      'pet:mall:delete',
      'pet:mall:info',
    ].forEach((perm) => assertPerm(perms, perm));
    results.push('admin menu + permissions');

    const createCatRes = await apiJson('admin/pet/mall/category/save', token, {
      method: 'POST',
      body: JSON.stringify({ name: categoryName, sort: 99, status: 1 }),
    });
    if (createCatRes.code !== 0) {
      throw new Error(`create category failed: ${JSON.stringify(createCatRes)}`);
    }

    const catPageRes = await apiJson('admin/pet/mall/category/page', token, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, name: categoryName }),
    });
    const createdCategory = (catPageRes.data?.records || []).find((item) => item.name === categoryName);
    if (!createdCategory?.id) {
      throw new Error(`created category not found: ${JSON.stringify(catPageRes)}`);
    }
    createdCategoryId = createdCategory.id;
    results.push('admin category create/page');

    const updateCatRes = await apiJson('admin/pet/mall/category/update', token, {
      method: 'POST',
      body: JSON.stringify({
        id: createdCategoryId,
        name: `${categoryName}_updated`,
        sort: 100,
        status: 1,
      }),
    });
    if (updateCatRes.code !== 0) {
      throw new Error(`update category failed: ${JSON.stringify(updateCatRes)}`);
    }
    results.push('admin category update');

    const catListRes = await apiJson('admin/pet/mall/category/list', token, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    const listed = (catListRes.data || []).find((item) => item.id === createdCategoryId);
    if (!listed) {
      throw new Error(`category list missing created item: ${JSON.stringify(catListRes)}`);
    }
    results.push('admin category list');

    const createSpuRes = await apiJson('admin/pet/mall/save', token, {
      method: 'POST',
      body: JSON.stringify({
        name: productName,
        categoryId: createdCategoryId,
        coverUrl: '/static/logo.png',
        description: 'e2e test product',
        price: 9.9,
        originalPrice: 19.9,
        pointPrice: 100,
        bizType: 2,
        status: 1,
        sort: 99,
      }),
    });
    if (createSpuRes.code !== 0) {
      throw new Error(`create spu failed: ${JSON.stringify(createSpuRes)}`);
    }
    results.push('admin spu create');

    const spuPageRes = await apiJson('admin/pet/mall/page', token, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, keyword: productName }),
    });
    const createdSpu = (spuPageRes.data?.records || []).find((item) => item.name === productName);
    if (!createdSpu?.id) {
      throw new Error(`created spu not found: ${JSON.stringify(spuPageRes)}`);
    }
    createdSpuId = createdSpu.id;
    if (!createdSpu.category || !createdSpu.coverUrl) {
      throw new Error(`spu missing category/coverUrl: ${JSON.stringify(createdSpu)}`);
    }
    results.push('admin spu page');

    const spuInfoRes = await apiJson('admin/pet/mall/info', token, {
      method: 'POST',
      body: JSON.stringify({ id: createdSpuId }),
    });
    if (spuInfoRes.code !== 0 || spuInfoRes.data?.name !== productName) {
      throw new Error(`spu info failed: ${JSON.stringify(spuInfoRes)}`);
    }
    results.push('admin spu info');

    const updateSpuRes = await apiJson('admin/pet/mall/update', token, {
      method: 'POST',
      body: JSON.stringify({
        id: createdSpuId,
        name: `${productName}_updated`,
        categoryId: createdCategoryId,
        coverUrl: '/static/logo.png',
        description: 'e2e updated',
        price: 8.8,
        originalPrice: 18.8,
        pointPrice: 88,
        bizType: 2,
        status: 1,
        sort: 100,
      }),
    });
    if (updateSpuRes.code !== 0) {
      throw new Error(`update spu failed: ${JSON.stringify(updateSpuRes)}`);
    }
    results.push('admin spu update');

    const appCatRes = await apiJson('app/pet/mall/categories', null, {
      method: 'POST',
      body: JSON.stringify({}),
    });
    const appCats = appCatRes.data || [];
    if (!appCats.length || !appCats.find((item) => item.name === '全部商品')) {
      throw new Error(`app categories invalid: ${JSON.stringify(appCatRes)}`);
    }
    results.push('app categories');

    const appPageRes = await apiJson('app/pet/mall/page', null, {
      method: 'POST',
      body: JSON.stringify({ page: 1, limit: 50, categoryId: createdCategoryId }),
    });
    const appRecords = appPageRes.data?.records || [];
    const appSpu = appRecords.find((item) => item.id === createdSpuId);
    if (!appSpu || appSpu.name !== `${productName}_updated`) {
      throw new Error(`app page missing updated spu: ${JSON.stringify(appPageRes)}`);
    }
    if (!appSpu.coverUrl || !appSpu.description) {
      throw new Error(`app spu missing cover/description: ${JSON.stringify(appSpu)}`);
    }
    results.push('app page by category');

    const appInfoRes = await apiJson('app/pet/mall/info', null, {
      method: 'POST',
      body: JSON.stringify({ id: createdSpuId }),
    });
    if (appInfoRes.code !== 0 || appInfoRes.data?.originalPrice == null) {
      throw new Error(`app info failed: ${JSON.stringify(appInfoRes)}`);
    }
    results.push('app info');

    const deleteSpuRes = await apiJson('admin/pet/mall/delete', token, {
      method: 'POST',
      body: JSON.stringify({ id: createdSpuId }),
    });
    if (deleteSpuRes.code !== 0) {
      throw new Error(`delete spu failed: ${JSON.stringify(deleteSpuRes)}`);
    }
    createdSpuId = null;
    results.push('admin spu delete');

    const deleteCatRes = await apiJson('admin/pet/mall/category/delete', token, {
      method: 'POST',
      body: JSON.stringify({ id: createdCategoryId }),
    });
    if (deleteCatRes.code !== 0) {
      throw new Error(`delete category failed: ${JSON.stringify(deleteCatRes)}`);
    }
    createdCategoryId = null;
    results.push('admin category delete');

    console.log('\n=== Mall category + SPU e2e ===');
    console.log(`Passed: ${results.join(', ')}`);
    console.log('[PASS] mall category spu e2e passed');
  } finally {
    if (createdSpuId) {
      await conn.query('DELETE FROM mall_spu WHERE id = ?', [createdSpuId]).catch(() => {});
    }
    if (createdCategoryId) {
      await conn.query('DELETE FROM mall_category WHERE id = ?', [createdCategoryId]).catch(() => {});
    }
    await conn.end();
  }
}

main().catch((error) => {
  console.error('\n[FAIL]', error.message || error);
  process.exit(1);
});
