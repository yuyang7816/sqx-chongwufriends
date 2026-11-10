<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import PageTopbar from '@/components/common/PageTopbar.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import {
  createMerchantCoachApi,
  createMerchantCourseApi,
  createMerchantCourseTimeApi,
  createMerchantStudentApi,
  createMerchantStudentCourseApi,
  deleteMerchantCoachApi,
  deleteMerchantCourseApi,
  deleteMerchantCourseTimeApi,
  deleteMerchantStudentCourseApi,
  getMerchantCoachCourseRevenueApi,
  getMerchantCoachPageApi,
  getMerchantCoachRevenueApi,
  getMerchantCoachSummaryApi,
  getMerchantCourseOrderDetailApi,
  getMerchantCourseOrderPageApi,
  getMerchantCourseOverviewApi,
  getMerchantCoursePageApi,
  getMerchantCourseStatsByTimeApi,
  getMerchantCourseTimePageApi,
  getMerchantCourseTimeStudentsApi,
  getMerchantStudentCoursePageApi,
  getMerchantStudentPageApi,
  refundMerchantCourseOrderApi,
  updateMerchantCoachApi,
  updateMerchantCourseApi,
  updateMerchantCourseTimeApi,
  updateMerchantStudentApi,
  updateMerchantStudentCourseNumApi,
  updateMerchantStudentCourseTimeApi,
} from '@/api/modules/merchant-course'
import { getUserCenterPage } from '@/api/modules/user-center'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()

const QQ_MAP_KEY = 'PSPBZ-Q4IY3-ABH3Q-O4RTI-OEMVS-NUFIP'
const DEFAULT_LAT = 34.34281541842994
const DEFAULT_LNG = 108.93970884382725

function getToday() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeListData(data) {
  return {
    list: data?.list || data?.records || [],
    records: data?.records || data?.list || [],
    total: Number(data?.totalCount || data?.total || 0),
  }
}

function getImageUrl(data) {
  return String(data?.url || data?.data || data?.src || data?.path || data || '')
}

function buildCoachStr(coachList) {
  return coachList.map((item) => `${item.id}:${item.money}`).join(',')
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num : 0
}

const merchantId = computed(() => Number(route.params.id || route.query.storeId || 0))
const merchantName = computed(() => String(route.query.storeName || '门店课程'))
const activeTab = ref(String(route.query.tab || 'overview'))

const tabOptions = [
  { name: 'overview', label: '商家统计' },
  { name: 'course-stats', label: '课程统计' },
  { name: 'courses', label: '课程管理' },
  { name: 'coaches', label: '教练管理' },
  { name: 'students', label: '学员管理' },
  { name: 'schedule', label: '排课管理' },
  { name: 'orders', label: '订单管理' },
]

const yesNoOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
]

const courseStatusOptions = [
  { label: '全部', value: '' },
  { label: '上架', value: 1 },
  { label: '下架', value: 2 },
]

const scheduleStatusOptions = [
  { label: '全部', value: '' },
  { label: '待上课', value: 1 },
  { label: '上课中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
]

const studentCourseStatusOptions = [
  { label: '全部', value: '' },
  { label: '待上课', value: 1 },
  { label: '已上课', value: 2 },
  { label: '未上课', value: 3 },
  { label: '已签到', value: 4 },
]

const orderStatusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已退款', value: 3 },
]

const weekOptions = [
  { label: '周一', value: '1' },
  { label: '周二', value: '2' },
  { label: '周三', value: '3' },
  { label: '周四', value: '4' },
  { label: '周五', value: '5' },
  { label: '周六', value: '6' },
  { label: '周日', value: '7' },
]

const overviewLoading = ref(false)
const overviewData = ref({})
const courseStatsLoading = ref(false)
const courseStatsDate = reactive({
  startTime: `${getToday().slice(0, 8)}01`,
  endTime: getToday(),
})
const courseStatsData = ref({})

const courseLoading = ref(false)
const coursePage = ref(1)
const courseSize = ref(10)
const courseTotal = ref(0)
const courseList = ref([])
const courseFilters = reactive({
  courseName: '',
  status: '',
})

const coachLoading = ref(false)
const coachPage = ref(1)
const coachSize = ref(10)
const coachTotal = ref(0)
const coachList = ref([])
const coachFilters = reactive({
  coachName: '',
  phone: '',
  status: '',
})

const studentLoading = ref(false)
const studentPage = ref(1)
const studentSize = ref(10)
const studentTotal = ref(0)
const studentList = ref([])
const studentFilters = reactive({
  studentName: '',
})

const scheduleLoading = ref(false)
const schedulePage = ref(1)
const scheduleSize = ref(10)
const scheduleTotal = ref(0)
const scheduleList = ref([])
const scheduleFilters = reactive({
  courseName: '',
  coachName: '',
  status: '',
})

const orderLoading = ref(false)
const orderPage = ref(1)
const orderSize = ref(10)
const orderTotal = ref(0)
const orderList = ref([])
const orderPageTotalMoney = ref(0)
const orderFilters = reactive({
  status: '',
  courseName: '',
  coachPhone: '',
  userName: '',
  coachName: '',
  ordersNo: '',
  studentName: '',
})

const courseDialogVisible = ref(false)
const courseDialogSaving = ref(false)
const courseDialogTitle = ref('添加课程')
const courseUploadLoading = ref(false)
const courseMapDialogVisible = ref(false)
const mapContainerRef = ref()
const mapLoading = ref(false)
let qqMapScriptPromise = null
let qqMapInstance = null
let qqMapMarker = null

const courseForm = reactive({
  courseId: 0,
  courseName: '',
  coursePeopleNum: 1,
  courseAddress: '',
  courseNum: 1,
  courseMinute: '',
  coursePrice: '',
  courseContent: '',
  status: 1,
  latitude: '',
  longitude: '',
  courseImg: [],
})

const coachDialogVisible = ref(false)
const coachDialogSaving = ref(false)
const coachDialogTitle = ref('添加教练')
const coachUploadLoading = ref('')
const coachForm = reactive({
  coachId: 0,
  coachName: '',
  coachAge: '',
  phone: '',
  sex: 1,
  coachLabel: [],
  labelInput: '',
  coachAvatar: '',
  coachImages: '',
  introduction: '',
  experience: '',
  beGood: '',
  teachingImage: [],
  userId: 0,
  userName: '',
})

const studentDialogVisible = ref(false)
const studentDialogSaving = ref(false)
const studentDialogTitle = ref('添加学员')
const studentForm = reactive({
  studentId: 0,
  studentName: '',
  phone: '',
  sex: 1,
  age: '',
  avatar: '',
  status: 1,
  userId: 0,
  userName: '',
})

const studentCourseDialogVisible = ref(false)
const studentCourseLoading = ref(false)
const studentCoursePage = ref(1)
const studentCourseSize = ref(10)
const studentCourseTotal = ref(0)
const studentCourseList = ref([])
const currentStudent = ref({ studentId: 0, studentName: '' })

const coursePickerVisible = ref(false)
const coursePickerLoading = ref(false)
const coursePickerPage = ref(1)
const coursePickerSize = ref(10)
const coursePickerTotal = ref(0)
const coursePickerList = ref([])

const studentCourseNumDialogVisible = ref(false)
const studentCourseNumSaving = ref(false)
const studentCourseNumForm = reactive({
  studentCourseId: 0,
  courseNum: '',
  useCourseNum: '',
})

const scheduleFormDialogVisible = ref(false)
const scheduleFormSaving = ref(false)
const scheduleFormTitle = ref('添加排课')
const scheduleContext = reactive({
  type: 'all',
  coachId: 0,
  courseId: 0,
})
const scheduleForm = reactive({
  ruleType: 1,
  startDate: [],
  weekNums: '',
  startTime: '',
  endTime: '',
  courseId: '',
  num: '',
  coachId: '',
  money: '',
  coachNameList: [],
})

const scheduleEditDialogVisible = ref(false)
const scheduleEditSaving = ref(false)
const scheduleEditForm = reactive({
  courseTimeId: 0,
  courseId: 0,
  courseStartTime: '',
  courseEndTime: '',
  status: 1,
  money: '',
  coachId: '',
  coachNameList: [],
})

const scheduleStudentDialogVisible = ref(false)
const scheduleStudentLoading = ref(false)
const scheduleStudentPage = ref(1)
const scheduleStudentSize = ref(10)
const scheduleStudentTotal = ref(0)
const scheduleStudentList = ref([])
const scheduleStudentFilterStatus = ref('')
const currentCourseTimeId = ref(0)

const coachScheduleDialogVisible = ref(false)
const coachScheduleLoading = ref(false)
const coachSchedulePage = ref(1)
const coachScheduleSize = ref(10)
const coachScheduleTotal = ref(0)
const coachScheduleList = ref([])
const coachScheduleStatus = ref('')
const coachScheduleContext = reactive({
  type: 'coach',
  coachId: 0,
  courseId: 0,
  title: '',
})

const studentScheduleDialogVisible = ref(false)
const studentScheduleLoading = ref(false)
const studentSchedulePage = ref(1)
const studentScheduleSize = ref(10)
const studentScheduleTotal = ref(0)
const studentScheduleList = ref([])
const studentScheduleStatus = ref('')
const currentStudentCourseId = ref(0)

const coachRevenueDialogVisible = ref(false)
const coachRevenueLoading = ref(false)
const coachRevenueDate = reactive({
  startTime: `${getToday().slice(0, 8)}01`,
  endTime: getToday(),
})
const coachRevenueSummary = ref({})
const coachRevenueStats = ref({})
const coachRevenueCourseList = ref([])
const currentCoachId = ref(0)

const orderStudentDialogVisible = ref(false)
const orderStudentList = ref([])

const partialRefundDialogVisible = ref(false)
const partialRefundSaving = ref(false)
const partialRefundForm = reactive({
  ordersId: 0,
  refundMoney: '',
  maxRefundMoney: 0,
})

const userPickerVisible = ref(false)
const userPickerLoading = ref(false)
const userPickerPage = ref(1)
const userPickerSize = ref(10)
const userPickerTotal = ref(0)
const userPickerList = ref([])
const userPickerFilters = reactive({
  phone: '',
  userName: '',
})

const courseOptions = ref([])
const coachOptions = ref([])

const overviewGroups = computed(() => [
  {
    title: '商家统计',
    items: [
      ['收益概览', overviewData.value.money],
      ['学员统计', overviewData.value.studentCount],
      ['教练统计', overviewData.value.coachCount],
    ],
  },
  {
    title: '排课统计',
    items: [
      ['待上课', overviewData.value.courseTimeCount1],
      ['进行中', overviewData.value.courseTimeCount2],
      ['已完成', overviewData.value.courseTimeCount3],
    ],
  },
  {
    title: '订单统计',
    items: [
      ['待支付', overviewData.value.courseOrdersCount1],
      ['已支付', overviewData.value.courseOrdersCount2],
      ['已退款', overviewData.value.courseOrdersCount3],
    ],
  },
  {
    title: '课程统计',
    items: [
      ['上架中', overviewData.value.courseCount1],
      ['已下架', overviewData.value.courseCount2],
    ],
  },
  {
    title: '学员课程',
    items: [
      ['待上课', overviewData.value.studentCourseCount1],
      ['已上课', overviewData.value.studentCourseCount2],
      ['未上课', overviewData.value.studentCourseCount3],
    ],
  },
])

const courseStatsGroups = computed(() => [
  {
    title: '课程统计',
    items: [
      ['学员课程数', courseStatsData.value.studentCount],
      ['教练课程数', courseStatsData.value.coachCount],
      ['教练费用', courseStatsData.value.coachMoney],
    ],
  },
  {
    title: '课程状态统计',
    items: [
      ['待上课', courseStatsData.value.courseTimeCount1],
      ['上课中', courseStatsData.value.courseTimeCount2],
      ['已完成', courseStatsData.value.courseTimeCount3],
      ['已取消', courseStatsData.value.courseTimeCount4],
    ],
  },
  {
    title: '学员上课统计',
    items: [
      ['已上课', courseStatsData.value.studentCount2],
      ['待上课', courseStatsData.value.studentCount1],
    ],
  },
])

async function requestJsonp(url) {
  return new Promise((resolve, reject) => {
    const callbackName = `jsonp_callback_${Date.now()}_${Math.random().toString(36).slice(2)}`
    const separator = url.includes('?') ? '&' : '?'
    const script = document.createElement('script')
    window[callbackName] = (payload) => {
      cleanup()
      resolve(payload)
    }
    function cleanup() {
      delete window[callbackName]
      script.remove()
    }
    script.onerror = () => {
      cleanup()
      reject(new Error('地图服务请求失败'))
    }
    script.src = `${url}${separator}output=jsonp&callback=${callbackName}`
    document.body.appendChild(script)
  })
}

function waitForQQMapsReady(timeout = 20000) {
  const start = Date.now()
  return new Promise((resolve, reject) => {
    const check = () => {
      const maps = window.qq?.maps
      if (maps?.Map && maps?.LatLng) {
        resolve(maps)
        return
      }
      if (Date.now() - start >= timeout) {
        reject(new Error('腾讯地图脚本加载超时'))
        return
      }
      window.setTimeout(check, 100)
    }
    check()
  })
}

function ensureQQMaps() {
  if (window.qq?.maps?.Map && window.qq?.maps?.LatLng) return Promise.resolve(window.qq.maps)
  if (!qqMapScriptPromise) {
    qqMapScriptPromise = new Promise((resolve, reject) => {
      const existingScript = document.getElementById('qq-map-script')
      const script = existingScript || document.createElement('script')
      script.id = 'qq-map-script'
      script.src = `https://map.qq.com/api/js?v=2.exp&key=${QQ_MAP_KEY}`
      script.async = true
      const handleReady = async () => {
        try {
          const maps = await waitForQQMapsReady()
          resolve(maps)
        } catch (error) {
          reject(error)
        }
      }
      script.onload = handleReady
      script.onerror = () => reject(new Error('腾讯地图脚本加载失败'))
      if (!existingScript) {
        document.head.appendChild(script)
      } else {
        handleReady()
      }
    })
  }
  return qqMapScriptPromise
}

function createQQMapLatLng(lat, lng) {
  const maps = window.qq?.maps
  if (!maps?.LatLng) throw new Error('腾讯地图初始化失败')
  return new maps.LatLng(Number(lat), Number(lng))
}

function updateMapMarker(lat, lng) {
  const maps = window.qq?.maps
  if (!maps || !qqMapInstance) return
  const position = createQQMapLatLng(lat, lng)
  qqMapInstance.setCenter(position)
  if (!qqMapMarker) {
    qqMapMarker = new maps.Marker({ map: qqMapInstance, position })
    return
  }
  qqMapMarker.setPosition(position)
}

async function initCourseMap() {
  const maps = await ensureQQMaps()
  await nextTick()
  if (!mapContainerRef.value) return
  const latitude = Number(courseForm.latitude || DEFAULT_LAT)
  const longitude = Number(courseForm.longitude || DEFAULT_LNG)
  const center = createQQMapLatLng(latitude, longitude)
  qqMapInstance = new maps.Map(mapContainerRef.value, {
    center,
    zoom: 13,
    mapTypeId: maps.MapTypeId.ROADMAP,
  })
  qqMapMarker = new maps.Marker({ map: qqMapInstance, position: center })
  maps.event.addListener(qqMapInstance, 'click', async (event) => {
    courseForm.longitude = String(event.latLng.getLng())
    courseForm.latitude = String(event.latLng.getLat())
    updateMapMarker(courseForm.latitude, courseForm.longitude)
    const response = await requestJsonp(`https://apis.map.qq.com/ws/geocoder/v1/?location=${courseForm.latitude},${courseForm.longitude}&key=${QQ_MAP_KEY}&get_poi=1`)
    const result = response?.result
    if (result?.formatted_addresses?.recommend) {
      courseForm.courseAddress = result.formatted_addresses.recommend
    }
  })
}

async function locateCourseAddress() {
  if (!courseForm.courseAddress) {
    showError('请输入课程地址')
    return
  }
  mapLoading.value = true
  try {
    const response = await requestJsonp(`https://apis.map.qq.com/ws/geocoder/v1/?address=${encodeURIComponent(courseForm.courseAddress)}&key=${QQ_MAP_KEY}&get_poi=1`)
    const location = response?.result?.location
    if (!location) {
      showError('未找到对应坐标')
      return
    }
    courseForm.longitude = String(location.lng)
    courseForm.latitude = String(location.lat)
    if (courseMapDialogVisible.value) {
      await initCourseMap()
      updateMapMarker(courseForm.latitude, courseForm.longitude)
    }
  } finally {
    mapLoading.value = false
  }
}

watch(courseMapDialogVisible, async (visible) => {
  if (!visible) return
  try {
    await initCourseMap()
  } catch (error) {
    showError(error.message || '地图初始化失败')
  }
})

async function handleSingleUpload(field, option, target) {
  try {
    if (target === 'coach') coachUploadLoading.value = field
    if (target === 'course') courseUploadLoading.value = true
    const data = await uploadMerchantImageApi(option.file)
    const url = getImageUrl(data)
    if (target === 'coach') coachForm[field] = url
    if (target === 'student') studentForm[field] = url
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    if (target === 'coach') coachUploadLoading.value = ''
    if (target === 'course') courseUploadLoading.value = false
  }
}

async function handleArrayUpload(field, option, target) {
  try {
    if (target === 'coach') coachUploadLoading.value = field
    if (target === 'course') courseUploadLoading.value = true
    const data = await uploadMerchantImageApi(option.file)
    const url = getImageUrl(data)
    if (target === 'coach') coachForm[field].push(url)
    if (target === 'course') courseForm[field].push(url)
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    if (target === 'coach') coachUploadLoading.value = ''
    if (target === 'course') courseUploadLoading.value = false
  }
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    overviewData.value = await getMerchantCourseOverviewApi({ tearoomId: merchantId.value })
  } finally {
    overviewLoading.value = false
  }
}

async function loadCourseStats() {
  courseStatsLoading.value = true
  try {
    courseStatsData.value = await getMerchantCourseStatsByTimeApi({
      tearoomId: merchantId.value,
      startTime: courseStatsDate.startTime,
      endTime: courseStatsDate.endTime,
    })
  } finally {
    courseStatsLoading.value = false
  }
}

async function loadCourseOptions() {
  const data = await getMerchantCoursePageApi({ page: -1, limit: 100, tearoomId: merchantId.value, courseName: '', status: '' })
  courseOptions.value = data.list || []
}

async function loadCoachOptions() {
  const data = await getMerchantCoachPageApi({ page: -1, limit: 100, tearoomId: merchantId.value, coachName: '', phone: '', status: '' })
  coachOptions.value = data.list || []
}

async function loadCourses() {
  courseLoading.value = true
  try {
    const data = await getMerchantCoursePageApi({
      page: coursePage.value,
      limit: courseSize.value,
      tearoomId: merchantId.value,
      courseName: courseFilters.courseName.trim(),
      status: courseFilters.status,
    })
    courseList.value = data.list || []
    courseTotal.value = data.total || 0
  } finally {
    courseLoading.value = false
  }
}

async function loadCoaches() {
  coachLoading.value = true
  try {
    const data = await getMerchantCoachPageApi({
      page: coachPage.value,
      limit: coachSize.value,
      tearoomId: merchantId.value,
      coachName: coachFilters.coachName.trim(),
      phone: coachFilters.phone.trim(),
      status: coachFilters.status,
      userName: '',
    })
    coachList.value = data.list || []
    coachTotal.value = data.total || 0
  } finally {
    coachLoading.value = false
  }
}

async function loadStudents() {
  studentLoading.value = true
  try {
    const data = await getMerchantStudentPageApi({
      page: studentPage.value,
      limit: studentSize.value,
      tearoomId: merchantId.value,
      studentName: studentFilters.studentName.trim(),
      phone: '',
      status: '',
      userName: '',
    })
    studentList.value = data.records || data.list || []
    studentTotal.value = data.total || 0
  } finally {
    studentLoading.value = false
  }
}

async function loadSchedules() {
  scheduleLoading.value = true
  try {
    const data = await getMerchantCourseTimePageApi({
      page: schedulePage.value,
      limit: scheduleSize.value,
      tearoomId: merchantId.value,
      courseName: scheduleFilters.courseName.trim(),
      coachName: scheduleFilters.coachName.trim(),
      status: scheduleFilters.status,
    })
    scheduleList.value = data.list || []
    scheduleTotal.value = data.total || 0
  } finally {
    scheduleLoading.value = false
  }
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const data = await getMerchantCourseOrderPageApi({
      page: orderPage.value,
      limit: orderSize.value,
      tearoomId: merchantId.value,
      ordersNo: orderFilters.ordersNo.trim(),
      userName: orderFilters.userName.trim(),
      studentName: orderFilters.studentName.trim(),
      courseName: orderFilters.courseName.trim(),
      coachName: orderFilters.coachName.trim(),
      coachPhone: orderFilters.coachPhone.trim(),
      status: orderFilters.status,
    })
    orderList.value = data.list || []
    orderTotal.value = data.total || 0
    orderPageTotalMoney.value = orderList.value.reduce((sum, item) => sum + formatMoney(item.payMoney || item.payment), 0)
  } finally {
    orderLoading.value = false
  }
}

async function loadStudentCourses() {
  studentCourseLoading.value = true
  try {
    const data = await getMerchantStudentCoursePageApi({
      page: studentCoursePage.value,
      limit: studentCourseSize.value,
      tearoomId: merchantId.value,
      studentId: currentStudent.value.studentId,
    })
    studentCourseList.value = data.list || []
    studentCourseTotal.value = data.total || 0
  } finally {
    studentCourseLoading.value = false
  }
}

async function loadCoursePicker() {
  coursePickerLoading.value = true
  try {
    const data = await getMerchantCoursePageApi({
      page: coursePickerPage.value,
      limit: coursePickerSize.value,
      tearoomId: merchantId.value,
    })
    coursePickerList.value = data.list || []
    coursePickerTotal.value = data.total || 0
  } finally {
    coursePickerLoading.value = false
  }
}

async function loadScheduleStudents() {
  scheduleStudentLoading.value = true
  try {
    const data = await getMerchantCourseTimeStudentsApi({
      page: scheduleStudentPage.value,
      limit: scheduleStudentSize.value,
      courseTimeId: currentCourseTimeId.value,
      status: scheduleStudentFilterStatus.value,
    })
    scheduleStudentList.value = data.records || data.list || []
    scheduleStudentTotal.value = data.total || 0
  } finally {
    scheduleStudentLoading.value = false
  }
}

async function loadCoachScheduleList() {
  coachScheduleLoading.value = true
  try {
    const data = await getMerchantCourseTimePageApi({
      page: coachSchedulePage.value,
      limit: coachScheduleSize.value,
      tearoomId: merchantId.value,
      coachId: coachScheduleContext.coachId || undefined,
      courseId: coachScheduleContext.courseId || undefined,
      status: coachScheduleStatus.value,
      coachName: '',
    })
    coachScheduleList.value = data.list || []
    coachScheduleTotal.value = data.total || 0
  } finally {
    coachScheduleLoading.value = false
  }
}

async function loadStudentScheduleList() {
  studentScheduleLoading.value = true
  try {
    const data = await getMerchantCourseTimeStudentsApi({
      studentId: currentStudent.value.studentId,
      page: studentSchedulePage.value,
      limit: studentScheduleSize.value,
      coachName: '',
      status: studentScheduleStatus.value,
      studentCourseId: currentStudentCourseId.value,
    })
    studentScheduleList.value = data.records || data.list || []
    studentScheduleTotal.value = data.total || 0
  } finally {
    studentScheduleLoading.value = false
  }
}

async function loadCoachRevenue() {
  coachRevenueLoading.value = true
  try {
    const [summary, stats, courseListData] = await Promise.all([
      getMerchantCoachSummaryApi({ coachId: currentCoachId.value }),
      getMerchantCoachRevenueApi({ coachId: currentCoachId.value, startTime: coachRevenueDate.startTime, endTime: coachRevenueDate.endTime }),
      getMerchantCoachCourseRevenueApi({ coachId: currentCoachId.value, startTime: coachRevenueDate.startTime, endTime: coachRevenueDate.endTime }),
    ])
    coachRevenueSummary.value = summary || {}
    coachRevenueStats.value = stats || {}
    coachRevenueCourseList.value = courseListData || []
  } finally {
    coachRevenueLoading.value = false
  }
}

async function loadUserPicker() {
  userPickerLoading.value = true
  try {
    const data = await getUserCenterPage({
      page: userPickerPage.value,
      limit: userPickerSize.value,
      phone: userPickerFilters.phone.trim(),
      userName: userPickerFilters.userName.trim(),
      invitationCode: '',
    })
    userPickerList.value = data.list || []
    userPickerTotal.value = data.total || 0
  } finally {
    userPickerLoading.value = false
  }
}

function goBack() {
  router.push('/merchants')
}

function syncTabQuery() {
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: activeTab.value,
    },
  })
}

function handleTabChange() {
  syncTabQuery()
  if (activeTab.value === 'overview') loadOverview()
  if (activeTab.value === 'course-stats') loadCourseStats()
  if (activeTab.value === 'courses') loadCourses()
  if (activeTab.value === 'coaches') loadCoaches()
  if (activeTab.value === 'students') loadStudents()
  if (activeTab.value === 'schedule') loadSchedules()
  if (activeTab.value === 'orders') loadOrders()
}

function resetCourseForm() {
  Object.assign(courseForm, {
    courseId: 0,
    courseName: '',
    coursePeopleNum: 1,
    courseAddress: '',
    courseNum: 1,
    courseMinute: '',
    coursePrice: '',
    courseContent: '',
    status: 1,
    latitude: '',
    longitude: '',
    courseImg: [],
  })
}

function openCourseDialog(row) {
  resetCourseForm()
  if (row) {
    courseDialogTitle.value = '修改课程'
    Object.assign(courseForm, {
      courseId: row.courseId,
      courseName: row.courseName || '',
      coursePeopleNum: row.coursePeopleNum || 1,
      courseAddress: row.courseAddress || '',
      courseNum: row.courseNum || 1,
      courseMinute: row.courseMinute || '',
      coursePrice: row.coursePrice || '',
      courseContent: row.courseContent || '',
      status: Number(row.status || 1),
      latitude: row.latitude || '',
      longitude: row.longitude || '',
      courseImg: row.courseImg ? String(row.courseImg).split(',').filter(Boolean) : [],
    })
  } else {
    courseDialogTitle.value = '添加课程'
  }
  courseDialogVisible.value = true
}

async function submitCourse() {
  if (!courseForm.courseName.trim()) return showError('请输入课程名称')
  if (!courseForm.courseAddress.trim()) return showError('请输入课程地址')
  courseDialogSaving.value = true
  try {
    const payload = {
      tearoomId: merchantId.value,
      courseId: courseForm.courseId || undefined,
      courseName: courseForm.courseName.trim(),
      coursePeopleNum: courseForm.coursePeopleNum,
      courseAddress: courseForm.courseAddress.trim(),
      courseNum: courseForm.courseNum,
      courseMinute: courseForm.courseMinute,
      coursePrice: courseForm.coursePrice,
      courseContent: courseForm.courseContent,
      status: Number(courseForm.status || 1),
      latitude: courseForm.latitude,
      longitude: courseForm.longitude,
      courseImg: courseForm.courseImg.join(','),
    }
    if (courseForm.courseId) {
      await updateMerchantCourseApi(payload)
    } else {
      delete payload.courseId
      await createMerchantCourseApi(payload)
    }
    showSuccess('操作成功')
    courseDialogVisible.value = false
    await loadCourses()
    await loadCourseOptions()
  } finally {
    courseDialogSaving.value = false
  }
}

async function toggleCourseStatus(row, status) {
  try {
    await updateMerchantCourseApi({
      tearoomId: merchantId.value,
      courseId: row.courseId,
      status,
      courseName: row.courseName,
      coursePeopleNum: row.coursePeopleNum,
      courseAddress: row.courseAddress,
      courseNum: row.courseNum,
      coursePrice: row.coursePrice,
      courseContent: row.courseContent,
      latitude: row.latitude,
      longitude: row.longitude,
      courseMinute: row.courseMinute,
      courseImg: row.courseImg,
    })
    showSuccess('操作成功')
  } catch (error) {
    row.status = status === 1 ? 2 : 1
    throw error
  } finally {
    loadCourses()
  }
}

async function removeCourse(row) {
  await confirmAction('确定删除此课程?')
  await deleteMerchantCourseApi(row.courseId)
  showSuccess('删除成功')
  await loadCourses()
}

function resetCoachForm() {
  Object.assign(coachForm, {
    coachId: 0,
    coachName: '',
    coachAge: '',
    phone: '',
    sex: 1,
    coachLabel: [],
    labelInput: '',
    coachAvatar: '',
    coachImages: '',
    introduction: '',
    experience: '',
    beGood: '',
    teachingImage: [],
    userId: 0,
    userName: '',
  })
}

function openCoachDialog(row) {
  resetCoachForm()
  if (row) {
    coachDialogTitle.value = '修改教练'
    Object.assign(coachForm, {
      coachId: row.coachId,
      coachName: row.coachName || '',
      coachAge: row.coachAge || '',
      phone: row.phone || '',
      sex: Number(row.sex || 1),
      coachLabel: row.coachLabel ? String(row.coachLabel).split(',').filter(Boolean) : [],
      coachAvatar: row.coachAvatar || '',
      coachImages: row.coachImages || '',
      introduction: row.introduction || '',
      experience: row.experience || '',
      beGood: row.beGood || '',
      teachingImage: row.teachingImage ? String(row.teachingImage).split(',').filter(Boolean) : [],
      userId: Number(row.userId || 0),
      userName: row.userName || '',
    })
  } else {
    coachDialogTitle.value = '添加教练'
  }
  coachDialogVisible.value = true
}

function addCoachLabel() {
  const value = coachForm.labelInput.trim()
  if (!value) return
  coachForm.coachLabel.push(value)
  coachForm.labelInput = ''
}

async function submitCoach() {
  if (!coachForm.coachName.trim()) return showError('请输入教练名称')
  coachDialogSaving.value = true
  try {
    const payload = {
      tearoomId: merchantId.value,
      coachId: coachForm.coachId || undefined,
      coachName: coachForm.coachName.trim(),
      coachAge: coachForm.coachAge,
      phone: coachForm.phone.trim(),
      sex: Number(coachForm.sex || 1),
      coachLabel: coachForm.coachLabel.join(','),
      coachAvatar: coachForm.coachAvatar,
      experience: coachForm.experience,
      beGood: coachForm.beGood,
      introduction: coachForm.introduction,
      coachImages: coachForm.coachImages,
      teachingImage: coachForm.teachingImage.join(','),
      userId: coachForm.userId || 0,
    }
    if (coachForm.coachId) {
      await updateMerchantCoachApi(payload)
    } else {
      delete payload.coachId
      await createMerchantCoachApi(payload)
    }
    showSuccess('操作成功')
    coachDialogVisible.value = false
    await loadCoaches()
    await loadCoachOptions()
  } finally {
    coachDialogSaving.value = false
  }
}

async function removeCoach(row) {
  await confirmAction('确定删除此教练?')
  await deleteMerchantCoachApi({ userId: row.userId, tearoomId: merchantId.value })
  showSuccess('删除成功')
  await loadCoaches()
}

function resetStudentForm() {
  Object.assign(studentForm, {
    studentId: 0,
    studentName: '',
    phone: '',
    sex: 1,
    age: '',
    avatar: '',
    status: 1,
    userId: 0,
    userName: '',
  })
}

function openStudentDialog(row) {
  resetStudentForm()
  if (row) {
    studentDialogTitle.value = '修改学员'
    Object.assign(studentForm, {
      studentId: row.studentId,
      studentName: row.studentName || '',
      phone: row.phone || '',
      sex: Number(row.sex || 1),
      age: row.age || '',
      avatar: row.avatar || row.coachAvatar || '',
      status: Number(row.status || 1),
      userId: Number(row.userId || 0),
      userName: row.userName || '',
    })
  } else {
    studentDialogTitle.value = '添加学员'
  }
  studentDialogVisible.value = true
}

async function submitStudent() {
  if (!studentForm.studentName.trim()) return showError('学员名称不能为空')
  if (!studentForm.phone.trim()) return showError('电话不能为空')
  studentDialogSaving.value = true
  try {
    const payload = {
      studentId: studentForm.studentId || undefined,
      studentName: studentForm.studentName.trim(),
      phone: studentForm.phone.trim(),
      sex: Number(studentForm.sex || 1),
      age: studentForm.age,
      avatar: studentForm.avatar,
      status: Number(studentForm.status || 1),
      userId: studentForm.userId || 0,
      studentCourseList: [],
      tearoomId: merchantId.value,
    }
    if (studentForm.studentId) {
      await updateMerchantStudentApi(payload)
    } else {
      delete payload.studentId
      await createMerchantStudentApi(payload)
    }
    showSuccess('操作成功')
    studentDialogVisible.value = false
    await loadStudents()
  } finally {
    studentDialogSaving.value = false
  }
}

function openStudentCourses(row) {
  currentStudent.value = { studentId: row.studentId, studentName: row.studentName || '' }
  studentCoursePage.value = 1
  studentCourseDialogVisible.value = true
  loadStudentCourses()
}

function openCoursePicker() {
  coursePickerPage.value = 1
  coursePickerVisible.value = true
  loadCoursePicker()
}

async function bindStudentCourse(row) {
  await createMerchantStudentCourseApi({
    tearoomId: merchantId.value,
    courseId: row.courseId,
    studentId: currentStudent.value.studentId,
    courseNum: row.courseNum,
    useCourseNum: row.useCourseNum,
    status: row.status,
  })
  showSuccess('操作成功')
  coursePickerVisible.value = false
  await loadStudentCourses()
}

async function removeStudentCourse(row) {
  await confirmAction('确定删除学员课程?')
  await deleteMerchantStudentCourseApi(row.studentCourseId)
  showSuccess('删除成功')
  await loadStudentCourses()
}

function openStudentCourseNumDialog(row) {
  Object.assign(studentCourseNumForm, {
    studentCourseId: row.studentCourseId,
    courseNum: row.courseNum,
    useCourseNum: row.useCourseNum,
  })
  studentCourseNumDialogVisible.value = true
}

async function submitStudentCourseNum() {
  studentCourseNumSaving.value = true
  try {
    await updateMerchantStudentCourseNumApi({
      studentCourseId: studentCourseNumForm.studentCourseId,
      courseNum: studentCourseNumForm.courseNum,
      useCourseNum: studentCourseNumForm.useCourseNum,
    })
    showSuccess('修改成功')
    studentCourseNumDialogVisible.value = false
    await loadStudentCourses()
  } finally {
    studentCourseNumSaving.value = false
  }
}

function openStudentSchedule(row) {
  currentStudentCourseId.value = row.studentCourseId
  studentSchedulePage.value = 1
  studentScheduleStatus.value = ''
  studentScheduleDialogVisible.value = true
  loadStudentScheduleList()
}

async function toggleStudentScheduleStatus(row) {
  await updateMerchantStudentCourseTimeApi({
    studentCourseTimeId: row.studentCourseTimeId,
    status: row.status,
  })
  showSuccess('操作成功')
  await loadStudentScheduleList()
}

function resetScheduleForm() {
  Object.assign(scheduleForm, {
    ruleType: 1,
    startDate: [],
    weekNums: '',
    startTime: '',
    endTime: '',
    courseId: '',
    num: '',
    coachId: '',
    money: '',
    coachNameList: [],
  })
}

async function openScheduleDialog(type = 'all', row = null) {
  scheduleContext.type = type
  scheduleContext.coachId = type === 'coach' ? Number(row?.coachId || 0) : 0
  scheduleContext.courseId = type === 'course' ? Number(row?.courseId || 0) : 0
  scheduleFormTitle.value = '添加排课'
  resetScheduleForm()
  if (type === 'course') scheduleForm.courseId = scheduleContext.courseId
  await Promise.all([loadCourseOptions(), loadCoachOptions()])
  scheduleFormDialogVisible.value = true
}

function addScheduleCoach() {
  if (!scheduleForm.coachId) return showError('请选择教练')
  if (!scheduleForm.money) return showError('请输入教练收入')
  const target = coachOptions.value.find((item) => Number(item.coachId) === Number(scheduleForm.coachId))
  if (!target) return
  scheduleForm.coachNameList.push({
    id: target.coachId,
    name: target.coachName,
    money: scheduleForm.money,
  })
  scheduleForm.coachId = ''
  scheduleForm.money = ''
}

async function submitSchedule() {
  const dateTime = scheduleForm.ruleType === 1 ? [].concat(scheduleForm.startDate || []).filter(Boolean).join(',') : scheduleForm.startDate
  if (!scheduleForm.startTime || !scheduleForm.endTime) return showError('请选择上课和下课时间')
  const coachNameList = [...scheduleForm.coachNameList]
  if (scheduleContext.type === 'coach') {
    if (!scheduleForm.money) return showError('收入不能为空')
    coachNameList.push({ id: scheduleContext.coachId, money: scheduleForm.money })
  }
  if (!coachNameList.length) return showError('请添加教练信息')
  scheduleFormSaving.value = true
  try {
    await createMerchantCourseTimeApi({
      tearoomId: merchantId.value,
      courseId: scheduleContext.type === 'coach' ? scheduleForm.courseId : (scheduleForm.courseId || scheduleContext.courseId),
      type: scheduleForm.ruleType,
      dateTime,
      weekNums: scheduleForm.weekNums,
      num: scheduleForm.ruleType === 2 ? scheduleForm.num : '',
      coachStr: buildCoachStr(coachNameList),
      courseTime: `${scheduleForm.startTime}-${scheduleForm.endTime}`,
      courseTimeId: '',
    })
    showSuccess('操作成功')
    scheduleFormDialogVisible.value = false
    if (activeTab.value === 'schedule') await loadSchedules()
    if (coachScheduleDialogVisible.value) await loadCoachScheduleList()
  } finally {
    scheduleFormSaving.value = false
  }
}

function openScheduleEditDialog(row) {
  Object.assign(scheduleEditForm, {
    courseTimeId: row.courseTimeId,
    courseId: row.courseId,
    courseStartTime: row.courseStartTime,
    courseEndTime: row.courseEndTime,
    status: Number(row.status || 1),
    money: '',
    coachId: '',
    coachNameList: (row.courseTimeCoachList || []).map((item) => ({
      id: item.coachId,
      name: item.coachName,
      money: item.money,
    })),
  })
  scheduleEditDialogVisible.value = true
  loadCoachOptions()
}

function addScheduleEditCoach() {
  if (!scheduleEditForm.coachId) return showError('请选择教练')
  if (!scheduleEditForm.money) return showError('请输入教练收入')
  const target = coachOptions.value.find((item) => Number(item.coachId) === Number(scheduleEditForm.coachId))
  if (!target) return
  scheduleEditForm.coachNameList.push({
    id: target.coachId,
    name: target.coachName,
    money: scheduleEditForm.money,
  })
  scheduleEditForm.coachId = ''
  scheduleEditForm.money = ''
}

async function submitScheduleEdit() {
  scheduleEditSaving.value = true
  try {
    await updateMerchantCourseTimeApi({
      tearoomId: merchantId.value,
      courseId: scheduleEditForm.courseId,
      courseTimeId: scheduleEditForm.courseTimeId,
      courseStartTime: scheduleEditForm.courseStartTime,
      courseEndTime: scheduleEditForm.courseEndTime,
      status: scheduleEditForm.status,
      coachStr: buildCoachStr(scheduleEditForm.coachNameList),
    })
    showSuccess('操作成功')
    scheduleEditDialogVisible.value = false
    await loadSchedules()
    if (coachScheduleDialogVisible.value) await loadCoachScheduleList()
  } finally {
    scheduleEditSaving.value = false
  }
}

async function removeSchedule(row, refresh = 'schedule') {
  await confirmAction('确定删除排课信息?')
  await deleteMerchantCourseTimeApi(row.courseTimeId)
  showSuccess('删除成功')
  if (refresh === 'schedule') await loadSchedules()
  if (refresh === 'coachSchedule') await loadCoachScheduleList()
}

function openScheduleStudents(row) {
  currentCourseTimeId.value = row.courseTimeId
  scheduleStudentPage.value = 1
  scheduleStudentFilterStatus.value = ''
  scheduleStudentDialogVisible.value = true
  loadScheduleStudents()
}

function openCoachSchedule(row) {
  coachScheduleContext.type = 'coach'
  coachScheduleContext.coachId = Number(row.coachId || 0)
  coachScheduleContext.courseId = 0
  coachScheduleContext.title = '教练排课表'
  coachSchedulePage.value = 1
  coachScheduleStatus.value = ''
  coachScheduleDialogVisible.value = true
  loadCoachScheduleList()
}

function openCourseSchedule(row) {
  coachScheduleContext.type = 'course'
  coachScheduleContext.courseId = Number(row.courseId || 0)
  coachScheduleContext.coachId = 0
  coachScheduleContext.title = '课程排课表'
  coachSchedulePage.value = 1
  coachScheduleStatus.value = ''
  coachScheduleDialogVisible.value = true
  loadCoachScheduleList()
}

function openCoachRevenueDialog(row) {
  currentCoachId.value = Number(row.coachId || 0)
  coachRevenueDate.startTime = `${getToday().slice(0, 8)}01`
  coachRevenueDate.endTime = getToday()
  coachRevenueDialogVisible.value = true
  loadCoachRevenue()
}

async function refundOrder(row, money) {
  await confirmAction('确定要退款?')
  await refundMerchantCourseOrderApi({ ordersId: row.courseOrdersId, refundMoney: money })
  showSuccess('退款成功')
  await loadOrders()
}

function openPartialRefund(row) {
  partialRefundForm.ordersId = row.courseOrdersId
  partialRefundForm.refundMoney = ''
  partialRefundForm.maxRefundMoney = row.refundMoney ? Number(row.payMoney) - Number(row.refundMoney) : Number(row.payMoney)
  partialRefundDialogVisible.value = true
}

async function submitPartialRefund() {
  if (!partialRefundForm.refundMoney) return showError('请输入退款金额')
  if (Number(partialRefundForm.refundMoney) <= 0) return showError('退款金额必须大于0')
  if (Number(partialRefundForm.refundMoney) > Number(partialRefundForm.maxRefundMoney)) return showError('退款金额不能大于最大可退款金额')
  partialRefundSaving.value = true
  try {
    await refundMerchantCourseOrderApi({
      ordersId: partialRefundForm.ordersId,
      refundMoney: partialRefundForm.refundMoney,
    })
    showSuccess('退款成功')
    partialRefundDialogVisible.value = false
    await loadOrders()
  } finally {
    partialRefundSaving.value = false
  }
}

async function openOrderStudents(row) {
  const data = await getMerchantCourseOrderDetailApi({ ordersId: row.courseOrdersId })
  orderStudentList.value = data?.courseOrdersStudentList || []
  orderStudentDialogVisible.value = true
}

function openUserPicker() {
  userPickerPage.value = 1
  userPickerVisible.value = true
  loadUserPicker()
}

function selectBindUser(row) {
  coachForm.userId = Number(row.userId || row.id || 0)
  coachForm.userName = row.userName || ''
  userPickerVisible.value = false
}

watch(
  () => route.query.tab,
  (value) => {
    if (value && value !== activeTab.value) {
      activeTab.value = String(value)
      handleTabChange()
    }
  },
)

watch(
  () => merchantId.value,
  async () => {
    if (!merchantId.value) return
    await Promise.all([loadCourseOptions(), loadCoachOptions()])
    await loadOverview()
    if (activeTab.value !== 'overview') handleTabChange()
  },
  { immediate: true },
)
</script>

<template>
  <div class="mcur">
    <PageTopbar :title="merchantName || '门店课程'" @back="goBack" />

    <header class="mcur-hero nf-page-hero">
      <span class="mcur-hero__badge">羽毛球馆</span>
      <h1 class="mcur-hero__title">门店课程</h1>
      <p class="mcur-hero__desc">{{ merchantName ? `${merchantName} · ` : '' }}管理课程、教练、学员、排课与订单，支持统计概览与多 tab 业务操作</p>
    </header>

    <div class="mcur-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="tab in tabOptions" :key="tab.name" :name="tab.name" :label="tab.label">
          <div class="mcur-panel nf-table-card">
          <template v-if="tab.name === 'overview'">
            <div v-loading="overviewLoading" class="section-stack">
              <section v-for="group in overviewGroups" :key="group.title" class="stats-group">
                <div class="section-title">{{ group.title }}</div>
                <div class="stats-grid">
                  <div v-for="item in group.items" :key="item[0]" class="stats-card">
                    <div class="stats-label">{{ item[0] }}</div>
                    <div class="stats-value">{{ item[1] || 0 }}</div>
                  </div>
                </div>
              </section>
            </div>
          </template>

          <template v-else-if="tab.name === 'course-stats'">
            <div class="toolbar">
              <el-date-picker v-model="courseStatsDate.startTime" type="date" value-format="YYYY-MM-DD" placeholder="开始时间" />
              <el-date-picker v-model="courseStatsDate.endTime" type="date" value-format="YYYY-MM-DD" placeholder="结束时间" />
              <el-button type="primary" @click="loadCourseStats">查询</el-button>
            </div>
            <div v-loading="courseStatsLoading" class="section-stack">
              <section v-for="group in courseStatsGroups" :key="group.title" class="stats-group">
                <div class="section-title">{{ group.title }}</div>
                <div class="stats-grid">
                  <div v-for="item in group.items" :key="item[0]" class="stats-card">
                    <div class="stats-label">{{ item[0] }}</div>
                    <div class="stats-value">{{ item[1] || 0 }}</div>
                  </div>
                </div>
              </section>
            </div>
          </template>

          <template v-else-if="tab.name === 'courses'">
            <div class="toolbar">
              <el-input v-model="courseFilters.courseName" placeholder="请输入课程名称" clearable class="toolbar-item" @keyup.enter="coursePage = 1; loadCourses()" />
              <el-select v-model="courseFilters.status" placeholder="状态" clearable class="toolbar-item" @change="coursePage = 1; loadCourses()">
                <el-option v-for="item in courseStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button type="primary" @click="coursePage = 1; loadCourses()">查询</el-button>
              <el-button @click="courseFilters.courseName = ''; courseFilters.status = ''; coursePage = 1; loadCourses()">重置</el-button>
              <el-button type="primary" @click="openCourseDialog()">添加</el-button>
            </div>
            <el-table v-loading="courseLoading" :data="courseList" border>
              <el-table-column prop="courseId" label="编号" width="80" fixed="left" />
              <el-table-column prop="courseName" label="课程名称" min-width="180" />
              <el-table-column label="课程图片" width="110">
                <template #default="{ row }">
                  <el-image v-if="row.courseImg" :src="String(row.courseImg).split(',')[0]" fit="cover" class="thumb" :preview-src-list="String(row.courseImg).split(',').filter(Boolean)" preview-teleported />
                  <span v-else>暂无图片</span>
                </template>
              </el-table-column>
              <el-table-column prop="coursePeopleNum" label="课程人数" width="110" />
              <el-table-column prop="courseAddress" label="课程地址" min-width="220" show-overflow-tooltip />
              <el-table-column prop="courseNum" label="课程节数" width="100" />
              <el-table-column prop="courseMinute" label="课时时长(分钟)" width="130" />
              <el-table-column prop="coursePrice" label="课程价格" width="100" />
              <el-table-column prop="courseContent" label="课程内容" min-width="200" show-overflow-tooltip />
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="状态" width="130">
                <template #default="{ row }">
                  <div class="status-switch">
                    <span :class="row.status === 1 ? 'status-up' : 'status-down'">{{ row.status === 1 ? '上架' : '下架' }}</span>
                    <el-switch v-model="row.status" :active-value="1" :inactive-value="2" @change="toggleCourseStatus(row, $event)" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-space wrap alignment="start">
                    <el-button size="small" type="warning" plain @click="openCourseSchedule(row)">排课表</el-button>
                    <el-button size="small" type="primary" :disabled="!isAuth('merchantList:update')" @click="openCourseDialog(row)">修改</el-button>
                    <el-button size="small" type="danger" :disabled="!isAuth('merchantList:delete')" @click="removeCourse(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <BasePagination :current-page="coursePage" :page-size="courseSize" :page-sizes="[10, 20, 30, 50, 100]" :total="courseTotal" @update:currentPage="coursePage = $event; loadCourses()" @update:pageSize="courseSize = $event; coursePage = 1; loadCourses()" />
          </template>

          <template v-else-if="tab.name === 'coaches'">
            <div class="toolbar">
              <el-input v-model="coachFilters.coachName" placeholder="请输入教练名称" clearable class="toolbar-item" @keyup.enter="coachPage = 1; loadCoaches()" />
              <el-input v-model="coachFilters.phone" placeholder="请输入教练电话" clearable class="toolbar-item" @keyup.enter="coachPage = 1; loadCoaches()" />
              <el-select v-model="coachFilters.status" placeholder="状态" clearable class="toolbar-item" @change="coachPage = 1; loadCoaches()">
                <el-option v-for="item in courseStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button type="primary" @click="coachPage = 1; loadCoaches()">查询</el-button>
              <el-button @click="coachFilters.coachName = ''; coachFilters.phone = ''; coachFilters.status = ''; coachPage = 1; loadCoaches()">重置</el-button>
              <el-button type="primary" @click="openCoachDialog()">添加</el-button>
            </div>
            <el-table v-loading="coachLoading" :data="coachList" border>
              <el-table-column prop="coachId" label="编号" width="80" fixed="left" />
              <el-table-column prop="coachName" label="教练名称" min-width="120" />
              <el-table-column prop="phone" label="教练电话" width="130" />
              <el-table-column prop="coachAge" label="教练年龄" width="100" />
              <el-table-column label="性别" width="80">
                <template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : '女' }}</template>
              </el-table-column>
              <el-table-column prop="coachLabel" label="标签" min-width="180" show-overflow-tooltip />
              <el-table-column label="头像" width="100">
                <template #default="{ row }">
                  <el-image v-if="row.coachAvatar" :src="row.coachAvatar" class="thumb" fit="cover" :preview-src-list="[row.coachAvatar]" preview-teleported />
                </template>
              </el-table-column>
              <el-table-column prop="introduction" label="个人介绍" min-width="220" show-overflow-tooltip />
              <el-table-column prop="experience" label="个人经历" min-width="220" show-overflow-tooltip />
              <el-table-column prop="beGood" label="个人擅长" min-width="220" show-overflow-tooltip />
              <el-table-column label="真实头像" width="100">
                <template #default="{ row }">
                  <el-image v-if="row.coachImages" :src="row.coachImages" class="thumb" fit="cover" :preview-src-list="[row.coachImages]" preview-teleported />
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row }">
                  <el-space wrap alignment="start">
                    <el-button size="small" type="primary" @click="openCoachDialog(row)">修改</el-button>
                    <el-button size="small" type="primary" plain @click="openCoachRevenueDialog(row)">收益统计</el-button>
                    <el-button size="small" type="warning" plain @click="openCoachSchedule(row)">排课表</el-button>
                    <el-button size="small" type="danger" @click="removeCoach(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <BasePagination :current-page="coachPage" :page-size="coachSize" :page-sizes="[10, 20, 30, 50, 100]" :total="coachTotal" @update:currentPage="coachPage = $event; loadCoaches()" @update:pageSize="coachSize = $event; coachPage = 1; loadCoaches()" />
          </template>

          <template v-else-if="tab.name === 'students'">
            <div class="toolbar">
              <el-input v-model="studentFilters.studentName" placeholder="请输入学员名称" clearable class="toolbar-item" @keyup.enter="studentPage = 1; loadStudents()" />
              <el-button type="primary" @click="studentPage = 1; loadStudents()">查询</el-button>
              <el-button type="primary" @click="openStudentDialog()">添加</el-button>
            </div>
            <el-table v-loading="studentLoading" :data="studentList" border>
              <el-table-column prop="studentId" label="编号" width="80" fixed="left" />
              <el-table-column prop="studentName" label="学员名称" min-width="120" />
              <el-table-column label="头像" width="100">
                <template #default="{ row }">
                  <el-image v-if="row.avatar" :src="row.avatar" class="thumb" fit="cover" :preview-src-list="String(row.avatar).split(',').filter(Boolean)" preview-teleported />
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="电话" width="130" />
              <el-table-column label="性别" width="80">
                <template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : '女' }}</template>
              </el-table-column>
              <el-table-column prop="age" label="年龄" width="90" />
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="210" fixed="right">
                <template #default="{ row }">
                  <el-space wrap alignment="start">
                    <el-button size="small" type="info" plain @click="openStudentCourses(row)">课程列表</el-button>
                    <el-button size="small" type="primary" @click="openStudentDialog(row)">修改</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <BasePagination :current-page="studentPage" :page-size="studentSize" :page-sizes="[5, 10, 15, 20]" :total="studentTotal" @update:currentPage="studentPage = $event; loadStudents()" @update:pageSize="studentSize = $event; studentPage = 1; loadStudents()" />
          </template>

          <template v-else-if="tab.name === 'schedule'">
            <div class="toolbar">
              <el-input v-model="scheduleFilters.courseName" placeholder="请输入课程名称" clearable class="toolbar-item" @keyup.enter="schedulePage = 1; loadSchedules()" />
              <el-input v-model="scheduleFilters.coachName" placeholder="请输入代课教练" clearable class="toolbar-item" @keyup.enter="schedulePage = 1; loadSchedules()" />
              <el-select v-model="scheduleFilters.status" placeholder="状态" clearable class="toolbar-item" @change="schedulePage = 1; loadSchedules()">
                <el-option v-for="item in scheduleStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-button type="primary" @click="schedulePage = 1; loadSchedules()">查询</el-button>
              <el-button @click="scheduleFilters.courseName = ''; scheduleFilters.coachName = ''; scheduleFilters.status = ''; schedulePage = 1; loadSchedules()">重置</el-button>
              <el-button type="primary" @click="openScheduleDialog('all')">添加</el-button>
            </div>
            <el-table v-loading="scheduleLoading" :data="scheduleList" border>
              <el-table-column prop="courseTimeId" label="编号" width="80" fixed="left" />
              <el-table-column prop="course.courseName" label="课程名称" min-width="160" fixed="left" />
              <el-table-column label="代课教练信息" min-width="260">
                <template #default="{ row }">
                  <div v-if="row.courseTimeCoachList?.length" class="multi-line-cell">
                    <div v-for="item in row.courseTimeCoachList" :key="`${row.courseTimeId}-${item.coachId}`">{{ item.coachName }} / {{ item.phone }}</div>
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="courseStartTime" label="上课时间" width="160" />
              <el-table-column prop="courseEndTime" label="下课时间" width="160" />
              <el-table-column prop="studentCount" label="上课人数" width="90" />
              <el-table-column prop="course.courseAddress" label="上课地址" min-width="200" show-overflow-tooltip />
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <span>{{ row.status === 1 ? '待上课' : row.status === 2 ? '上课中' : row.status === 3 ? '已完成' : '已取消' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-space wrap alignment="start">
                    <el-button size="small" type="warning" plain @click="openScheduleStudents(row)">查看学员</el-button>
                    <el-button size="small" type="primary" @click="openScheduleEditDialog(row)">修改</el-button>
                    <el-button size="small" type="danger" @click="removeSchedule(row)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <BasePagination :current-page="schedulePage" :page-size="scheduleSize" :page-sizes="[5, 10, 15, 20]" :total="scheduleTotal" @update:currentPage="schedulePage = $event; loadSchedules()" @update:pageSize="scheduleSize = $event; schedulePage = 1; loadSchedules()" />
          </template>

          <template v-else-if="tab.name === 'orders'">
            <div class="toolbar toolbar-wrap">
              <el-select v-model="orderFilters.status" placeholder="状态" clearable class="toolbar-item" @change="orderPage = 1; loadOrders()">
                <el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-input v-model="orderFilters.courseName" placeholder="课程名称" clearable class="toolbar-item" @keyup.enter="orderPage = 1; loadOrders()" />
              <el-input v-model="orderFilters.coachPhone" placeholder="教练手机号" clearable class="toolbar-item" @keyup.enter="orderPage = 1; loadOrders()" />
              <el-input v-model="orderFilters.userName" placeholder="用户昵称" clearable class="toolbar-item" @keyup.enter="orderPage = 1; loadOrders()" />
              <el-input v-model="orderFilters.coachName" placeholder="教练名称" clearable class="toolbar-item" @keyup.enter="orderPage = 1; loadOrders()" />
              <el-input v-model="orderFilters.ordersNo" placeholder="订单号" clearable class="toolbar-item" @keyup.enter="orderPage = 1; loadOrders()" />
              <el-button type="primary" @click="orderPage = 1; loadOrders()">查询</el-button>
              <el-button @click="Object.assign(orderFilters, { status: '', courseName: '', coachPhone: '', userName: '', coachName: '', ordersNo: '', studentName: '' }); orderPage = 1; loadOrders()">重置</el-button>
            </div>
            <el-table v-loading="orderLoading" :data="orderList" border>
              <el-table-column prop="courseOrdersId" label="编号" width="80" fixed="left" />
              <el-table-column prop="userName" label="下单用户" width="140" fixed="left">
                <template #default="{ row }">
                  <el-link v-if="row.userId" type="primary" @click="router.push(`/users/${row.userId}`)">{{ row.userName || '未绑定' }}</el-link>
                  <span v-else>{{ row.userName || '未绑定' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" width="120" />
              <el-table-column prop="courseName" label="课程名称" min-width="160" />
              <el-table-column label="课程图片" width="110">
                <template #default="{ row }">
                  <el-image v-if="row.courseImg" :src="String(row.courseImg).split(',')[0]" class="thumb" fit="cover" :preview-src-list="String(row.courseImg).split(',').filter(Boolean)" preview-teleported />
                  <span v-else>暂无图片</span>
                </template>
              </el-table-column>
              <el-table-column prop="ordersNo" label="订单号" min-width="180" />
              <el-table-column label="代课教练信息" min-width="240">
                <template #default="{ row }">
                  <div v-if="row.courseTimeCoachList?.length" class="multi-line-cell">
                    <div v-for="item in row.courseTimeCoachList" :key="`${row.courseOrdersId}-${item.coachId}`">{{ item.coachName }} / {{ item.phone }}</div>
                  </div>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="payMoney" label="支付金额" width="110">
                <template #default="{ row }">{{ formatMoney(row.payMoney) }}元</template>
              </el-table-column>
              <el-table-column prop="refundMoney" label="已退款金额" width="120">
                <template #default="{ row }">{{ row.refundMoney ? `${formatMoney(row.refundMoney)}元` : '-' }}</template>
              </el-table-column>
              <el-table-column prop="payWay" label="支付方式" width="100">
                <template #default="{ row }">{{ row.payWay === 1 ? '微信' : row.payWay === 2 ? '支付宝' : row.payWay === 3 ? '零钱' : '-' }}</template>
              </el-table-column>
              <el-table-column label="订单状态" width="130">
                <template #default="{ row }">
                  <div>{{ row.status === 1 ? '待支付' : row.status === 2 ? '已支付' : '已退款' }}</div>
                  <div v-if="row.refundMoney && row.status === 2 && Number(row.refundMoney) !== Number(row.payMoney)" class="partial-refund">(已部分退款)</div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="160" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-space wrap alignment="start">
                    <el-button size="small" type="primary" @click="openOrderStudents(row)">学员</el-button>
                    <el-button v-if="row.status === 2 && !row.refundMoney" size="small" type="warning" @click="refundOrder(row, row.payMoney)">退全款</el-button>
                    <el-button v-if="row.status === 2 && Number(row.payMoney) > Number(row.refundMoney || 0)" size="small" type="warning" plain @click="openPartialRefund(row)">部分退款</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>
            <div class="page-total">本页累计支付统计：{{ orderPageTotalMoney.toFixed(2) }}元</div>
            <BasePagination :current-page="orderPage" :page-size="orderSize" :page-sizes="[10, 20, 30, 50, 100]" :total="orderTotal" @update:currentPage="orderPage = $event; loadOrders()" @update:pageSize="orderSize = $event; orderPage = 1; loadOrders()" />
          </template>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="courseDialogVisible" :title="courseDialogTitle" width="760px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">课程名称</span><el-input v-model="courseForm.courseName" placeholder="请输入课程名称" /></div>
        <div class="form-row">
          <span class="form-label">课程图片</span>
          <div class="image-list-wrap">
            <div v-for="(item, index) in courseForm.courseImg" :key="`${item}-${index}`" class="image-box">
              <img :src="item" class="preview-img" />
              <el-button class="image-remove" circle size="small" @click="courseForm.courseImg.splice(index, 1)">x</el-button>
            </div>
            <el-upload :show-file-list="false" :http-request="(option) => handleArrayUpload('courseImg', option, 'course')">
              <el-button :loading="courseUploadLoading">上传图片</el-button>
            </el-upload>
          </div>
        </div>
        <div class="form-row"><span class="form-label">课程人数</span><el-input v-model="courseForm.coursePeopleNum" type="number" /></div>
        <div class="form-row"><span class="form-label">课程节数</span><el-input v-model="courseForm.courseNum" type="number" /></div>
        <div class="form-row"><span class="form-label">课时时长(分钟)</span><el-input v-model="courseForm.courseMinute" type="number" /></div>
        <div class="form-row"><span class="form-label">课程价格</span><el-input v-model="courseForm.coursePrice" type="number" /></div>
        <div class="form-row"><span class="form-label">课程内容</span><el-input v-model="courseForm.courseContent" /></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="courseForm.status"><el-radio v-for="item in yesNoOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">课程地址</span><div class="grow-row"><el-input v-model="courseForm.courseAddress" placeholder="请输入课程地址" /><el-button @click="locateCourseAddress">获取经纬度</el-button><el-button plain @click="courseMapDialogVisible = true">地图选点</el-button></div></div>
        <div class="form-row"><span class="form-label">经度</span><el-input v-model="courseForm.longitude" /></div>
        <div class="form-row"><span class="form-label">纬度</span><el-input v-model="courseForm.latitude" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="courseDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="courseDialogSaving" @click="submitCourse">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="courseMapDialogVisible" title="地图选点" width="900px" class="nf-pro-dialog" align-center>
<div ref="mapContainerRef" class="map-container"></div>
    </el-dialog>

    <el-dialog v-model="coachDialogVisible" :title="coachDialogTitle" width="860px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">绑定用户</span><div class="grow-row"><el-input v-model="coachForm.userName" placeholder="未绑定用户" readonly /><el-button @click="openUserPicker">选择用户</el-button></div></div>
        <div class="form-row"><span class="form-label">教练名称</span><el-input v-model="coachForm.coachName" /></div>
        <div class="form-row"><span class="form-label">教练年龄</span><el-input v-model="coachForm.coachAge" type="number" /></div>
        <div class="form-row"><span class="form-label">教练电话</span><el-input v-model="coachForm.phone" /></div>
        <div class="form-row"><span class="form-label">性别</span><el-radio-group v-model="coachForm.sex"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">标签</span><div class="grow-row wrap-row"><el-tag v-for="(item, index) in coachForm.coachLabel" :key="`${item}-${index}`" closable @close="coachForm.coachLabel.splice(index, 1)">{{ item }}</el-tag><el-input v-model="coachForm.labelInput" placeholder="请输入标签" class="small-input" @keyup.enter="addCoachLabel" /><el-button @click="addCoachLabel">添加</el-button></div></div>
        <div class="form-row"><span class="form-label">头像</span><div class="grow-row"><img v-if="coachForm.coachAvatar" :src="coachForm.coachAvatar" class="preview-img" /><el-upload :show-file-list="false" :http-request="(option) => handleSingleUpload('coachAvatar', option, 'coach')"><el-button :loading="coachUploadLoading === 'coachAvatar'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">真实头像</span><div class="grow-row"><img v-if="coachForm.coachImages" :src="coachForm.coachImages" class="preview-img" /><el-upload :show-file-list="false" :http-request="(option) => handleSingleUpload('coachImages', option, 'coach')"><el-button :loading="coachUploadLoading === 'coachImages'">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">个人介绍</span><el-input v-model="coachForm.introduction" type="textarea" :rows="4" /></div>
        <div class="form-row"><span class="form-label">个人经历</span><el-input v-model="coachForm.experience" type="textarea" :rows="4" /></div>
        <div class="form-row"><span class="form-label">个人擅长</span><el-input v-model="coachForm.beGood" type="textarea" :rows="4" /></div>
        <div class="form-row"><span class="form-label">教学照片</span><div class="image-list-wrap"><div v-for="(item, index) in coachForm.teachingImage" :key="`${item}-${index}`" class="image-box"><img :src="item" class="preview-img" /><el-button class="image-remove" circle size="small" @click="coachForm.teachingImage.splice(index, 1)">x</el-button></div><el-upload v-if="coachForm.teachingImage.length < 3" :show-file-list="false" :http-request="(option) => handleArrayUpload('teachingImage', option, 'coach')"><el-button :loading="coachUploadLoading === 'teachingImage'">上传</el-button></el-upload></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="coachDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="coachDialogSaving" @click="submitCoach">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" :title="studentDialogTitle" width="720px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">学员名称</span><el-input v-model="studentForm.studentName" /></div>
        <div class="form-row"><span class="form-label">电话</span><el-input v-model="studentForm.phone" /></div>
        <div class="form-row"><span class="form-label">性别</span><el-radio-group v-model="studentForm.sex"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group></div>
        <div class="form-row"><span class="form-label">年龄</span><el-input v-model="studentForm.age" type="number" /></div>
        <div class="form-row"><span class="form-label">头像</span><div class="grow-row"><img v-if="studentForm.avatar" :src="studentForm.avatar" class="preview-img" /><el-upload :show-file-list="false" :http-request="(option) => handleSingleUpload('avatar', option, 'student')"><el-button>上传</el-button></el-upload></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="studentDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="studentDialogSaving" @click="submitStudent">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="studentCourseDialogVisible" :title="`${currentStudent.studentName || '学员'}课程列表`" width="980px" class="nf-pro-dialog" align-center>
<div class="toolbar"><el-button type="primary" @click="loadStudentCourses">刷新</el-button><el-button type="primary" plain @click="openCoursePicker">添加课程</el-button></div>
      <el-table v-loading="studentCourseLoading" :data="studentCourseList" border>
        <el-table-column prop="courseId" label="编号" width="80" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="courseNum" label="课程节数" width="100" />
        <el-table-column prop="useCourseNum" label="已上课程" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space wrap alignment="start">
              <el-button size="small" type="info" plain @click="openStudentSchedule(row)">排课表</el-button>
              <el-button size="small" type="primary" plain @click="openStudentCourseNumDialog(row)">修改课节</el-button>
              <el-button size="small" type="danger" @click="removeStudentCourse(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="studentCoursePage" :page-size="studentCourseSize" :page-sizes="[5, 10, 15, 20]" :total="studentCourseTotal" @update:currentPage="studentCoursePage = $event; loadStudentCourses()" @update:pageSize="studentCourseSize = $event; studentCoursePage = 1; loadStudentCourses()" />
    </el-dialog>

    <el-dialog v-model="coursePickerVisible" title="课程列表" width="900px" class="nf-pro-dialog" align-center>
<el-table v-loading="coursePickerLoading" :data="coursePickerList" border>
        <el-table-column prop="courseId" label="编号" width="80" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="coursePeopleNum" label="课程人数" width="100" />
        <el-table-column prop="courseAddress" label="课程地址" min-width="200" />
        <el-table-column prop="courseNum" label="课程节数" width="100" />
        <el-table-column prop="coursePrice" label="课程价格" width="100" />
        <el-table-column prop="courseContent" label="课程内容" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="bindStudentCourse(row)">确定</el-button></template>
        </el-table-column>
      </el-table>
      <BasePagination :current-page="coursePickerPage" :page-size="coursePickerSize" :page-sizes="[10, 20, 30, 50, 100]" :total="coursePickerTotal" @update:currentPage="coursePickerPage = $event; loadCoursePicker()" @update:pageSize="coursePickerSize = $event; coursePickerPage = 1; loadCoursePicker()" />
    </el-dialog>

    <el-dialog v-model="studentCourseNumDialogVisible" title="修改课节" width="560px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">课程节数</span><el-input v-model="studentCourseNumForm.courseNum" type="number" /></div>
        <div class="form-row"><span class="form-label">已上课程</span><el-input v-model="studentCourseNumForm.useCourseNum" type="number" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="studentCourseNumDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="studentCourseNumSaving" @click="submitStudentCourseNum">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleFormDialogVisible" :title="scheduleFormTitle" width="760px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">类型</span><el-radio-group v-model="scheduleForm.ruleType"><el-radio :value="1">日期</el-radio><el-radio :value="2">周期</el-radio></el-radio-group></div>
        <div class="form-row" v-if="scheduleForm.ruleType === 1"><span class="form-label">日期</span><el-date-picker v-model="scheduleForm.startDate" type="dates" value-format="YYYY-MM-DD" placeholder="选择一个或多个日期" /></div>
        <div class="form-row" v-else><span class="form-label">起始日期</span><el-date-picker v-model="scheduleForm.startDate" type="date" value-format="YYYY-MM-DD" /></div>
        <div class="form-row" v-if="scheduleForm.ruleType === 2"><span class="form-label">周期</span><el-select v-model="scheduleForm.weekNums" placeholder="请选择周期"><el-option v-for="item in weekOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></div>
        <div class="form-row"><span class="form-label">上课时间</span><el-time-select v-model="scheduleForm.startTime" start="00:00" step="00:05" end="23:59" /></div>
        <div class="form-row"><span class="form-label">下课时间</span><el-time-select v-model="scheduleForm.endTime" start="00:00" step="00:05" end="23:59" /></div>
        <div class="form-row" v-if="scheduleContext.type !== 'course'"><span class="form-label">选择课程</span><el-select v-model="scheduleForm.courseId" placeholder="请选择课程"><el-option v-for="item in courseOptions" :key="item.courseId" :label="item.courseName" :value="item.courseId" /></el-select></div>
        <div class="form-row" v-if="scheduleForm.ruleType === 2"><span class="form-label">课节数量</span><el-input v-model="scheduleForm.num" type="number" /></div>
        <div class="form-row" v-if="scheduleContext.type === 'coach'"><span class="form-label">收入</span><el-input v-model="scheduleForm.money" type="number" /></div>
        <div class="form-row" v-else><span class="form-label">选择教练</span><div class="grow-row wrap-row"><el-tag v-for="(item, index) in scheduleForm.coachNameList" :key="`${item.id}-${index}`" closable @close="scheduleForm.coachNameList.splice(index, 1)">{{ item.name }}:{{ item.money }}/￥</el-tag><el-select v-model="scheduleForm.coachId" placeholder="请选择教练" class="small-input"><el-option v-for="item in coachOptions" :key="item.coachId" :label="item.coachName" :value="item.coachId" /></el-select><el-input v-model="scheduleForm.money" type="number" placeholder="教练收入" class="small-input" @keyup.enter="addScheduleCoach" /><el-button type="primary" plain @click="addScheduleCoach">确定</el-button></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="scheduleFormDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="scheduleFormSaving" @click="submitSchedule">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleEditDialogVisible" title="修改排课时间" width="760px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">上课时间</span><el-date-picker v-model="scheduleEditForm.courseStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm" /></div>
        <div class="form-row"><span class="form-label">下课时间</span><el-date-picker v-model="scheduleEditForm.courseEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm" /></div>
        <div class="form-row"><span class="form-label">选择教练</span><div class="grow-row wrap-row"><el-tag v-for="(item, index) in scheduleEditForm.coachNameList" :key="`${item.id}-${index}`" closable @close="scheduleEditForm.coachNameList.splice(index, 1)">{{ item.name }}:{{ item.money }}/￥</el-tag><el-select v-model="scheduleEditForm.coachId" placeholder="请选择教练" class="small-input"><el-option v-for="item in coachOptions" :key="item.coachId" :label="item.coachName" :value="item.coachId" /></el-select><el-input v-model="scheduleEditForm.money" type="number" placeholder="教练收入" class="small-input" @keyup.enter="addScheduleEditCoach" /><el-button type="primary" plain @click="addScheduleEditCoach">确定</el-button></div></div>
        <div class="form-row"><span class="form-label">状态</span><el-radio-group v-model="scheduleEditForm.status"><el-radio :value="1">待上课</el-radio><el-radio :value="2">上课中</el-radio><el-radio :value="3">已完成</el-radio><el-radio :value="4">已取消</el-radio></el-radio-group></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="scheduleEditDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="scheduleEditSaving" @click="submitScheduleEdit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleStudentDialogVisible" title="上课学员" width="980px" class="nf-pro-dialog" align-center>
<div class="toolbar"><el-select v-model="scheduleStudentFilterStatus" placeholder="状态" clearable class="toolbar-item" @change="scheduleStudentPage = 1; loadScheduleStudents()"><el-option v-for="item in studentCourseStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button type="primary" @click="scheduleStudentPage = 1; loadScheduleStudents()">查询</el-button></div>
      <el-table v-loading="scheduleStudentLoading" :data="scheduleStudentList" border>
        <el-table-column prop="studentId" label="编号" width="80" />
        <el-table-column prop="studentName" label="学员名称" min-width="120" />
        <el-table-column label="头像" width="100"><template #default="{ row }"><el-image v-if="row.avatar" :src="String(row.avatar).split(',')[0]" class="thumb" fit="cover" :preview-src-list="String(row.avatar).split(',').filter(Boolean)" preview-teleported /></template></el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="性别" width="80"><template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : '女' }}</template></el-table-column>
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column label="状态" width="100"><template #default="{ row }">{{ row.status === 1 ? '待上课' : row.status === 2 ? '已上课' : row.status === 3 ? '未上课' : '已签到' }}</template></el-table-column>
      </el-table>
      <BasePagination :current-page="scheduleStudentPage" :page-size="scheduleStudentSize" :page-sizes="[5, 10, 15, 20]" :total="scheduleStudentTotal" @update:currentPage="scheduleStudentPage = $event; loadScheduleStudents()" @update:pageSize="scheduleStudentSize = $event; scheduleStudentPage = 1; loadScheduleStudents()" />
    </el-dialog>

    <el-dialog v-model="coachScheduleDialogVisible" :title="coachScheduleContext.title" width="1200px" class="nf-pro-dialog" align-center>
<div class="toolbar"><el-select v-model="coachScheduleStatus" placeholder="状态" clearable class="toolbar-item" @change="coachSchedulePage = 1; loadCoachScheduleList()"><el-option v-for="item in scheduleStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button type="primary" @click="coachSchedulePage = 1; loadCoachScheduleList()">刷新</el-button><el-button type="primary" @click="openScheduleDialog(coachScheduleContext.type, { coachId: coachScheduleContext.coachId, courseId: coachScheduleContext.courseId })">添加</el-button></div>
      <el-table v-loading="coachScheduleLoading" :data="coachScheduleList" border>
        <el-table-column prop="courseTimeId" label="编号" width="80" />
        <el-table-column prop="course.courseName" label="课程名称" min-width="160" />
        <el-table-column v-if="coachScheduleContext.type !== 'coach'" label="代课教练信息" min-width="240"><template #default="{ row }"><div v-if="row.courseTimeCoachList?.length" class="multi-line-cell"><div v-for="item in row.courseTimeCoachList" :key="`${row.courseTimeId}-${item.coachId}`">{{ item.coachName }} / {{ item.phone }}</div></div><span v-else>-</span></template></el-table-column>
        <el-table-column prop="courseStartTime" label="上课时间" width="160" />
        <el-table-column prop="courseEndTime" label="下课时间" width="160" />
        <el-table-column prop="course.courseMinute" label="课时时长(分钟)" width="120" />
        <el-table-column prop="studentCount" label="上课人数" width="90" />
        <el-table-column prop="course.courseAddress" label="上课地址" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="状态" width="100"><template #default="{ row }">{{ row.status === 1 ? '待上课' : row.status === 2 ? (coachScheduleContext.type === 'coach' ? '上课中' : '已上课') : row.status === 3 ? '已完成' : '已取消' }}</template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><el-space wrap alignment="start"><el-button size="small" type="warning" plain @click="openScheduleStudents(row)">查看学员</el-button><el-button size="small" type="primary" @click="openScheduleEditDialog(row)">修改</el-button><el-button size="small" type="danger" @click="removeSchedule(row, 'coachSchedule')">删除</el-button></el-space></template></el-table-column>
      </el-table>
      <BasePagination :current-page="coachSchedulePage" :page-size="coachScheduleSize" :page-sizes="[5, 10, 15, 20]" :total="coachScheduleTotal" @update:currentPage="coachSchedulePage = $event; loadCoachScheduleList()" @update:pageSize="coachScheduleSize = $event; coachSchedulePage = 1; loadCoachScheduleList()" />
    </el-dialog>

    <el-dialog v-model="studentScheduleDialogVisible" title="学员排课表" width="1180px" class="nf-pro-dialog" align-center>
<div class="toolbar"><el-select v-model="studentScheduleStatus" placeholder="状态" clearable class="toolbar-item" @change="studentSchedulePage = 1; loadStudentScheduleList()"><el-option v-for="item in studentCourseStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button type="primary" @click="studentSchedulePage = 1; loadStudentScheduleList()">刷新</el-button></div>
      <el-table v-loading="studentScheduleLoading" :data="studentScheduleList" border>
        <el-table-column prop="studentCourseTimeId" label="编号" width="80" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="courseStartTime" label="上课时间" width="160" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="studentTime" label="签到时间" width="160" />
        <el-table-column label="照片" width="100"><template #default="{ row }"><el-image v-if="row.image" :src="String(row.image).split(',')[0]" class="thumb" fit="cover" :preview-src-list="String(row.image).split(',').filter(Boolean)" preview-teleported /></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="是否请假" width="90"><template #default="{ row }">{{ row.isLeave === 1 ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="状态" width="160"><template #default="{ row }"><div class="status-switch"><span>{{ row.status === 1 ? '待上课' : row.status === 2 ? '已上课' : row.status === 3 ? '未上课' : '已签到' }}</span><el-switch v-if="row.status === 2 || row.status === 3" v-model="row.status" :active-value="2" :inactive-value="3" @change="toggleStudentScheduleStatus(row)" /></div></template></el-table-column>
      </el-table>
      <BasePagination :current-page="studentSchedulePage" :page-size="studentScheduleSize" :page-sizes="[5, 10, 15, 20]" :total="studentScheduleTotal" @update:currentPage="studentSchedulePage = $event; loadStudentScheduleList()" @update:pageSize="studentScheduleSize = $event; studentSchedulePage = 1; loadStudentScheduleList()" />
    </el-dialog>

    <el-dialog v-model="coachRevenueDialogVisible" title="教练收益统计" width="1100px" class="nf-pro-dialog" align-center>
<div v-loading="coachRevenueLoading" class="section-stack">
        <section class="stats-group">
          <div class="section-title">课程统计</div>
          <div class="stats-grid">
            <div class="stats-card"><div class="stats-label">总收益</div><div class="stats-value">{{ coachRevenueSummary.sumMoney || 0 }}元</div></div>
            <div class="stats-card"><div class="stats-label">今日收益金额</div><div class="stats-value">{{ coachRevenueSummary.nowMoney || 0 }}元</div></div>
            <div class="stats-card"><div class="stats-label">本月收益金额</div><div class="stats-value">{{ coachRevenueSummary.monthMoney || 0 }}元</div></div>
            <div class="stats-card"><div class="stats-label">总上课数</div><div class="stats-value">{{ coachRevenueSummary.sumCount || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">今日课程数</div><div class="stats-value">{{ coachRevenueSummary.nowCount || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">本月课程数</div><div class="stats-value">{{ coachRevenueSummary.monthCount || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">待上课节数</div><div class="stats-value">{{ coachRevenueSummary.courseCount1 || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">上课中</div><div class="stats-value">{{ coachRevenueSummary.courseCount2 || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">已完成</div><div class="stats-value">{{ coachRevenueSummary.courseCount3 || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">已取消</div><div class="stats-value">{{ coachRevenueSummary.courseCount4 || 0 }}节</div></div>
          </div>
        </section>
        <section class="stats-group">
          <div class="section-title">收益统计</div>
          <div class="toolbar">
            <el-date-picker v-model="coachRevenueDate.startTime" type="date" value-format="YYYY-MM-DD" placeholder="开始时间" />
            <el-date-picker v-model="coachRevenueDate.endTime" type="date" value-format="YYYY-MM-DD" placeholder="结束时间" />
            <el-button type="primary" @click="loadCoachRevenue">查询</el-button>
          </div>
          <div class="stats-grid">
            <div class="stats-card"><div class="stats-label">上课收益</div><div class="stats-value">{{ coachRevenueStats.courseMoney || 0 }}元</div></div>
            <div class="stats-card"><div class="stats-label">上课统计</div><div class="stats-value">{{ coachRevenueStats.courseCount || 0 }}节</div></div>
          </div>
        </section>
        <section v-for="(item, index) in coachRevenueCourseList" :key="index" class="course-revenue-item">
          <div class="section-title">{{ item.courseName }}</div>
          <div class="stats-grid">
            <div class="stats-card"><div class="stats-label">总上课数</div><div class="stats-value">{{ item.counts || 0 }}节</div></div>
            <div class="stats-card"><div class="stats-label">总收入金额</div><div class="stats-value">{{ item.sumMoney || 0 }}元</div></div>
            <div class="stats-card"><div class="stats-label">单节收入金额</div><div class="stats-value">{{ item.money || 0 }}元</div></div>
          </div>
        </section>
      </div>
    </el-dialog>

    <el-dialog v-model="orderStudentDialogVisible" title="订单学员" width="820px" class="nf-pro-dialog" align-center>
<el-table :data="orderStudentList" border>
        <el-table-column prop="courseOrdersStudentId" label="编号" width="80" />
        <el-table-column prop="studentName" label="学员名称" min-width="120" />
        <el-table-column label="学员头像" width="100"><template #default="{ row }"><el-image v-if="row.avatar" :src="String(row.avatar).split(',')[0]" class="thumb" fit="cover" :preview-src-list="String(row.avatar).split(',').filter(Boolean)" preview-teleported /></template></el-table-column>
        <el-table-column label="性别" width="80"><template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : '女' }}</template></el-table-column>
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="partialRefundDialogVisible" title="部分退款" width="560px" class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">退款金额</span><div class="grow-col"><el-input v-model="partialRefundForm.refundMoney" type="number" min="0" placeholder="请输入退款金额" /><div class="form-tip">最大可退款金额为：{{ partialRefundForm.maxRefundMoney.toFixed(2) }}</div></div></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="partialRefundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="partialRefundSaving" @click="submitPartialRefund">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="userPickerVisible" title="用户列表" width="900px" class="nf-pro-dialog" align-center>
<div class="toolbar"><el-input v-model="userPickerFilters.phone" placeholder="手机号" clearable class="toolbar-item" @keyup.enter="userPickerPage = 1; loadUserPicker()" /><el-input v-model="userPickerFilters.userName" placeholder="昵称" clearable class="toolbar-item" @keyup.enter="userPickerPage = 1; loadUserPicker()" /><el-button type="primary" @click="userPickerPage = 1; loadUserPicker()">查询</el-button><el-button @click="userPickerFilters.phone = ''; userPickerFilters.userName = ''; userPickerPage = 1; loadUserPicker()">重置</el-button></div>
      <el-table v-loading="userPickerLoading" :data="userPickerList" border>
        <el-table-column prop="userId" label="编号" width="80" />
        <el-table-column prop="userName" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column label="性别" width="80"><template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : '女' }}</template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="110"><template #default="{ row }"><el-button size="small" type="warning" @click="selectBindUser(row)">确定添加</el-button></template></el-table-column>
      </el-table>
      <BasePagination :current-page="userPickerPage" :page-size="userPickerSize" :page-sizes="[5, 10, 15, 20]" :total="userPickerTotal" @update:currentPage="userPickerPage = $event; loadUserPicker()" @update:pageSize="userPickerSize = $event; userPickerPage = 1; loadUserPicker()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mcur {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mcur-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mcur-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm, 6px);
}

.mcur-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mcur-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mcur-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    padding: 0 16px;
    background: #fff;
    border: 1px solid var(--nf-border);
    border-radius: 8px 8px 0 0;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    overflow: visible;
  }

  :deep(.el-tab-pane) {
    padding-top: 0;
  }
}

.mcur-panel.nf-table-card {
  margin-bottom: 0;
  margin-top: 0;
  padding: 16px;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;
  box-shadow: var(--nf-shadow-card);

  :deep(.el-table) {
    --el-table-border-color: var(--nf-border);
    --el-table-header-bg-color: var(--nf-sidebar-bg-alt, #f7f8fa);
  }

  :deep(.el-table th.el-table__cell) {
    font-weight: 600;
    color: var(--nf-text-primary);
  }

  :deep(.el-table td.el-table__cell) {
    background: #fff;
  }
}

.section-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--nf-text-primary);
  padding-left: 10px;
  border-left: 3px solid var(--nf-brand);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.stats-card {
  border: 1px solid var(--nf-border);
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.stats-label {
  font-size: 13px;
  color: var(--nf-text-secondary);
  margin-bottom: 8px;
}

.stats-value {
  font-size: 22px;
  color: var(--nf-brand-deep);
  font-weight: 600;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.toolbar-wrap {
  align-items: flex-start;
}

.toolbar-item {
  width: 180px;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.status-switch {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-up {
  color: var(--el-color-success);
}

.status-down {
  color: var(--el-color-danger);
}

.page-total {
  margin: 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-color-danger);
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 140px;
  line-height: 32px;
  text-align: right;
  color: var(--el-text-color-regular);
  flex-shrink: 0;
}

.grow-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.grow-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.wrap-row {
  flex-wrap: wrap;
}

.small-input {
  width: 180px;
}

.image-list-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex: 1;
}

.image-box {
  position: relative;
}

.preview-img {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid var(--el-border-color-light);
}

.image-remove {
  position: absolute;
  top: -8px;
  right: -8px;
}

.map-container {
  width: 100%;
  height: 480px;
}

.multi-line-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.course-revenue-item {
  padding: 16px;
  border: 1px solid var(--nf-border);
  border-radius: 8px;
  background: #fff;
}

.partial-refund {
  color: var(--el-color-danger);
}

.form-tip {
  color: var(--el-color-danger);
  font-size: 12px;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
  }

  .toolbar-item,
  .small-input {
    width: 100%;
  }

  .grow-row {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
