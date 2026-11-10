import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getNavApi, loginApi, logoutApi } from '@/api/modules/auth'
import { clearPermissions, ensureMenuIndexes, normalizeMenuTree, normalizePermissions, setPermissions } from '@/utils/tool'

const STORAGE_KEY = 'admin-auth'

function getStoredAuth() {
  if (typeof window === 'undefined') {
    return null
  }
  const raw = window.sessionStorage.getItem(STORAGE_KEY)
  return raw ? JSON.parse(raw) : null
}

export const useAuthStore = defineStore('auth', () => {
  const stored = getStoredAuth()
  const token = ref(stored?.token || '')
  const refreshToken = ref(stored?.refreshToken || '')
  const expiresAt = ref(stored?.expiresAt || 0)
  const profile = ref(stored?.profile || { name: '', roleCode: '', roleName: '', email: '' })
  const permissions = ref(stored?.permissions || [])
  // 先用缓存渲染，进入路由后必须重新拉 nav，避免菜单/权限变更后侧栏仍显示旧数据
  const menuList = ref(ensureMenuIndexes(stored?.menuList || []))
  const navLoaded = ref(false)
  const isLoggedIn = computed(() => Boolean(token.value) && expiresAt.value > Date.now())

  function persist() {
    if (token.value) {
      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
        token: token.value,
        refreshToken: refreshToken.value,
        expiresAt: expiresAt.value,
        profile: profile.value,
        permissions: permissions.value,
        menuList: menuList.value,
      }))
    } else {
      window.sessionStorage.removeItem(STORAGE_KEY)
    }
  }

  function applyStoredAuth(cached) {
    token.value = cached.token
    refreshToken.value = cached.refreshToken
    expiresAt.value = cached.expiresAt
    profile.value = cached.profile
    permissions.value = cached.permissions || []
    menuList.value = ensureMenuIndexes(cached.menuList || [])
    // 不信任本地缓存为最终菜单，强制走一次 fetchNavData
    navLoaded.value = false
    setPermissions(permissions.value)
  }

  async function fetchNavData() {
    const navData = await getNavApi()
    permissions.value = normalizePermissions(navData.permissions, navData.menuList || [])
    menuList.value = ensureMenuIndexes(normalizeMenuTree(navData.menuList || []))
    navLoaded.value = true
    setPermissions(permissions.value)
    persist()
    return navData
  }

  async function restore() {
    const cached = getStoredAuth()
    if (!cached || !cached.token || cached.expiresAt <= Date.now()) {
      await logout()
      return false
    }
    applyStoredAuth(cached)
    try {
      await fetchNavData()
    } catch (error) {
      if (!navLoaded.value) {
        throw error
      }
    }
    return true
  }

  async function login(form) {
    const data = await loginApi(form)
    token.value = data.token
    refreshToken.value = data.refreshToken || ''
    expiresAt.value = data.expiresAt || Date.now() + 2 * 60 * 60 * 1000
    profile.value = data.profile || { name: form.username, roleCode: '', roleName: '', email: '' }
    permissions.value = []
    menuList.value = []
    navLoaded.value = false
    persist()
    const navData = await fetchNavData()
    return { ...data, permissions: navData.permissions || permissions.value, menuList: menuList.value }
  }

  async function logout() {
    const cachedToken = token.value
    try {
      if (cachedToken) {
        await logoutApi()
      }
    } catch (error) {
      // 退出接口失败不阻塞本地清态
    }
    token.value = ''
    refreshToken.value = ''
    expiresAt.value = 0
    profile.value = { name: '', roleCode: '', roleName: '', email: '' }
    permissions.value = []
    menuList.value = []
    navLoaded.value = false
    clearPermissions()
    persist()
  }

  function hasPermission(permission) {
    if (!permission) return true
    const list = Array.isArray(permissions.value) ? permissions.value : []
    return list.includes(permission)
  }

  return { token, refreshToken, expiresAt, profile, permissions, menuList, navLoaded, isLoggedIn, restore, login, logout, fetchNavData, hasPermission }
})
