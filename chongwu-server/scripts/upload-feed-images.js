const fs = require('fs');
const path = require('path');
const https = require('https');
const mysql = require('mysql2/promise');

const ASSETS_DIR = 'C:\\Users\\Administrator\\.cursor\\projects\\F-work-AI-chongwuFriends\\assets';
const UPLOAD_URL = 'https://cwjpyadmin.xianmxkj.com/sqx_fast/file/upload';

// productId -> image file name
const MAPPING = {
  7007: 'feed_7007_chicken.png',
  7010: 'feed_7010_canned.png',
  7201: 'feed_7201_beef.png',
  7002: 'feed_7002_freezedried.png',
  7011: 'feed_7002_freezedried.png', // 与 7002 同名复用
  7202: 'feed_7202_tuna.png',
  7003: 'feed_7003_wetcombo.png',
  7203: 'feed_7203_mixedfreeze.png',
  7204: 'feed_7204_goatmilk.png',
  7205: 'feed_7205_softcanned.png',
  7206: 'feed_7206_hairball.png',
};

function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const fileBuffer = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const boundary = '----SQXUploadBoundary' + Math.random().toString(16).slice(2);
    const header = `--${boundary}\r\nContent-Disposition: form-data; name="file"; filename="${fileName}"\r\nContent-Type: image/png\r\n\r\n`;
    const footer = `\r\n--${boundary}--\r\n`;
    const payload = Buffer.concat([Buffer.from(header), fileBuffer, Buffer.from(footer)]);

    const url = new URL(UPLOAD_URL);
    const options = {
      hostname: url.hostname,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': `multipart/form-data; boundary=${boundary}`,
        'Content-Length': payload.length,
      },
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (c) => (data += c));
      res.on('end', () => {
        try {
          resolve(JSON.parse(data));
        } catch (e) {
          resolve({ raw: data });
        }
      });
    });
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

async function main() {
  const results = {};
  const uniqueFiles = [...new Set(Object.values(MAPPING))];

  console.log(`共 ${uniqueFiles.length} 张图片需要上传`);
  for (const fileName of uniqueFiles) {
    const filePath = path.join(ASSETS_DIR, fileName);
    if (!fs.existsSync(filePath)) {
      console.error(`[缺失] ${fileName}`);
      continue;
    }
    try {
      const res = await uploadFile(filePath);
      const url = res?.data?.path || res?.data?.relativePath;
      console.log(`[上传] ${fileName} => ${url} (code=${res?.code})`);
      results[fileName] = url;
    } catch (e) {
      console.error(`[上传失败] ${fileName}: ${e.message}`);
    }
  }

  // 更新数据库
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  console.log('\n=== 更新数据库 cover_url ===');
  for (const [pid, fileName] of Object.entries(MAPPING)) {
    const url = results[fileName];
    if (!url) {
      console.log(`[跳过] 商品 ${pid} 无上传URL`);
      continue;
    }
    const [r] = await conn.query(
      'UPDATE mall_spu SET cover_url=? WHERE id=? AND biz_type=2;',
      [url, pid]
    );
    console.log(`[更新] 商品 ${pid} <= ${url} (affected=${r.affectedRows})`);
  }

  // 校验
  const [rows] = await conn.query(
    'SELECT id, name, cover_url FROM mall_spu WHERE biz_type=2 ORDER BY sort, id;'
  );
  console.log('\n=== 更新后结果 ===');
  console.table(rows);
  await conn.end();
}

main().catch((e) => {
  console.error('ERROR:', e.message);
  process.exit(1);
});
