const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'shuju.xianmxkj.com',port:3306,user:'root',password:'4A4h85myDa8fNic8',database:'chongwu'});
  await c.beginTransaction();
  try {
    // 1. 删除投喂商品菜单及其按钮（2160/2161/2162/2163）
    await c.query("DELETE FROM sys_menu WHERE menu_id IN (2160,2161,2162,2163)");
    // 2. 投喂订单(2033) 改名 赠送记录，移到 商城管理(2030) 下
    await c.query("UPDATE sys_menu SET name='赠送记录', parent_id=2030, order_num=3, perms='pet:feed:list' WHERE menu_id=2033");
    // 3. 删除投喂模块目录(2184)
    await c.query("DELETE FROM sys_menu WHERE menu_id=2184");
    await c.commit();
    console.log('menu cleanup committed');
  } catch (e) {
    await c.rollback();
    console.error('FAIL', e.message); process.exit(1);
  }
  const [m] = await c.query("SELECT menu_id,parent_id,name,url,perms,type,order_num FROM sys_menu WHERE parent_id=2030 OR menu_id=2030 ORDER BY order_num,menu_id");
  console.log('=== 商城管理 子菜单 ===');
  console.table(m);
  const [chk] = await c.query("SELECT menu_id,name FROM sys_menu WHERE menu_id IN (2160,2161,2162,2163,2184)");
  console.log('remaining deleted menus:', chk.length);
  await c.end();
})().catch(e=>{console.error(e.message);process.exit(1);});
