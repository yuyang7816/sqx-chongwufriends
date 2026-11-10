import mysql from 'mysql2/promise';

const config = {
  host: 'shuju.xianmxkj.com',
  port: 3306,
  user: 'root',
  password: '4A4h85myDa8fNic8',
  database: 'chongwu',
  multipleStatements: true
};

const ddl = [
  {
    column: 'birthday',
    sql: "ALTER TABLE tb_user ADD COLUMN birthday VARCHAR(16) DEFAULT NULL COMMENT '生日 yyyy-MM-dd'"
  },
  {
    column: 'wechat',
    sql: "ALTER TABLE tb_user ADD COLUMN wechat VARCHAR(64) DEFAULT NULL COMMENT '微信号'"
  }
];

const connection = await mysql.createConnection(config);

try {
  const [rows] = await connection.query(
    `SELECT COLUMN_NAME
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = ?
       AND TABLE_NAME = 'tb_user'
       AND COLUMN_NAME IN ('birthday', 'wechat')`,
    [config.database]
  );

  const existing = new Set(rows.map(row => row.COLUMN_NAME));

  for (const item of ddl) {
    if (existing.has(item.column)) {
      console.log(`skip: tb_user.${item.column} already exists`);
      continue;
    }
    await connection.query(item.sql);
    console.log(`ok: tb_user.${item.column} added`);
  }

  const [columns] = await connection.query('SHOW COLUMNS FROM tb_user');
  const names = columns.map(col => col.Field);
  console.log('verify birthday:', names.includes('birthday'));
  console.log('verify wechat:', names.includes('wechat'));
} finally {
  await connection.end();
}
