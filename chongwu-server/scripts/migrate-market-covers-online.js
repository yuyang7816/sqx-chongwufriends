const fs = require('fs')
const path = require('path')
const mysql = require('mysql2/promise')

const ONLINE_DOMAIN = 'https://cwjpy.xianmxkj.com'
const UPLOAD_API = 'https://cwjpyadmin.xianmxkj.com/sqx_fast/file/upload'
const LOCAL_DIRS = [
  'F:/work/AI_chongwu/upload',
  path.resolve(__dirname, '../sqx-application/upload'),
  path.resolve(__dirname, '../../upload'),
]

function findLocalUploadFile(coverUrl) {
  const text = String(coverUrl || '').trim()
  if (!text || text.startsWith('/static/')) return ''
  const normalized = text
    .replace(/^https?:\/\/[^/]+/i, '')
    .replace(/^\/sqx_fast/, '')
    .replace(/^\/upload\//, '')
    .replace(/^\//, '')
  for (const dir of LOCAL_DIRS) {
    const full = path.join(dir, normalized)
    if (fs.existsSync(full)) return full
  }
  return ''
}

function findStaticFile(coverUrl) {
  const text = String(coverUrl || '').trim()
  if (!text.startsWith('/static/')) return ''
  const rel = text.replace(/^\//, '')
  const candidates = [
    path.resolve(__dirname, '../../chongwu_admin/public', rel),
    path.resolve(__dirname, '../../chongwu-uniapp', rel),
  ]
  for (const full of candidates) {
    if (fs.existsSync(full)) return full
  }
  return ''
}

async function uploadFile(filePath) {
  const form = new FormData()
  const blob = new Blob([fs.readFileSync(filePath)])
  form.append('file', blob, path.basename(filePath))
  const res = await fetch(UPLOAD_API, { method: 'POST', body: form })
  const body = await res.json()
  if (!(body.code === 0 || body.code === 200)) {
    throw new Error(`upload failed: ${JSON.stringify(body)}`)
  }
  const relative = String(body.data?.relativePath || '').trim()
  if (!relative) throw new Error(`no relativePath: ${JSON.stringify(body)}`)
  const normalized = relative.startsWith('/') ? relative : `/${relative}`
  const uploadPath = normalized.includes('/upload/')
    ? normalized.slice(normalized.indexOf('/upload/'))
    : `/upload/${normalized.replace(/^\/+/, '')}`
  return `${ONLINE_DOMAIN}/sqx_fast${uploadPath}`
}

async function main() {
  const conn = await mysql.createConnection({
    host: 'shuju.xianmxkj.com',
    user: 'root',
    password: '4A4h85myDa8fNic8',
    database: 'chongwu',
  })

  const [rows] = await conn.query(
    `SELECT id, name, cover_url FROM island_market_item
     WHERE (is_deleted = 0 OR is_deleted IS NULL)
       AND cover_url IS NOT NULL AND cover_url <> ''
     ORDER BY id`,
  )

  const cache = new Map()
  let updated = 0

  for (const row of rows) {
    const oldUrl = String(row.cover_url || '').trim()
    if (/cwjpy\.xianmxkj\.com|cwjpyadmin\.xianmxkj\.com/i.test(oldUrl)) {
      console.log(`skip #${row.id} already online`)
      continue
    }

    const localFile = findLocalUploadFile(oldUrl) || findStaticFile(oldUrl)
    if (!localFile) {
      console.warn(`missing local file for #${row.id} ${row.name}: ${oldUrl}`)
      continue
    }

    let newUrl = cache.get(localFile)
    if (!newUrl) {
      newUrl = await uploadFile(localFile)
      cache.set(localFile, newUrl)
      console.log(`uploaded ${path.basename(localFile)} -> ${newUrl}`)
    }

    await conn.query('UPDATE island_market_item SET cover_url = ? WHERE id = ?', [newUrl, row.id])
    updated += 1
    console.log(`update #${row.id} ${row.name}`)
  }

  const [after] = await conn.query(
    'SELECT id, name, cover_url FROM island_market_item WHERE is_deleted = 0 OR is_deleted IS NULL ORDER BY id',
  )
  console.log('DONE updated=', updated)
  console.log(JSON.stringify(after, null, 2))
  await conn.end()
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
