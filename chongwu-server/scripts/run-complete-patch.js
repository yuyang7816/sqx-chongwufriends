const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

async function main() {
  const sqlPath = path.join(__dirname, '../sql/sys-config-complete-patch-20260804.sql');
  const sql = fs.readFileSync(sqlPath, 'utf8');
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
    multipleStatements: true,
  });
  console.log('Executing complete patch SQL...');
  await conn.query(sql);

  const keys = [
    'sms_provider',
    'sms_smsbao_username',
    'sms_smsbao_password',
    'sms_ali_access_key',
    'sms_ali_access_secret',
    'sms_tencent_sdk_appid',
    'sms_tencent_appkey',
    'mail_host',
    'mail_port',
    'mail_send_mail',
    'mail_auth_code',
    'map_tianditu_key',
    'google_map_key',
    'sys_getui_appid',
    'sys_admin_domain',
    'file_endpoint',
    'file_bucket',
    'file_ali_oss_accessKeyId',
    'file_bt_endpoint',
    'user_agreement',
    'privacy_policy',
    'pay_wx_app_id',
  ];
  const [rows] = await conn.query(
    `SELECT param_key, param_value, param_type, remark FROM sys_config WHERE param_key IN (${keys
      .map(() => '?')
      .join(',')}) ORDER BY param_type, param_key`,
    keys
  );
  console.log('patched configs:', rows.length);
  rows.forEach((r) => {
    const val = r.param_value == null ? '' : String(r.param_value);
    const show = val.length > 48 ? val.slice(0, 48) + '...' : val;
    console.log(`  [${r.param_type}] ${r.param_key} = ${JSON.stringify(show)}`);
  });

  const [tpl] = await conn.query('SELECT biz_code, LEFT(content,60) c FROM sms_template ORDER BY biz_code');
  console.log('sms_template:', tpl);

  const [duanxin] = await conn.query(
    "SELECT param_key, param_value FROM sys_config WHERE param_type='duanxin' ORDER BY param_key"
  );
  console.log('duanxin all:', duanxin);

  await conn.end();
  console.log('DONE');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
