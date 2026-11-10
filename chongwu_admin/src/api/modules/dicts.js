import { get, post, resolveBusinessResponse } from '@/api/http'

export function getDictPage(params = {}) {
  return get('sys/dict/list', {
    page: params.page || 1,
    limit: params.limit || 10,
    ...params,
  })
    .then(resolveBusinessResponse)
    .then((data) => {
      const page = data?.page || data
      return {
        list: page?.list || [],
        total: Number(page?.totalCount || page?.total || 0),
      }
    })
}

export function createDictApi(data) {
  return post('sys/dict/save', data).then(resolveBusinessResponse)
}

export function updateDictApi(id, data) {
  return post('sys/dict/update', { ...data, id }).then(resolveBusinessResponse)
}

export function deleteDictApi(id) {
  return post('sys/dict/delete', [id]).then(resolveBusinessResponse)
}
