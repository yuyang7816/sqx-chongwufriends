const fs = require('fs');
const path = require('path');
const https = require('https');
const mysql = require('mysql2/promise');

const ONLINE_DOMAIN = 'https://cwjpy.xianmxkj.com';
const UPLOAD_URL = 'https://cwjpyadmin.xianmxkj.com/sqx_fast/file/upload';
const ASSETS = 'C:\\Users\\Administrator\\.cursor\\projects\\F-work-AI-chongwuFriends\\assets';

const FILES = {
  home: path.join(ASSETS, 'banner-home-heta.png'),
  shop: path.join(ASSETS, 'banner-shop-heta.png'),
};

function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const fileBuffer = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const boundary = '----SQXUploadBoundary' + Math.random().toString(16).slice(2);
    const header =
      `--${boundary}\r\n` +
      `Content-Disposition: form-data; name="file"; filename="${fileName}"\r\n` +
      `Content-Type: image/png\r\n\r\n`;
    const footer = `\r\n--${boundary}--\r\n`;
    const payload = Buffer.concat([Buffer.from(header), fileBuffer, Buffer.from(footer)]);

    const url = new URL(UPLOAD_URL);
    const req = https.request(
      {
        hostname: url.hostname,
        path: url.pathname,
        method: 'POST',
        headers: {
          'Content-Type': `multipart/form-data; boundary=${boundary}`,
          'Content-Length': payload.length,
        },
      },
      (res) => {
        let data = '';
        res.on('data', (c) => (data += c));
        res.on('end', () => {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            reject(new Error(`invalid json: ${data}`));
          }
        });
      }
    );
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

function toPublicUrl(body) {
  const relative = String(body?.data?.relativePath || body?.data?.path || '').trim();
  if (!relative) throw new Error(`no path in upload response: ${JSON.stringify(body)}`);
  if (/^https?:\/\//i.test(relative)) return relative;
  const normalized = relative.startsWith('/') ? relative : `/${relative}`;
  if (normalized.startsWith('/sqx_fast/')) return `${ONLINE_DOMAIN}${normalized}`;
  if (normalized.startsWith('/upload/')) return `${ONLINE_DOMAIN}/sqx_fast${normalized}`;
  return `${ONLINE_DOMAIN}/sqx_fast/upload/${normalized.replace(/^\/+/, '')}`;
}

async function main() {
  for (const [key, file] of Object.entries(FILES)) {
    if (!fs.existsSync(file)) throw new Error(`missing file: ${file}`);
  }

  console.log('上传首页 banner...');
  const homeRes = await uploadFile(FILES.home);
  if (!(homeRes.code === 0 || homeRes.code === 200)) {
    throw new Error(`home upload failed: ${JSON.stringify(homeRes)}`);
  }
  const homeUrl = toPublicUrl(homeRes);
  console.log('home =>', homeUrl);

  console.log('上传商城 banner...');
  const shopRes = await uploadFile(FILES.shop);
  if (!(shopRes.code === 0 || shopRes.code === 200)) {
    throw new Error(`shop upload failed: ${JSON.stringify(shopRes)}`);
  }
  const shopUrl = toPublicUrl(shopRes);
  console.log('shop =>', shopUrl);

  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  // 首页主 banner
  const [r1] = await conn.query(
    `UPDATE operation_banner
     SET image_url = ?, name = ?, sub_title = ?
     WHERE id = 1 AND position_code = 'home'`,
    [homeUrl, '和它交朋友', '给每个毛孩子一个温暖的家']
  );
  console.log('update home #1 affected=', r1.affectedRows);

  // 首页第二条也换成同主题图，保持风格统一
  const [r2] = await conn.query(
    `UPDATE operation_banner
     SET image_url = ?
     WHERE id = 2 AND position_code = 'home'`,
    [homeUrl]
  );
  console.log('update home #2 affected=', r2.affectedRows);

  // 商城主 banner
  const [r4] = await conn.query(
    `UPDATE operation_banner
     SET image_url = ?, name = ?, sub_title = ?, jump_url = ?
     WHERE id = 4 AND position_code = 'shop'`,
    [shopUrl, '萌宠好物市集', '门店同款好物精选', '']
  );
  console.log('update shop #4 affected=', r4.affectedRows);

  // 下线 e2e 测试脏数据
  const [rOff] = await conn.query(
    `UPDATE operation_banner
     SET status = 0
     WHERE position_code = 'shop' AND id IN (5, 6, 7)`
  );
  console.log('disable e2e shop banners affected=', rOff.affectedRows);

  const [rows] = await conn.query(
    `SELECT id, name, image_url, jump_url, position_code, sort, status, sub_title
     FROM operation_banner
     WHERE position_code IN ('home', 'shop')
     ORDER BY position_code, sort, id`
  );
  console.log('\n=== 更新后 ===');
  console.table(rows);
  await conn.end();
}

main().catch((e) => {
  console.error('ERROR:', e.message);
  process.exit(1);
});
