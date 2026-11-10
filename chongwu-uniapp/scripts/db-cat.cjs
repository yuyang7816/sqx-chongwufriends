const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'shuju.xianmxkj.com',port:3306,user:'root',password:'4A4h85myDa8fNic8',database:'chongwu'});
  const [cols] = await c.query("SHOW COLUMNS FROM mall_category");
  console.log('mall_category cols:', cols.map(x=>x.Field).join(', '));
  const [m] = await c.query("SELECT * FROM mall_category WHERE is_deleted=0 ORDER BY sort,id");
  console.table(m.map(x=>({id:x.id,name:x.name,sort:x.sort,status:x.status})));
  await c.end();
})().catch(e=>{console.error(e.message);process.exit(1);});
