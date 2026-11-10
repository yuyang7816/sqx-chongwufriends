import { get, post, resolveBusinessResponse } from '@/api/http'

function normalizeResult(result) {
  return result?.data || result?.date || result || {}
}

function normalizePageData(data) {
  const source = normalizeResult(data)
  return {
    list: source?.list || source?.records || [],
    records: source?.records || source?.list || [],
    total: Number(source?.totalCount || source?.total || 0),
    totalCount: Number(source?.totalCount || source?.total || 0),
    ...source,
  }
}

function resolveFlexibleResponse(result) {
  if (result?.code === 0 || result?.code === 200 || result?.status === 0 || result?.status === 200) {
    return normalizeResult(result)
  }
  return resolveBusinessResponse(result)
}

export function getMerchantCourseOverviewApi(params = {}) {
  return get('studentCourse/selectShopCount', params).then(resolveFlexibleResponse)
}

export function getMerchantCourseStatsByTimeApi(params = {}) {
  return get('studentCourse/selectShopCountByTime', params).then(resolveFlexibleResponse)
}

export function getMerchantCoursePageApi(params = {}) {
  return get('course/selectCourseList', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function createMerchantCourseApi(data = {}) {
  return post('course/insertCourse', data).then(resolveBusinessResponse)
}

export function updateMerchantCourseApi(data = {}) {
  return post('course/updateCourse', data).then(resolveBusinessResponse)
}

export function deleteMerchantCourseApi(courseId) {
  return post('course/deleteCourse', null, { params: { courseId } }).then(resolveBusinessResponse)
}

export function getMerchantCoachPageApi(params = {}) {
  return get('coach/selectCoachList', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function createMerchantCoachApi(data = {}) {
  return post('coach/insertCoach', data).then(resolveBusinessResponse)
}

export function updateMerchantCoachApi(data = {}) {
  return post('coach/updateCoach', data).then(resolveBusinessResponse)
}

export function deleteMerchantCoachApi(params = {}) {
  return post('user/deleteTearoomUser', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantCoachSummaryApi(params = {}) {
  return get('coach/selectCoachDataCount', params).then(resolveFlexibleResponse)
}

export function getMerchantCoachRevenueApi(params = {}) {
  return get('coach/selectShopCoachDataCount', params).then(resolveFlexibleResponse)
}

export function getMerchantCoachCourseRevenueApi(params = {}) {
  return get('courseTime/selectCourseTimeByCoachIdAndTime', params).then(resolveFlexibleResponse)
}

export function getMerchantStudentPageApi(params = {}) {
  return get('student/selectStudentList', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function createMerchantStudentApi(data = {}) {
  return post('student/insertStudent', data).then(resolveBusinessResponse)
}

export function updateMerchantStudentApi(data = {}) {
  return post('student/updateStudent', data).then(resolveBusinessResponse)
}

export function getMerchantStudentCoursePageApi(params = {}) {
  return get('studentCourse/selectStudentCourse', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function createMerchantStudentCourseApi(data = {}) {
  return post('studentCourse/insertStudentCourse', data).then(resolveBusinessResponse)
}

export function deleteMerchantStudentCourseApi(studentCourseId) {
  return post('studentCourse/deleteStudentCourse', null, { params: { studentCourseId } }).then(resolveBusinessResponse)
}

export function updateMerchantStudentCourseNumApi(params = {}) {
  return post('studentCourse/updateStudentCourseNum', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantCourseTimePageApi(params = {}) {
  return get('courseTime/selectCourseTimeList', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function createMerchantCourseTimeApi(params = {}) {
  return post('courseTime/insertCourseTime', null, { params }).then(resolveBusinessResponse)
}

export function updateMerchantCourseTimeApi(data = {}) {
  return post('courseTime/updateCourseTime', data).then(resolveBusinessResponse)
}

export function deleteMerchantCourseTimeApi(courseTimeId) {
  return post('courseTime/deleteCourseTime', null, { params: { courseTimeId } }).then(resolveBusinessResponse)
}

export function getMerchantCourseTimeStudentsApi(params = {}) {
  return get('studentCourseTime/selectStudentCourseTime', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function updateMerchantStudentCourseTimeApi(data = {}) {
  return post('studentCourseTime/updateStudentCourseTime', data).then(resolveBusinessResponse)
}

export function getMerchantCourseOrderPageApi(params = {}) {
  return get('courseOrders/selectCourseOrdersList', params).then((result) => normalizePageData(resolveFlexibleResponse(result)))
}

export function refundMerchantCourseOrderApi(params = {}) {
  return post('courseOrders/refundCourseOrders', null, { params }).then(resolveBusinessResponse)
}

export function getMerchantCourseOrderDetailApi(params = {}) {
  return get('courseOrders/selectCourseOrdersDetails', params).then(resolveFlexibleResponse)
}
