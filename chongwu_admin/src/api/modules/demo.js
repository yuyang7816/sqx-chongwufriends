import http from '@/api/http'

export function getDemoHealth() {
  return http({
    url: '/health',
    method: 'get',
  })
}
