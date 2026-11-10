import { get, post, resolveBusinessResponse } from '@/api/http'

export function getMessageCenterListApi(params = {}) {
  return get('message/selectMessageByType', params).then(resolveBusinessResponse)
}

export function auditMessageApi(params = {}) {
  return post('message/auditMessage', null, { params }).then(resolveBusinessResponse)
}

export function sendMessageApi(params = {}) {
  return get('cash/sendMsg', params).then(resolveBusinessResponse)
}

export function cancelUserByMessageApi(userId, messageId) {
  return post(`user/deleteUserByUserId/${userId}`, null, { params: { messageId } }).then(resolveBusinessResponse)
}
