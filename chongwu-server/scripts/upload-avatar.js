const fs = require('fs');
const path = require('path');
const https = require('https');

const ASSETS_DIR = 'C:\\Users\\Administrator\\.cursor\\projects\\F-work-AI-chongwuFriends\\assets';
const UPLOAD_URL = 'https://cwjpyadmin.xianmxkj.com/sqx_fast/file/upload';

function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const fileBuffer = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const boundary = '----SQXUploadBoundary' + Math.random().toString(16).slice(2);
    const header = `--${boundary}\r\nContent-Disposition: form-data; name="file"; filename="${fileName}"\r\nContent-Type: image/png\r\n\r\n`;
    const footer = `\r\n--${boundary}--\r\n`;
    const payload = Buffer.concat([Buffer.from(header), fileBuffer, Buffer.from(footer)]);
    const url = new URL(UPLOAD_URL);
    const req = https.request({ hostname: url.hostname, path: url.pathname, method: 'POST',
      headers: { 'Content-Type': `multipart/form-data; boundary=${boundary}`, 'Content-Length': payload.length } },
      (res) => { let d=''; res.on('data',c=>d+=c); res.on('end',()=>{ try{resolve(JSON.parse(d));}catch(e){resolve({raw:d});} }); });
    req.on('error', reject); req.write(payload); req.end();
  });
}

async function main() {
  const fileName = 'default_user_avatar.png';
  const res = await uploadFile(path.join(ASSETS_DIR, fileName));
  console.log('URL:', res?.data?.path || res?.data?.relativePath);
}
main().catch(e=>{console.error('ERR',e.message);process.exit(1);});
