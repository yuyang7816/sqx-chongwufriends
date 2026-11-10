const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

const DB = {
  host: process.env.DB_HOST || 'shuju.xianmxkj.com',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '4A4h85myDa8fNic8',
  database: process.env.DB_NAME || 'chongwu',
};

const EXPECTED_PET_MENUS = [
  { url: 'dashboard', name: '控制台' },
  { url: 'users', name: '用户列表' },
  { url: 'pet/stores', name: '门店管理' },
  { url: 'pet/archives', name: '宠物档案' },
  { url: 'pet/community', name: '动态管理' },
  { url: 'pet/community/complaints', name: '投诉管理' },
  { url: 'pet/sensitive', name: '敏感词' },
  { url: 'pet/mall', name: '商品管理' },
  { url: 'pet/orders', name: '订单管理' },
  { url: 'pet/feed', name: '投喂订单' },
  { url: 'content/decorations', name: '首页装修' },
  { url: 'pet/adopt', name: '领养申请' },
  { url: 'pet/tasks', name: '岛宅任务' },
  { url: 'pet/ranking', name: '打榜排行' },
  { url: 'pet/strategy', name: '养宠攻略' },
  { url: 'pet/market', name: '集市道具' },
  { url: 'pet/island-pets', name: '岛宅状态' },
  { url: 'pet/payment-orders', name: '支付单' },
  { url: 'pet/payment-log', name: '支付回调日志' },
  { url: 'system/menus', name: '菜单管理' },
  { url: 'system/roles', name: '角色权限' },
  { url: 'system/configs', name: '系统配置' },
  { url: 'system/admins', name: '管理员' },
];

const EXPECTED_BUTTON_PERMS = [
  'bannerList:add',
  'bannerList:update',
  'bannerList:delete',
  'pet:asset:list',
  'pet:community:audit',
  'pet:community:offline',
  'pet:community:delete',
  'pet:community:comment:delete',
  'pet:community:batchAudit',
  'pet:community:complaint:handle',
  'pet:community:complaint:config',
];

async function runSql(conn, fileName) {
  const sqlPath = path.join(__dirname, '../sql', fileName);
  const sql = fs.readFileSync(sqlPath, 'utf8');
  await conn.query(sql);
  console.log(`[OK] executed ${fileName}`);
}

async function main() {
  const conn = await mysql.createConnection({ ...DB, multipleStatements: true });
  try {
    await runSql(conn, 'pet-menu-community-complaint-upgrade-20260625.sql');

    const [menus] = await conn.query(
      "SELECT menu_id, parent_id, name, url, perms, type FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2100 ORDER BY menu_id"
    );
    const [roleMenus] = await conn.query(
      'SELECT menu_id FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (2023, 2034, 2035, 2036, 2037, 2090, 2091, 2092, 2093, 2094, 2095, 2096)'
    );
    const [configRows] = await conn.query(
      "SELECT param_key FROM sys_config WHERE param_key = 'community_complaint_types'"
    );

    const menuByUrl = new Map(menus.filter((row) => row.url).map((row) => [row.url, row]));
    const missingMenus = EXPECTED_PET_MENUS.filter((item) => !menuByUrl.has(item.url));
    const buttonPerms = menus
      .filter((row) => Number(row.type) === 2 && row.perms)
      .map((row) => row.perms);

    console.log('\n=== Menu audit ===');
    console.log(`Pet menus in DB: ${menus.length}`);
    console.log(`Missing expected menus: ${missingMenus.length ? missingMenus.map((item) => item.url).join(', ') : 'none'}`);
    console.log(`Complaint menu present: ${menuByUrl.has('pet/community/complaints') ? 'yes' : 'no'}`);
    console.log(`Role(1) granted new menus: ${roleMenus.length}/12`);
    console.log(`community_complaint_types config: ${configRows.length ? 'yes' : 'no'}`);

    const missingButtons = EXPECTED_BUTTON_PERMS.filter((perm) => !buttonPerms.includes(perm));
    console.log(`Missing button perms: ${missingButtons.length ? missingButtons.join(', ') : 'none'}`);

    const sysConfigMenu = menus.find((row) => row.menu_id === 2073);
    console.log(`System config perms: ${sysConfigMenu?.perms || '-'}`);

    if (missingMenus.length || !menuByUrl.has('pet/community/complaints') || roleMenus.length < 12 || missingButtons.length) {
      process.exitCode = 1;
      console.error('\n[FAIL] menu audit failed');
      return;
    }
    console.log('\n[PASS] menu audit passed');
  } finally {
    await conn.end();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
