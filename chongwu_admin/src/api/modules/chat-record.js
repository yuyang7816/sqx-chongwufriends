import { get, post, resolveBusinessResponse } from '@/api/http'

export function getChatRecordPageApi(params = {}) {
  return get('admin/question/getChatList', { ...params, type: 1 })
    .then(resolveBusinessResponse)
    .then((data) => ({
      list: data?.records || data?.list || [],
      total: Number(data?.total || data?.totalCount || 0),
    }))
}

export function createChatRecordApi(data) {
  return post('admin/question/saveChat', data).then(resolveBusinessResponse)
}

export function updateChatRecordApi(data) {
  return post('admin/question/updateChat', data).then(resolveBusinessResponse)
}

export function deleteChatRecordApi(chatId) {
  return post('admin/question/deleteChat', null, { params: { chatId } }).then(resolveBusinessResponse)
}
