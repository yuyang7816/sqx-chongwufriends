const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'shuju.xianmxkj.com',port:3306,user:'root',password:'4A4h85myDa8fNic8',database:'chongwu'});
  await c.query("UPDATE sys_menu SET name='商品订单' WHERE menu_id=2147");
  const [m] = await c.query("SELECT menu_id,name,url FROM sys_menu WHERE menu_id=2147");
  console.table(m);
  await c.end();
})().catch(e=>{console.error(e.message);process.exit(1);});
