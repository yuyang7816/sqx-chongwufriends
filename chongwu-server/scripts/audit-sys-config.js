const mysql = require('mysql2/promise');

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    port: 3306,
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  });

  const [cfgs] = await conn.query(
    `SELECT param_key, param_value, param_type, remark FROM sys_config WHERE IFNULL(status,'1')='1' ORDER BY param_type, param_key`
  );
  console.log('=== sys_config count:', cfgs.length, '===');
  const byType = {};
  for (const r of cfgs) {
    const t = r.param_type || 'null';
    byType[t] = byType[t] || [];
    byType[t].push(r);
  }
  for (const [t, list] of Object.entries(byType)) {
    console.log('\n[' + t + '] ' + list.length);
    for (const r of list) {
      const val = String(r.param_value ?? '').length > 40
        ? String(r.param_value).slice(0, 40) + '...'
        : (r.param_value ?? '');
      console.log(' ', r.param_key, '=', JSON.stringify(val), '|', r.remark || '');
    }
  }

  const needed = [
    'sms_provider', 'sms_smsbao_username', 'sms_smsbao_password',
    'sms_ali_access_key', 'sms_ali_access_secret',
    'sms_tencent_sdk_appid', 'sms_tencent_appkey',
    'sys_server_name', 'sys_user_ma_appid', 'sys_user_ma_secret',
    'file_storage_type', 'file_local_upload_path', 'file_cdn_domain',
    'map_platform', 'map_tencent_key', 'map_tianditu_key', 'google_map_key',
    'mail_host', 'mail_send_mail', 'mail_port', 'mail_auth_code',
    'sys_getui_appid', 'sys_getui_appkey', 'sys_getui_mastersecret',
    'sys_h5_domain', 'sys_admin_domain',
    'island_daily_stat_init', 'island_feed_progress_gain',
    'ranking_daily_free_limit', 'pet_pay_mock_enabled',
  ];
  console.log('\n=== missing from needed list ===');
  const keys = new Set(cfgs.map((c) => c.param_key));
  for (const k of needed) {
    if (!keys.has(k)) console.log(' MISSING', k);
  }

  const [tpl] = await conn.query('SELECT id, biz_code, LEFT(content,80) content FROM sms_template');
  console.log('\n=== sms_template ===', tpl);

  const [tables] = await conn.query(`SHOW TABLES`);
  console.log('\n=== tables count ===', tables.length);

  // check key tables row counts
  const checkTables = [
    'sms_template', 'sys_config', 'sys_menu', 'sys_user', 'sys_role',
    'island_task', 'island_market_item', 'mall_category', 'home_banner',
    'verify_code',
  ];
  console.log('\n=== table row counts ===');
  for (const t of checkTables) {
    try {
      const [r] = await conn.query(`SELECT COUNT(*) c FROM \`${t}\``);
      console.log(' ', t, r[0].c);
    } catch (e) {
      console.log(' ', t, 'ERR', e.message);
    }
  }

  await conn.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
