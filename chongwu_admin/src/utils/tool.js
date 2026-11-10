export function getUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const random = (Math.random() * 16) | 0
    const value = c === 'x' ? random : (random & 0x3) | 0x8
    return value.toString(16)
  })
}

/** 线上管理端域名（图片访问域名） */
export const ONLINE_FILE_DOMAIN = 'https://cwjpyadmin.xianmxkj.com'

/** 默认用户头像（用户未设置头像时展示） */
export const DEFAULT_USER_AVATAR = 'https://cwjpyadmin.xianmxkj.com/sqx_fast/upload/2026/07/31/2896816a-fb10-424b-b5e5-976630b79cca.png'

/** 线上文件访问根路径（含 context-path） */
export function getUploadPublicBaseUrl() {
  const fromEnv = import.meta.env?.VITE_UPLOAD_PUBLIC_BASE_URL
  const baseUrl = fromEnv || `${ONLINE_FILE_DOMAIN}/sqx_fast/`
  return baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`
}

export function getApiBaseUrl() {
  // 优先环境变量；否则按下方本地/线上二选一（注释切换即可）
  const fromEnv = import.meta.env?.VITE_API_BASE_URL
  // 线上业务 API：
  const baseUrl = fromEnv || `${ONLINE_FILE_DOMAIN}/sqx_fast/`
  // 本地业务 API（需先启动 Redis + 后端 9294）：
  // const baseUrl = fromEnv || 'http://192.168.0.122:9294/sqx_fast/'
  return baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`
}

export function getUploadBaseUrl() {
  // 图片上传走线上管理端域名
  const fromEnv = import.meta.env.VITE_UPLOAD_BASE_URL
  if (fromEnv) {
    return fromEnv.endsWith('/') ? fromEnv : `${fromEnv}/`
  }
  return getUploadPublicBaseUrl()
  // 本地上传备份：
  // return getApiBaseUrl()
}

/** 统一上传接口路径 */
export function getUploadApiUrl() {
  const base = getUploadBaseUrl()
  const normalizedBase = base.endsWith('/') ? base : `${base}/`
  const apiPath = `${normalizedBase}file/upload`
  // 开发态代理路径是相对根路径，需拼当前页 origin，避免被 axios baseURL 指到本地后端
  if (apiPath.startsWith('/') && typeof window !== 'undefined' && window.location?.origin) {
    return `${window.location.origin}${apiPath}`
  }
  return apiPath
}

function normalizeUploadPath(pathname = '') {
  let path = String(pathname || '').trim()
  if (!path) return ''
  path = path.replace(/^\/sqx_fast(?=\/|$)/, '')
  if (!path.startsWith('/')) path = `/${path}`
  return path
}

/** 上传文件访问地址：统一拼公网上传域名 */
export function joinApiFileUrl(pathname) {
  const path = normalizeUploadPath(pathname)
  if (!path) return ''
  return `${getUploadPublicBaseUrl().replace(/\/+$/, '')}${path}`
}

function extractUploadPath(value) {
  const text = String(value || '').trim()
  if (!text) return ''
  if (/^https?:\/\//i.test(text)) {
    try {
      const u = new URL(text)
      return normalizeUploadPath(u.pathname)
    } catch {
      return ''
    }
  }
  return normalizeUploadPath(text)
}

/**
 * 将上传结果转为管理端可访问地址。
 * 本地开发时 upload 走 ONLINE_FILE_DOMAIN，预览也拼同一域名。
 */
export function toPublicFileUrl(input) {
  if (input == null || input === '') return ''

  if (typeof input === 'object') {
    const relative = String(input.relativePath || '').trim()
    if (relative.includes('/upload/')) {
      return joinApiFileUrl(relative)
    }
  }

  let raw = input
  if (typeof raw === 'object') {
    raw = raw.url || raw.src || raw.path || raw.data || ''
  }
  const value = String(raw || '').trim()
  if (!value) return ''

  const uploadHost = (() => {
    try {
      return new URL(getUploadPublicBaseUrl()).host
    } catch {
      return ''
    }
  })()

  if (/^https?:\/\//i.test(value)) {
    try {
      const u = new URL(value)
      if (u.pathname.includes('/upload/')) {
        // 统一归一到当前上传域名，避免本地 API 域名下的历史地址 404
        return joinApiFileUrl(u.pathname)
      }
      // 其它 CDN（如 tu.mianyazuju.com）保持原样
      if (uploadHost && u.host === uploadHost) return value
      return value
    } catch {
      return value
    }
  }

  if (value.startsWith('//')) {
    return toPublicFileUrl(`https:${value}`)
  }

  // 小程序/管理端本地静态资源（如 /static/strategy/*.jpg、/static/pets/*.png）
  // 走前端 public，不能拼后端 API，否则管理端预览 404
  const staticPath = value.startsWith('/static/')
    ? value
    : (value.startsWith('static/') ? `/${value}` : '')
  if (staticPath) {
    return staticPath
  }

  if (value.includes('/upload/')) {
    return joinApiFileUrl(value)
  }

  // 非 upload 相对路径，拼上传域名
  return joinApiFileUrl(value)
}

export function getCaptchaUrl(uuid) {
  return `${getApiBaseUrl()}captcha.jpg?uuid=${uuid}`
}

export function isAuth(key) {
  return JSON.parse(window.sessionStorage.getItem('permissions') || '[]').indexOf(key) !== -1 || false
}

export function setPermissions(permissions = []) {
  window.sessionStorage.setItem('permissions', JSON.stringify(permissions))
}

export function clearPermissions() {
  window.sessionStorage.removeItem('permissions')
}

export function clearAuthCache() {
  window.sessionStorage.removeItem('admin-auth')
  clearPermissions()
}

export function collectPermsFromMenu(menuList = []) {
  const set = new Set()
  const walk = (items) => {
    items.forEach((item) => {
      String(item?.perms || '').split(',').map((value) => value.trim()).filter(Boolean).forEach((perm) => set.add(perm))
      walk(item?.list || item?.children || [])
    })
  }
  walk(menuList)
  return [...set]
}

export function normalizePermissions(permissions, menuList = []) {
  if (Array.isArray(permissions) && permissions.length) {
    return permissions
  }
  if (permissions && typeof permissions === 'object') {
    const values = Object.values(permissions).filter(Boolean)
    if (values.length) {
      return values
    }
  }
  return collectPermsFromMenu(menuList)
}

const routePathMap = {
  dashboard: '/dashboard',
  home: '/dashboard',
  users: '/users',
  userList: '/users',
  orderCenter: '/orders',
  financeList: '/finance',
  merchantList: '/merchants',
  merchantEnter: '/merchants/applications',
  devices: '/merchants/devices',
  localityJlfw: '/coaches/services',
  dispatchMission: '/coaches/orders',
  musicActivity: '/music/categories',
  musicLibrary: '/music/library',
  musicPlay: '/music/plays',
  aboutTheBall: '/about-the-ball',
  displayCabinet: '/cabinets/display',
  commodity: '/cabinets/goods',
  orderCenterCommodity: '/cabinets/orders',
  userList: '/users',
  autonym: '/users/certifications',
  userCertification: '/users/certifications',
  integral: '/users/integrals',
  integralDetails: '/users/integrals',
  points: '/users/integrals',
  chatRecord: '/users/chats',
  transferGift: '/users/transfers',
  message: '/users/messages',
  messageList: '/users/messages',
  messageZx: '/users/cancellations',
  bannerList: '/content/decorations',
  memberDetails: '/members/cards',
  IntegralGoods: '/members/recharges',
  exchangeList: '/members/exchange-records',
  memberDetailsVip: '/members/period-cards',
  vipPrivilegeVip: '/members/privileges',
  couponPoints: '/coupon-points',
  coupon: '/coupons/maker',
  couponissue: '/coupons/issues',
  couponuser: '/coupons/users',
  timedGiftCoupon: '/coupons/activities',
  couponFk: '/coupons/card-manager',
  couponTg: '/coupons/verify',
  paihang: '/rankings/open',
  xiaofei: '/rankings/consume',
  loveRecord: '/rankings/love-wallet',
  materialsList: '/content/help-center',
  help: '/content/help-center',
  citys: '/content/cities',
  city: '/content/cities',
  'content/decorations': '/content/decorations',
  'content/help-center': '/content/help-center',
  'content/cities': '/content/cities',
  app: '/system/upgrades',
  appinfo: '/system/upgrades',
  allocationList: '/system/configs',
  common: '/system/configs',
  'sys/user': '/system/admins',
  'sys/role': '/system/roles',
  'sys/menu': '/system/menus',
  'system/menus': '/system/menus',
  'system/roles': '/system/roles',
  'system/configs': '/system/configs',
  'system/admins': '/system/admins',
  'pet/stores': '/pet/stores',
  'pet/cities': '/pet/cities',
  'pet/home-social': '/pet/home-social',
  'pet/archives': '/pet/archives',
  'pet/breeds': '/pet/breeds',
  'pet/adopt': '/pet/adopt',
  'pet/community': '/pet/community',
  'pet/community/complaints': '/pet/community/complaints',
  'pet/community/complaint-types': '/pet/community/complaint-types',
  'pet/sensitive': '/pet/sensitive',
  'pet/mall': '/pet/mall',
  'pet/mall/categories': '/pet/mall/categories',
  'pet/mall/specs': '/pet/mall/specs',
  'pet/orders': '/pet/orders',
  'pet/feed': '/pet/feed',
  'pet/coupon': '/pet/coupon',
  'pet/coupon-grant': '/pet/coupon-grant',
  'pet/exchange': '/pet/exchange',
  'pet/tasks': '/pet/tasks',
  'pet/ranking': '/pet/ranking',
  'pet/strategy': '/pet/strategy',
  'pet/strategy/categories': '/pet/strategy/categories',
  'pet/market': '/pet/market',
  'pet/island-pets': '/pet/island-pets',
  'pet/messages': '/pet/messages',
}

export function mapMenuUrlToPath(url) {
  if (!url) return ''
  if (routePathMap[url]) return routePathMap[url]
  if (url.startsWith('pet/') || url.startsWith('system/') || url.startsWith('content/')) {
    return `/${url}`
  }
  return ''
}

export function buildMissingPagePath(menuUrl = '', title = '') {
  const query = new URLSearchParams({
    menuUrl,
    title,
  })
  return `/404?${query.toString()}`
}

export function ensureMenuIndexes(menuList = []) {
  return flattenSinglePageDirectories(
    menuList.map((item) => ({
      ...item,
      index: item.index || item.path || `menu-${item.id}`,
      children: ensureMenuIndexes(item.children || []),
    })),
  )
}

/** 将「用户管理」这类仅含一个页面子项的目录展平为可直接点击的菜单项 */
function flattenSinglePageDirectories(menuList = []) {
  return menuList.flatMap((item) => {
    const children = item.children || []
    const pageChildren = children.filter((child) => child.path)
    const isUserManageDir =
      !item.path &&
      pageChildren.length === 1 &&
      (item.title === '用户管理' || pageChildren[0].path === '/users')

    if (isUserManageDir) {
      const only = pageChildren[0]
      return [{
        ...only,
        id: item.id || only.id,
        title: item.title || only.title,
        icon: item.icon || only.icon,
        index: only.path || only.index,
        children: [],
      }]
    }

    return [item]
  })
}

export function normalizeMenuTree(menuList = []) {
  return menuList
    .map((item) => {
      const menuType = Number(item.type)
      const routePath = menuType === 1
        ? mapMenuUrlToPath(item.url) || buildMissingPagePath(item.url, item.name)
        : ''
      return {
        id: item.menuId,
        title: item.name,
        icon: item.icon,
        type: menuType,
        perms: item.perms || '',
        path: routePath,
        index: routePath || `menu-${item.menuId}`,
        children: normalizeMenuTree(item.list || item.children || []),
      }
    })
    .filter((item) => item.type !== 2)
}
