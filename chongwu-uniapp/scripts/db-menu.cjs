const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'shuju.xianmxkj.com',port:3306,user:'root',password:'4A4h85myDa8fNic8',database:'chongwu'});
  const [m] = await c.query("SELECT menu_id,parent_id,name,url,perms,type,icon,order_num FROM sys_menu WHERE url LIKE '%feed%' OR name LIKE '%投喂%' OR name LIKE '%赠送%' ORDER BY menu_id");
  console.log('=== feed-related menus ===');
  console.table(m);
  await c.end();
})().catch(e=>{console.error(e.message);process.exit(1);});
