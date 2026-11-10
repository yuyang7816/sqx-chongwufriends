import { reactive } from 'vue'
import { useRoute } from 'vue-router'

/**
 * 从 URL query 预填手机号/用户名筛选（禁止 userId）
 */
export function useRouteUserFilter() {
  const route = useRoute()
  const filters = reactive({
    phone: String(route.query.phone || '').trim(),
    username: String(route.query.username || route.query.userName || '').trim(),
  })

  function appendUserFilter(params = {}) {
    const next = { ...params }
    if (filters.phone) next.phone = filters.phone
    if (filters.username) next.username = filters.username
    return next
  }

  return { filters, appendUserFilter }
}

export function buildUserJumpQuery(user = {}) {
  const query = {}
  if (user.phone) query.phone = user.phone
  else if (user.userName || user.username) query.username = user.userName || user.username
  return query
}
