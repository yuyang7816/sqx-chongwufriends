const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'shuju.xianmxkj.com',port:3306,user:'root',password:'4A4h85myDa8fNic8',database:'chongwu'});
  const [m] = await c.query("SELECT menu_id,parent_id,name,url,perms,type,order_num FROM sys_menu WHERE name LIKE '%商城%' OR name LIKE '%商品%' OR url LIKE '%mall%' OR url LIKE '%spu%' OR url LIKE '%order%' ORDER BY parent_id,order_num,menu_id");
  console.log('=== mall-related menus ===');
  console.table(m);
  await c.end();
})().catch(e=>{console.error(e.message);process.exit(1);});
