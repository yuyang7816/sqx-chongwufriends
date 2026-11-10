import { post, resolveBusinessResponse } from '@/api/http'

const PREFIX = 'admin/messages'

function toPageList(data) {
  return {
    list: data?.records || [],
    total: Number(data?.total || 0),
  }
}

export function getPetMessagePageApi(params = {}) {
  return post(`${PREFIX}/page`, {
    page: params.page || 1,
    limit: params.limit || 10,
    type: params.type,
    userId: params.userId,
    phone: params.phone,
    userName: params.userName || params.username,
    isRead: params.isRead,
  })
    .then(resolveBusinessResponse)
    .then(toPageList)
}

export function broadcastPetMessageApi(data = {}) {
  return post(`${PREFIX}/broadcast`, data).then(resolveBusinessResponse)
}

export function deletePetMessageApi(id) {
  return post(`${PREFIX}/delete`, { id }).then(resolveBusinessResponse)
}
