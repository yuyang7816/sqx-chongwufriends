import { createModuleStorage, createPageResult } from '@/mock/database'

function poster(title, colorA, colorB) {
  return `data:image/svg+xml;utf8,${encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="220" height="90" viewBox="0 0 220 90"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="${colorA}"/><stop offset="100%" stop-color="${colorB}"/></linearGradient></defs><rect width="220" height="90" rx="18" fill="url(#g)"/><text x="20" y="42" fill="#fff" font-size="22" font-family="Arial, PingFang SC, sans-serif" font-weight="700">${title}</text><text x="20" y="66" fill="rgba(255,255,255,.9)" font-size="12" font-family="Arial, PingFang SC, sans-serif">Mock Banner</text></svg>`)}`
}

const storage = createModuleStorage('admin-decorations', [
  { id: 7001, module: 'banner', name: '秋季福利主视觉', image: poster('首页轮播', '#44b6a6', '#7a8bff'), link: '/activity/autumn', sort: 1, status: 'show', createdAt: '2026-05-10 11:21:22' },
  { id: 7002, module: 'popup', name: '限时弹窗活动', image: poster('首页弹窗', '#ef5350', '#ec407a'), link: '/popup/campaign', sort: 1, status: 'show', createdAt: '2026-05-12 20:35:52' },
  { id: 7003, module: 'category', name: '新人专区素材', image: poster('首页分类', '#4db6ac', '#26c6da'), link: '/content/categories', sort: 1, status: 'show', createdAt: '2026-05-14 08:23:15' },
])

export function pageDecorations(params) {
  const keyword = (params.keyword || '').trim()
  const list = storage.getAll().filter((item) => {
    const matchKeyword = !keyword || [item.name, item.link].some((field) => field.includes(keyword))
    const matchStatus = !params.status || item.status === params.status
    const matchModule = !params.module || item.module === params.module
    return matchKeyword && matchStatus && matchModule
  })
  return createPageResult(list.sort((a, b) => a.sort - b.sort), params.pageNum, params.pageSize)
}

export function createDecoration(payload) {
  const list = storage.getAll()
  list.unshift({ id: Date.now(), createdAt: new Date().toLocaleString('zh-CN', { hour12: false }), ...payload })
  storage.setAll(list)
}

export function updateDecoration(id, payload) {
  storage.setAll(storage.getAll().map((item) => (item.id === Number(id) ? { ...item, ...payload } : item)))
}

export function deleteDecoration(id) {
  storage.setAll(storage.getAll().filter((item) => item.id !== Number(id)))
}
