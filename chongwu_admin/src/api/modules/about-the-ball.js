import { get, post, resolveBusinessResponse } from '@/api/http'

function normalizePageData(data) {
  return {
    list: data?.list || data?.records || [],
    total: Number(data?.totalCount || data?.total || 0),
  }
}

export function getAboutBallPageApi(params = {}) {
  return get('dateBall/selectDateBallList', params)
    .then(resolveBusinessResponse)
    .then(normalizePageData)
}

export function getAboutBallPeoplePageApi(params = {}) {
  return get('dateBall/selectDateBallPeopleList', params)
    .then(resolveBusinessResponse)
    .then(normalizePageData)
}

export function createAboutBallApi(data = {}) {
  return post('dateBall/insertDateBall', data).then(resolveBusinessResponse)
}

export function updateAboutBallApi(data = {}) {
  return post('dateBall/updateDateBall', data).then(resolveBusinessResponse)
}

export function deleteAboutBallApi(dateBallId) {
  return post('dateBall/deleteDateBall', null, { params: { dateBallId } }).then(resolveBusinessResponse)
}

export function getAboutBallUserPageApi(params = {}) {
  return get('user/selectUserList', params)
    .then(resolveBusinessResponse)
    .then(normalizePageData)
}

export function getAboutBallVenueOptionsApi() {
  return get('tearoom/selectTearoomList', { page: 1, limit: 1000, tearoomName: '', city: '', status: '' })
    .then(resolveBusinessResponse)
    .then((data) => data?.list || data?.records || [])
}

export function getAboutBallCommonOptionsApi(typeId) {
  return get(`common/type/${typeId}`)
    .then(resolveBusinessResponse)
    .then((data) => String(data?.value || '').split(',').map((item) => item.trim()).filter(Boolean))
}
