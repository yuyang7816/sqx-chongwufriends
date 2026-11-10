import { get, post, resolveBusinessResponse } from '@/api/http'

export const authApi = {
  login: '/sys/login',
  nav: '/sys/menu/nav',
  logout: '/sys/logout',
  refreshToken: '/replace-with-java/auth/refresh-token',
}

export function loginApi(data) {
  return post(authApi.login, {
    username: data.username,
    password: data.password,
    uuid: data.uuid,
    captcha: data.captcha,
  }, {
    silentError: true,
  }).then(resolveBusinessResponse).then((response) => {
    const token = response?.token || response?.accessToken || ''
    const expire = Number(response?.expire || 0)
    if (!token) {
      return Promise.reject(new Error('登录成功但未返回 token'))
    }
    return {
      token,
      refreshToken: response?.refreshToken || '',
      expiresAt: Date.now() + (expire > 0 ? expire * 1000 : 2 * 60 * 60 * 1000),
      profile: response?.user || response?.userInfo || {
        name: data.username,
        roleCode: '',
        roleName: '',
        email: '',
      },
      permissions: response?.permissions || [],
      raw: response,
    }
  })
}

export function logoutApi() {
  return post(authApi.logout, {}, { silentError: true }).catch(() => true)
}

export function getNavApi() {
  return get(authApi.nav, {}, { silentError: true }).then(resolveBusinessResponse)
}
