import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import zlib from 'node:zlib'
import { promisify } from 'node:util'

const UPLOAD_PUBLIC_BASE = 'https://cwjpyadmin.xianmxkj.com/sqx_fast'
const gunzip = promisify(zlib.gunzip)
const inflate = promisify(zlib.inflate)
const brotliDecompress = promisify(zlib.brotliDecompress)

function rewriteUploadResponseBody(raw) {
  try {
    const json = JSON.parse(raw)
    const data = json?.data
    if (!data || typeof data !== 'object') return raw
    const relative = String(data.relativePath || '').trim()
    let uploadPath = ''
    if (relative.includes('/upload/')) {
      uploadPath = relative.startsWith('/') ? relative : `/${relative}`
      uploadPath = uploadPath.replace(/^\/sqx_fast(?=\/|$)/, '')
    } else {
      const path = String(data.path || data.url || '').trim()
      const idx = path.indexOf('/upload/')
      if (idx >= 0) uploadPath = path.slice(idx)
    }
    if (!uploadPath.startsWith('/upload/')) return raw
    const publicUrl = `${UPLOAD_PUBLIC_BASE}${uploadPath}`
    data.path = publicUrl
    if (data.url != null) data.url = publicUrl
    return JSON.stringify(json)
  } catch {
    return raw
  }
}

async function decodeProxyBody(proxyRes, chunks) {
  const buf = Buffer.concat(chunks)
  const encoding = String(proxyRes.headers['content-encoding'] || '').toLowerCase()
  try {
    if (encoding.includes('gzip')) return (await gunzip(buf)).toString('utf8')
    if (encoding.includes('br')) return (await brotliDecompress(buf)).toString('utf8')
    if (encoding.includes('deflate')) return (await inflate(buf)).toString('utf8')
  } catch {
    // 解压失败时按明文处理
  }
  return buf.toString('utf8')
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
      vueTemplate: true,
    }),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': '/src',
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@use "@/styles/element/index.scss" as *;',
      },
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // 开发态上传走线上，并改写返回里的 localhost path
      '/upload-api': {
        target: 'https://cwjpyadmin.xianmxkj.com',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/upload-api/, '/sqx_fast'),
        selfHandleResponse: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes, req, res) => {
            const chunks = []
            proxyRes.on('data', (chunk) => chunks.push(chunk))
            proxyRes.on('end', async () => {
              try {
                const raw = await decodeProxyBody(proxyRes, chunks)
                const isUpload = String(req.url || '').includes('/file/upload')
                  || String(req.url || '').includes('/alioss/upload')
                const body = isUpload ? rewriteUploadResponseBody(raw) : raw
                const skipHeaders = new Set([
                  'content-length',
                  'content-encoding',
                  'transfer-encoding',
                ])
                Object.keys(proxyRes.headers || {}).forEach((name) => {
                  if (skipHeaders.has(name.toLowerCase())) return
                  const value = proxyRes.headers[name]
                  if (value != null) res.setHeader(name, value)
                })
                const buf = Buffer.from(body, 'utf8')
                res.statusCode = proxyRes.statusCode || 200
                res.setHeader('content-type', 'application/json;charset=UTF-8')
                res.setHeader('content-length', String(buf.length))
                res.end(buf)
              } catch (error) {
                res.statusCode = 502
                res.setHeader('content-type', 'application/json;charset=UTF-8')
                res.end(JSON.stringify({
                  code: 500,
                  msg: error?.message || 'upload proxy failed',
                }))
              }
            })
          })
        },
      },
    },
  },
  preview: {
    host: '0.0.0.0',
    port: 4173,
  },
})
