<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import PageTopbar from '@/components/common/PageTopbar.vue'
import { updateOrderApi } from '@/api/modules/order-center'
import {
  createMerchantGoodsSpecialTimeApi,
  createMerchantGoodsTimeApi,
  createMerchantEmployeeApi,
  createMerchantCouponIssueApi,
  createMerchantPresetPrizeRecordApi,
  createMerchantPrizeApi,
  createMerchantPrizeRecordApi,
  createMerchantRefundRuleApi,
  createMerchantWelfareWordApi,
  createMerchantMusicApi,
  createMerchantMemberWalletApi,
  createMerchantMemberWalletDetailApi,
  deleteMerchantCouponIssueApi,
  deleteMerchantGoodsSpecialTimeApi,
  deleteMerchantGoodsTimeApi,
  deleteMerchantEmployeeApi,
  deleteMerchantGoodsApi,
  deleteMerchantMusicApi,
  deleteMerchantMemberWalletApi,
  deleteMerchantMemberWalletDetailApi,
  deleteMerchantPresetPrizeRecordApi,
  deleteMerchantPrizeApi,
  deleteMerchantRefundRuleApi,
  deleteMerchantWelfareWordApi,
  getMerchantCollectWaterPageApi,
  getMerchantCouponListApi,
  getMerchantCouponIssuePageApi,
  getMerchantDetailApi,
  getMerchantDurationStatsApi,
  getMerchantEmployeeOpenStatsApi,
  getMerchantEmployeePermissionApi,
  getMerchantEmployeeWorkRecordPageApi,
  getMerchantEarningDetailApi,
  getMerchantEmployeesApi,
  getMerchantGoodsPageApi,
  getMerchantGoodsSpecialTimePageApi,
  getMerchantGoodsTimePageApi,
  getMerchantIncomeStatsApi,
  getMerchantLoveWalletApi,
  getMerchantLoveWalletDetailPageApi,
  getMerchantMemberWalletDetailPageApi,
  getMerchantMemberWalletPageApi,
  getMerchantMemberWalletSummaryApi,
  getMerchantMusicPageApi,
  getMerchantOpenStatsApi,
  getMerchantOrderPageApi,
  getMerchantOrderStatusStatsApi,
  getMerchantOrderVipDetailsApi,
  getMerchantPresetPrizeRecordPageApi,
  getMerchantPrizePageApi,
  getMerchantPrizeLogisticsApi,
  getMerchantPrizeRecordPageApi,
  getMerchantQueuePageApi,
  getMerchantRefundRulePageApi,
  getMerchantRenewRecordPageApi,
  getMerchantRevenueSummaryApi,
  getMerchantRevenueTrendApi,
  getMerchantRevenueSourceApi,
  getMerchantWriteOffStatsApi,
  getMerchantWelfareWordPageApi,
  getMusicLibraryPageApi,
  getMusicTypePageApi,
  refundMerchantOrderApi,
  updateMerchantEmployeePermissionApi,
  updateMerchantApi,
  updateMerchantCouponIssueStatusApi,
  updateMerchantEmployeeApi,
  updateMerchantGoodsApi,
  updateMerchantGoodsSpecialTimeApi,
  updateMerchantGoodsTimeApi,
  updateMerchantLoveWalletApi,
  updateMerchantPresetPrizeRecordApi,
  updateMerchantPrizeApi,
  updateMerchantPrizeRecordApi,
  updateMerchantRefundRuleApi,
  updateMerchantWelfareWordApi,
  uploadMerchantImageApi,
  updateMerchantQueueApi,
  changeMerchantOrderRoomApi,
  updateMerchantMemberWalletByTypeApi,
  updateMerchantMemberWalletApi,
  updateMerchantMemberWalletDetailApi,
} from '@/api/modules/merchant-center'
import { getPrizeGoldenDetailsApi, getPrizeRechargeRecordsApi, getPrizeRecordsApi, getUserCenterPage, getUserCouponsApi, updateUserPrizeGoldenApi } from '@/api/modules/user-center'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { getApiBaseUrl, isAuth } from '@/utils/tool'

const route = useRoute()
const router = useRouter()
const QQ_MAP_KEY = 'PSPBZ-Q4IY3-ABH3Q-O4RTI-OEMVS-NUFIP'

const tabs = [
  { name: 'info', label: '羽毛球馆信息' },
  { name: 'open-stats', label: '开台统计' },
  { name: 'income', label: '羽毛球馆收入' },
  { name: 'goods', label: '羽毛球场管理' },
  { name: 'orders', label: '羽毛球馆订单' },
  { name: 'coupon-issue', label: '已发布优惠券' },
  { name: 'coupon-record', label: '优惠券领取记录' },
  { name: 'employees', label: '员工管理' },
  { name: 'refund', label: '退款规则配置' },
  { name: 'stats', label: '羽毛球馆统计' },
  { name: 'revenue', label: '收入来源' },
  { name: 'revenue-analysis', label: '营业额分析' },
  { name: 'write-off', label: '核销统计' },
  { name: 'earning-detail', label: '收益明细' },
  { name: 'duration', label: '开台时长统计' },
  { name: 'queue', label: '排号列表' },
  { name: 'love-wallet', label: '爱心钱包' },
  { name: 'love-board', label: '爱心公示' },
  { name: 'member-wallet', label: '会员钱包' },
  { name: 'member-wallet-detail', label: '会员钱包明细' },
  { name: 'water', label: '领水记录' },
  { name: 'music', label: '收藏歌单' },
  { name: 'prize-config', label: '奖品配置' },
  { name: 'prize-record', label: '抽奖记录' },
  { name: 'count-record', label: '次数记录' },
  { name: 'count-recharge', label: '充值次数记录' },
  { name: 'preset-prize-record', label: '预设抽奖记录' },
  { name: 'users', label: '用户列表' },
]

const merchantId = computed(() => {
  const parsed = Number.parseInt(String(route.params.id || '0'), 10)
  return Number.isFinite(parsed) ? parsed : 0
})
const activeTab = ref(String(route.query.tab || 'info'))

const detailLoading = ref(false)
const saveLoading = ref(false)
const editable = ref(false)
const imageUploadingField = ref('')

const goodsLoading = ref(false)
const goodsPage = ref(1)
const goodsSize = ref(10)
const goodsTotal = ref(0)
const goodsList = ref([])
const goodsSelection = ref([])
const goodsQrDialogVisible = ref(false)
const goodsQrTitle = ref('开台二维码')
const goodsQrUrl = ref('')
const goodsBatchPriceVisible = ref(false)
const goodsBatchPriceLoading = ref(false)
const goodsBatchPriceForm = reactive({
  hourPrice: '',
  subscription: '',
})
const goodsStatsVisible = ref(false)
const goodsStatsLoading = ref(false)
const goodsStatsTargetName = ref('')
const goodsStatsGoodsId = ref(0)
const goodsStatsDate = ref(getToday())
const goodsStatsWeekRange = ref([])
const goodsStatsMonthDate = ref(getToday())
const goodsStatsYearDate = ref(getToday())
const goodsStatsDaily = ref({})
const goodsStatsWeekly = ref({})
const goodsStatsMonth = ref({})
const goodsStatsYear = ref({})
const goodsLiveVisible = ref(false)
const goodsLiveTitle = ref('直播推流')
const goodsLiveUrl = ref('')
const goodsTimeDialogVisible = ref(false)
const goodsTimeDialogLoading = ref(false)
const goodsTimeDialogTitle = ref('特殊价格')
const goodsTimeTarget = ref({ goodsId: 0, goodsName: '' })
const goodsTimePage = ref(1)
const goodsTimeSize = ref(10)
const goodsTimeTotal = ref(0)
const goodsTimeLoading = ref(false)
const goodsTimeList = ref([])
const goodsTimeEditVisible = ref(false)
const goodsTimeEditLoading = ref(false)
const goodsTimeForm = reactive({
  goodsTimeId: 0,
  ruleType: 1,
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: '',
  hourPrice: '',
  minutePrice: '',
})

const goodsSpecialTimeDialogVisible = ref(false)
const goodsSpecialTimeDialogLoading = ref(false)
const goodsSpecialTimePage = ref(1)
const goodsSpecialTimeSize = ref(10)
const goodsSpecialTimeTotal = ref(0)
const goodsSpecialTimeList = ref([])
const goodsSpecialTimeEditVisible = ref(false)
const goodsSpecialTimeEditLoading = ref(false)
const goodsSpecialTimeForm = reactive({
  goodsSpecialTimeId: 0,
  weekNum: '',
  startTime: '',
  endTime: '',
})

const orderLoading = ref(false)
const orderPage = ref(1)
const orderSize = ref(10)
const orderTotal = ref(0)
const orderList = ref([])
const refundDialogVisible = ref(false)
const refundDialogLoading = ref(false)
const refundDialogTitle = ref('退款')
const refundForm = reactive({
  ordersNo: '',
  refundMoney: '',
  refundRemark: '',
  maxRefundMoney: 0,
})
const orderVipVisible = ref(false)
const orderVipData = ref({})
const renewVisible = ref(false)
const renewLoading = ref(false)
const renewPage = ref(1)
const renewSize = ref(10)
const renewTotal = ref(0)
const renewList = ref([])
const renewOrdersNo = ref('')
const changeRoomVisible = ref(false)
const changeRoomLoading = ref(false)
const changeRoomOrderId = ref(0)
const changeRoomHourPrice = ref(0)
const changeRoomGoodsList = ref([])
const orderPageTotalMoney = ref(0)
const transferOrderId = ref(0)
const couponIssueLoading = ref(false)
const couponIssuePage = ref(1)
const couponIssueSize = ref(10)
const couponIssueTotal = ref(0)
const couponIssueList = ref([])
const couponIssueType = ref(2)
const couponIssueDialogVisible = ref(false)
const couponIssueDialogLoading = ref(false)
const couponListLoading = ref(false)
const couponOptions = ref([])
const couponIssueForm = reactive({
  type: 2,
  couponId: '',
  couponName: '',
  couponType: '',
  goodsIds: [],
  issueNumber: 0,
  isLimit: 1,
  status: 1,
  teamRule: '',
  buyRule: '',
  hour: '',
  integral: '',
  validDay: '',
  minHour: '',
})
const couponRecordLoading = ref(false)
const couponRecordPage = ref(1)
const couponRecordSize = ref(10)
const couponRecordTotal = ref(0)
const couponRecordList = ref([])
const couponRecordFilters = reactive({
  platformType: '',
  couponName: '',
})

const employeeLoading = ref(false)
const employeePage = ref(1)
const employeeSize = ref(10)
const employeeTotal = ref(0)
const employeeList = ref([])
const employeeDialogVisible = ref(false)
const employeeDialogLoading = ref(false)
const employeeForm = reactive({
  userId: 0,
  startTime: '',
  endTime: '',
  tearoomUserStatus: 1,
})
const employeePermissionVisible = ref(false)
const employeePermissionEditable = ref(false)
const employeePermissionLoading = ref(false)
const employeePermissionForm = reactive({
  tearoomId: 0,
  userId: 0,
  isDaijiesuan: 2,
  isDiange: 2,
  isDingdanguanli: 2,
  isHexiaotongji: 2,
  isHuiyuanguanli: 2,
  isHuiyuanquanyi: 2,
  isKaitaishouyi: 2,
  isMendianhuiyuan: 2,
  isShangjiakaitai: 2,
  isShangjiakaitaitongji: 2,
  isShichangtongji: 2,
  isShouyi: 2,
  isShouyimingxi: 2,
  isStatus: 2,
  isTixian: 2,
  isXiaofeipaihang: 2,
  isYuangongguanli: 2,
  isYuyin: 2,
  isZengsongyouhuijuan: 2,
  isZhangdantongji: 2,
  isZhuotaiguanli: 2,
  isZhuotaishuju: 2,
  isTuikuan: 2,
  isLingshui: 2,
  isMusic: 2,
  isShouRuLaiYuan: 2,
})
const employeeWorkVisible = ref(false)
const employeeWorkLoading = ref(false)
const employeeWorkPage = ref(1)
const employeeWorkSize = ref(10)
const employeeWorkTotal = ref(0)
const employeeWorkList = ref([])
const employeeCurrentUserId = ref(0)
const employeeOpenStatsVisible = ref(false)
const employeeOpenStatsLoading = ref(false)
const employeeOpenStats = ref({})

const userPickerVisible = ref(false)
const userPickerLoading = ref(false)
const userPickerPage = ref(1)
const userPickerSize = ref(10)
const userPickerTotal = ref(0)
const userPickerData = ref([])
const userPickerMode = ref('merchant-owner')
const userPickerFilters = reactive({
  phone: '',
  userName: '',
  invitationCode: '',
})

const statsLoading = reactive({
  open: false,
  income: false,
  revenue: false,
})
const statWay = ref(1)
const statDate = ref(getToday())
const openStats = ref({})
const incomeStats = ref({})
const revenueType = ref(1)
const revenueStart = ref(getToday())
const revenueEnd = ref(getToday())
const revenueStats = ref({})
const revenueAnalysis = ref({})
const writeOffStats = ref({})
const earningDetailLoading = ref(false)
const earningDetailWay = ref(1)
const earningDetailPage = ref(1)
const earningDetailList = ref([])
const earningOrderVisible = ref(false)
const earningOrderLoading = ref(false)
const earningOrderPage = ref(1)
const earningOrderSize = ref(10)
const earningOrderTotal = ref(0)
const earningOrderList = ref([])
const earningOrderDateTime = ref('')
const durationLoading = ref(false)
const durationList = ref([])

const waterLoading = ref(false)
const waterPage = ref(1)
const waterSize = ref(10)
const waterTotal = ref(0)
const waterList = ref([])
const waterFilters = reactive({
  userName: '',
  phone: '',
})

const queueLoading = ref(false)
const queuePage = ref(1)
const queueSize = ref(10)
const queueTotal = ref(0)
const queueList = ref([])
const queueFilters = reactive({
  status: '',
  createTime: getToday(),
})

const loveWalletLoading = ref(false)
const loveWalletSummary = ref({})
const loveWalletPage = ref(1)
const loveWalletSize = ref(10)
const loveWalletTotal = ref(0)
const loveWalletList = ref([])
const loveWalletFilters = reactive({
  moneyType: '',
  userName: '',
  phone: '',
  title: '',
  content: '',
})

const welfareWordLoading = ref(false)
const welfareWordPage = ref(1)
const welfareWordSize = ref(10)
const welfareWordTotal = ref(0)
const welfareWordList = ref([])
const welfareWordKeyword = ref('')
const welfareWordDialogVisible = ref(false)
const welfareWordDialogLoading = ref(false)
const welfareWordForm = reactive({
  welfareWordId: 0,
  welfareWordTitle: '',
  welfareWordContent: '',
  welfareImg: '',
  sort: 0,
})

const memberWalletLoading = ref(false)
const memberWalletSummary = ref({})
const memberWalletPage = ref(1)
const memberWalletSize = ref(10)
const memberWalletTotal = ref(0)
const memberWalletList = ref([])
const memberWalletKeyword = ref('')
const memberWalletDialogVisible = ref(false)
const memberWalletDialogLoading = ref(false)
const memberWalletForm = reactive({
  memberUserId: 0,
  phone: '',
  money: '',
})

const memberWalletDetailLoading = ref(false)
const memberWalletDetailPage = ref(1)
const memberWalletDetailSize = ref(10)
const memberWalletDetailTotal = ref(0)
const memberWalletDetailList = ref([])
const memberWalletDetailDialogVisible = ref(false)
const memberWalletDetailDialogLoading = ref(false)
const memberWalletDetailForm = reactive({
  id: 0,
  memberUserId: 0,
  phone: '',
  type: 1,
  money: '',
  title: '',
  content: '',
})

const musicLoading = ref(false)
const musicPage = ref(1)
const musicSize = ref(10)
const musicTotal = ref(0)
const musicList = ref([])
const musicKeyword = ref('')
const musicLibraryVisible = ref(false)
const musicLibraryLoading = ref(false)
const musicLibraryPage = ref(1)
const musicLibrarySize = ref(10)
const musicLibraryTotal = ref(0)
const musicLibraryList = ref([])
const musicTypeOptions = ref([])
const musicLibraryFilters = reactive({
  musicTypeId: '',
  musicName: '',
  musicAuthor: '',
})

const prizeConfigLoading = ref(false)
const prizeConfigPage = ref(1)
const prizeConfigSize = ref(10)
const prizeConfigTotal = ref(0)
const prizeConfigList = ref([])
const prizePickerVisible = ref(false)
const prizePickerMode = ref('prize-record')
const prizeConfigDialogVisible = ref(false)
const prizeConfigDialogLoading = ref(false)
const prizeConfigForm = reactive({
  prizeId: 0,
  prizeType: 1,
  prizeName: '',
  prizeImage: '',
  weight: '',
  sort: '',
  parent: '',
})

const prizeRecordLoading = ref(false)
const prizeRecordPage = ref(1)
const prizeRecordSize = ref(10)
const prizeRecordTotal = ref(0)
const prizeRecordList = ref([])
const prizeRecordFilters = reactive({
  userName: '',
  phone: '',
  prizeName: '',
})
const prizeRecordDialogVisible = ref(false)
const prizeRecordDialogLoading = ref(false)
const prizeRecordForm = reactive({
  prizeUserId: 0,
  userId: 0,
  userName: '',
  prizeId: 0,
  prizeName: '',
  prizeImage: '',
  prizeType: 1,
  parent: '',
  weight: '',
})
const prizeShipDialogVisible = ref(false)
const prizeShipDialogLoading = ref(false)
const prizeShipForm = reactive({
  prizeUserId: 0,
  prizeId: 0,
  prizeName: '',
  prizeImage: '',
  prizeType: 1,
  parent: '',
  userId: 0,
  weight: '',
  expressNumber: '',
  status: 2,
})
const prizeLogisticsVisible = ref(false)
const prizeLogisticsLoading = ref(false)
const prizeLogisticsList = ref([])

const countRecordLoading = ref(false)
const countRecordPage = ref(1)
const countRecordSize = ref(10)
const countRecordTotal = ref(0)
const countRecordList = ref([])
const countRecordUserName = ref('')
const countUpdateDialogVisible = ref(false)
const countUpdateDialogLoading = ref(false)
const countUpdateForm = reactive({
  userId: 0,
  type: 1,
  goldenNum: '',
})

const rechargeRecordLoading = ref(false)
const rechargeRecordPage = ref(1)
const rechargeRecordSize = ref(10)
const rechargeRecordTotal = ref(0)
const rechargeRecordList = ref([])
const rechargeRecordUserName = ref('')

const presetPrizeRecordLoading = ref(false)
const presetPrizeRecordPage = ref(1)
const presetPrizeRecordSize = ref(10)
const presetPrizeRecordTotal = ref(0)
const presetPrizeRecordList = ref([])
const presetPrizeRecordFilters = reactive({
  status: '',
  userName: '',
  phone: '',
  prizeName: '',
})
const presetPrizeRecordDialogVisible = ref(false)
const presetPrizeRecordDialogLoading = ref(false)
const presetPrizeRecordForm = reactive({
  prizeUserSysId: 0,
  userId: 0,
  userName: '',
  prizeId: 0,
  prizeName: '',
  prizeImage: '',
  prizeType: 1,
  parent: '',
  weight: '',
  status: 1,
})

const userListLoading = ref(false)
const userListPage = ref(1)
const userListSize = ref(10)
const userListTotal = ref(0)
const userList = ref([])
const userListFilters = reactive({
  userName: '',
  phone: '',
  inviterCode: '',
})
const userCountDetailVisible = ref(false)
const userPrizeDetailVisible = ref(false)
const userRechargeDetailVisible = ref(false)

const infoFormRef = ref()
const mapDialogVisible = ref(false)
const mapLoading = ref(false)
const mapInitializing = ref(false)
const mapContainerRef = ref()
let qqMapScriptPromise = null
let qqMapInstance = null
let qqMapMarker = null
let qqMapGeocoder = null

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

const refundRuleLoading = ref(false)
const refundRuleList = ref([])
const refundRuleTotal = ref(0)
const refundRulePage = ref(1)
const refundRuleSize = ref(10)
const refundRuleDialogVisible = ref(false)
const refundRuleDialogLoading = ref(false)
const refundRuleForm = reactive({
  refundRuleId: 0,
  hour: '',
  rate: '',
})

const merchantStatusOptions = [
  { label: '正常', value: 1 },
  { label: '打烊', value: 2 },
  { label: '今日已满', value: 3 },
]

const shopTypeOptions = [
  { label: '自营', value: 1 },
  { label: '三方', value: 2 },
]

const moneyScopeOptions = [
  { label: '有加减', value: 1 },
  { label: '有加', value: 2 },
  { label: '有减', value: 3 },
  { label: '没有', value: 4 },
]

const goodsStatusOptions = [
  { label: '全部', value: '' },
  { label: '上架', value: 1 },
  { label: '暂停使用', value: 2 },
  { label: '清洁中', value: 3 },
  { label: '维护中', value: 4 },
  { label: '已打烊', value: 5 },
]

const employeeStatusOptions = [
  { label: '正常', value: 1 },
  { label: '上班', value: 2 },
  { label: '下班', value: 3 },
]

const openTypeOptions = [
  { label: '全部', value: '' },
  { label: '商家开台', value: 1 },
  { label: '用户开台', value: 2 },
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

const priceRuleTypeOptions = [
  { label: '普通日', value: 1 },
  { label: '节假日', value: 2 },
]

const orderStatusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 1 },
  { label: '预约中', value: 2 },
  { label: '使用中', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
  { label: '待评价', value: 6 },
  { label: '待补费', value: 7 },
]

const infoInitial = {
  tearoomId: 0,
  userId: 0,
  userName: '',
  tearoomName: '',
  tearoomPhone: '',
  tearoomTitleImage: '',
  tearoomCoverImage: '',
  qunImg: '',
  wxCode: '',
  siteImg: '',
  shopType: 1,
  rate: '',
  keyCode: '',
  shopPoiId: '',
  ksAppId: '',
  ksAppSecret: '',
  mtEmail: '',
  mtPassword: '',
  mtStoreId: '',
  dyPoiId: '',
  dyAppId: '',
  dySecret: '',
  wifiNo: '',
  wifiPwd: '',
  yyToken: '',
  yyId: '',
  broadcastContent: '',
  cameraId: '',
  tearoomProvince: '',
  tearoomCity: '',
  tearoomDistrict: '',
  tearoomAddress: '',
  tearoomLatitude: '',
  tearoomLongitude: '',
  startTime: '',
  endTime: '',
  status: 1,
  moneyScope: 4,
  isVipPrivilege: 2,
  isFaceRecognition: 2,
  isMusic: 2,
  isReserve: 2,
  isWelfare: 2,
  welfareContent: '',
  welfareImg: '',
  isRefund: 1,
  isWxPay: 1,
  isQrCode: 2,
  openTime1: '',
  closeTime1: '',
  startTime2: '',
  endTime2: '',
  isPrize: 2,
  prizeNum: '',
  prizeDay: '',
  prizeHour: '',
  prizeTitle: '',
  prizeContent: '',
  luckySort: '',
  luckyContent: '',
}

const infoForm = reactive({ ...infoInitial })

const infoRules = {
  tearoomName: [{ required: true, message: '请输入羽毛球馆名称', trigger: 'blur' }],
  tearoomPhone: [
    { required: true, message: '请输入羽毛球馆电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式错误', trigger: 'blur' },
  ],
  rate: [{ required: true, message: '请输入佣金比例', trigger: 'blur' }],
}

const goodsFilters = reactive({
  goodsName: '',
  status: '',
})

const orderFilters = reactive({
  isShop: '',
  status: '',
  ordersNo: '',
  phone: '',
  userName: '',
  goodsName: '',
  startTime: '',
  endTime: '',
})

function getToday() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${date}`
}

function amount(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function textValue(value) {
  return String(value ?? '').trim()
}

function boolText(value) {
  return Number(value) === 1 ? '是' : '否'
}

function shopTypeText(value) {
  return Number(value) === 2 ? '三方' : '自营'
}

function merchantStatusText(value) {
  return merchantStatusOptions.find((item) => item.value === Number(value))?.label || '-'
}

function goodsStatusText(value) {
  return goodsStatusOptions.find((item) => item.value === Number(value))?.label || '-'
}

function employeeStatusText(value) {
  return employeeStatusOptions.find((item) => item.value === Number(value))?.label || '-'
}

function openTypeText(value) {
  return Number(value) === 1 ? '商家开台' : '用户开台'
}

function orderStatusText(value) {
  return orderStatusOptions.find((item) => item.value === Number(value))?.label || '-'
}

function payWayText(value) {
  const map = { 1: '微信', 2: '支付宝', 3: '零钱' }
  return map[Number(value)] || '-'
}

function orderTypeText(value) {
  if (Number(value) === 1) return '开台'
  if (Number(value) === 3) return '预约'
  return '-'
}

function weekText(value) {
  return weekOptions.find((item) => item.value === String(value))?.label || '-'
}

function priceRuleTypeText(value) {
  return priceRuleTypeOptions.find((item) => item.value === Number(value))?.label || '-'
}

function platformText(value) {
  const map = { 1: '美团', 2: '抖音', 3: '快手' }
  return map[Number(value)] || '-'
}

function firstImage(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .find(Boolean) || ''
}

function buildAddress() {
  return [infoForm.tearoomProvince, infoForm.tearoomCity, infoForm.tearoomDistrict, infoForm.tearoomAddress].filter(Boolean).join('')
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/merchants')
}

function buildAddressKeyword() {
  return [infoForm.tearoomProvince, infoForm.tearoomCity, infoForm.tearoomDistrict, infoForm.tearoomAddress].filter(Boolean).join('')
}

function requestJsonp(url) {
  return new Promise((resolve, reject) => {
    const callbackName = `qq_jsonp_${Date.now()}_${Math.random().toString(16).slice(2)}`
    const script = document.createElement('script')
    const separator = url.includes('?') ? '&' : '?'
    const cleanup = () => {
      delete window[callbackName]
      script.remove()
    }
    window[callbackName] = (payload) => {
      cleanup()
      resolve(payload)
    }
    script.onerror = () => {
      cleanup()
      reject(new Error('地图服务请求失败'))
    }
    script.src = `${url}${separator}output=jsonp&callback=${callbackName}`
    document.body.appendChild(script)
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

function updateMapMarker(lat, lng) {
  const maps = window.qq?.maps
  if (!maps || !qqMapInstance) return
  const position = new maps.LatLng(Number(lat), Number(lng))
  qqMapInstance.setCenter(position)
  if (!qqMapMarker) {
    qqMapMarker = new maps.Marker({ map: qqMapInstance, position })
    return
  }
  qqMapMarker.setPosition(position)
}

async function initMap() {
  if (!mapDialogVisible.value) return
  mapInitializing.value = true
  try {
    const maps = await ensureQQMaps()
    await nextTick()
    if (!mapContainerRef.value) return
    const latitude = Number(infoForm.tearoomLatitude || 34.34281541842994)
    const longitude = Number(infoForm.tearoomLongitude || 108.93970884382725)
    const center = new maps.LatLng(latitude, longitude)
    qqMapInstance = new maps.Map(mapContainerRef.value, {
      center,
      zoom: 13,
      mapTypeId: maps.MapTypeId.ROADMAP,
    })
    qqMapMarker = new maps.Marker({ map: qqMapInstance, position: center })
    qqMapGeocoder = new maps.Geocoder({
      complete: ({ detail }) => {
        const location = detail?.location
        if (!location) return
        infoForm.tearoomLongitude = String(location.lng)
        infoForm.tearoomLatitude = String(location.lat)
        updateMapMarker(location.lat, location.lng)
      },
    })
    maps.event.addListener(qqMapInstance, 'click', async (event) => {
      const lng = event.latLng.getLng()
      const lat = event.latLng.getLat()
      infoForm.tearoomLongitude = String(lng)
      infoForm.tearoomLatitude = String(lat)
      updateMapMarker(lat, lng)
      try {
        const response = await requestJsonp(`https://apis.map.qq.com/ws/geocoder/v1/?location=${lat},${lng}&key=${QQ_MAP_KEY}&get_poi=1`)
        const result = response?.result
        if (!result) return
        infoForm.tearoomAddress = result.formatted_addresses?.recommend || infoForm.tearoomAddress
        infoForm.tearoomProvince = result.ad_info?.province || infoForm.tearoomProvince
        infoForm.tearoomCity = result.ad_info?.city || infoForm.tearoomCity
        infoForm.tearoomDistrict = result.ad_info?.district || infoForm.tearoomDistrict
      } catch (error) {
        showError(error.message || '地图逆解析失败')
      }
    })
  } finally {
    mapInitializing.value = false
  }
}

async function openMapDialog() {
  mapDialogVisible.value = true
  await initMap()
}

async function locateByAddress() {
  const address = buildAddressKeyword()
  if (!address) {
    showError('请先填写省市区和详细地址')
    return
  }
  mapLoading.value = true
  try {
    const response = await requestJsonp(`https://apis.map.qq.com/ws/geocoder/v1/?address=${encodeURIComponent(address)}&key=${QQ_MAP_KEY}&get_poi=1`)
    const location = response?.result?.location
    if (!location) {
      showError('未找到对应坐标')
      return
    }
    infoForm.tearoomLongitude = String(location.lng)
    infoForm.tearoomLatitude = String(location.lat)
    if (mapDialogVisible.value) {
      await initMap()
      updateMapMarker(location.lat, location.lng)
    }
    showSuccess('经纬度已更新')
  } catch (error) {
    showError(error.message || '地址定位失败')
  } finally {
    mapLoading.value = false
  }
}

function goUserDetail(userId) {
  if (!userId) {
    showError('当前记录未绑定用户')
    return
  }
  router.push(`/users/${userId}`)
}

function normalizeDetail(data = {}) {
  const dutyTimes = String(data.tearoomDutyTime || '').split('-')
  const timerDoorTimes = String(data.tearoomTimerDoor || '').split('-')
  return {
    ...infoInitial,
    ...data,
    tearoomId: Number(data.tearoomId || merchantId.value || 0),
    userId: Number(data.userId || 0),
    userName: data.userName || '',
    tearoomPhone: data.tearoomPhone || data.phone || '',
    tearoomLatitude: data.tearoomLatitude || data.latitude || '',
    tearoomLongitude: data.tearoomLongitude || data.longitude || '',
    openTime1: dutyTimes[0] || '',
    closeTime1: dutyTimes[1] || '',
    startTime2: timerDoorTimes[0] || '',
    endTime2: timerDoorTimes[1] || '',
    userEntityList: data.userEntityList || [],
  }
}

function buildDetailPayload() {
  const payload = {
    tearoomId: merchantId.value || undefined,
    tearoomTitleImage: textValue(infoForm.tearoomTitleImage),
    tearoomCoverImage: textValue(infoForm.tearoomCoverImage),
    tearoomName: textValue(infoForm.tearoomName),
    tearoomPhone: textValue(infoForm.tearoomPhone),
    phone: textValue(infoForm.tearoomPhone),
    tearoomAddress: textValue(infoForm.tearoomAddress),
    tearoomLongitude: textValue(infoForm.tearoomLongitude),
    tearoomLatitude: textValue(infoForm.tearoomLatitude),
    longitude: textValue(infoForm.tearoomLongitude),
    latitude: textValue(infoForm.tearoomLatitude),
    startTime: textValue(infoForm.startTime),
    endTime: textValue(infoForm.endTime),
    tearoomProvince: textValue(infoForm.tearoomProvince),
    tearoomCity: textValue(infoForm.tearoomCity),
    tearoomDistrict: textValue(infoForm.tearoomDistrict),
    status: Number(infoForm.status || 1),
    userId: Number(infoForm.userId || 0),
    keyCode: textValue(infoForm.keyCode),
    yyToken: textValue(infoForm.yyToken),
    yyId: textValue(infoForm.yyId),
    wifiNo: textValue(infoForm.wifiNo),
    wifiPwd: textValue(infoForm.wifiPwd),
    moneyScope: Number(infoForm.moneyScope || 4),
    isReserve: Number(infoForm.isReserve || 2),
    rate: textValue(infoForm.rate),
    broadcastContent: textValue(infoForm.broadcastContent),
    cameraId: textValue(infoForm.cameraId),
    qunImg: textValue(infoForm.qunImg),
    wxCode: textValue(infoForm.wxCode),
    isQrCode: Number(infoForm.isQrCode || 2),
    shopType: Number(infoForm.shopType || 1),
    isVipPrivilege: Number(infoForm.isVipPrivilege || 2),
    shopPoiId: textValue(infoForm.shopPoiId),
    isWelfare: Number(infoForm.isWelfare || 2),
    welfareContent: textValue(infoForm.welfareContent),
    welfareImg: textValue(infoForm.welfareImg),
    isFaceRecognition: Number(infoForm.isFaceRecognition || 2),
    isMusic: Number(infoForm.isMusic || 2),
    ksAppId: textValue(infoForm.ksAppId),
    ksAppSecret: textValue(infoForm.ksAppSecret),
    mtEmail: textValue(infoForm.mtEmail),
    mtPassword: textValue(infoForm.mtPassword),
    mtStoreId: textValue(infoForm.mtStoreId),
    dyPoiId: textValue(infoForm.dyPoiId),
    dyAppId: textValue(infoForm.dyAppId),
    dySecret: textValue(infoForm.dySecret),
    luckySort: textValue(infoForm.luckySort),
    luckyContent: textValue(infoForm.luckyContent),
    isRefund: Number(infoForm.isRefund || 2),
    isWxPay: Number(infoForm.isWxPay || 2),
    siteImg: textValue(infoForm.siteImg),
    tearoomDutyTime: infoForm.openTime1 && infoForm.closeTime1 ? `${infoForm.openTime1}-${infoForm.closeTime1}` : '',
    tearoomTimerDoor: infoForm.startTime2 && infoForm.endTime2 ? `${infoForm.startTime2}-${infoForm.endTime2}` : '',
    isPrize: Number(infoForm.isPrize || 2),
    prizeNum: textValue(infoForm.prizeNum),
    prizeDay: textValue(infoForm.prizeDay),
    prizeHour: textValue(infoForm.prizeHour),
    prizeTitle: textValue(infoForm.prizeTitle),
    prizeContent: infoForm.prizeContent || '',
  }
  Object.keys(payload).forEach((key) => {
    if (payload[key] === '' || payload[key] === null || typeof payload[key] === 'undefined') {
      delete payload[key]
    }
  })
  return payload
}

async function loadDetail() {
  if (!merchantId.value) return
  detailLoading.value = true
  try {
    const data = await getMerchantDetailApi(merchantId.value)
    Object.assign(infoForm, infoInitial, normalizeDetail(data))
  } finally {
    detailLoading.value = false
  }
}

async function saveDetail() {
  const valid = await validateForm(infoFormRef)
  if (!valid) return
  saveLoading.value = true
  try {
    await updateMerchantApi(buildDetailPayload())
    editable.value = false
    showSuccess('门店信息已更新')
    await loadDetail()
  } finally {
    saveLoading.value = false
  }
}

function cancelEdit() {
  editable.value = false
  loadDetail()
}

async function handleImageUpload(field, option) {
  imageUploadingField.value = field
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = typeof data === 'string' ? data : data?.url || data?.data || data?.src || data?.path || ''
    infoForm[field] = String(url || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    imageUploadingField.value = ''
  }
}

function buildGoodsParams() {
  return {
    page: goodsPage.value,
    limit: goodsSize.value,
    goodsName: textValue(goodsFilters.goodsName),
    goodsCategory: 1,
    status: goodsFilters.status,
    tearoomId: merchantId.value,
  }
}

async function loadGoods() {
  goodsLoading.value = true
  try {
    const data = await getMerchantGoodsPageApi(buildGoodsParams())
    goodsList.value = data.list || []
    goodsTotal.value = Number(data.total || 0)
  } finally {
    goodsLoading.value = false
  }
}

function handleGoodsSearch() {
  goodsPage.value = 1
  loadGoods()
}

function handleGoodsReset() {
  goodsFilters.goodsName = ''
  goodsFilters.status = ''
  handleGoodsSearch()
}

async function toggleGoodsStatus(row) {
  const nextStatus = Number(row.status) === 1 ? 2 : 1
  await updateMerchantGoodsApi({ ...row, goodsId: row.goodsId, status: nextStatus })
  showSuccess('羽毛球场状态已更新')
  await loadGoods()
}

async function deleteGoods(row) {
  await confirmAction(`确认删除“${row.goodsName || '当前羽毛球场'}”吗？`)
  await deleteMerchantGoodsApi(row.goodsId)
  showSuccess('羽毛球场已删除')
  await loadGoods()
}

function openGoodsQr(row) {
  goodsQrTitle.value = `${row.goodsName || '羽毛球场'}开台二维码`
  const baseUrl = getApiBaseUrl()
  goodsQrUrl.value = `${baseUrl}app/invite/mpCreateQr?invitationCode=${merchantId.value},${row.goodsId}&page=pages/index/order`
  goodsQrDialogVisible.value = true
}

function openWaterQr() {
  goodsQrTitle.value = `${infoForm.tearoomName || '门店'}领水二维码`
  const baseUrl = getApiBaseUrl()
  goodsQrUrl.value = `${baseUrl}app/invite/mpCreateQr?invitationCode=${merchantId.value}&page=pages/index/collectWater`
  goodsQrDialogVisible.value = true
}

function openGoodsTimeDialog(row) {
  goodsTimeTarget.value = { goodsId: Number(row.goodsId || 0), goodsName: row.goodsName || '' }
  goodsTimePage.value = 1
  goodsTimeDialogTitle.value = `${row.goodsName || '羽毛球场'}特殊价格`
  goodsTimeDialogVisible.value = true
  loadGoodsTimeList()
}

function resetGoodsTimeForm() {
  Object.assign(goodsTimeForm, {
    goodsTimeId: 0,
    ruleType: 1,
    startDate: '',
    endDate: '',
    startTime: '',
    endTime: '',
    hourPrice: '',
    minutePrice: '',
  })
}

function buildGoodsTimeParams() {
  return {
    page: goodsTimePage.value,
    limit: goodsTimeSize.value,
    goodsId: goodsTimeTarget.value.goodsId,
  }
}

async function loadGoodsTimeList() {
  if (!goodsTimeTarget.value.goodsId) return
  goodsTimeLoading.value = true
  try {
    const data = await getMerchantGoodsTimePageApi(buildGoodsTimeParams())
    goodsTimeList.value = data.list || []
    goodsTimeTotal.value = Number(data.total || 0)
  } finally {
    goodsTimeLoading.value = false
  }
}

function openGoodsTimeEdit(row) {
  resetGoodsTimeForm()
  if (row) {
    Object.assign(goodsTimeForm, {
      goodsTimeId: Number(row.goodsTimeId || 0),
      ruleType: Number(row.ruleType || 1),
      startDate: String(row.startDate || ''),
      endDate: String(row.endDate || ''),
      startTime: row.startTime || '',
      endTime: row.endTime || '',
      hourPrice: row.hourPrice ?? '',
      minutePrice: row.minutePrice ?? '',
    })
  }
  goodsTimeEditVisible.value = true
}

async function submitGoodsTime() {
  if (!goodsTimeTarget.value.goodsId) {
    showError('缺少球场信息')
    return
  }
  if (!goodsTimeForm.startTime || !goodsTimeForm.endTime) {
    showError('请选择开始和结束时间')
    return
  }
  if (goodsTimeForm.hourPrice === '' || goodsTimeForm.minutePrice === '') {
    showError('请填写价格')
    return
  }
  if (!goodsTimeForm.startDate || !goodsTimeForm.endDate) {
    showError('请选择起止日期')
    return
  }
  const payload = {
    goodsId: goodsTimeTarget.value.goodsId,
    goodsTimeId: Number(goodsTimeForm.goodsTimeId || 0),
    ruleType: Number(goodsTimeForm.ruleType || 1),
    startDate: goodsTimeForm.startDate,
    endDate: goodsTimeForm.endDate,
    startTime: goodsTimeForm.startTime,
    endTime: goodsTimeForm.endTime,
    hourPrice: goodsTimeForm.hourPrice,
    minutePrice: goodsTimeForm.minutePrice,
  }
  goodsTimeEditLoading.value = true
  try {
    if (payload.goodsTimeId) {
      await updateMerchantGoodsTimeApi(payload)
    } else {
      delete payload.goodsTimeId
      await createMerchantGoodsTimeApi(payload)
    }
    goodsTimeEditVisible.value = false
    showSuccess('特殊价格已保存')
    await loadGoodsTimeList()
  } finally {
    goodsTimeEditLoading.value = false
  }
}

async function deleteGoodsTime(row) {
  await confirmAction('确认删除当前特殊价格吗？')
  await deleteMerchantGoodsTimeApi(row.goodsTimeId)
  showSuccess('特殊价格已删除')
  await loadGoodsTimeList()
}

function openGoodsSpecialTimeDialog(row) {
  goodsTimeTarget.value = { goodsId: Number(row.goodsId || 0), goodsName: row.goodsName || '' }
  goodsSpecialTimePage.value = 1
  goodsSpecialTimeDialogVisible.value = true
  loadGoodsSpecialTimeList()
}

function resetGoodsSpecialTimeForm() {
  Object.assign(goodsSpecialTimeForm, {
    goodsSpecialTimeId: 0,
    weekNum: '',
    startTime: '',
    endTime: '',
  })
}

function buildGoodsSpecialTimeParams() {
  return {
    page: goodsSpecialTimePage.value,
    limit: goodsSpecialTimeSize.value,
    goodsId: goodsTimeTarget.value.goodsId,
  }
}

async function loadGoodsSpecialTimeList() {
  if (!goodsTimeTarget.value.goodsId) return
  goodsSpecialTimeDialogLoading.value = true
  try {
    const data = await getMerchantGoodsSpecialTimePageApi(buildGoodsSpecialTimeParams())
    goodsSpecialTimeList.value = data.list || []
    goodsSpecialTimeTotal.value = Number(data.total || 0)
  } finally {
    goodsSpecialTimeDialogLoading.value = false
  }
}

function openGoodsSpecialTimeEdit(row) {
  resetGoodsSpecialTimeForm()
  if (row) {
    Object.assign(goodsSpecialTimeForm, {
      goodsSpecialTimeId: Number(row.goodsSpecialTimeId || 0),
      weekNum: String(row.weekNum || ''),
      startTime: row.startTime || '',
      endTime: row.endTime || '',
    })
  }
  goodsSpecialTimeEditVisible.value = true
}

async function submitGoodsSpecialTime() {
  if (!goodsTimeTarget.value.goodsId) {
    showError('缺少球场信息')
    return
  }
  if (!goodsSpecialTimeForm.weekNum) {
    showError('请选择日期')
    return
  }
  if (!goodsSpecialTimeForm.startTime || !goodsSpecialTimeForm.endTime) {
    showError('请选择开始和结束时间')
    return
  }
  const payload = {
    goodsId: goodsTimeTarget.value.goodsId,
    goodsSpecialTimeId: Number(goodsSpecialTimeForm.goodsSpecialTimeId || 0),
    weekNum: goodsSpecialTimeForm.weekNum,
    startTime: goodsSpecialTimeForm.startTime,
    endTime: goodsSpecialTimeForm.endTime,
  }
  goodsSpecialTimeEditLoading.value = true
  try {
    if (payload.goodsSpecialTimeId) {
      await updateMerchantGoodsSpecialTimeApi(payload)
    } else {
      delete payload.goodsSpecialTimeId
      await createMerchantGoodsSpecialTimeApi(payload)
    }
    goodsSpecialTimeEditVisible.value = false
    showSuccess('特殊忙时已保存')
    await loadGoodsSpecialTimeList()
  } finally {
    goodsSpecialTimeEditLoading.value = false
  }
}

async function deleteGoodsSpecialTime(row) {
  await confirmAction('确认删除当前特殊忙时吗？')
  await deleteMerchantGoodsSpecialTimeApi(row.goodsSpecialTimeId)
  showSuccess('特殊忙时已删除')
  await loadGoodsSpecialTimeList()
}

function downloadQrImage() {
  if (!goodsQrUrl.value) return
  const link = document.createElement('a')
  link.href = goodsQrUrl.value
  link.target = '_blank'
  link.download = `${goodsQrTitle.value}.png`
  link.click()
}

function openGoodsCreate() {
  router.push(`/merchants/${merchantId.value}/goods/create`)
}

function openGoodsEdit(row) {
  router.push(`/merchants/${merchantId.value}/goods/${row.goodsId}/edit`)
}

async function openGoodsStats(row) {
  goodsStatsVisible.value = true
  goodsStatsGoodsId.value = Number(row.goodsId || 0)
  goodsStatsTargetName.value = row.goodsName || ''
  goodsStatsDate.value = getToday()
  goodsStatsMonthDate.value = getToday()
  goodsStatsYearDate.value = getToday()
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 6)
  goodsStatsWeekRange.value = [formatDateOnly(start), formatDateOnly(end)]
  await loadGoodsStatsPanels()
}

function formatDateOnly(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function loadGoodsStatsPanels() {
  if (!goodsStatsGoodsId.value) return
  goodsStatsLoading.value = true
  try {
    const [dayData, weekData, monthData, yearData] = await Promise.all([
      getMerchantGoodsStatsByTimeApi({ goodsId: goodsStatsGoodsId.value, time: goodsStatsDate.value, flag: 1 }),
      getMerchantGoodsStatsByTimeApi({ goodsId: goodsStatsGoodsId.value, time: goodsStatsWeekRange.value?.[0] || '', endTime: goodsStatsWeekRange.value?.[1] || '', flag: 4 }),
      getMerchantGoodsStatsByTimeApi({ goodsId: goodsStatsGoodsId.value, time: goodsStatsMonthDate.value, flag: 2 }),
      getMerchantGoodsStatsByTimeApi({ goodsId: goodsStatsGoodsId.value, time: goodsStatsYearDate.value, flag: 3 }),
    ])
    goodsStatsDaily.value = dayData || {}
    goodsStatsWeekly.value = weekData || {}
    goodsStatsMonth.value = monthData || {}
    goodsStatsYear.value = yearData || {}
  } finally {
    goodsStatsLoading.value = false
  }
}

function openGoodsLive(row) {
  if (!textValue(row.goodsTips)) {
    showError('暂无推流链接，请先添加或者修改推流链接')
    return
  }
  goodsLiveTitle.value = `${row.goodsName || '羽毛球场'}直播推流`
  goodsLiveUrl.value = textValue(row.goodsTips)
  goodsLiveVisible.value = true
}

async function downloadBatchGoodsQr() {
  for (const row of goodsSelection.value) {
    goodsQrTitle.value = `${row.goodsName || '羽毛球场'}开台二维码`
    const baseUrl = getApiBaseUrl()
    goodsQrUrl.value = `${baseUrl}app/invite/mpCreateQr?invitationCode=${merchantId.value},${row.goodsId}&page=pages/index/order`
    downloadQrImage()
  }
  showSuccess('已开始下载二维码')
}

function openBatchPriceDialog() {
  goodsBatchPriceForm.hourPrice = ''
  goodsBatchPriceForm.subscription = ''
  goodsBatchPriceVisible.value = true
}

async function submitBatchPrice() {
  if (!goodsSelection.value.length) {
    showError('请先选择羽毛球场')
    return
  }
  goodsBatchPriceLoading.value = true
  try {
    for (const row of goodsSelection.value) {
      await updateMerchantGoodsApi({
        ...row,
        goodsId: row.goodsId,
        hourPrice: goodsBatchPriceForm.hourPrice === '' ? row.hourPrice : goodsBatchPriceForm.hourPrice,
        subscription: goodsBatchPriceForm.subscription === '' ? row.subscription : goodsBatchPriceForm.subscription,
      })
    }
    goodsBatchPriceVisible.value = false
    showSuccess('批量修改成功')
    await loadGoods()
  } finally {
    goodsBatchPriceLoading.value = false
  }
}

function buildOrderParams() {
  const params = {
    page: orderPage.value,
    limit: orderSize.value,
    phone: textValue(orderFilters.phone),
    userName: textValue(orderFilters.userName),
    goodsName: textValue(orderFilters.goodsName),
    ordersNo: textValue(orderFilters.ordersNo),
    status: orderFilters.status,
    startTime: orderFilters.startTime,
    endTime: orderFilters.endTime,
    tearoomId: merchantId.value,
    isShop: orderFilters.isShop,
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const data = await getMerchantOrderPageApi(buildOrderParams())
    orderList.value = data.list || []
    orderTotal.value = Number(data.total || 0)
    orderPageTotalMoney.value = (orderList.value || []).reduce((sum, item) => {
      const value = Number(item.payment ?? item.payMoney ?? 0)
      return sum + (Number.isFinite(value) ? value : 0)
    }, 0)
  } finally {
    orderLoading.value = false
  }
}

function handleOrderSearch() {
  orderPage.value = 1
  loadOrders()
}

function handleOrderReset() {
  Object.assign(orderFilters, {
    isShop: '',
    status: '',
    ordersNo: '',
    phone: '',
    userName: '',
    goodsName: '',
    startTime: '',
    endTime: '',
  })
  handleOrderSearch()
}

function openRefundDialog(row) {
  const maxRefundMoney = Number(row.payMoney || 0) - Number(row.refundMoney || 0)
  refundDialogTitle.value = '部分退款'
  refundForm.ordersNo = row.ordersNo || ''
  refundForm.maxRefundMoney = Math.max(maxRefundMoney, 0)
  refundForm.refundMoney = maxRefundMoney > 0 ? maxRefundMoney.toFixed(2) : ''
  refundForm.refundRemark = ''
  refundDialogVisible.value = true
}

async function refundAllOrder(row) {
  const maxRefundMoney = Number(row.payMoney || 0) - Number(row.refundMoney || 0)
  if (maxRefundMoney <= 0) {
    showError('当前订单没有可退款金额')
    return
  }
  await confirmAction('确定退全款?')
  await refundMerchantOrderApi({ ordersNo: row.ordersNo, refundMoney: maxRefundMoney })
  showSuccess('退款成功')
  await loadOrders()
}

async function submitRefund() {
  const amount = Number(refundForm.refundMoney)
  if (!refundForm.ordersNo) {
    showError('缺少订单号')
    return
  }
  if (!Number.isFinite(amount)) {
    showError('请输入有效退款金额')
    return
  }
  if (amount <= 0) {
    showError('退款金额必须大于0')
    return
  }
  if (amount > Number(refundForm.maxRefundMoney || 0)) {
    showError(`退款金额不可高于${amountValue(refundForm.maxRefundMoney)}元`)
    return
  }
  refundDialogLoading.value = true
  try {
    await refundMerchantOrderApi({
      ordersNo: refundForm.ordersNo,
      refundMoney: amount,
      refundRemark: textValue(refundForm.refundRemark),
    })
    refundDialogVisible.value = false
    showSuccess('退款成功')
    await loadOrders()
  } finally {
    refundDialogLoading.value = false
  }
}

function amountValue(value) {
  return Number(value || 0).toFixed(2)
}

function buildEmployeeParams() {
  return {
    page: employeePage.value,
    limit: employeeSize.value,
    tearoomUser: merchantId.value,
  }
}

async function loadEmployees() {
  employeeLoading.value = true
  try {
    const data = await getMerchantEmployeesApi(buildEmployeeParams())
    employeeList.value = data.list || []
    employeeTotal.value = Number(data.total || 0)
  } finally {
    employeeLoading.value = false
  }
}

function buildRefundRuleParams() {
  return {
    page: refundRulePage.value,
    limit: refundRuleSize.value,
    tearoomId: merchantId.value,
  }
}

async function loadRefundRules() {
  refundRuleLoading.value = true
  try {
    const data = await getMerchantRefundRulePageApi(buildRefundRuleParams())
    refundRuleList.value = data.list || []
    refundRuleTotal.value = Number(data.total || 0)
  } finally {
    refundRuleLoading.value = false
  }
}

function openRefundRuleDialog(row) {
  Object.assign(refundRuleForm, {
    refundRuleId: Number(row?.refundRuleId || 0),
    hour: row?.hour ?? '',
    rate: row?.rate ?? '',
  })
  refundRuleDialogVisible.value = true
}

async function submitRefundRule() {
  if (!merchantId.value) {
    showError('缺少门店ID')
    return
  }
  if (refundRuleForm.hour === '' || refundRuleForm.rate === '') {
    showError('请填写完整退款规则')
    return
  }
  const payload = {
    tearoomId: merchantId.value,
    refundRuleId: Number(refundRuleForm.refundRuleId || 0),
    hour: refundRuleForm.hour,
    rate: refundRuleForm.rate,
  }
  refundRuleDialogLoading.value = true
  try {
    if (payload.refundRuleId) {
      await updateMerchantRefundRuleApi(payload)
    } else {
      delete payload.refundRuleId
      await createMerchantRefundRuleApi(payload)
    }
    refundRuleDialogVisible.value = false
    showSuccess('退款规则已保存')
    await loadRefundRules()
  } finally {
    refundRuleDialogLoading.value = false
  }
}

async function deleteRefundRule(row) {
  await confirmAction('确认删除当前退款规则吗？')
  await deleteMerchantRefundRuleApi(row.refundRuleId)
  showSuccess('退款规则已删除')
  await loadRefundRules()
}

function openEmployeeDialog(row) {
  employeeForm.userId = Number(row?.userId || 0)
  employeeForm.startTime = row?.startTime || ''
  employeeForm.endTime = row?.endTime || ''
  employeeForm.tearoomUserStatus = Number(row?.tearoomUserStatus || 1)
  employeeDialogVisible.value = true
}

async function submitEmployeeDialog() {
  if (!employeeForm.userId) {
    showError('请选择员工用户')
    return
  }
  if (!employeeForm.startTime || !employeeForm.endTime) {
    showError('请选择工作时间')
    return
  }
  employeeDialogLoading.value = true
  try {
    await updateMerchantEmployeeApi({
      userId: employeeForm.userId,
      tearoomId: merchantId.value,
      startTime: employeeForm.startTime,
      endTime: employeeForm.endTime,
      status: employeeForm.tearoomUserStatus,
    })
    employeeDialogVisible.value = false
    showSuccess('员工设置已更新')
    await loadEmployees()
  } finally {
    employeeDialogLoading.value = false
  }
}

async function deleteEmployee(row) {
  await confirmAction(`确认删除员工“${row.userName || '当前员工'}”吗？`)
  await deleteMerchantEmployeeApi({ userId: row.userId, tearoomId: merchantId.value })
  showSuccess('员工已删除')
  await loadEmployees()
}

function resetEmployeePermissionForm() {
  Object.assign(employeePermissionForm, {
    tearoomId: merchantId.value,
    userId: employeeCurrentUserId.value,
    isDaijiesuan: 2,
    isDiange: 2,
    isDingdanguanli: 2,
    isHexiaotongji: 2,
    isHuiyuanguanli: 2,
    isHuiyuanquanyi: 2,
    isKaitaishouyi: 2,
    isMendianhuiyuan: 2,
    isShangjiakaitai: 2,
    isShangjiakaitaitongji: 2,
    isShichangtongji: 2,
    isShouyi: 2,
    isShouyimingxi: 2,
    isStatus: 2,
    isTixian: 2,
    isXiaofeipaihang: 2,
    isYuangongguanli: 2,
    isYuyin: 2,
    isZengsongyouhuijuan: 2,
    isZhangdantongji: 2,
    isZhuotaiguanli: 2,
    isZhuotaishuju: 2,
    isTuikuan: 2,
    isLingshui: 2,
    isMusic: 2,
    isShouRuLaiYuan: 2,
  })
}

async function openEmployeePermission(row) {
  employeeCurrentUserId.value = Number(row.userId || 0)
  employeePermissionEditable.value = false
  employeePermissionLoading.value = true
  employeePermissionVisible.value = true
  try {
    const data = await getMerchantEmployeePermissionApi({ tearoomId: merchantId.value, userId: employeeCurrentUserId.value })
    resetEmployeePermissionForm()
    if (data) {
      Object.assign(employeePermissionForm, data)
    }
  } finally {
    employeePermissionLoading.value = false
  }
}

async function saveEmployeePermission() {
  employeePermissionLoading.value = true
  try {
    await updateMerchantEmployeePermissionApi({ ...employeePermissionForm, tearoomId: merchantId.value, userId: employeeCurrentUserId.value })
    showSuccess('权限已更新')
    employeePermissionEditable.value = false
  } finally {
    employeePermissionLoading.value = false
  }
}

async function loadEmployeeWorkRecords() {
  employeeWorkLoading.value = true
  try {
    const data = await getMerchantEmployeeWorkRecordPageApi({
      tearoomId: merchantId.value,
      userId: employeeCurrentUserId.value,
      page: employeeWorkPage.value,
      limit: employeeWorkSize.value,
    })
    employeeWorkList.value = data.list || []
    employeeWorkTotal.value = Number(data.total || 0)
  } finally {
    employeeWorkLoading.value = false
  }
}

async function openEmployeeWorkRecords(row) {
  employeeCurrentUserId.value = Number(row.userId || 0)
  employeeWorkPage.value = 1
  employeeWorkVisible.value = true
  await loadEmployeeWorkRecords()
}

async function openEmployeeOpenStats(row) {
  employeeCurrentUserId.value = Number(row.userId || 0)
  employeeOpenStatsVisible.value = true
  employeeOpenStatsLoading.value = true
  try {
    employeeOpenStats.value = await getMerchantEmployeeOpenStatsApi({
      tearoomId: merchantId.value,
      userId: employeeCurrentUserId.value,
      time: statDate.value,
      type: statWay.value,
    })
  } finally {
    employeeOpenStatsLoading.value = false
  }
}

async function openOrderVipDialog(row) {
  orderVipData.value = await getMerchantOrderVipDetailsApi(row.ordersNo)
  orderVipVisible.value = true
}

async function loadRenewRecords() {
  renewLoading.value = true
  try {
    const data = await getMerchantRenewRecordPageApi({ page: renewPage.value, limit: renewSize.value, content: renewOrdersNo.value, state: 400 })
    renewList.value = data.list || []
    renewTotal.value = Number(data.total || 0)
  } finally {
    renewLoading.value = false
  }
}

async function openRenewDialog(row) {
  renewOrdersNo.value = row.ordersNo || ''
  renewPage.value = 1
  renewVisible.value = true
  await loadRenewRecords()
}

async function openChangeRoomDialog(row) {
  changeRoomOrderId.value = Number(row.ordersId || 0)
  changeRoomHourPrice.value = Number(row.hourMoney || row.goods?.hourPrice || 0)
  changeRoomVisible.value = true
  changeRoomLoading.value = true
  try {
    const data = await getMerchantGoodsPageApi({ page: 1, limit: 100, goodsName: '', status: '', tearoomId: merchantId.value })
    changeRoomGoodsList.value = (data.list || []).filter((item) => Number(item.hourPrice || 0) === changeRoomHourPrice.value)
  } finally {
    changeRoomLoading.value = false
  }
}

async function openTransferUserPicker(row) {
  transferOrderId.value = Number(row.ordersId || 0)
  userPickerMode.value = 'order-transfer'
  userPickerVisible.value = true
  await loadUserPickerData()
}

async function changeRoom(row) {
  await changeMerchantOrderRoomApi({ ordersId: changeRoomOrderId.value, goodsId: row.goodsId })
  showSuccess('换台成功')
  changeRoomVisible.value = false
  await loadOrders()
}

async function loadCouponIssues() {
  couponIssueLoading.value = true
  try {
    const data = await getMerchantCouponIssuePageApi({
      page: couponIssuePage.value - 1,
      size: couponIssueSize.value,
      couponName: '',
      tearoomName: '',
      type: couponIssueType.value,
      tearoomId: merchantId.value,
    })
    couponIssueList.value = data?.content || data?.records || data?.list || []
    couponIssueTotal.value = Number(data?.totalElements || data?.totalCount || data?.total || 0)
  } finally {
    couponIssueLoading.value = false
  }
}

async function loadCouponOptions() {
  couponListLoading.value = true
  try {
    const data = await getMerchantCouponListApi({
      page: 0,
      size: 100,
      type: couponIssueForm.type,
      status: '',
      couponName: '',
    })
    couponOptions.value = data?.content || data?.records || data?.list || []
  } finally {
    couponListLoading.value = false
  }
}

function syncCouponIssueSelection() {
  const current = couponOptions.value.find((item) => String(item.couponId) === String(couponIssueForm.couponId))
  if (!current) return
  couponIssueForm.couponName = current.couponName || ''
  couponIssueForm.couponType = current.couponType || ''
  couponIssueForm.hour = current.hour || ''
  couponIssueForm.integral = current.integral || ''
  couponIssueForm.validDay = current.validDay || ''
  couponIssueForm.minHour = current.minHour || ''
}

async function openCouponIssueDialog() {
  Object.assign(couponIssueForm, {
    type: 2,
    couponId: '',
    couponName: '',
    couponType: '',
    goodsIds: [],
    issueNumber: 0,
    isLimit: 1,
    status: 1,
    teamRule: '',
    buyRule: '',
    hour: '',
    integral: '',
    validDay: '',
    minHour: '',
  })
  couponIssueDialogVisible.value = true
  await Promise.all([loadCouponOptions(), loadGoods()])
}

async function submitCouponIssue() {
  if (!couponIssueForm.couponId) {
    showError('请选择优惠券')
    return
  }
  if (!couponIssueForm.goodsIds.length) {
    showError('请选择羽毛球场')
    return
  }
  if (Number(couponIssueForm.isLimit) === 1 && Number(couponIssueForm.issueNumber) <= 0) {
    showError('请输入发布数量')
    return
  }
  if (!textValue(couponIssueForm.teamRule) || !textValue(couponIssueForm.buyRule)) {
    showError('请填写使用规则和购买须知')
    return
  }
  couponIssueDialogLoading.value = true
  try {
    await createMerchantCouponIssueApi({
      couponId: couponIssueForm.couponId,
      tearoomId: merchantId.value,
      goodsIds: couponIssueForm.goodsIds.join(','),
      status: couponIssueForm.status,
      type: couponIssueForm.type,
      teamRule: couponIssueForm.teamRule,
      buyRule: couponIssueForm.buyRule,
      issueNumber: couponIssueForm.issueNumber,
      remainNumber: couponIssueForm.issueNumber,
      hour: couponIssueForm.hour,
      couponName: couponIssueForm.couponName,
      integral: couponIssueForm.integral,
      validDay: couponIssueForm.validDay,
      isLimit: couponIssueForm.isLimit,
      couponType: couponIssueForm.couponType,
      minHour: couponIssueForm.minHour,
    })
    couponIssueDialogVisible.value = false
    showSuccess('发布成功')
    await loadCouponIssues()
  } finally {
    couponIssueDialogLoading.value = false
  }
}

async function removeCouponIssue(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMerchantCouponIssueApi(row.couponIssueId)
  showSuccess('删除成功')
  await loadCouponIssues()
}

async function toggleCouponIssueStatus(row, previousValue) {
  try {
    await updateMerchantCouponIssueStatusApi({
      fabu_coupon_id: row.couponIssueId,
      status: Number(row.status || 0),
    })
    showSuccess('操作成功')
  } catch (error) {
    row.status = previousValue
  }
}

async function loadCouponRecords() {
  couponRecordLoading.value = true
  try {
    const data = await getUserCouponsApi({
      page: couponRecordPage.value - 1,
      size: couponRecordSize.value,
      couponName: couponRecordFilters.couponName,
      type: couponIssueType.value,
      nickName: '',
      tearoomId: merchantId.value,
      platformType: couponRecordFilters.platformType,
    })
    couponRecordList.value = data?.content || data?.records || data?.list || []
    couponRecordTotal.value = Number(data?.totalElements || data?.totalCount || data?.total || 0)
  } finally {
    couponRecordLoading.value = false
  }
}

function openUserPicker() {
  userPickerMode.value = 'merchant-owner'
  userPickerVisible.value = true
  loadUserPickerData()
}

function buildUserPickerParams() {
  const params = {
    page: userPickerPage.value,
    limit: userPickerSize.value,
    userName: textValue(userPickerFilters.userName),
    phone: textValue(userPickerFilters.phone),
    invitationCode: textValue(userPickerFilters.invitationCode),
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadUserPickerData() {
  userPickerLoading.value = true
  try {
    const data = await getUserCenterPage(buildUserPickerParams())
    userPickerData.value = data.list || []
    userPickerTotal.value = Number(data.total || 0)
  } finally {
    userPickerLoading.value = false
  }
}

function selectMerchantOwner(row) {
  infoForm.userId = Number(row.userId || 0)
  infoForm.userName = row.userName || ''
  userPickerVisible.value = false
}

async function addEmployee(row) {
  await createMerchantEmployeeApi({ phone: row.phone, tearoomId: merchantId.value })
  userPickerVisible.value = false
  showSuccess('员工已添加')
  await loadEmployees()
}

function openPrizeRecordUserPicker() {
  userPickerMode.value = 'prize-record-user'
  userPickerVisible.value = true
  loadUserPickerData()
}

function openPresetPrizeRecordUserPicker() {
  userPickerMode.value = 'preset-prize-record-user'
  userPickerVisible.value = true
  loadUserPickerData()
}

function handleUserPickerAction(row) {
  if (userPickerMode.value === 'merchant-owner') return selectMerchantOwner(row)
  if (userPickerMode.value === 'order-transfer') {
    updateOrderApi({
      ordersId: transferOrderId.value,
      userId: row.userId,
      userNam: row.userName,
      phone: row.phone,
    }).then(async () => {
      userPickerVisible.value = false
      showSuccess('转单成功')
      await loadOrders()
    })
    return
  }
  if (userPickerMode.value === 'prize-record-user') {
    prizeRecordForm.userId = Number(row.userId || 0)
    prizeRecordForm.userName = row.userName || ''
    userPickerVisible.value = false
    return
  }
  if (userPickerMode.value === 'preset-prize-record-user') {
    presetPrizeRecordForm.userId = Number(row.userId || 0)
    presetPrizeRecordForm.userName = row.userName || ''
    userPickerVisible.value = false
    return
  }
  return addEmployee(row)
}

function openPrizePicker(mode) {
  prizePickerMode.value = mode
  prizePickerVisible.value = true
  loadPrizeConfigList()
}

function selectPrizeForForm(row) {
  if (prizePickerMode.value === 'prize-record') {
    prizeRecordForm.prizeId = Number(row.prizeId || 0)
    prizeRecordForm.prizeName = row.prizeName || ''
    prizeRecordForm.prizeImage = row.prizeImage || ''
    prizeRecordForm.prizeType = Number(row.prizeType || 1)
    prizeRecordForm.parent = row.parent || ''
  } else {
    presetPrizeRecordForm.prizeId = Number(row.prizeId || 0)
    presetPrizeRecordForm.prizeName = row.prizeName || ''
    presetPrizeRecordForm.prizeImage = row.prizeImage || ''
    presetPrizeRecordForm.prizeType = Number(row.prizeType || 1)
    presetPrizeRecordForm.parent = row.parent || ''
  }
  prizePickerVisible.value = false
}

function buildStatParams() {
  return {
    tearoomId: merchantId.value,
    time: statDate.value,
    type: statWay.value,
    flag: statWay.value,
  }
}

async function loadStats() {
  statsLoading.open = true
  statsLoading.income = true
  try {
    const [openData, incomeData] = await Promise.all([
      getMerchantOpenStatsApi(buildStatParams()),
      getMerchantIncomeStatsApi(buildStatParams()),
    ])
    openStats.value = openData || {}
    incomeStats.value = incomeData || {}
  } finally {
    statsLoading.open = false
    statsLoading.income = false
  }
}

async function loadRevenueSource() {
  statsLoading.revenue = true
  try {
    const data = await getMerchantRevenueSourceApi({
      startTime: revenueStart.value,
      endTime: revenueEnd.value,
      type: revenueType.value,
      tearoomId: merchantId.value,
    })
    revenueStats.value = data || {}
  } finally {
    statsLoading.revenue = false
  }
}

async function loadRevenueAnalysis() {
  statsLoading.revenue = true
  try {
    revenueAnalysis.value = await getMerchantRevenueTrendApi({ tearoomId: merchantId.value })
  } finally {
    statsLoading.revenue = false
  }
}

async function loadWriteOffStats() {
  statsLoading.revenue = true
  try {
    writeOffStats.value = await getMerchantWriteOffStatsApi({
      time: statDate.value,
      flag: statWay.value,
      tearoomId: merchantId.value,
    })
  } finally {
    statsLoading.revenue = false
  }
}

async function loadEarningDetails() {
  earningDetailLoading.value = true
  try {
    const data = await getMerchantEarningDetailApi({
      page: earningDetailPage.value,
      limit: 10,
      way: earningDetailWay.value,
      tearoomId: merchantId.value,
    })
    const dateList = data?.dateTimeList || []
    const moneyList = data?.ordersMoneyList || []
    earningDetailList.value = dateList.map((dateTime, index) => ({
      dateTime,
      money: moneyList[index] || 0,
    }))
  } finally {
    earningDetailLoading.value = false
  }
}

async function loadEarningOrderList() {
  if (!earningOrderDateTime.value) return
  earningOrderLoading.value = true
  try {
    const data = await getMerchantOrderPageApi({
      page: earningOrderPage.value,
      limit: earningOrderSize.value,
      status: 4,
      dateTime: earningOrderDateTime.value,
      tearoomId: merchantId.value,
    })
    earningOrderList.value = data.list || []
    earningOrderTotal.value = Number(data.total || 0)
  } finally {
    earningOrderLoading.value = false
  }
}

async function openEarningOrderDialog(row) {
  earningOrderDateTime.value = row.dateTime || ''
  earningOrderPage.value = 1
  earningOrderVisible.value = true
  await loadEarningOrderList()
}

async function loadDurationRecords() {
  durationLoading.value = true
  try {
    const data = await getMerchantDurationStatsApi({
      startTime: orderFilters.startTime || '',
      endTime: orderFilters.endTime || '',
      tearoomId: merchantId.value,
    })
    durationList.value = (Array.isArray(data) ? data : data?.list || data?.records || []).map((item) => ({
      ...item,
      sumSecondText: formatDuration(item.sumSecond),
    }))
  } finally {
    durationLoading.value = false
  }
}

function formatDuration(totalSeconds) {
  const seconds = Number(totalSeconds || 0)
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainSeconds = seconds % 60
  return `${String(hours).padStart(2, '0')}时${String(minutes).padStart(2, '0')}分${String(remainSeconds).padStart(2, '0')}秒`
}

function buildWaterParams() {
  const params = {
    page: waterPage.value,
    limit: waterSize.value,
    tearoomId: merchantId.value,
    userName: textValue(waterFilters.userName),
    phone: textValue(waterFilters.phone),
  }
  Object.keys(params).forEach((key) => {
    if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
      delete params[key]
    }
  })
  return params
}

async function loadWaterRecords() {
  waterLoading.value = true
  try {
    const data = await getMerchantCollectWaterPageApi(buildWaterParams())
    waterList.value = data.list || []
    waterTotal.value = Number(data.total || 0)
  } finally {
    waterLoading.value = false
  }
}

function handleWaterSearch() {
  waterPage.value = 1
  loadWaterRecords()
}

function handleWaterReset() {
  waterFilters.userName = ''
  waterFilters.phone = ''
  handleWaterSearch()
}

function resolvePagedList(data) {
  return Array.isArray(data) ? data : data?.list || data?.records || data?.content || []
}

function resolvePagedTotal(data, list = []) {
  return Number(data?.totalCount || data?.total || data?.totalElements || list.length || 0)
}

async function loadQueueRecords() {
  queueLoading.value = true
  try {
    const data = await getMerchantQueuePageApi({
      page: queuePage.value,
      limit: queueSize.value,
      tearoomId: merchantId.value,
      status: queueFilters.status,
      createTime: queueFilters.createTime,
      goodsCategory: '',
    })
    queueList.value = data.list || []
    queueTotal.value = Number(data.total || 0)
  } finally {
    queueLoading.value = false
  }
}

async function markQueueMissed(row) {
  await confirmAction('确定要过号吗？')
  await updateMerchantQueueApi({ queueUpId: row.queueUpId, status: 3 })
  showSuccess('操作成功')
  await loadQueueRecords()
}

async function loadLoveWallet() {
  loveWalletLoading.value = true
  try {
    const [summary, detailData] = await Promise.all([
      getMerchantLoveWalletApi({ tearoomId: merchantId.value }),
      getMerchantLoveWalletDetailPageApi({
        page: loveWalletPage.value,
        limit: loveWalletSize.value,
        tearoomId: merchantId.value,
        moneyType: loveWalletFilters.moneyType,
        userName: textValue(loveWalletFilters.userName),
        phone: textValue(loveWalletFilters.phone),
        title: textValue(loveWalletFilters.title),
        content: textValue(loveWalletFilters.content),
      }),
    ])
    loveWalletSummary.value = summary || {}
    loveWalletList.value = detailData.list || []
    loveWalletTotal.value = Number(detailData.total || 0)
  } finally {
    loveWalletLoading.value = false
  }
}

async function loadWelfareWords() {
  welfareWordLoading.value = true
  try {
    const data = await getMerchantWelfareWordPageApi({
      page: welfareWordPage.value,
      limit: welfareWordSize.value,
      welfareWordTitle: welfareWordKeyword.value,
      tearoomId: merchantId.value,
    })
    welfareWordList.value = data.list || []
    welfareWordTotal.value = Number(data.total || 0)
  } finally {
    welfareWordLoading.value = false
  }
}

function openWelfareWordDialog(row) {
  Object.assign(welfareWordForm, {
    welfareWordId: Number(row?.welfareWordId || 0),
    welfareWordTitle: row?.welfareWordTitle || '',
    welfareWordContent: row?.welfareWordContent || '',
    welfareImg: row?.welfareImg || '',
    sort: Number(row?.sort || 0),
  })
  welfareWordDialogVisible.value = true
}

async function submitWelfareWord() {
  welfareWordDialogLoading.value = true
  try {
    const payload = {
      welfareWordId: welfareWordForm.welfareWordId || undefined,
      welfareWordTitle: textValue(welfareWordForm.welfareWordTitle),
      welfareWordContent: welfareWordForm.welfareWordContent,
      welfareImg: textValue(welfareWordForm.welfareImg),
      sort: Number(welfareWordForm.sort || 0),
      tearoomId: merchantId.value,
    }
    if (payload.welfareWordId) {
      await updateMerchantWelfareWordApi(payload)
    } else {
      delete payload.welfareWordId
      await createMerchantWelfareWordApi(payload)
    }
    welfareWordDialogVisible.value = false
    showSuccess('操作成功')
    await loadWelfareWords()
  } finally {
    welfareWordDialogLoading.value = false
  }
}

async function removeWelfareWord(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMerchantWelfareWordApi({ welfareWordId: row.welfareWordId })
  showSuccess('删除成功')
  await loadWelfareWords()
}

async function loadMemberWallet() {
  memberWalletLoading.value = true
  try {
    const [summary, walletData] = await Promise.all([
      getMerchantMemberWalletSummaryApi({ tearoomId: merchantId.value }),
      getMerchantMemberWalletPageApi({
        page: memberWalletPage.value,
        limit: memberWalletSize.value,
        phone: memberWalletKeyword.value,
        tearoomId: merchantId.value,
      }),
    ])
    memberWalletSummary.value = summary || {}
    const list = resolvePagedList(walletData)
    memberWalletList.value = list
    memberWalletTotal.value = resolvePagedTotal(walletData, list)
  } finally {
    memberWalletLoading.value = false
  }
}

function openMemberWalletDialog(row) {
  Object.assign(memberWalletForm, {
    memberUserId: Number(row?.memberUserId || 0),
    phone: row?.phone || '',
    money: row?.money ?? '',
  })
  memberWalletDialogVisible.value = true
}

async function submitMemberWallet() {
  memberWalletDialogLoading.value = true
  try {
    const payload = {
      memberUserId: memberWalletForm.memberUserId || undefined,
      phone: textValue(memberWalletForm.phone),
      money: memberWalletForm.money,
      tearoomId: merchantId.value,
    }
    if (payload.memberUserId) {
      await updateMerchantMemberWalletApi(payload)
    } else {
      delete payload.memberUserId
      await createMerchantMemberWalletApi(payload)
    }
    memberWalletDialogVisible.value = false
    showSuccess('操作成功')
    await loadMemberWallet()
  } finally {
    memberWalletDialogLoading.value = false
  }
}

async function removeMemberWallet(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMerchantMemberWalletApi({ memberUserMoneyId: row.memberUserId })
  showSuccess('删除成功')
  await loadMemberWallet()
}

async function loadMemberWalletDetails() {
  memberWalletDetailLoading.value = true
  try {
    const data = await getMerchantMemberWalletDetailPageApi({
      page: memberWalletDetailPage.value,
      limit: memberWalletDetailSize.value,
      phone: memberWalletKeyword.value,
      tearoomId: merchantId.value,
    })
    const list = resolvePagedList(data)
    memberWalletDetailList.value = list
    memberWalletDetailTotal.value = resolvePagedTotal(data, list)
  } finally {
    memberWalletDetailLoading.value = false
  }
}

function openMemberWalletDetailDialog(row) {
  Object.assign(memberWalletDetailForm, {
    id: Number(row?.id || 0),
    memberUserId: Number(row?.memberUserId || 0),
    phone: row?.phone || '',
    type: Number(row?.type || 1),
    money: row?.money ?? '',
    title: row?.title || '',
    content: row?.content || '',
  })
  memberWalletDetailDialogVisible.value = true
}

async function submitMemberWalletDetail() {
  memberWalletDetailDialogLoading.value = true
  try {
    const payload = {
      id: memberWalletDetailForm.id || undefined,
      memberUserId: memberWalletDetailForm.memberUserId || undefined,
      phone: textValue(memberWalletDetailForm.phone),
      tearoomId: merchantId.value,
      type: Number(memberWalletDetailForm.type || 1),
      money: memberWalletDetailForm.money,
      title: textValue(memberWalletDetailForm.title),
      content: textValue(memberWalletDetailForm.content),
    }
    if (payload.id) {
      await updateMerchantMemberWalletDetailApi(payload)
    } else {
      delete payload.id
      await createMerchantMemberWalletDetailApi(payload)
    }
    memberWalletDetailDialogVisible.value = false
    showSuccess('操作成功')
    await loadMemberWalletDetails()
  } finally {
    memberWalletDetailDialogLoading.value = false
  }
}

async function removeMemberWalletDetail(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMerchantMemberWalletDetailApi({ memberUserMoneyDetailsId: row.id })
  showSuccess('删除成功')
  await loadMemberWalletDetails()
}

async function loadMusicList() {
  musicLoading.value = true
  try {
    const data = await getMerchantMusicPageApi({
      page: musicPage.value,
      limit: musicSize.value,
      search: musicKeyword.value,
      tearoomId: merchantId.value,
    })
    const list = resolvePagedList(data)
    musicList.value = list
    musicTotal.value = resolvePagedTotal(data, list)
  } finally {
    musicLoading.value = false
  }
}

async function loadMusicTypeOptions() {
  const data = await getMusicTypePageApi({ page: 1, limit: 50 })
  musicTypeOptions.value = resolvePagedList(data)
}

async function loadMusicLibraryList() {
  musicLibraryLoading.value = true
  try {
    const data = await getMusicLibraryPageApi({
      page: musicLibraryPage.value,
      limit: musicLibrarySize.value,
      musicTypeId: musicLibraryFilters.musicTypeId,
      musicName: textValue(musicLibraryFilters.musicName),
      musicAuthor: textValue(musicLibraryFilters.musicAuthor),
      tearoomName: '',
    })
    const list = resolvePagedList(data)
    musicLibraryList.value = list
    musicLibraryTotal.value = resolvePagedTotal(data, list)
  } finally {
    musicLibraryLoading.value = false
  }
}

async function openMusicLibraryDialog() {
  musicLibraryVisible.value = true
  musicLibraryPage.value = 1
  musicLibraryFilters.musicTypeId = ''
  musicLibraryFilters.musicName = ''
  musicLibraryFilters.musicAuthor = ''
  await Promise.all([loadMusicTypeOptions(), loadMusicLibraryList()])
}

async function selectMusicLibraryRow(row) {
  await createMerchantMusicApi({
    musicLibraryId: row.musicLibraryId,
    musicUrl: row.musicUrl,
    musicName: row.musicName,
    tearoomId: merchantId.value,
    musicPicUrl: row.musicPicUrl,
    musicAuthor: row.musicAuthor,
  })
  musicLibraryVisible.value = false
  showSuccess('操作成功')
  await loadMusicList()
}

async function loadPrizeConfigList() {
  prizeConfigLoading.value = true
  try {
    const data = await getMerchantPrizePageApi({ page: prizeConfigPage.value, limit: prizeConfigSize.value, tearoomId: merchantId.value })
    const list = resolvePagedList(data)
    prizeConfigList.value = list
    prizeConfigTotal.value = resolvePagedTotal(data, list)
  } finally {
    prizeConfigLoading.value = false
  }
}

function openPrizeConfigDialog(row) {
  Object.assign(prizeConfigForm, {
    prizeId: Number(row?.prizeId || 0),
    prizeType: Number(row?.prizeType || 1),
    prizeName: row?.prizeName || '',
    prizeImage: row?.prizeImage || '',
    weight: row?.weight ?? '',
    sort: row?.sort ?? '',
    parent: row?.parent || '',
  })
  prizeConfigDialogVisible.value = true
}

async function submitPrizeConfig() {
  prizeConfigDialogLoading.value = true
  try {
    const payload = {
      prizeId: prizeConfigForm.prizeId || undefined,
      prizeType: Number(prizeConfigForm.prizeType || 1),
      prizeName: textValue(prizeConfigForm.prizeName),
      prizeImage: textValue(prizeConfigForm.prizeImage),
      weight: prizeConfigForm.weight,
      sort: prizeConfigForm.sort,
      parent: textValue(prizeConfigForm.parent),
      tearoomId: merchantId.value,
    }
    if (payload.prizeId) {
      await updateMerchantPrizeApi(payload)
    } else {
      delete payload.prizeId
      await createMerchantPrizeApi(payload)
    }
    prizeConfigDialogVisible.value = false
    showSuccess('操作成功')
    await loadPrizeConfigList()
  } finally {
    prizeConfigDialogLoading.value = false
  }
}

async function removePrizeConfig(row) {
  await confirmAction('确定删除此条信息?')
  await deleteMerchantPrizeApi(row.prizeId, merchantId.value)
  showSuccess('删除成功')
  await loadPrizeConfigList()
}

async function loadPrizeRecords() {
  prizeRecordLoading.value = true
  try {
    const data = await getMerchantPrizeRecordPageApi({
      page: prizeRecordPage.value,
      limit: prizeRecordSize.value,
      userName: prizeRecordFilters.userName,
      phone: prizeRecordFilters.phone,
      prizeName: prizeRecordFilters.prizeName,
    })
    const list = resolvePagedList(data)
    prizeRecordList.value = list
    prizeRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    prizeRecordLoading.value = false
  }
}

function openPrizeRecordDialog(row) {
  Object.assign(prizeRecordForm, {
    prizeUserId: Number(row?.prizeUserId || 0),
    userId: Number(row?.userId || 0),
    userName: row?.userName || '',
    prizeId: Number(row?.prizeId || 0),
    prizeName: row?.prizeName || '',
    prizeImage: row?.prizeImage || '',
    prizeType: Number(row?.prizeType || 1),
    parent: row?.parent || '',
    weight: row?.weight ?? '',
  })
  prizeRecordDialogVisible.value = true
}

async function submitPrizeRecord() {
  if (!prizeRecordForm.userId || !prizeRecordForm.prizeId) {
    showError('请填写用户ID和奖品ID')
    return
  }
  prizeRecordDialogLoading.value = true
  try {
    const payload = {
      prizeUserId: prizeRecordForm.prizeUserId || undefined,
      prizeId: prizeRecordForm.prizeId,
      prizeName: textValue(prizeRecordForm.prizeName),
      prizeImage: textValue(prizeRecordForm.prizeImage),
      prizeType: Number(prizeRecordForm.prizeType || 1),
      parent: textValue(prizeRecordForm.parent),
      userId: prizeRecordForm.userId,
      weight: prizeRecordForm.weight,
      tearoomId: merchantId.value,
    }
    if (payload.prizeUserId) {
      await updateMerchantPrizeRecordApi(payload)
    } else {
      delete payload.prizeUserId
      await createMerchantPrizeRecordApi(payload)
    }
    prizeRecordDialogVisible.value = false
    showSuccess('操作成功')
    await loadPrizeRecords()
  } finally {
    prizeRecordDialogLoading.value = false
  }
}

function openPrizeShipDialog(row) {
  Object.assign(prizeShipForm, {
    prizeUserId: Number(row?.prizeUserId || 0),
    prizeId: Number(row?.prizeId || 0),
    prizeName: row?.prizeName || '',
    prizeImage: row?.prizeImage || '',
    prizeType: Number(row?.prizeType || 1),
    parent: row?.parent || '',
    userId: Number(row?.userId || 0),
    weight: row?.weight ?? '',
    expressNumber: row?.expressNumber || '',
    status: 2,
  })
  prizeShipDialogVisible.value = true
}

async function submitPrizeShipment() {
  if (!textValue(prizeShipForm.expressNumber)) {
    showError('请输入快递单号')
    return
  }
  prizeShipDialogLoading.value = true
  try {
    await updateMerchantPrizeRecordApi({
      prizeUserId: prizeShipForm.prizeUserId,
      prizeId: prizeShipForm.prizeId,
      prizeName: prizeShipForm.prizeName,
      prizeImage: prizeShipForm.prizeImage,
      prizeType: prizeShipForm.prizeType,
      parent: prizeShipForm.parent,
      userId: prizeShipForm.userId,
      weight: prizeShipForm.weight,
      tearoomId: merchantId.value,
      expressNumber: textValue(prizeShipForm.expressNumber),
      status: prizeShipForm.status,
    })
    prizeShipDialogVisible.value = false
    showSuccess('发货成功')
    await loadPrizeRecords()
  } finally {
    prizeShipDialogLoading.value = false
  }
}

async function openPrizeLogisticsDialog(row) {
  if (!textValue(row?.expressNumber)) {
    showError('当前记录暂无快递单号')
    return
  }
  prizeLogisticsVisible.value = true
  prizeLogisticsLoading.value = true
  try {
    const data = await getMerchantPrizeLogisticsApi(row.expressNumber)
    prizeLogisticsList.value = data?.list || data?.data?.list || data?.result?.list || []
  } finally {
    prizeLogisticsLoading.value = false
  }
}

async function loadCountRecords() {
  countRecordLoading.value = true
  try {
    const data = await getPrizeGoldenDetailsApi({
      page: countRecordPage.value,
      limit: countRecordSize.value,
      userId: '',
      userName: countRecordUserName.value,
      phone: '',
      platform: '',
      tearoomId: merchantId.value,
    })
    const list = resolvePagedList(data)
    countRecordList.value = list
    countRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    countRecordLoading.value = false
  }
}

function openCountUpdateDialog(row) {
  countUpdateForm.userId = Number(row.userId || 0)
  countUpdateForm.type = 1
  countUpdateForm.goldenNum = ''
  countUpdateDialogVisible.value = true
}

async function submitCountUpdate() {
  countUpdateDialogLoading.value = true
  try {
    await updateUserPrizeGoldenApi({
      goldenNum: countUpdateForm.goldenNum,
      type: countUpdateForm.type,
      userId: countUpdateForm.userId,
      tearoomId: merchantId.value,
    })
    countUpdateDialogVisible.value = false
    showSuccess('修改成功')
    await loadMerchantUsers()
  } finally {
    countUpdateDialogLoading.value = false
  }
}

async function openUserCountDetail(row) {
  countRecordPage.value = 1
  userCountDetailVisible.value = true
  countRecordLoading.value = true
  try {
    const data = await getPrizeGoldenDetailsApi({ page: countRecordPage.value, limit: countRecordSize.value, userId: row.userId, userName: '', phone: '', platform: '', tearoomId: merchantId.value })
    const list = resolvePagedList(data)
    countRecordList.value = list
    countRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    countRecordLoading.value = false
  }
}

async function openUserPrizeDetail(row) {
  prizeRecordPage.value = 1
  userPrizeDetailVisible.value = true
  prizeRecordLoading.value = true
  try {
    const data = await getPrizeRecordsApi({ page: prizeRecordPage.value, limit: prizeRecordSize.value, userId: row.userId, prizeName: '', status: '', tearoomId: merchantId.value })
    const list = resolvePagedList(data)
    prizeRecordList.value = list
    prizeRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    prizeRecordLoading.value = false
  }
}

async function openUserRechargeDetail(row) {
  rechargeRecordPage.value = 1
  userRechargeDetailVisible.value = true
  rechargeRecordLoading.value = true
  try {
    const data = await getPrizeRechargeRecordsApi({ page: rechargeRecordPage.value, limit: rechargeRecordSize.value, userId: row.userId, platform: '', tearoomId: merchantId.value })
    const list = resolvePagedList(data)
    rechargeRecordList.value = list
    rechargeRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    rechargeRecordLoading.value = false
  }
}

async function loadRechargeRecords() {
  rechargeRecordLoading.value = true
  try {
    const data = await getPrizeRechargeRecordsApi({
      page: rechargeRecordPage.value,
      limit: rechargeRecordSize.value,
      userId: '',
      userName: rechargeRecordUserName.value,
      phone: '',
      tearoomId: merchantId.value,
    })
    const list = resolvePagedList(data)
    rechargeRecordList.value = list
    rechargeRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    rechargeRecordLoading.value = false
  }
}

async function loadPresetPrizeRecords() {
  presetPrizeRecordLoading.value = true
  try {
    const data = await getMerchantPresetPrizeRecordPageApi({
      page: presetPrizeRecordPage.value,
      limit: presetPrizeRecordSize.value,
      status: presetPrizeRecordFilters.status,
      userName: presetPrizeRecordFilters.userName,
      phone: presetPrizeRecordFilters.phone,
      prizeName: presetPrizeRecordFilters.prizeName,
      prizeType: '',
    })
    const list = resolvePagedList(data)
    presetPrizeRecordList.value = list
    presetPrizeRecordTotal.value = resolvePagedTotal(data, list)
  } finally {
    presetPrizeRecordLoading.value = false
  }
}

function openPresetPrizeRecordDialog(row) {
  Object.assign(presetPrizeRecordForm, {
    prizeUserSysId: Number(row?.prizeUserSysId || 0),
    userId: Number(row?.userId || 0),
    userName: row?.userName || '',
    prizeId: Number(row?.prizeId || 0),
    prizeName: row?.prizeName || '',
    prizeImage: row?.prizeImage || '',
    prizeType: Number(row?.prizeType || 1),
    parent: row?.parent || '',
    weight: row?.weight ?? '',
    status: Number(row?.status || 1),
  })
  presetPrizeRecordDialogVisible.value = true
}

async function submitPresetPrizeRecord() {
  if (!presetPrizeRecordForm.userId || !presetPrizeRecordForm.prizeId) {
    showError('请填写用户ID和奖品ID')
    return
  }
  presetPrizeRecordDialogLoading.value = true
  try {
    const payload = {
      prizeUserSysId: presetPrizeRecordForm.prizeUserSysId || undefined,
      prizeId: presetPrizeRecordForm.prizeId,
      prizeName: textValue(presetPrizeRecordForm.prizeName),
      prizeImage: textValue(presetPrizeRecordForm.prizeImage),
      prizeType: Number(presetPrizeRecordForm.prizeType || 1),
      parent: textValue(presetPrizeRecordForm.parent),
      userId: presetPrizeRecordForm.userId,
      weight: presetPrizeRecordForm.weight,
      tearoomId: merchantId.value,
      status: Number(presetPrizeRecordForm.status || 1),
    }
    if (payload.prizeUserSysId) {
      await updateMerchantPresetPrizeRecordApi(payload)
    } else {
      delete payload.prizeUserSysId
      await createMerchantPresetPrizeRecordApi(payload)
    }
    presetPrizeRecordDialogVisible.value = false
    showSuccess('操作成功')
    await loadPresetPrizeRecords()
  } finally {
    presetPrizeRecordDialogLoading.value = false
  }
}

async function removePresetPrizeRecord(row) {
  await confirmAction('确定删除预设奖品?')
  await deleteMerchantPresetPrizeRecordApi({ prizeUserSysId: row.prizeUserSysId })
  showSuccess('删除成功')
  await loadPresetPrizeRecords()
}

async function loadMerchantUsers() {
  userListLoading.value = true
  try {
    const data = await getUserCenterPage({
      page: userListPage.value,
      limit: userListSize.value,
      userName: userListFilters.userName,
      inviterCode: userListFilters.inviterCode,
      startTime: '',
      endTime: '',
      phone: userListFilters.phone,
      prizeTearoomId: merchantId.value,
    })
    userList.value = data.list || []
    userListTotal.value = Number(data.total || 0)
  } finally {
    userListLoading.value = false
  }
}

function handleTabChange(name) {
  activeTab.value = name
}

watch(
  activeTab,
  async (value) => {
    router.replace({ query: { ...route.query, tab: value } })
    if (value === 'open-stats') await loadStats()
    if (value === 'income') await Promise.all([loadStats(), loadGoods()])
    if (value === 'goods') await loadGoods()
    if (value === 'orders') await loadOrders()
    if (value === 'coupon-issue') await loadCouponIssues()
    if (value === 'coupon-record') await loadCouponRecords()
    if (value === 'employees') await loadEmployees()
    if (value === 'refund') await loadRefundRules()
    if (value === 'stats') await loadStats()
    if (value === 'revenue') await loadRevenueSource()
    if (value === 'revenue-analysis') await loadRevenueAnalysis()
    if (value === 'write-off') await loadWriteOffStats()
    if (value === 'earning-detail') await loadEarningDetails()
    if (value === 'duration') await loadDurationRecords()
    if (value === 'queue') await loadQueueRecords()
    if (value === 'love-wallet') await loadLoveWallet()
    if (value === 'love-board') await loadWelfareWords()
    if (value === 'member-wallet') await loadMemberWallet()
    if (value === 'member-wallet-detail') await loadMemberWalletDetails()
    if (value === 'water') await loadWaterRecords()
    if (value === 'music') await loadMusicList()
    if (value === 'prize-config') await loadPrizeConfigList()
    if (value === 'prize-record') await loadPrizeRecords()
    if (value === 'count-record') await loadCountRecords()
    if (value === 'count-recharge') await loadRechargeRecords()
    if (value === 'preset-prize-record') await loadPresetPrizeRecords()
    if (value === 'users') await loadMerchantUsers()
  },
  { immediate: false },
)

onMounted(async () => {
  await loadDetail()
  if (activeTab.value === 'open-stats') await loadStats()
  if (activeTab.value === 'income') await Promise.all([loadStats(), loadGoods()])
  if (activeTab.value === 'goods') await loadGoods()
  if (activeTab.value === 'orders') await loadOrders()
  if (activeTab.value === 'coupon-issue') await loadCouponIssues()
  if (activeTab.value === 'coupon-record') await loadCouponRecords()
  if (activeTab.value === 'employees') await loadEmployees()
  if (activeTab.value === 'refund') await loadRefundRules()
  if (activeTab.value === 'stats') await loadStats()
  if (activeTab.value === 'revenue') await loadRevenueSource()
  if (activeTab.value === 'revenue-analysis') await loadRevenueAnalysis()
  if (activeTab.value === 'write-off') await loadWriteOffStats()
  if (activeTab.value === 'earning-detail') await loadEarningDetails()
  if (activeTab.value === 'duration') await loadDurationRecords()
  if (activeTab.value === 'queue') await loadQueueRecords()
  if (activeTab.value === 'love-wallet') await loadLoveWallet()
  if (activeTab.value === 'love-board') await loadWelfareWords()
  if (activeTab.value === 'member-wallet') await loadMemberWallet()
  if (activeTab.value === 'member-wallet-detail') await loadMemberWalletDetails()
  if (activeTab.value === 'water') await loadWaterRecords()
  if (activeTab.value === 'music') await loadMusicList()
  if (activeTab.value === 'prize-config') await loadPrizeConfigList()
  if (activeTab.value === 'prize-record') await loadPrizeRecords()
  if (activeTab.value === 'count-record') await loadCountRecords()
  if (activeTab.value === 'count-recharge') await loadRechargeRecords()
  if (activeTab.value === 'preset-prize-record') await loadPresetPrizeRecords()
  if (activeTab.value === 'users') await loadMerchantUsers()
})
</script>

<template>
  <div class="mdetail">
    <PageTopbar title="门店详情" @back="goBack" />

    <header class="mdetail-hero nf-page-hero">
      <span class="mdetail-hero__badge">羽毛球馆</span>
      <h1 class="mdetail-hero__title">门店详情</h1>
      <p class="mdetail-hero__desc">查看与编辑羽毛球馆基础信息、收入统计、订单、员工及运营配置，支持多 tab 业务管理</p>
    </header>

    <div class="mdetail-tabs" v-loading="detailLoading">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="item in tabs" :key="item.name" :label="item.label" :name="item.name" />
      </el-tabs>

      <div class="mdetail-body nf-table-card">
      <template v-if="activeTab === 'info'">
        <div class="detail-toolbar">
          <el-button v-if="!editable && isAuth('merchantList:update')" type="primary" @click="editable = true">修改</el-button>
          <template v-else-if="editable">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" :loading="saveLoading" @click="saveDetail">保存</el-button>
          </template>
        </div>

        <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="150px" class="merchant-info-form">
          <el-row :gutter="16">
            <el-col :span="24"><div class="form-section-title">图片信息</div></el-col>
            <el-col :span="12">
              <el-form-item label="羽毛球馆logo">
                <div class="upload-block">
                  <img v-if="infoForm.tearoomTitleImage" :src="infoForm.tearoomTitleImage" class="image-preview" />
                  <el-empty v-else description="暂无图片" :image-size="64" />
                  <el-upload v-if="editable" :show-file-list="false" :http-request="(option) => handleImageUpload('tearoomTitleImage', option)">
                    <el-button :loading="imageUploadingField === 'tearoomTitleImage'">上传</el-button>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="群二维码">
                <div class="upload-block">
                  <img v-if="infoForm.qunImg" :src="infoForm.qunImg" class="image-preview" />
                  <el-empty v-else description="暂无图片" :image-size="64" />
                  <el-upload v-if="editable" :show-file-list="false" :http-request="(option) => handleImageUpload('qunImg', option)">
                    <el-button :loading="imageUploadingField === 'qunImg'">上传</el-button>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="微信二维码">
                <div class="upload-block">
                  <img v-if="infoForm.wxCode" :src="infoForm.wxCode" class="image-preview" />
                  <el-empty v-else description="暂无图片" :image-size="64" />
                  <el-upload v-if="editable" :show-file-list="false" :http-request="(option) => handleImageUpload('wxCode', option)">
                    <el-button :loading="imageUploadingField === 'wxCode'">上传</el-button>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="场地图">
                <div class="upload-block">
                  <img v-if="infoForm.siteImg" :src="infoForm.siteImg" class="image-preview" />
                  <el-empty v-else description="暂无图片" :image-size="64" />
                  <el-upload v-if="editable" :show-file-list="false" :http-request="(option) => handleImageUpload('siteImg', option)">
                    <el-button :loading="imageUploadingField === 'siteImg'">上传</el-button>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24"><div class="form-section-title">基础信息</div></el-col>
            <el-col :span="12">
              <el-form-item label="绑定用户">
                <div class="bind-user-row">
                  <el-input v-model="infoForm.userName" readonly placeholder="请选择绑定用户" />
                  <el-button v-if="infoForm.userId" @click="goUserDetail(infoForm.userId)">查看用户</el-button>
                  <el-button v-if="editable" type="primary" plain @click="openUserPicker">选择</el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="店铺类型">
                <el-radio-group v-model="infoForm.shopType" :disabled="!editable">
                  <el-radio v-for="item in shopTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="羽毛球馆名称" prop="tearoomName"><el-input v-model="infoForm.tearoomName" :disabled="!editable" placeholder="请输入羽毛球馆名称" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="羽毛球馆电话" prop="tearoomPhone"><el-input v-model="infoForm.tearoomPhone" :disabled="!editable" placeholder="请输入羽毛球馆电话" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="佣金比例" prop="rate"><el-input v-model="infoForm.rate" :disabled="!editable" placeholder="请输入佣金比例" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="设备标识码"><el-input v-model="infoForm.keyCode" :disabled="!editable" placeholder="请输入设备标识码" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="快手门店ID"><el-input v-model="infoForm.shopPoiId" :disabled="!editable" placeholder="请输入快手门店ID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="快手APPID"><el-input v-model="infoForm.ksAppId" :disabled="!editable" placeholder="请输入快手APPID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="快手AppSecret"><el-input v-model="infoForm.ksAppSecret" :disabled="!editable" placeholder="请输入快手AppSecret" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="美团Email"><el-input v-model="infoForm.mtEmail" :disabled="!editable" placeholder="请输入美团Email" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="美团密码"><el-input v-model="infoForm.mtPassword" :disabled="!editable" placeholder="请输入美团密码" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="美团门店ID"><el-input v-model="infoForm.mtStoreId" :disabled="!editable" placeholder="请输入美团门店ID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="抖音门店ID"><el-input v-model="infoForm.dyPoiId" :disabled="!editable" placeholder="请输入抖音门店ID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="抖音AppID"><el-input v-model="infoForm.dyAppId" :disabled="!editable" placeholder="请输入抖音AppID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="抖音Secret"><el-input v-model="infoForm.dySecret" :disabled="!editable" placeholder="请输入抖音Secret" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="wifi账号"><el-input v-model="infoForm.wifiNo" :disabled="!editable" placeholder="请输入wifi账号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="wifi密码"><el-input v-model="infoForm.wifiPwd" :disabled="!editable" placeholder="请输入wifi密码" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="语音播报token"><el-input v-model="infoForm.yyToken" :disabled="!editable" placeholder="请输入语音播报token" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="语音播报ID"><el-input v-model="infoForm.yyId" :disabled="!editable" placeholder="请输入语音播报ID" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="播报内容"><el-input v-model="infoForm.broadcastContent" type="textarea" :rows="2" :disabled="!editable" placeholder="请输入播报内容" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="摄像头配置"><el-input v-model="infoForm.cameraId" :disabled="!editable" placeholder="请输入摄像头配置" /></el-form-item></el-col>
            <el-col :span="24"><div class="form-section-title">位置信息</div></el-col>
            <el-col :span="12"><el-form-item label="城市"><el-input :model-value="buildAddress()" disabled /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="省份"><el-input v-model="infoForm.tearoomProvince" :disabled="!editable" placeholder="请输入省份" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="城市"><el-input v-model="infoForm.tearoomCity" :disabled="!editable" placeholder="请输入城市" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="区县"><el-input v-model="infoForm.tearoomDistrict" :disabled="!editable" placeholder="请输入区县" /></el-form-item></el-col>
            <el-col :span="24">
              <el-form-item label="羽毛球馆地址">
                <div class="address-actions-row">
                  <el-input v-model="infoForm.tearoomAddress" :disabled="!editable" placeholder="请输入羽毛球馆地址" />
                  <el-button v-if="editable" :loading="mapLoading" @click="locateByAddress">获取经纬度</el-button>
                  <el-button v-if="editable" type="primary" plain @click="openMapDialog">地图选点</el-button>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="羽毛球馆经度"><el-input v-model="infoForm.tearoomLongitude" :disabled="!editable" placeholder="请输入羽毛球馆经度" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="羽毛球馆纬度"><el-input v-model="infoForm.tearoomLatitude" :disabled="!editable" placeholder="请输入羽毛球馆纬度" /></el-form-item></el-col>
            <el-col :span="24"><div class="form-section-title">营业与功能</div></el-col>
            <el-col :span="12"><el-form-item label="开店时间"><el-time-select v-model="infoForm.startTime" :disabled="!editable" placeholder="选择开店时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="闭店时间"><el-time-select v-model="infoForm.endTime" :disabled="!editable" placeholder="选择闭店时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="羽毛球馆状态"><el-radio-group v-model="infoForm.status" :disabled="!editable"><el-radio v-for="item in merchantStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="零钱操作权限"><el-radio-group v-model="infoForm.moneyScope" :disabled="!editable"><el-radio v-for="item in moneyScopeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否享受会员权益"><el-radio-group v-model="infoForm.isVipPrivilege" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否开启人脸识别"><el-radio-group v-model="infoForm.isFaceRecognition" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="门店是否支持点歌"><el-radio-group v-model="infoForm.isMusic" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否开启预定"><el-radio-group v-model="infoForm.isReserve" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否开启爱心活动"><el-radio-group v-model="infoForm.isWelfare" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="爱心活动介绍"><el-input v-model="infoForm.welfareContent" :disabled="!editable" placeholder="请输入爱心活动介绍" /></el-form-item></el-col>
            <el-col :span="12">
              <el-form-item label="爱心活动背景图">
                <div class="upload-block">
                  <img v-if="infoForm.welfareImg" :src="infoForm.welfareImg" class="image-preview" />
                  <el-empty v-else description="暂无图片" :image-size="64" />
                  <el-upload v-if="editable" :show-file-list="false" :http-request="(option) => handleImageUpload('welfareImg', option)">
                    <el-button :loading="imageUploadingField === 'welfareImg'">上传</el-button>
                  </el-upload>
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12"><el-form-item label="幸运用户序号"><el-input v-model="infoForm.luckySort" :disabled="!editable" placeholder="请输入幸运用户序号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="幸运用户播报内容"><el-input v-model="infoForm.luckyContent" :disabled="!editable" placeholder="请输入幸运用户播报内容" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否允许操作退款"><el-radio-group v-model="infoForm.isRefund" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="是否开启微信支付"><el-radio-group v-model="infoForm.isWxPay" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="扫码器是否自动开台"><el-radio-group v-model="infoForm.isQrCode" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="人员值守开始时间"><el-time-select v-model="infoForm.openTime1" :disabled="!editable" placeholder="选择开始时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="人员值守结束时间"><el-time-select v-model="infoForm.closeTime1" :disabled="!editable" placeholder="选择结束时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="定时开门开灯时间"><el-time-select v-model="infoForm.startTime2" :disabled="!editable" placeholder="选择开灯时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="定时关门关灯时间"><el-time-select v-model="infoForm.endTime2" :disabled="!editable" placeholder="选择关灯时间" :picker-options="{ start: '00:00', step: '00:01', end: '23:59' }" /></el-form-item></el-col>
            <el-col :span="24"><div class="form-section-title">抽奖配置</div></el-col>
            <el-col :span="12"><el-form-item label="是否开启抽奖"><el-radio-group v-model="infoForm.isPrize" :disabled="!editable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="当月开台几次赠送次数"><el-input v-model="infoForm.prizeNum" :disabled="!editable" placeholder="请输入当月开台几次赠送次数" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="当月抽奖日期"><el-input v-model="infoForm.prizeDay" :disabled="!editable" placeholder="请输入当月抽奖日期" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="单次开台时长大于几小时计数"><el-input v-model="infoForm.prizeHour" :disabled="!editable" placeholder="请输入单次开台时长大于几小时计数" /></el-form-item></el-col>
            <el-col :span="24"><el-form-item label="抽奖标题"><el-input v-model="infoForm.prizeTitle" :disabled="!editable" placeholder="请输入抽奖标题" /></el-form-item></el-col>
            <el-col :span="24">
              <el-form-item label="抽奖规则">
                <div v-if="editable" class="prize-editor">
                  <QuillEditor v-model:content="infoForm.prizeContent" content-type="html" theme="snow" />
                </div>
                <div v-else class="rich-preview" v-html="infoForm.prizeContent || '<p>-</p>'" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </template>

      <template v-else-if="activeTab === 'open-stats'">
        <div class="stats-toolbar">
          <el-select v-model="statWay" style="width: 160px" @change="loadStats">
            <el-option :value="1" label="按天查询" />
            <el-option :value="2" label="按月查询" />
            <el-option :value="3" label="按年查询" />
          </el-select>
          <el-date-picker v-model="statDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始时间" @change="loadStats" />
        </div>
        <el-row :gutter="16" v-loading="statsLoading.open">
          <el-col :span="6"><div class="stat-card"><span>总收益</span><strong>{{ amount(openStats.sumMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>总订单</span><strong>{{ openStats.sumCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>微信收益</span><strong>{{ amount(openStats.wxMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>微信订单</span><strong>{{ openStats.wxCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>现金收益</span><strong>{{ amount(openStats.xjMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>现金订单</span><strong>{{ openStats.xjCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>门店会员收益</span><strong>{{ amount(openStats.mdMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>门店会员订单</span><strong>{{ openStats.mdCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>美团核销收益</span><strong>{{ amount(openStats.mtMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>美团核销订单</span><strong>{{ openStats.mtCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>快手核销收益</span><strong>{{ amount(openStats.ksMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>快手核销订单</span><strong>{{ openStats.ksCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>抖音核销收益</span><strong>{{ amount(openStats.dyMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>抖音核销订单</span><strong>{{ openStats.dyCount || 0 }}单</strong></div></el-col>
        </el-row>
      </template>

      <template v-else-if="activeTab === 'income'">
        <div class="stats-toolbar">
          <el-select v-model="statWay" style="width: 160px" @change="loadStats">
            <el-option :value="1" label="按天查询" />
            <el-option :value="2" label="按月查询" />
            <el-option :value="3" label="按年查询" />
          </el-select>
          <el-date-picker v-model="statDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始时间" @change="loadStats" />
        </div>
        <el-row :gutter="16" v-loading="statsLoading.income">
          <el-col :span="6"><div class="stat-card"><span>总收入</span><strong>{{ amount(incomeStats.sumMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>当前收入</span><strong>{{ amount(incomeStats.money || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>微信收入</span><strong>{{ amount(incomeStats.wxMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>支付宝收入</span><strong>{{ amount(incomeStats.zfbMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>零钱收入</span><strong>{{ amount(incomeStats.lqMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>订单年成交量</span><strong>{{ incomeStats.yearCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>订单月成交量</span><strong>{{ incomeStats.monthCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>订单日成交量</span><strong>{{ incomeStats.dayCount || 0 }}单</strong></div></el-col>
        </el-row>
        <el-table v-loading="goodsLoading" :data="goodsList" border>
          <el-table-column prop="goodsId" label="编号" width="90" />
          <el-table-column prop="goodsName" label="羽毛球名称" min-width="160" />
          <el-table-column label="羽毛球状态" width="120"><template #default="{ row }">{{ goodsStatusText(row.status) }}</template></el-table-column>
          <el-table-column label="羽毛球总收益" width="140"><template #default="{ row }">{{ amount(row.sumMoney || 0) }}</template></el-table-column>
        </el-table>
      </template>

      <template v-else-if="activeTab === 'goods'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="羽毛球名称"><el-input v-model="goodsFilters.goodsName" clearable placeholder="请输入羽毛球名称" @keyup.enter="handleGoodsSearch" /></el-form-item>
          <el-form-item label="羽毛球场状态"><el-select v-model="goodsFilters.status" clearable placeholder="请选择" @change="handleGoodsSearch"><el-option v-for="item in goodsStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item><el-space><el-button type="primary" @click="handleGoodsSearch">查询</el-button><el-button @click="handleGoodsReset">重置</el-button><el-button type="primary" @click="openGoodsCreate">添加</el-button><el-button type="primary" plain :disabled="!goodsSelection.length" @click="downloadBatchGoodsQr">批量下载二维码</el-button><el-button type="warning" :disabled="!goodsSelection.length" @click="openBatchPriceDialog">批量修改价格</el-button></el-space></el-form-item>
        </el-form>

        <el-table v-loading="goodsLoading" :data="goodsList" border @selection-change="goodsSelection = $event">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="goodsId" label="编号" width="90" fixed="left" />
          <el-table-column label="开台前图片" width="100"><template #default="{ row }"><el-image v-if="row.goodsCoverImage" :src="row.goodsCoverImage" fit="cover" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
          <el-table-column label="开台后图片" width="100"><template #default="{ row }"><el-image v-if="row.goodsTitleImage" :src="row.goodsTitleImage" fit="cover" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
          <el-table-column prop="goodsName" label="羽毛球场名称" min-width="160" />
          <el-table-column prop="goodsType" label="羽毛球场信息" min-width="160" />
          <el-table-column prop="hourPrice" label="半小时价" width="120"><template #default="{ row }">{{ amount(row.hourPrice) }}</template></el-table-column>
          <el-table-column prop="minutePrice" label="分钟价" width="120"><template #default="{ row }">{{ amount(row.minutePrice) }}</template></el-table-column>
          <el-table-column prop="yyToken" label="语音播报token" min-width="180" />
          <el-table-column prop="yyId" label="语音播报ID" min-width="160" />
          <el-table-column prop="goodsTips" label="推流链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="cameraId" label="摄像头配置" min-width="160" />
          <el-table-column prop="sales" label="销量" width="100" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="开台二维码" width="120"><template #default="{ row }"><el-button size="small" type="primary" @click="openGoodsQr(row)">查看</el-button></template></el-table-column>
          <el-table-column label="是否线下开台" width="140"><template #default="{ row }"><el-switch :model-value="row.isOpen" :active-value="1" :inactive-value="2" :disabled="!isAuth('merchantList:update')" @change="updateMerchantGoodsApi({ ...row, isOpen: $event }).then(() => loadGoods())" /></template></el-table-column>
          <el-table-column label="羽毛球状态" width="170"><template #default="{ row }"><el-tag>{{ goodsStatusText(row.status) }}</el-tag><el-button size="small" type="primary" plain :disabled="!isAuth('merchantList:update')" @click="toggleGoodsStatus(row)">切换</el-button></template></el-table-column>
          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-space wrap>
                <el-button size="small" type="primary" @click="openGoodsEdit(row)">编辑</el-button>
                <el-button size="small" type="primary" plain @click="openGoodsStats(row)">数据详情</el-button>
                <el-button size="small" type="danger" plain @click="openGoodsLive(row)">直播</el-button>
                <el-button size="small" type="info" plain @click="openGoodsTimeDialog(row)">特殊价格</el-button>
                <el-button size="small" type="warning" plain @click="openGoodsSpecialTimeDialog(row)">特殊忙时</el-button>
                <el-button size="small" type="danger" :disabled="!isAuth('merchantList:delete')" @click="deleteGoods(row)">删除</el-button>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <BasePagination :current-page="goodsPage" :page-size="goodsSize" :total="goodsTotal" @update:currentPage="goodsPage = $event; loadGoods()" @update:pageSize="goodsSize = $event; goodsPage = 1; loadGoods()" />
      </template>

      <template v-else-if="activeTab === 'orders'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="开台类型"><el-select v-model="orderFilters.isShop" clearable placeholder="请选择" @change="handleOrderSearch"><el-option v-for="item in openTypeOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="订单状态"><el-select v-model="orderFilters.status" clearable placeholder="请选择" @change="handleOrderSearch"><el-option v-for="item in orderStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
          <el-form-item label="订单号"><el-input v-model="orderFilters.ordersNo" clearable placeholder="请输入订单号" @keyup.enter="handleOrderSearch" /></el-form-item>
          <el-form-item label="预约人电话"><el-input v-model="orderFilters.phone" clearable placeholder="请输入预约人电话" @keyup.enter="handleOrderSearch" /></el-form-item>
          <el-form-item label="预约人姓名"><el-input v-model="orderFilters.userName" clearable placeholder="请输入预约人姓名" @keyup.enter="handleOrderSearch" /></el-form-item>
          <el-form-item label="羽毛球场名称"><el-input v-model="orderFilters.goodsName" clearable placeholder="请输入羽毛球场名称" @keyup.enter="handleOrderSearch" /></el-form-item>
          <el-form-item label="开始时间"><el-date-picker v-model="orderFilters.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" /></el-form-item>
          <el-form-item label="截止时间"><el-date-picker v-model="orderFilters.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择截止时间" /></el-form-item>
          <el-form-item><el-space><el-button type="primary" @click="handleOrderSearch">查询</el-button><el-button @click="handleOrderReset">重置</el-button></el-space></el-form-item>
        </el-form>

        <el-table v-loading="orderLoading" :data="orderList" border>
          <el-table-column prop="ordersId" label="编号" width="90" fixed="left" />
          <el-table-column label="开台类型" width="100"><template #default="{ row }">{{ openTypeText(row.isShop) }}</template></el-table-column>
          <el-table-column prop="tearoomName" label="羽毛球馆" min-width="160" fixed="left" />
          <el-table-column label="羽毛球馆图片" width="110"><template #default="{ row }"><el-image v-if="firstImage(row.tearoomTitleImage)" :src="firstImage(row.tearoomTitleImage)" class="thumb-image" preview-teleported /><span v-else>暂无图片</span></template></el-table-column>
          <el-table-column prop="goodsName" label="羽毛球场名称" min-width="160" />
          <el-table-column prop="ordersNo" label="订单号" min-width="190" />
          <el-table-column label="订单类型" width="100"><template #default="{ row }">{{ orderTypeText(row.ordersType) }}</template></el-table-column>
          <el-table-column label="用户昵称" width="140"><template #default="{ row }"><span v-if="row.userId" class="link-text" @click="goUserDetail(row.userId)">{{ row.payUserName || '未绑定' }}</span><span v-else class="text-danger">商家开台</span></template></el-table-column>
          <el-table-column label="会员等级" width="100"><template #default="{ row }">{{ row.vip?.vipName || row.vipName || '-' }}</template></el-table-column>
          <el-table-column label="每小时价格" width="110"><template #default="{ row }"><span style="color: deepskyblue;">{{ row.goods?.hourPrice || '-' }}{{ row.goods?.hourPrice ? '元' : '' }}</span></template></el-table-column>
          <el-table-column label="每分钟价格" width="110"><template #default="{ row }"><span style="color: deepskyblue;">{{ row.goods?.minutePrice || '-' }}{{ row.goods?.minutePrice ? '元' : '' }}</span></template></el-table-column>
          <el-table-column label="小时数" width="90"><template #default="{ row }">{{ row.productId ? '-' : row.hour || '-' }}</template></el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="优惠券平台" width="100"><template #default="{ row }">{{ platformText(row.platformType) }}</template></el-table-column>
          <el-table-column prop="couponName" label="优惠券名称" min-width="120" />
          <el-table-column label="优惠券金额" width="110"><template #default="{ row }">{{ amount(row.couponMoney) }}元</template></el-table-column>
          <el-table-column label="会员优惠金额" width="120"><template #default="{ row }">{{ amount(row.vipMoney) }}元</template></el-table-column>
          <el-table-column label="支付金额" width="120"><template #default="{ row }"><span>{{ amount(row.payMoney) }}元</span><el-button size="small" type="primary" plain style="margin-top: 4px;" @click="openOrderVipDialog(row)">其他金额</el-button></template></el-table-column>
          <el-table-column label="已退款金额" width="120"><template #default="{ row }">{{ amount(row.refundMoney) }}元</template></el-table-column>
          <el-table-column prop="refundRemark" label="退款原因" min-width="160" />
          <el-table-column prop="endTime" label="结束时间" min-width="160" />
          <el-table-column prop="minuteNum" label="超出时间(分钟)" width="120" />
          <el-table-column prop="minuteMoney" label="超出费用" width="100" />
          <el-table-column prop="shopMoney" label="商家总收益" width="110" />
          <el-table-column label="支付方式" width="90"><template #default="{ row }">{{ payWayText(row.payWay) }}</template></el-table-column>
          <el-table-column label="订单状态" width="100"><template #default="{ row }">{{ orderStatusText(row.status) }}</template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column prop="payTime" label="付款时间" min-width="160" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button v-if="Number(row.status) === 3" size="small" type="primary" @click="openChangeRoomDialog(row)">换台</el-button>
                <el-button size="small" type="danger" plain :disabled="!isAuth('orderAdmin:zhuandan')" @click="openTransferUserPicker(row)">转单</el-button>
                <el-button size="small" type="warning" v-if="[2, 3, 4].includes(Number(row.status))" :disabled="!isAuth('orderAdmin:refund') || Number(row.payMoney || 0) <= Number(row.refundMoney || 0)" @click="refundAllOrder(row)">退全款</el-button>
                <el-button size="small" type="warning" plain v-if="[2, 3, 4].includes(Number(row.status))" :disabled="!isAuth('orderAdmin:refund') || Number(row.payMoney || 0) <= Number(row.refundMoney || 0)" @click="openRefundDialog(row)">部分退款</el-button>
                <el-button size="small" type="primary" plain @click="openRenewDialog(row)">续费记录</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="order-total-money">本页累计交易统计：{{ amount(orderPageTotalMoney) }}元</div>

        <BasePagination :current-page="orderPage" :page-size="orderSize" :total="orderTotal" @update:currentPage="orderPage = $event; loadOrders()" @update:pageSize="orderSize = $event; orderPage = 1; loadOrders()" />
      </template>

      <template v-else-if="activeTab === 'coupon-issue'">
        <div class="detail-toolbar">
          <el-select v-model="couponIssueType" style="width: 160px" @change="couponIssuePage = 1; loadCouponIssues()">
            <el-option :value="1" label="团购券" />
            <el-option :value="2" label="积分兑换券" />
            <el-option :value="3" label="新人券" />
            <el-option :value="4" label="会员券" />
          </el-select>
          <el-button type="primary" plain @click="couponIssuePage = 1; loadCouponIssues()">刷新</el-button>
          <el-button type="primary" @click="openCouponIssueDialog()">发布</el-button>
        </div>
        <el-table v-loading="couponIssueLoading" :data="couponIssueList" border>
          <el-table-column prop="couponIssueId" label="编号" width="90" />
          <el-table-column label="券类型" width="100"><template #default="{ row }">{{ Number(row.couponType) === 1 ? '优惠券' : Number(row.couponType) === 2 ? '实物' : Number(row.couponType) === 3 ? '定时券' : '-' }}</template></el-table-column>
          <el-table-column prop="couponName" label="优惠券名称" min-width="160" />
          <el-table-column prop="issueNumber" label="发布数量" width="100" />
          <el-table-column prop="remainNumber" label="剩余数量" width="100" />
          <el-table-column prop="hour" label="使用时长(半小时)" width="120" />
          <el-table-column prop="validDay" label="有效期限(天)" width="120" />
          <el-table-column prop="teamRule" label="使用规则" min-width="200" show-overflow-tooltip />
          <el-table-column prop="buyRule" label="购买须知" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-color="#13ce66"
                inactive-color="#ff4949"
                @change="toggleCouponIssueStatus(row, row.status === 1 ? 0 : 1)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" type="danger" @click="removeCouponIssue(row)">删除</el-button></template></el-table-column>
        </el-table>
        <BasePagination :current-page="couponIssuePage" :page-size="couponIssueSize" :total="couponIssueTotal" @update:currentPage="couponIssuePage = $event; loadCouponIssues()" @update:pageSize="couponIssueSize = $event; couponIssuePage = 1; loadCouponIssues()" />
      </template>

      <template v-else-if="activeTab === 'coupon-record'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="优惠券平台"><el-select v-model="couponRecordFilters.platformType" clearable @change="couponRecordPage = 1; loadCouponRecords()"><el-option :value="1" label="美团" /><el-option :value="2" label="抖音" /><el-option :value="3" label="快手" /></el-select></el-form-item>
          <el-form-item label="优惠券名称"><el-input v-model="couponRecordFilters.couponName" clearable placeholder="请输入优惠券名称" @keyup.enter="couponRecordPage = 1; loadCouponRecords()" /></el-form-item>
          <el-form-item><el-button type="primary" @click="couponRecordPage = 1; loadCouponRecords()">查询</el-button></el-form-item>
        </el-form>
        <el-table v-loading="couponRecordLoading" :data="couponRecordList" border>
          <el-table-column prop="couponUserId" label="编号" width="90" />
          <el-table-column label="优惠券平台" width="100"><template #default="{ row }">{{ platformText(row.platformType) }}</template></el-table-column>
          <el-table-column label="优惠券类型" width="100"><template #default="{ row }">{{ Number(row.type) === 2 ? '优惠券' : Number(row.type) === 3 ? '实物' : Number(row.type) === 4 ? '定时券' : '-' }}</template></el-table-column>
          <el-table-column prop="couponName" label="优惠券名称" min-width="160" />
          <el-table-column label="用户昵称" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.nickName || '未绑定' }}</span></template></el-table-column>
          <el-table-column prop="hour" label="使用时长(半小时)" width="120" />
          <el-table-column prop="failureTime" label="有效期限(天)" width="120" />
          <el-table-column label="状态" width="100"><template #default="{ row }">{{ Number(row.status) === 1 ? '未使用' : Number(row.status) === 2 ? '已使用' : Number(row.status) === 3 ? '已过期' : '-' }}</template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="couponRecordPage" :page-size="couponRecordSize" :total="couponRecordTotal" @update:currentPage="couponRecordPage = $event; loadCouponRecords()" @update:pageSize="couponRecordSize = $event; couponRecordPage = 1; loadCouponRecords()" />
      </template>

      <template v-else-if="activeTab === 'employees'">
        <div class="detail-toolbar">
          <el-button type="primary" @click="openUserPicker">添加员工</el-button>
        </div>
        <el-table v-loading="employeeLoading" :data="employeeList" border>
          <el-table-column prop="userId" label="编号" width="90" />
          <el-table-column prop="userName" label="昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '未设置' }}</span></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="age" label="年龄" width="90" />
          <el-table-column label="性别" width="80"><template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : Number(row.sex) === 2 ? '女' : '-' }}</template></el-table-column>
          <el-table-column label="工作时间" min-width="180"><template #default="{ row }">{{ row.startTime || '-' }} - {{ row.endTime || '-' }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }">{{ employeeStatusText(row.tearoomUserStatus) }}</template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="openEmployeeDialog(row)">设置</el-button>
                <el-button size="small" type="primary" plain @click="openEmployeeOpenStats(row)">开台统计</el-button>
                <el-button size="small" type="warning" plain @click="openEmployeeWorkRecords(row)">上班记录</el-button>
                <el-button size="small" type="warning" @click="openEmployeePermission(row)">查看权限</el-button>
                <el-button size="small" type="danger" @click="deleteEmployee(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <BasePagination :current-page="employeePage" :page-size="employeeSize" :total="employeeTotal" @update:currentPage="employeePage = $event; loadEmployees()" @update:pageSize="employeeSize = $event; employeePage = 1; loadEmployees()" />
      </template>

      <template v-else-if="activeTab === 'refund'">
        <div class="detail-toolbar">
          <el-button type="primary" @click="openRefundRuleDialog()">新增退款规则</el-button>
        </div>
        <el-table v-loading="refundRuleLoading" :data="refundRuleList" border>
          <el-table-column prop="refundRuleId" label="编号" width="90" />
          <el-table-column prop="hour" label="距离开始前时间(小时)" min-width="180" />
          <el-table-column prop="rate" label="退款比例" min-width="120" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
                <div class="action-buttons single-line">
                  <el-button size="small" type="primary" @click="openRefundRuleDialog(row)">修改</el-button>
                  <el-button size="small" type="danger" @click="deleteRefundRule(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
        </el-table>
        <BasePagination :current-page="refundRulePage" :page-size="refundRuleSize" :total="refundRuleTotal" @update:currentPage="refundRulePage = $event; loadRefundRules()" @update:pageSize="refundRuleSize = $event; refundRulePage = 1; loadRefundRules()" />
      </template>

      <template v-else-if="activeTab === 'stats'">
        <div class="stats-toolbar">
          <el-select v-model="statWay" style="width: 160px" @change="loadStats">
            <el-option :value="1" label="按天查询" />
            <el-option :value="2" label="按月查询" />
            <el-option :value="3" label="按年查询" />
          </el-select>
          <el-date-picker v-model="statDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始时间" @change="loadStats" />
        </div>
        <el-row :gutter="16" v-loading="statsLoading.open || statsLoading.income">
          <el-col :span="6"><div class="stat-card"><span>总收益</span><strong>{{ amount(openStats.sumMoney || incomeStats.sumMoney) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>总订单</span><strong>{{ openStats.sumCount || 0 }}单</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>微信收益</span><strong>{{ amount(openStats.wxMoney || incomeStats.wxMoney) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>抖音核销收益</span><strong>{{ amount(openStats.dyMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>快手核销收益</span><strong>{{ amount(openStats.ksMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>现金收益</span><strong>{{ amount(openStats.xjMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>门店会员收益</span><strong>{{ amount(openStats.mdMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>当前收入</span><strong>{{ amount(incomeStats.money || 0) }}元</strong></div></el-col>
        </el-row>
      </template>

      <template v-else-if="activeTab === 'revenue'">
        <div class="stats-toolbar">
          <el-select v-model="revenueType" style="width: 140px" @change="loadRevenueSource"><el-option :value="1" label="金额" /><el-option :value="2" label="订单" /></el-select>
          <el-date-picker v-model="revenueStart" type="date" value-format="YYYY-MM-DD" placeholder="选择开始时间" />
          <el-date-picker v-model="revenueEnd" type="date" value-format="YYYY-MM-DD" placeholder="选择截止时间" />
          <el-button type="primary" @click="loadRevenueSource">查询</el-button>
        </div>
        <el-row :gutter="16" v-loading="statsLoading.revenue">
          <el-col :span="6"><div class="stat-card"><span>{{ revenueType === 1 ? '营销总额' : '总销量' }}</span><strong>{{ revenueType === 1 ? `${amount(revenueStats.sumMoney || 0)}元` : `${revenueStats.sumCount || 0}单` }}</strong></div></el-col>
        </el-row>
        <div v-for="(item, index) in revenueStats.list || []" :key="index" class="revenue-day-card">
          <div class="revenue-day-title">日期：{{ item.date }} <span>{{ revenueType === 1 ? `日营业额：${item.dayTotalMoney || 0}元` : `日单量：${item.dayTotalCount || 0}单` }}</span></div>
          <el-row :gutter="16">
            <el-col :span="6"><div class="stat-card"><span>抖音</span><strong>{{ revenueType === 1 ? `${amount(item.dyMoney || 0)}元` : `${item.dyCount || 0}单` }}</strong></div></el-col>
            <el-col :span="6"><div class="stat-card"><span>快手</span><strong>{{ revenueType === 1 ? `${amount(item.ksMoney || 0)}元` : `${item.ksCount || 0}单` }}</strong></div></el-col>
            <el-col :span="6"><div class="stat-card"><span>美团</span><strong>{{ revenueType === 1 ? `${amount(item.mtMoney || 0)}元` : `${item.mtCount || 0}单` }}</strong></div></el-col>
            <el-col :span="6"><div class="stat-card"><span>微信</span><strong>{{ revenueType === 1 ? `${amount(item.wxMoney || 0)}元` : `${item.wxCount || 0}单` }}</strong></div></el-col>
          </el-row>
        </div>
      </template>

      <template v-else-if="activeTab === 'revenue-analysis'">
        <el-row :gutter="16" v-loading="statsLoading.revenue">
          <el-col :span="6"><div class="stat-card"><span>总营业额</span><strong>{{ amount(revenueAnalysis.sumMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>今日营业额</span><strong>{{ amount(revenueAnalysis.nowMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>昨天营业额</span><strong>{{ amount(revenueAnalysis.yesterdayMoney || 0) }}元</strong></div></el-col>
        </el-row>
        <div class="revenue-day-title">营业额分类</div>
        <el-row :gutter="16" v-loading="statsLoading.revenue">
          <el-col :span="6"><div class="stat-card"><span>抖音营业额</span><strong>{{ amount(revenueAnalysis.dyMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>快手营业额</span><strong>{{ amount(revenueAnalysis.ksMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>美团营业额</span><strong>{{ amount(revenueAnalysis.mtMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>微信营业额</span><strong>{{ amount(revenueAnalysis.wxMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>商家营业额</span><strong>{{ amount(revenueAnalysis.sjMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>赠送营业额</span><strong>{{ amount(revenueAnalysis.zsMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>其他金额</span><strong>{{ amount(revenueAnalysis.otherMoney || 0) }}元</strong></div></el-col>
        </el-row>
      </template>

      <template v-else-if="activeTab === 'write-off'">
        <div class="stats-toolbar">
          <el-select v-model="statWay" style="width: 160px" @change="loadWriteOffStats">
            <el-option :value="1" label="按天查询" />
            <el-option :value="2" label="按月查询" />
            <el-option :value="3" label="按年查询" />
          </el-select>
          <el-date-picker v-model="statDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始时间" @change="loadWriteOffStats" />
        </div>
        <el-row :gutter="16" v-loading="statsLoading.revenue">
          <el-col :span="6"><div class="stat-card"><span>美团核销总数</span><strong>{{ writeOffStats.mtCount || 0 }}个</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>美团核销金额</span><strong>{{ amount(writeOffStats.mtMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>抖音核销总数</span><strong>{{ writeOffStats.dyCount || 0 }}个</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>抖音核销金额</span><strong>{{ amount(writeOffStats.dyMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>购买核销总数</span><strong>{{ writeOffStats.payCount || 0 }}个</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>购买核销金额</span><strong>{{ amount(writeOffStats.payMoney || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>赠送核销总数</span><strong>{{ writeOffStats.zsCount || 0 }}个</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>赠送核销金额</span><strong>{{ amount(writeOffStats.zsMoney || 0) }}元</strong></div></el-col>
        </el-row>
      </template>

      <template v-else-if="activeTab === 'earning-detail'">
        <div class="stats-toolbar">
          <el-select v-model="earningDetailWay" style="width: 160px" @change="earningDetailPage = 1; loadEarningDetails()">
            <el-option :value="1" label="按天查询" />
            <el-option :value="2" label="按月查询" />
          </el-select>
        </div>
        <el-table v-loading="earningDetailLoading" :data="earningDetailList" border>
          <el-table-column type="index" label="编号" width="90" />
          <el-table-column prop="dateTime" label="时间" min-width="160" />
          <el-table-column label="收益" width="140"><template #default="{ row }">{{ amount(row.money || 0) }}元</template></el-table-column>
        </el-table>
        <div class="detail-toolbar"><el-button :disabled="earningDetailPage <= 1" @click="earningDetailPage -= 1; loadEarningDetails()">上一页</el-button><el-button @click="earningDetailPage += 1; loadEarningDetails()">下一页</el-button></div>
      </template>

      <template v-else-if="activeTab === 'duration'">
        <div class="stats-toolbar">
          <el-date-picker v-model="orderFilters.startTime" type="date" value-format="YYYY-MM-DD" placeholder="开始时间" />
          <el-date-picker v-model="orderFilters.endTime" type="date" value-format="YYYY-MM-DD" placeholder="截止时间" />
          <el-button type="primary" @click="loadDurationRecords">查询</el-button>
        </div>
        <el-row :gutter="16" v-loading="durationLoading">
          <el-col v-for="(item, index) in durationList" :key="index" :span="6">
            <div class="stat-card">
              <span>{{ item.goodsName }}</span>
              <strong>{{ item.sumSecondText }}</strong>
              <span>{{ amount(item.sumMoney || 0) }}元</span>
            </div>
          </el-col>
        </el-row>
      </template>

      <template v-else-if="activeTab === 'water'">
        <div class="detail-toolbar">
          <el-button type="primary" plain @click="openWaterQr">领水二维码</el-button>
        </div>
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="领水用户"><el-input v-model="waterFilters.userName" clearable placeholder="请输入领水用户" @keyup.enter="handleWaterSearch" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="waterFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleWaterSearch" /></el-form-item>
          <el-form-item><el-space><el-button type="primary" @click="handleWaterSearch">查询</el-button><el-button @click="handleWaterReset">重置</el-button></el-space></el-form-item>
        </el-form>
        <el-table v-loading="waterLoading" :data="waterList" border>
          <el-table-column prop="collectWaterId" label="编号" width="100" />
          <el-table-column prop="userName" label="领水用户" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
          <el-table-column label="用户头像" width="100"><template #default="{ row }"><el-avatar v-if="row.avatar" :src="row.avatar" :size="48" /><span v-else>-</span></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="counts" label="领取数量" width="100" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="waterPage" :page-size="waterSize" :total="waterTotal" @update:currentPage="waterPage = $event; loadWaterRecords()" @update:pageSize="waterSize = $event; waterPage = 1; loadWaterRecords()" />
      </template>

      <template v-else-if="activeTab === 'queue'">
        <div class="stats-toolbar">
          <el-select v-model="queueFilters.status" style="width: 140px" @change="queuePage = 1; loadQueueRecords()">
            <el-option :value="''" label="全部" />
            <el-option :value="1" label="排队中" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="已过号" />
          </el-select>
          <el-date-picker v-model="queueFilters.createTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" @change="queuePage = 1; loadQueueRecords()" />
          <el-button type="primary" @click="queuePage = 1; loadQueueRecords()">查询</el-button>
          <el-button @click="queueFilters.status = ''; queueFilters.createTime = getToday(); queuePage = 1; loadQueueRecords()">重置</el-button>
        </div>
        <el-table v-loading="queueLoading" :data="queueList" border>
          <el-table-column prop="queueUpId" label="编号" width="90" />
          <el-table-column prop="num" label="号码" width="100" />
          <el-table-column label="排号用户" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.user?.userName || row.userName || '-' }}</span></template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }">{{ Number(row.status) === 1 ? '排队中' : Number(row.status) === 2 ? '已完成' : '已过号' }}</template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="120"><template #default="{ row }"><el-button v-if="Number(row.status) === 1" link type="primary" @click="markQueueMissed(row)">过号</el-button></template></el-table-column>
        </el-table>
        <BasePagination :current-page="queuePage" :page-size="queueSize" :total="queueTotal" @update:currentPage="queuePage = $event; loadQueueRecords()" @update:pageSize="queueSize = $event; queuePage = 1; loadQueueRecords()" />
      </template>

      <template v-else-if="activeTab === 'love-wallet'">
        <el-row :gutter="16" v-loading="loveWalletLoading">
          <el-col :span="6"><div class="stat-card"><span>爱心账户</span><strong>{{ amount(loveWalletSummary.money || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>已捐款金额</span><strong>{{ amount(loveWalletSummary.donationMoney || 0) }}元</strong></div></el-col>
        </el-row>
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="类型"><el-select v-model="loveWalletFilters.moneyType" clearable placeholder="请选择类型" @change="loveWalletPage = 1; loadLoveWallet()"><el-option :value="1" label="爱心钱包" /><el-option :value="2" label="已捐款" /></el-select></el-form-item>
          <el-form-item label="用户昵称"><el-input v-model="loveWalletFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="loveWalletPage = 1; loadLoveWallet()" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="loveWalletFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="loveWalletPage = 1; loadLoveWallet()" /></el-form-item>
          <el-form-item><el-button type="primary" @click="loveWalletPage = 1; loadLoveWallet()">查询</el-button></el-form-item>
        </el-form>
        <el-table v-loading="loveWalletLoading" :data="loveWalletList" border>
          <el-table-column prop="id" label="编号" width="90" />
          <el-table-column label="类型" width="100"><template #default="{ row }">{{ Number(row.moneyType) === 1 ? '爱心钱包' : '已捐款' }}</template></el-table-column>
          <el-table-column label="用户昵称" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
          <el-table-column label="金额" width="100"><template #default="{ row }"><span :class="Number(row.type) === 1 ? 'success-text' : 'text-danger'">{{ Number(row.type) === 1 ? '+' : '-' }} {{ row.money }}</span></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="loveWalletPage" :page-size="loveWalletSize" :total="loveWalletTotal" @update:currentPage="loveWalletPage = $event; loadLoveWallet()" @update:pageSize="loveWalletSize = $event; loveWalletPage = 1; loadLoveWallet()" />
      </template>

      <template v-else-if="activeTab === 'love-board'">
        <div class="detail-toolbar"><el-input v-model="welfareWordKeyword" clearable placeholder="请输入标题" style="width: 240px" @keyup.enter="welfareWordPage = 1; loadWelfareWords()" /><el-button type="primary" @click="welfareWordPage = 1; loadWelfareWords()">查询</el-button><el-button type="primary" @click="openWelfareWordDialog()">添加</el-button></div>
        <el-table v-loading="welfareWordLoading" :data="welfareWordList" border>
          <el-table-column prop="welfareWordId" label="编号" width="90" />
          <el-table-column prop="welfareWordTitle" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="welfareWordContent" label="内容" min-width="260" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160"><template #default="{ row }"><div class="action-buttons single-line"><el-button size="small" type="primary" @click="openWelfareWordDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="removeWelfareWord(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="welfareWordPage" :page-size="welfareWordSize" :total="welfareWordTotal" @update:currentPage="welfareWordPage = $event; loadWelfareWords()" @update:pageSize="welfareWordSize = $event; welfareWordPage = 1; loadWelfareWords()" />
      </template>

      <template v-else-if="activeTab === 'member-wallet'">
        <el-row :gutter="16" v-loading="memberWalletLoading">
          <el-col :span="6"><div class="stat-card"><span>会员剩余总金额</span><strong>{{ amount(memberWalletSummary.money || 0) }}元</strong></div></el-col>
          <el-col :span="6"><div class="stat-card"><span>会员总人数</span><strong>{{ memberWalletSummary.count || 0 }}人</strong></div></el-col>
        </el-row>
        <div class="detail-toolbar"><el-input v-model="memberWalletKeyword" clearable placeholder="请输入手机号" style="width: 240px" @keyup.enter="memberWalletPage = 1; loadMemberWallet()" /><el-button type="primary" @click="memberWalletPage = 1; loadMemberWallet()">查询</el-button><el-button type="primary" @click="openMemberWalletDialog()">添加</el-button></div>
        <el-table v-loading="memberWalletLoading" :data="memberWalletList" border>
          <el-table-column prop="memberUserId" label="编号" width="90" />
          <el-table-column prop="phone" label="手机号" min-width="140"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.memberUserId)">{{ row.phone || '-' }}</span></template></el-table-column>
          <el-table-column prop="money" label="钱包金额" width="180"><template #default="{ row }"><div class="action-buttons single-line"><span>{{ row.money }}</span><el-button size="small" type="primary" plain @click="openMemberWalletDetailDialog({ memberUserId: row.memberUserId, phone: row.phone })">修改金额</el-button></div></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160"><template #default="{ row }"><div class="action-buttons single-line"><el-button size="small" type="primary" @click="openMemberWalletDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="removeMemberWallet(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="memberWalletPage" :page-size="memberWalletSize" :total="memberWalletTotal" @update:currentPage="memberWalletPage = $event; loadMemberWallet()" @update:pageSize="memberWalletSize = $event; memberWalletPage = 1; loadMemberWallet()" />
      </template>

      <template v-else-if="activeTab === 'member-wallet-detail'">
        <div class="detail-toolbar"><el-button type="primary" @click="openMemberWalletDetailDialog()">添加</el-button></div>
        <el-table v-loading="memberWalletDetailLoading" :data="memberWalletDetailList" border>
          <el-table-column prop="memberUserId" label="编号" width="90" />
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="金额" width="100"><template #default="{ row }"><span :class="Number(row.type) === 1 ? 'success-text' : 'text-danger'">{{ Number(row.type) === 1 ? '+' : '-' }} {{ row.money }}</span></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160"><template #default="{ row }"><div class="action-buttons single-line"><el-button size="small" type="primary" @click="openMemberWalletDetailDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="removeMemberWalletDetail(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="memberWalletDetailPage" :page-size="memberWalletDetailSize" :total="memberWalletDetailTotal" @update:currentPage="memberWalletDetailPage = $event; loadMemberWalletDetails()" @update:pageSize="memberWalletDetailSize = $event; memberWalletDetailPage = 1; loadMemberWalletDetails()" />
      </template>

      <template v-else-if="activeTab === 'music'">
        <div class="detail-toolbar"><el-input v-model="musicKeyword" clearable placeholder="请输入关键词" style="width: 240px" @keyup.enter="musicPage = 1; loadMusicList()" /><el-button type="primary" @click="musicPage = 1; loadMusicList()">查询</el-button><el-button @click="musicKeyword = ''; musicPage = 1; loadMusicList()">重置</el-button><el-button type="primary" @click="openMusicLibraryDialog()">添加</el-button></div>
        <el-table v-loading="musicLoading" :data="musicList" border>
          <el-table-column prop="shopCollectMusicId" label="编号" width="90" />
          <el-table-column prop="musicName" label="歌曲名称" min-width="160" />
          <el-table-column prop="musicAuthor" label="歌曲作者" min-width="140" />
          <el-table-column prop="musicUrl" label="歌曲地址" min-width="240" show-overflow-tooltip />
          <el-table-column label="封面图" width="100"><template #default="{ row }"><el-image v-if="row.musicPicUrl" :src="row.musicPicUrl" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="musicPage" :page-size="musicSize" :total="musicTotal" @update:currentPage="musicPage = $event; loadMusicList()" @update:pageSize="musicSize = $event; musicPage = 1; loadMusicList()" />
      </template>

      <template v-else-if="activeTab === 'prize-config'">
        <div class="detail-toolbar"><el-button type="primary" @click="openPrizeConfigDialog()">添加</el-button></div>
        <el-table v-loading="prizeConfigLoading" :data="prizeConfigList" border>
          <el-table-column prop="prizeId" label="编号" width="90" />
          <el-table-column label="奖品类型" width="100"><template #default="{ row }">{{ Number(row.prizeType) === 1 ? '优惠券' : Number(row.prizeType) === 2 ? '现金' : Number(row.prizeType) === 3 ? '实物' : '再抽一次' }}</template></el-table-column>
          <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
          <el-table-column label="奖品图片" width="100"><template #default="{ row }"><el-image v-if="row.prizeImage" :src="row.prizeImage" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
          <el-table-column prop="weight" label="权重" width="90" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="操作" width="160"><template #default="{ row }"><div class="action-buttons single-line"><el-button size="small" type="primary" @click="openPrizeConfigDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="removePrizeConfig(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="prizeConfigPage" :page-size="prizeConfigSize" :total="prizeConfigTotal" @update:currentPage="prizeConfigPage = $event; loadPrizeConfigList()" @update:pageSize="prizeConfigSize = $event; prizeConfigPage = 1; loadPrizeConfigList()" />
      </template>

      <template v-else-if="activeTab === 'prize-record'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="用户昵称"><el-input v-model="prizeRecordFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="prizeRecordPage = 1; loadPrizeRecords()" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="prizeRecordFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="prizeRecordPage = 1; loadPrizeRecords()" /></el-form-item>
          <el-form-item label="奖品名称"><el-input v-model="prizeRecordFilters.prizeName" clearable placeholder="请输入奖品名称" @keyup.enter="prizeRecordPage = 1; loadPrizeRecords()" /></el-form-item>
          <el-form-item><el-button type="primary" @click="prizeRecordPage = 1; loadPrizeRecords()">查询</el-button><el-button @click="prizeRecordFilters.userName = ''; prizeRecordFilters.phone = ''; prizeRecordFilters.prizeName = ''; prizeRecordPage = 1; loadPrizeRecords()">重置</el-button><el-button type="primary" @click="openPrizeRecordDialog()">添加</el-button></el-form-item>
        </el-form>
        <el-table v-loading="prizeRecordLoading" :data="prizeRecordList" border>
          <el-table-column prop="prizeUserId" label="编号" width="90" />
          <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
          <el-table-column prop="expressNumber" label="快递单号" min-width="160" />
          <el-table-column prop="money" label="抽奖消耗次数" width="120" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="240"><template #default="{ row }"><div class="action-buttons"><el-button size="small" type="primary" @click="openPrizeRecordDialog(row)">编辑</el-button><el-button size="small" type="warning" @click="openPrizeShipDialog(row)">发货</el-button><el-button size="small" type="primary" plain @click="openPrizeLogisticsDialog(row)">物流</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="prizeRecordPage" :page-size="prizeRecordSize" :total="prizeRecordTotal" @update:currentPage="prizeRecordPage = $event; loadPrizeRecords()" @update:pageSize="prizeRecordSize = $event; prizeRecordPage = 1; loadPrizeRecords()" />
      </template>

      <template v-else-if="activeTab === 'count-record'">
        <div class="detail-toolbar"><el-input v-model="countRecordUserName" clearable placeholder="请输入用户昵称" style="width: 240px" @keyup.enter="countRecordPage = 1; loadCountRecords()" /><el-button type="primary" @click="countRecordPage = 1; loadCountRecords()">查询</el-button></div>
        <el-table v-loading="countRecordLoading" :data="countRecordList" border>
          <el-table-column prop="prizeGoldenUserDetailsId" label="编号" width="90" />
          <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.user?.userName || row.userName || '-' }}</span></template></el-table-column>
          <el-table-column prop="content" label="内容" min-width="220" />
          <el-table-column label="次数" width="100"><template #default="{ row }"><span :class="Number(row.type) === 1 ? 'success-text' : 'text-danger'">{{ Number(row.type) === 1 ? '+' : '-' }} {{ row.money }}</span></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="countRecordPage" :page-size="countRecordSize" :total="countRecordTotal" @update:currentPage="countRecordPage = $event; loadCountRecords()" @update:pageSize="countRecordSize = $event; countRecordPage = 1; loadCountRecords()" />
      </template>

      <template v-else-if="activeTab === 'count-recharge'">
        <div class="detail-toolbar"><el-input v-model="rechargeRecordUserName" clearable placeholder="请输入用户昵称" style="width: 240px" @keyup.enter="rechargeRecordPage = 1; loadRechargeRecords()" /><el-button type="primary" @click="rechargeRecordPage = 1; loadRechargeRecords()">查询</el-button></div>
        <el-table v-loading="rechargeRecordLoading" :data="rechargeRecordList" border>
          <el-table-column prop="prizeNumPayId" label="编号" width="90" />
          <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
          <el-table-column prop="payMoney" label="充值金额" width="120" />
          <el-table-column prop="prizeNum" label="赠送次数" width="120" />
          <el-table-column prop="payDate" label="归属年月" min-width="140" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
        </el-table>
        <BasePagination :current-page="rechargeRecordPage" :page-size="rechargeRecordSize" :total="rechargeRecordTotal" @update:currentPage="rechargeRecordPage = $event; loadRechargeRecords()" @update:pageSize="rechargeRecordSize = $event; rechargeRecordPage = 1; loadRechargeRecords()" />
      </template>

      <template v-else-if="activeTab === 'preset-prize-record'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="状态"><el-select v-model="presetPrizeRecordFilters.status" clearable placeholder="请选择状态" @change="presetPrizeRecordPage = 1; loadPresetPrizeRecords()"><el-option :value="1" label="待抽奖" /><el-option :value="2" label="已抽中" /></el-select></el-form-item>
          <el-form-item label="用户昵称"><el-input v-model="presetPrizeRecordFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="presetPrizeRecordPage = 1; loadPresetPrizeRecords()" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="presetPrizeRecordFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="presetPrizeRecordPage = 1; loadPresetPrizeRecords()" /></el-form-item>
          <el-form-item label="奖品名称"><el-input v-model="presetPrizeRecordFilters.prizeName" clearable placeholder="请输入奖品名称" @keyup.enter="presetPrizeRecordPage = 1; loadPresetPrizeRecords()" /></el-form-item>
          <el-form-item><el-button type="primary" @click="presetPrizeRecordPage = 1; loadPresetPrizeRecords()">查询</el-button><el-button @click="presetPrizeRecordFilters.status = ''; presetPrizeRecordFilters.userName = ''; presetPrizeRecordFilters.phone = ''; presetPrizeRecordFilters.prizeName = ''; presetPrizeRecordPage = 1; loadPresetPrizeRecords()">重置</el-button><el-button type="primary" @click="openPresetPrizeRecordDialog()">添加</el-button></el-form-item>
        </el-form>
        <el-table v-loading="presetPrizeRecordLoading" :data="presetPrizeRecordList" border>
          <el-table-column prop="prizeUserSysId" label="编号" width="90" />
          <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
          <el-table-column label="状态" width="100"><template #default="{ row }">{{ Number(row.status) === 2 ? '已抽中' : '待抽奖' }}</template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="160"><template #default="{ row }"><div class="action-buttons single-line"><el-button size="small" type="primary" @click="openPresetPrizeRecordDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="removePresetPrizeRecord(row)">删除</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="presetPrizeRecordPage" :page-size="presetPrizeRecordSize" :total="presetPrizeRecordTotal" @update:currentPage="presetPrizeRecordPage = $event; loadPresetPrizeRecords()" @update:pageSize="presetPrizeRecordSize = $event; presetPrizeRecordPage = 1; loadPresetPrizeRecords()" />
      </template>

      <template v-else-if="activeTab === 'users'">
        <el-form :inline="true" class="search-grid" @submit.prevent>
          <el-form-item label="用户昵称"><el-input v-model="userListFilters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="userListPage = 1; loadMerchantUsers()" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="userListFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userListPage = 1; loadMerchantUsers()" /></el-form-item>
          <el-form-item label="邀请人邀请码"><el-input v-model="userListFilters.inviterCode" clearable placeholder="请输入邀请人邀请码" @keyup.enter="userListPage = 1; loadMerchantUsers()" /></el-form-item>
          <el-form-item><el-button type="primary" @click="userListPage = 1; loadMerchantUsers()">查询</el-button><el-button @click="userListFilters.userName = ''; userListFilters.phone = ''; userListFilters.inviterCode = ''; userListPage = 1; loadMerchantUsers()">重置</el-button></el-form-item>
        </el-form>
        <el-table v-loading="userListLoading" :data="userList" border>
          <el-table-column prop="userId" label="编号" width="90" />
          <el-table-column label="昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '未绑定' }}</span></template></el-table-column>
          <el-table-column label="性别" width="80"><template #default="{ row }">{{ Number(row.sex) === 1 ? '男' : Number(row.sex) === 2 ? '女' : '未设置' }}</template></el-table-column>
          <el-table-column label="头像" width="90"><template #default="{ row }"><el-avatar :src="row.avatar || ''" :size="40">{{ row.userName?.slice(0, 1) || 'U' }}</el-avatar></template></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="140" />
          <el-table-column prop="prizeNum" label="次数" width="160"><template #default="{ row }"><div class="action-buttons single-line"><span>{{ row.prizeNum || 0 }}</span><el-button size="small" type="primary" plain @click="openCountUpdateDialog(row)">修改</el-button></div></template></el-table-column>
          <el-table-column prop="invitationCode" label="邀请码" min-width="120" />
          <el-table-column prop="inviterCode" label="邀请人邀请码" min-width="120" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" width="280"><template #default="{ row }"><div class="action-buttons"><el-button size="small" type="primary" @click="goUserDetail(row.userId)">详情</el-button><el-button size="small" type="primary" plain @click="openUserCountDetail(row)">次数明细</el-button><el-button size="small" type="warning" @click="openUserPrizeDetail(row)">奖品</el-button><el-button size="small" type="primary" plain @click="openUserRechargeDetail(row)">次数充值明细</el-button></div></template></el-table-column>
        </el-table>
        <BasePagination :current-page="userListPage" :page-size="userListSize" :total="userListTotal" @update:currentPage="userListPage = $event; loadMerchantUsers()" @update:pageSize="userListSize = $event; userListPage = 1; loadMerchantUsers()" />
      </template>
      </div>
    </div>

    <el-dialog v-model="goodsQrDialogVisible" :title="goodsQrTitle" width="420px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="qr-dialog-body">
        <img :src="goodsQrUrl" alt="二维码" class="qr-image" />
        <div class="qr-actions">
          <el-button @click="window.open(goodsQrUrl, '_blank')">打开新窗口</el-button>
          <el-button type="primary" @click="downloadQrImage">下载</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="mapDialogVisible" title="地图选点" width="70%" destroy-on-close @opened="initMap" class="nf-pro-dialog" align-center>
<div v-loading="mapInitializing" class="map-dialog-layout">
        <div class="map-dialog-toolbar">
          <div class="map-hint">可搜索地址后自动定位，也可直接点击地图选点。</div>
          <el-space>
            <el-button :loading="mapLoading" @click="locateByAddress">按地址定位</el-button>
          </el-space>
        </div>
        <div ref="mapContainerRef" class="map-container" />
      </div>
    </el-dialog>

    <el-dialog v-model="goodsTimeDialogVisible" :title="goodsTimeDialogTitle" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="detail-toolbar">
        <el-button type="primary" @click="openGoodsTimeEdit()">新增特殊价格</el-button>
      </div>
      <el-table v-loading="goodsTimeLoading" :data="goodsTimeList" border>
        <el-table-column prop="goodsTimeId" label="编号" width="90" />
        <el-table-column label="类型" width="100"><template #default="{ row }">{{ priceRuleTypeText(row.ruleType) }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="120" />
        <el-table-column prop="endTime" label="结束时间" width="120" />
        <el-table-column prop="hourPrice" label="半小时价" width="120" />
        <el-table-column label="起始日期" min-width="120"><template #default="{ row }">{{ Number(row.ruleType) === 1 ? weekText(row.startDate) : row.startDate || '-' }}</template></el-table-column>
        <el-table-column label="结束日期" min-width="120"><template #default="{ row }">{{ Number(row.ruleType) === 1 ? weekText(row.endDate) : row.endDate || '-' }}</template></el-table-column>
        <el-table-column prop="minutePrice" label="分钟价" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
                <div class="action-buttons single-line">
                  <el-button size="small" type="primary" @click="openGoodsTimeEdit(row)">修改</el-button>
                  <el-button size="small" type="danger" @click="deleteGoodsTime(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
      </el-table>
      <BasePagination :current-page="goodsTimePage" :page-size="goodsTimeSize" :total="goodsTimeTotal" @update:currentPage="goodsTimePage = $event; loadGoodsTimeList()" @update:pageSize="goodsTimeSize = $event; goodsTimePage = 1; loadGoodsTimeList()" />
    </el-dialog>

    <AppDialog v-model="goodsTimeEditVisible" :title="goodsTimeForm.goodsTimeId ? '修改特殊价格' : '新增特殊价格'" width="620px" :loading="goodsTimeEditLoading" @confirm="submitGoodsTime">
      <el-form label-width="120px">
        <el-form-item label="类型">
          <el-radio-group v-model="goodsTimeForm.ruleType">
            <el-radio v-for="item in priceRuleTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="goodsTimeForm.ruleType === 1 ? '起始日期' : '起始日期'">
          <el-select v-if="goodsTimeForm.ruleType === 1" v-model="goodsTimeForm.startDate" placeholder="请选择星期">
            <el-option v-for="item in weekOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-else v-model="goodsTimeForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择起始日期" />
        </el-form-item>
        <el-form-item :label="goodsTimeForm.ruleType === 1 ? '结束日期' : '结束日期'">
          <el-select v-if="goodsTimeForm.ruleType === 1" v-model="goodsTimeForm.endDate" placeholder="请选择星期">
            <el-option v-for="item in weekOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker v-else v-model="goodsTimeForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="开始时间"><el-time-select v-model="goodsTimeForm.startTime" placeholder="选择时间" :picker-options="{ start: '00:00', step: '00:30', end: '23:59' }" /></el-form-item>
        <el-form-item label="结束时间"><el-time-select v-model="goodsTimeForm.endTime" placeholder="选择时间" :picker-options="{ start: '00:00', step: '00:30', end: '23:59' }" /></el-form-item>
        <el-form-item label="半小时价"><el-input v-model="goodsTimeForm.hourPrice" type="number" min="0" placeholder="请输入半小时价" /></el-form-item>
        <el-form-item label="分钟价"><el-input v-model="goodsTimeForm.minutePrice" type="number" min="0" placeholder="请输入分钟价" /></el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="goodsSpecialTimeDialogVisible" :title="`${goodsTimeTarget.goodsName || '羽毛球场'}特殊忙时`" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="detail-toolbar">
        <el-button type="primary" @click="openGoodsSpecialTimeEdit()">新增特殊忙时</el-button>
      </div>
      <el-table v-loading="goodsSpecialTimeDialogLoading" :data="goodsSpecialTimeList" border>
        <el-table-column prop="goodsSpecialTimeId" label="编号" width="90" />
        <el-table-column label="日期" width="120"><template #default="{ row }">{{ weekText(row.weekNum) }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="140" />
        <el-table-column prop="endTime" label="结束时间" width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
                <div class="action-buttons single-line">
                  <el-button size="small" type="primary" @click="openGoodsSpecialTimeEdit(row)">修改</el-button>
                  <el-button size="small" type="danger" @click="deleteGoodsSpecialTime(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
      </el-table>
      <BasePagination :current-page="goodsSpecialTimePage" :page-size="goodsSpecialTimeSize" :total="goodsSpecialTimeTotal" @update:currentPage="goodsSpecialTimePage = $event; loadGoodsSpecialTimeList()" @update:pageSize="goodsSpecialTimeSize = $event; goodsSpecialTimePage = 1; loadGoodsSpecialTimeList()" />
    </el-dialog>

    <AppDialog v-model="goodsSpecialTimeEditVisible" :title="goodsSpecialTimeForm.goodsSpecialTimeId ? '修改特殊忙时' : '新增特殊忙时'" width="560px" :loading="goodsSpecialTimeEditLoading" @confirm="submitGoodsSpecialTime">
      <el-form label-width="110px">
        <el-form-item label="日期">
          <el-select v-model="goodsSpecialTimeForm.weekNum" placeholder="请选择星期">
            <el-option v-for="item in weekOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-time-select v-model="goodsSpecialTimeForm.startTime" placeholder="选择时间" :picker-options="{ start: '00:00', step: '00:30', end: '23:59' }" /></el-form-item>
        <el-form-item label="结束时间"><el-time-select v-model="goodsSpecialTimeForm.endTime" placeholder="选择时间" :picker-options="{ start: '00:00', step: '00:30', end: '23:59' }" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="goodsBatchPriceVisible" title="批量修改金额" width="560px" :loading="goodsBatchPriceLoading" @confirm="submitBatchPrice">
      <el-form label-width="110px">
        <el-form-item label="半小时价"><el-input v-model="goodsBatchPriceForm.hourPrice" type="number" /></el-form-item>
        <el-form-item label="预约金额"><el-input v-model="goodsBatchPriceForm.subscription" type="number" /></el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="goodsStatsVisible" :title="`${goodsStatsTargetName || '羽毛球场'}数据详情`" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<div v-loading="goodsStatsLoading">
        <div class="goods-stats-section">
          <div class="goods-stats-head">
            <strong>日数据</strong>
            <el-date-picker v-model="goodsStatsDate" type="date" value-format="YYYY-MM-DD" @change="loadGoodsStatsPanels" />
          </div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="stat-card"><span>总收益</span><strong>{{ amount(goodsStatsDaily.money || 0) }}元</strong></div></el-col>
            <el-col :span="12"><div class="stat-card"><span>订单数</span><strong>{{ goodsStatsDaily.count || 0 }}单</strong></div></el-col>
          </el-row>
        </div>
        <div class="goods-stats-section">
          <div class="goods-stats-head">
            <strong>周数据</strong>
            <el-date-picker v-model="goodsStatsWeekRange" class="goods-stats-picker" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadGoodsStatsPanels" />
          </div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="stat-card"><span>总收益</span><strong>{{ amount(goodsStatsWeekly.money || 0) }}元</strong></div></el-col>
            <el-col :span="12"><div class="stat-card"><span>订单数</span><strong>{{ goodsStatsWeekly.count || 0 }}单</strong></div></el-col>
          </el-row>
        </div>
        <div class="goods-stats-section">
          <div class="goods-stats-head">
            <strong>月数据</strong>
            <el-date-picker v-model="goodsStatsMonthDate" type="month" value-format="YYYY-MM-DD" @change="loadGoodsStatsPanels" />
          </div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="stat-card"><span>总收益</span><strong>{{ amount(goodsStatsMonth.money || 0) }}元</strong></div></el-col>
            <el-col :span="12"><div class="stat-card"><span>订单数</span><strong>{{ goodsStatsMonth.count || 0 }}单</strong></div></el-col>
          </el-row>
        </div>
        <div class="goods-stats-section">
          <div class="goods-stats-head">
            <strong>年数据</strong>
            <el-date-picker v-model="goodsStatsYearDate" type="year" value-format="YYYY-MM-DD" @change="loadGoodsStatsPanels" />
          </div>
          <el-row :gutter="16">
            <el-col :span="12"><div class="stat-card"><span>总收益</span><strong>{{ amount(goodsStatsYear.money || 0) }}元</strong></div></el-col>
            <el-col :span="12"><div class="stat-card"><span>订单数</span><strong>{{ goodsStatsYear.count || 0 }}单</strong></div></el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="goodsLiveVisible" :title="goodsLiveTitle" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<video :src="goodsLiveUrl" controls style="width: 100%; max-height: 70vh" />
    </el-dialog>

    <AppDialog v-model="refundRuleDialogVisible" :title="refundRuleForm.refundRuleId ? '修改退款规则' : '新增退款规则'" width="560px" :loading="refundRuleDialogLoading" @confirm="submitRefundRule">
      <el-form label-width="140px">
        <el-form-item label="距离开始前时间">
          <el-input v-model="refundRuleForm.hour" type="number" min="0" placeholder="请输入距离开始前时间(单位：小时)">
            <template #append>小时</template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款比例">
          <el-input v-model="refundRuleForm.rate" type="number" min="0" step="0.01" placeholder="请输入退款比例，例如0.1">
            <template #append>0-1</template>
          </el-input>
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="couponIssueDialogVisible" title="发布优惠券" width="760px" :loading="couponIssueDialogLoading" @confirm="submitCouponIssue">
      <el-form label-width="110px">
        <el-form-item label="优惠券类型"><el-radio-group v-model="couponIssueForm.type" @change="couponIssueForm.couponId = ''; loadCouponOptions()"><el-radio :value="1">团购券</el-radio><el-radio :value="2">积分兑换券</el-radio><el-radio :value="3">新人优惠券</el-radio></el-radio-group></el-form-item>
        <el-form-item label="选择优惠券">
          <el-select v-model="couponIssueForm.couponId" filterable style="width: 100%" :loading="couponListLoading" @change="syncCouponIssueSelection">
            <el-option v-for="item in couponOptions" :key="item.couponId" :label="item.couponName" :value="item.couponId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择羽毛球场">
          <el-checkbox-group v-model="couponIssueForm.goodsIds">
            <el-checkbox v-for="item in goodsList" :key="item.goodsId" :label="item.goodsId">{{ item.goodsName }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="是否限量"><el-radio-group v-model="couponIssueForm.isLimit"><el-radio :value="1">限量</el-radio><el-radio :value="2">不限量</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="Number(couponIssueForm.isLimit) === 1" label="发布数量"><el-input v-model="couponIssueForm.issueNumber" type="number" placeholder="请输入发布数量" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="couponIssueForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="2">关闭</el-radio></el-radio-group></el-form-item>
        <el-form-item label="使用规则"><el-input v-model="couponIssueForm.teamRule" type="textarea" :rows="3" placeholder="请输入使用规则" /></el-form-item>
        <el-form-item label="购买须知"><el-input v-model="couponIssueForm.buyRule" type="textarea" :rows="3" placeholder="请输入购买须知" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="refundDialogVisible" :title="refundDialogTitle" width="560px" :loading="refundDialogLoading" @confirm="submitRefund">
      <el-form label-width="110px">
        <el-form-item label="订单号"><el-input :model-value="refundForm.ordersNo" disabled /></el-form-item>
        <el-form-item label="最大可退"><el-input :model-value="`${amountValue(refundForm.maxRefundMoney)}元`" disabled /></el-form-item>
        <el-form-item label="退款金额"><el-input v-model="refundForm.refundMoney" type="number" min="0" placeholder="请输入退款金额" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="refundForm.refundRemark" type="textarea" :rows="4" placeholder="请输入备注" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="employeeDialogVisible" title="员工设置" width="520px" :loading="employeeDialogLoading" @confirm="submitEmployeeDialog">
      <el-form label-width="120px">
        <el-form-item label="员工用户ID"><el-input :model-value="String(employeeForm.userId || '')" disabled /></el-form-item>
        <el-form-item label="工作开始时间"><el-time-picker v-model="employeeForm.startTime" format="HH:mm" value-format="HH:mm" placeholder="选择开始时间" /></el-form-item>
        <el-form-item label="工作结束时间"><el-time-picker v-model="employeeForm.endTime" format="HH:mm" value-format="HH:mm" placeholder="选择结束时间" /></el-form-item>
        <el-form-item label="用户状态"><el-radio-group v-model="employeeForm.tearoomUserStatus"><el-radio v-for="item in employeeStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="employeePermissionVisible" title="查看权限" width="760px" :loading="employeePermissionLoading" @confirm="saveEmployeePermission">
      <div class="detail-toolbar">
        <el-button v-if="!employeePermissionEditable" type="primary" @click="employeePermissionEditable = true">修改</el-button>
        <el-button v-else @click="employeePermissionEditable = false">取消修改</el-button>
      </div>
      <el-form label-width="180px" class="permission-form">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="待结算统计"><el-radio-group v-model="employeePermissionForm.isDaijiesuan" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="点歌管理"><el-radio-group v-model="employeePermissionForm.isDiange" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="订单管理"><el-radio-group v-model="employeePermissionForm.isDingdanguanli" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="核销统计"><el-radio-group v-model="employeePermissionForm.isHexiaotongji" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="会员管理"><el-radio-group v-model="employeePermissionForm.isHuiyuanguanli" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="会员权益"><el-radio-group v-model="employeePermissionForm.isHuiyuanquanyi" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开台收益"><el-radio-group v-model="employeePermissionForm.isKaitaishouyi" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="门店会员"><el-radio-group v-model="employeePermissionForm.isMendianhuiyuan" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="商家开台"><el-radio-group v-model="employeePermissionForm.isShangjiakaitai" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="商家开台统计"><el-radio-group v-model="employeePermissionForm.isShangjiakaitaitongji" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时长统计"><el-radio-group v-model="employeePermissionForm.isShichangtongji" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="总收益"><el-radio-group v-model="employeePermissionForm.isShouyi" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收益明细"><el-radio-group v-model="employeePermissionForm.isShouyimingxi" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="门店状态"><el-radio-group v-model="employeePermissionForm.isStatus" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提现"><el-radio-group v-model="employeePermissionForm.isTixian" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="消费排行"><el-radio-group v-model="employeePermissionForm.isXiaofeipaihang" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="员工管理"><el-radio-group v-model="employeePermissionForm.isYuangongguanli" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="语音播报"><el-radio-group v-model="employeePermissionForm.isYuyin" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="赠送优惠券"><el-radio-group v-model="employeePermissionForm.isZengsongyouhuijuan" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="账单对账"><el-radio-group v-model="employeePermissionForm.isZhangdantongji" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="桌台管理"><el-radio-group v-model="employeePermissionForm.isZhuotaiguanli" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="桌台数据"><el-radio-group v-model="employeePermissionForm.isZhuotaishuju" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="退款"><el-radio-group v-model="employeePermissionForm.isTuikuan" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="领水记录"><el-radio-group v-model="employeePermissionForm.isLingshui" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="点歌权限"><el-radio-group v-model="employeePermissionForm.isMusic" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收入来源"><el-radio-group v-model="employeePermissionForm.isShouRuLaiYuan" :disabled="!employeePermissionEditable"><el-radio :value="1">是</el-radio><el-radio :value="2">否</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
      </el-form>
    </AppDialog>

    <el-dialog v-model="employeeWorkVisible" title="上班记录" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="employeeWorkLoading" :data="employeeWorkList" border>
        <el-table-column prop="tearoomUserRecordId" label="编号" width="90" />
        <el-table-column prop="userId" label="员工ID" width="90" />
        <el-table-column prop="startTime" label="上班时间" min-width="160" />
        <el-table-column prop="endTime" label="下班时间" min-width="160" />
        <el-table-column prop="sumCount" label="总订单数量" width="110" />
        <el-table-column prop="sumMoney" label="总订单收益" width="110" />
        <el-table-column prop="wxMoney" label="商家微信订单收益" width="120" />
        <el-table-column prop="mtMoney" label="商家美团收益" width="110" />
        <el-table-column prop="ksMoney" label="商家快手收益" width="110" />
        <el-table-column prop="dyMoney" label="商家抖音收益" width="110" />
      </el-table>
      <BasePagination :current-page="employeeWorkPage" :page-size="employeeWorkSize" :total="employeeWorkTotal" @update:currentPage="employeeWorkPage = $event; loadEmployeeWorkRecords()" @update:pageSize="employeeWorkSize = $event; employeeWorkPage = 1; loadEmployeeWorkRecords()" />
    </el-dialog>

    <el-dialog v-model="employeeOpenStatsVisible" title="员工开台统计" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-row :gutter="16" v-loading="employeeOpenStatsLoading">
        <el-col :span="6"><div class="stat-card"><span>总收益</span><strong>{{ amount(employeeOpenStats.sumMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>总订单</span><strong>{{ employeeOpenStats.sumCount || 0 }}单</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>微信收益</span><strong>{{ amount(employeeOpenStats.wxMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>现金收益</span><strong>{{ amount(employeeOpenStats.xjMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>门店会员收益</span><strong>{{ amount(employeeOpenStats.mdMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>美团核销收益</span><strong>{{ amount(employeeOpenStats.mtMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>快手核销收益</span><strong>{{ amount(employeeOpenStats.ksMoney || 0) }}元</strong></div></el-col>
        <el-col :span="6"><div class="stat-card"><span>抖音核销收益</span><strong>{{ amount(employeeOpenStats.dyMoney || 0) }}元</strong></div></el-col>
      </el-row>
    </el-dialog>

    <el-dialog v-model="orderVipVisible" title="其他金额" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-row :gutter="16">
        <el-col v-for="(item, index) in orderVipData.vipList || []" :key="index" :span="6"><div class="stat-card"><span>{{ item.vipName }}需支付金额</span><strong>{{ amount(item.payMoney || 0) }}</strong></div></el-col>
      </el-row>
    </el-dialog>

    <el-dialog v-model="renewVisible" title="续费记录" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="renewLoading" :data="renewList" border>
        <el-table-column prop="id" label="编号" width="90" />
        <el-table-column label="续费类型" width="100"><template #default="{ row }">{{ Number(row.type) === 1 ? '优惠券' : Number(row.type) === 2 ? '会员卡' : Number(row.type) === 3 ? '微信' : '-' }}</template></el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="优惠券名称" min-width="180"><template #default="{ row }">{{ Number(row.type) === 1 ? row.selfCouponUser?.couponName || '-' : '-' }}</template></el-table-column>
        <el-table-column prop="url" label="金额" width="120" />
        <el-table-column prop="createAt" label="操作时间" min-width="160" />
      </el-table>
      <BasePagination :current-page="renewPage" :page-size="renewSize" :total="renewTotal" @update:currentPage="renewPage = $event; loadRenewRecords()" @update:pageSize="renewSize = $event; renewPage = 1; loadRenewRecords()" />
    </el-dialog>

    <el-dialog v-model="changeRoomVisible" title="羽毛球场列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="changeRoomLoading" :data="changeRoomGoodsList" border>
        <el-table-column prop="goodsId" label="编号" width="90" />
        <el-table-column prop="goodsName" label="羽毛球场名称" min-width="160" />
        <el-table-column label="开台前图片" width="100"><template #default="{ row }"><el-image v-if="row.goodsCoverImage" :src="row.goodsCoverImage" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
        <el-table-column label="开台后图片" width="100"><template #default="{ row }"><el-image v-if="row.goodsTitleImage" :src="row.goodsTitleImage" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
        <el-table-column prop="goodsType" label="羽毛球场信息" min-width="180" />
        <el-table-column prop="hourPrice" label="半小时价" width="120" />
        <el-table-column prop="minutePrice" label="分钟价" width="120" />
        <el-table-column prop="sales" label="销量" width="90" />
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button v-if="Number(row.hourPrice || 0) === changeRoomHourPrice" link type="primary" @click="changeRoom(row)">确定</el-button></template></el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="earningOrderVisible" title="收益详情" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="earningOrderLoading" :data="earningOrderList" border>
        <el-table-column prop="ordersId" label="编号" width="90" fixed="left" />
        <el-table-column prop="goodsName" label="羽毛球名称" min-width="160" fixed="left" />
        <el-table-column prop="ordersNo" label="订单号" min-width="190" />
        <el-table-column label="订单类型" width="100"><template #default="{ row }">{{ orderTypeText(row.ordersType) }}</template></el-table-column>
        <el-table-column label="用户昵称" width="140"><template #default="{ row }"><span v-if="row.userId" class="link-text" @click="goUserDetail(row.userId)">{{ row.payUserName || '未绑定' }}</span><span v-else class="text-danger">商家开台</span></template></el-table-column>
        <el-table-column label="每半小时价格" width="120"><template #default="{ row }"><span style="color: deepskyblue;">{{ row.goods?.hourPrice || '-' }}{{ row.goods?.hourPrice ? '元' : '' }}</span></template></el-table-column>
        <el-table-column label="小时数" width="90"><template #default="{ row }">{{ row.productId ? '-' : row.hour || '-' }}</template></el-table-column>
        <el-table-column label="优惠券平台" width="100"><template #default="{ row }">{{ platformText(row.platformType) }}</template></el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" min-width="120" />
        <el-table-column label="优惠券金额" width="110"><template #default="{ row }">{{ amount(row.couponMoney) }}元</template></el-table-column>
        <el-table-column label="会员优惠金额" width="120"><template #default="{ row }">{{ amount(row.vipMoney) }}元</template></el-table-column>
        <el-table-column label="支付金额" width="110"><template #default="{ row }">{{ amount(row.payMoney) }}元</template></el-table-column>
        <el-table-column label="已退款金额" width="120"><template #default="{ row }">{{ amount(row.refundMoney) }}元</template></el-table-column>
        <el-table-column prop="minuteMoney" label="超出费用" width="100" />
        <el-table-column prop="shopMoney" label="商家总收益" width="110" />
        <el-table-column label="支付方式" width="90"><template #default="{ row }">{{ payWayText(row.payWay) }}</template></el-table-column>
        <el-table-column label="订单状态" width="100"><template #default="{ row }">{{ orderStatusText(row.status) }}</template></el-table-column>
        <el-table-column prop="payTime" label="付款时间" min-width="160"><template #default="{ row }">{{ row.payTime || '未付款' }}</template></el-table-column>
      </el-table>
      <BasePagination :current-page="earningOrderPage" :page-size="earningOrderSize" :total="earningOrderTotal" @update:currentPage="earningOrderPage = $event; loadEarningOrderList()" @update:pageSize="earningOrderSize = $event; earningOrderPage = 1; loadEarningOrderList()" />
    </el-dialog>

    <AppDialog v-model="welfareWordDialogVisible" :title="welfareWordForm.welfareWordId ? '修改公示' : '添加公示'" width="880px" :loading="welfareWordDialogLoading" @confirm="submitWelfareWord">
      <el-form label-width="100px">
        <el-form-item label="排序"><el-input v-model="welfareWordForm.sort" type="number" placeholder="请输入排序" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="welfareWordForm.welfareWordTitle" placeholder="请输入标题" /></el-form-item>
        <el-form-item label="轮播图"><el-input v-model="welfareWordForm.welfareImg" placeholder="多个图片用英文逗号分隔" /></el-form-item>
        <el-form-item label="内容">
          <div class="prize-editor">
            <QuillEditor v-model:content="welfareWordForm.welfareWordContent" content-type="html" theme="snow" />
          </div>
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="memberWalletDialogVisible" :title="memberWalletForm.memberUserId ? '修改会员钱包' : '添加会员钱包'" width="620px" :loading="memberWalletDialogLoading" @confirm="submitMemberWallet">
      <el-form label-width="100px">
        <el-form-item label="手机号"><el-input v-model="memberWalletForm.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="钱包金额"><el-input v-model="memberWalletForm.money" type="number" placeholder="请输入钱包金额" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="memberWalletDetailDialogVisible" :title="memberWalletDetailForm.id ? '修改会员钱包明细' : '添加会员钱包明细'" width="680px" :loading="memberWalletDetailDialogLoading" @confirm="submitMemberWalletDetail">
      <el-form label-width="110px">
        <el-form-item label="会员用户ID"><el-input v-model="memberWalletDetailForm.memberUserId" type="number" placeholder="请输入会员用户ID" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="memberWalletDetailForm.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="修改类型"><el-radio-group v-model="memberWalletDetailForm.type"><el-radio :value="1">添加</el-radio><el-radio :value="2">减少</el-radio></el-radio-group></el-form-item>
        <el-form-item label="金额"><el-input v-model="memberWalletDetailForm.money" type="number" placeholder="请输入金额" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="memberWalletDetailForm.title" placeholder="请输入标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="memberWalletDetailForm.content" placeholder="请输入内容" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="countUpdateDialogVisible" title="修改次数" width="560px" :loading="countUpdateDialogLoading" @confirm="submitCountUpdate">
      <el-form label-width="100px">
        <el-form-item label="修改类型"><el-radio-group v-model="countUpdateForm.type"><el-radio :value="1">添加</el-radio><el-radio :value="2">减少</el-radio></el-radio-group></el-form-item>
        <el-form-item label="次数"><el-input v-model="countUpdateForm.goldenNum" type="number" placeholder="请输入次数" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="prizeConfigDialogVisible" :title="prizeConfigForm.prizeId ? '修改奖品配置' : '添加奖品配置'" width="680px" :loading="prizeConfigDialogLoading" @confirm="submitPrizeConfig">
      <el-form label-width="100px">
        <el-form-item label="奖品类型"><el-radio-group v-model="prizeConfigForm.prizeType"><el-radio :value="1">优惠券</el-radio><el-radio :value="2">现金</el-radio><el-radio :value="3">实物</el-radio><el-radio :value="4">再抽一次</el-radio></el-radio-group></el-form-item>
        <el-form-item label="奖品名称"><el-input v-model="prizeConfigForm.prizeName" placeholder="请输入奖品名称" /></el-form-item>
        <el-form-item label="奖品图片"><el-input v-model="prizeConfigForm.prizeImage" placeholder="请输入奖品图片地址" /></el-form-item>
        <el-form-item label="权重"><el-input v-model="prizeConfigForm.weight" type="number" placeholder="请输入权重" /></el-form-item>
        <el-form-item label="排序"><el-input v-model="prizeConfigForm.sort" type="number" placeholder="请输入排序" /></el-form-item>
        <el-form-item label="关联值"><el-input v-model="prizeConfigForm.parent" placeholder="请输入关联值" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="prizeRecordDialogVisible" :title="prizeRecordForm.prizeUserId ? '修改抽奖记录' : '添加抽奖记录'" width="680px" :loading="prizeRecordDialogLoading" @confirm="submitPrizeRecord">
      <el-form label-width="100px">
        <el-form-item label="用户ID"><el-input v-model="prizeRecordForm.userId" type="number" placeholder="请输入用户ID" /></el-form-item>
        <el-form-item label="用户昵称"><div class="bind-user-row"><el-input v-model="prizeRecordForm.userName" placeholder="请选择奖品用户" /><el-button type="primary" plain @click="openPrizeRecordUserPicker()">选择用户</el-button></div></el-form-item>
        <el-form-item label="奖品ID"><el-input v-model="prizeRecordForm.prizeId" type="number" placeholder="请输入奖品ID" /></el-form-item>
        <el-form-item label="奖品名称"><div class="bind-user-row"><el-input v-model="prizeRecordForm.prizeName" placeholder="请选择奖品" /><el-button type="primary" plain @click="openPrizePicker('prize-record')">选择奖品</el-button></div></el-form-item>
        <el-form-item label="奖品图片"><el-input v-model="prizeRecordForm.prizeImage" placeholder="请输入奖品图片地址" /></el-form-item>
        <el-form-item label="奖品类型"><el-input v-model="prizeRecordForm.prizeType" type="number" placeholder="请输入奖品类型" /></el-form-item>
        <el-form-item label="关联值"><el-input v-model="prizeRecordForm.parent" placeholder="请输入关联值" /></el-form-item>
        <el-form-item label="权重"><el-input v-model="prizeRecordForm.weight" type="number" placeholder="请输入权重" /></el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="prizeShipDialogVisible" title="发货" width="560px" :loading="prizeShipDialogLoading" @confirm="submitPrizeShipment">
      <el-form label-width="100px">
        <el-form-item label="快递单号"><el-input v-model="prizeShipForm.expressNumber" placeholder="请输入快递单号" /></el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="prizeLogisticsVisible" title="物流信息" width="60%" destroy-on-close class="nf-pro-dialog" align-center>
<div v-loading="prizeLogisticsLoading">
        <el-empty v-if="!prizeLogisticsList.length" description="暂无物流信息" />
        <el-timeline v-else>
          <el-timeline-item v-for="(item, index) in prizeLogisticsList" :key="index" :timestamp="item.time || item.acceptTime || '-'">
            {{ item.status || item.context || '-' }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <AppDialog v-model="presetPrizeRecordDialogVisible" :title="presetPrizeRecordForm.prizeUserSysId ? '修改预设抽奖记录' : '添加预设抽奖记录'" width="680px" :loading="presetPrizeRecordDialogLoading" @confirm="submitPresetPrizeRecord">
      <el-form label-width="100px">
        <el-form-item label="用户ID"><el-input v-model="presetPrizeRecordForm.userId" type="number" placeholder="请输入用户ID" /></el-form-item>
        <el-form-item label="用户昵称"><div class="bind-user-row"><el-input v-model="presetPrizeRecordForm.userName" placeholder="请选择奖品用户" /><el-button type="primary" plain @click="openPresetPrizeRecordUserPicker()">选择用户</el-button></div></el-form-item>
        <el-form-item label="奖品ID"><el-input v-model="presetPrizeRecordForm.prizeId" type="number" placeholder="请输入奖品ID" /></el-form-item>
        <el-form-item label="奖品名称"><div class="bind-user-row"><el-input v-model="presetPrizeRecordForm.prizeName" placeholder="请选择奖品" /><el-button type="primary" plain @click="openPrizePicker('preset-prize-record')">选择奖品</el-button></div></el-form-item>
        <el-form-item label="奖品图片"><el-input v-model="presetPrizeRecordForm.prizeImage" placeholder="请输入奖品图片地址" /></el-form-item>
        <el-form-item label="奖品类型"><el-input v-model="presetPrizeRecordForm.prizeType" type="number" placeholder="请输入奖品类型" /></el-form-item>
        <el-form-item label="关联值"><el-input v-model="presetPrizeRecordForm.parent" placeholder="请输入关联值" /></el-form-item>
        <el-form-item label="权重"><el-input v-model="presetPrizeRecordForm.weight" type="number" placeholder="请输入权重" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="presetPrizeRecordForm.status"><el-radio :value="1">待抽奖</el-radio><el-radio :value="2">已抽中</el-radio></el-radio-group></el-form-item>
      </el-form>
    </AppDialog>

    <el-dialog v-model="userCountDetailVisible" title="用户次数明细" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="countRecordLoading" :data="countRecordList" border>
        <el-table-column prop="prizeGoldenUserDetailsId" label="编号" width="90" />
        <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.user?.userName || row.userName || '-' }}</span></template></el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" />
        <el-table-column label="次数" width="100"><template #default="{ row }"><span :class="Number(row.type) === 1 ? 'success-text' : 'text-danger'">{{ Number(row.type) === 1 ? '+' : '-' }} {{ row.money }}</span></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="userPrizeDetailVisible" title="用户奖品明细" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="prizeRecordLoading" :data="prizeRecordList" border>
        <el-table-column prop="prizeUserId" label="编号" width="90" />
        <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
        <el-table-column prop="money" label="抽奖消耗次数" width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="userRechargeDetailVisible" title="用户次数充值明细" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="rechargeRecordLoading" :data="rechargeRecordList" border>
        <el-table-column prop="prizeNumPayId" label="编号" width="90" />
        <el-table-column label="用户昵称" min-width="160"><template #default="{ row }"><span class="link-text" @click="goUserDetail(row.userId)">{{ row.userName || '-' }}</span></template></el-table-column>
        <el-table-column prop="payMoney" label="充值金额" width="120" />
        <el-table-column prop="prizeNum" label="赠送次数" width="120" />
        <el-table-column prop="payDate" label="归属年月" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="userPickerVisible" title="用户列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="手机号"><el-input v-model="userPickerFilters.phone" clearable placeholder="请输入手机号" @keyup.enter="userPickerPage = 1; loadUserPickerData()" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="userPickerFilters.userName" clearable placeholder="请输入昵称" @keyup.enter="userPickerPage = 1; loadUserPickerData()" /></el-form-item>
        <el-form-item label="邀请码"><el-input v-model="userPickerFilters.invitationCode" clearable placeholder="请输入邀请码" @keyup.enter="userPickerPage = 1; loadUserPickerData()" /></el-form-item>
        <el-form-item><el-space><el-button type="primary" @click="userPickerPage = 1; loadUserPickerData()">查询</el-button><el-button @click="userPickerFilters.phone = ''; userPickerFilters.userName = ''; userPickerFilters.invitationCode = ''; userPickerPage = 1; loadUserPickerData()">重置</el-button></el-space></el-form-item>
      </el-form>
      <el-table v-loading="userPickerLoading" :data="userPickerData" border>
        <el-table-column prop="userId" label="编号" width="90" />
        <el-table-column prop="userName" label="昵称" min-width="160" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="invitationCode" label="邀请码" min-width="120" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
              <div class="action-buttons single-line">
                <el-button size="small" type="primary" @click="handleUserPickerAction(row)">{{ userPickerMode === 'merchant-owner' ? '绑定门店' : '确定' }}</el-button>
                <el-button v-if="userPickerMode === 'merchant-owner'" size="small" type="warning" @click="addEmployee(row)">添加员工</el-button>
              </div>
            </template>
          </el-table-column>
      </el-table>
      <BasePagination :current-page="userPickerPage" :page-size="userPickerSize" :total="userPickerTotal" @update:currentPage="userPickerPage = $event; loadUserPickerData()" @update:pageSize="userPickerSize = $event; userPickerPage = 1; loadUserPickerData()" />
    </el-dialog>

    <el-dialog v-model="musicLibraryVisible" title="音乐库列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="detail-toolbar">
        <el-select v-model="musicLibraryFilters.musicTypeId" clearable placeholder="请选择分类" style="width: 180px" @change="musicLibraryPage = 1; loadMusicLibraryList()">
          <el-option v-for="item in musicTypeOptions" :key="item.musicTypeId || item.id" :label="item.musicTypeName || item.name" :value="item.musicTypeId || item.id" />
        </el-select>
        <el-input v-model="musicLibraryFilters.musicName" clearable placeholder="请输入歌曲名称" style="width: 180px" @keyup.enter="musicLibraryPage = 1; loadMusicLibraryList()" />
        <el-input v-model="musicLibraryFilters.musicAuthor" clearable placeholder="请输入歌曲作者" style="width: 180px" @keyup.enter="musicLibraryPage = 1; loadMusicLibraryList()" />
        <el-button type="primary" @click="musicLibraryPage = 1; loadMusicLibraryList()">查询</el-button>
        <el-button @click="musicLibraryFilters.musicTypeId = ''; musicLibraryFilters.musicName = ''; musicLibraryFilters.musicAuthor = ''; musicLibraryPage = 1; loadMusicLibraryList()">重置</el-button>
      </div>
      <el-table v-loading="musicLibraryLoading" :data="musicLibraryList" border>
        <el-table-column prop="musicLibraryId" label="编号" width="90" />
        <el-table-column prop="musicTypeName" label="分类名称" min-width="120" />
        <el-table-column prop="musicName" label="歌曲名称" min-width="160" />
        <el-table-column prop="musicAuthor" label="歌曲作者" min-width="140" />
        <el-table-column prop="musicUrl" label="歌曲地址" min-width="240" show-overflow-tooltip />
        <el-table-column label="歌曲封面图" width="100"><template #default="{ row }"><el-image v-if="row.musicPicUrl" :src="row.musicPicUrl" class="thumb-image" preview-teleported /><span v-else>暂无图片</span></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button size="small" type="primary" @click="selectMusicLibraryRow(row)">选择</el-button></template></el-table-column>
      </el-table>
      <BasePagination :current-page="musicLibraryPage" :page-size="musicLibrarySize" :total="musicLibraryTotal" @update:currentPage="musicLibraryPage = $event; loadMusicLibraryList()" @update:pageSize="musicLibrarySize = $event; musicLibraryPage = 1; loadMusicLibraryList()" />
    </el-dialog>

    <el-dialog v-model="prizePickerVisible" title="奖品列表" width="70%" destroy-on-close class="nf-pro-dialog" align-center>
<el-table v-loading="prizeConfigLoading" :data="prizeConfigList" border>
        <el-table-column prop="prizeId" label="编号" width="90" />
        <el-table-column label="奖品类型" width="100"><template #default="{ row }">{{ Number(row.prizeType) === 1 ? '优惠券' : Number(row.prizeType) === 2 ? '现金' : Number(row.prizeType) === 3 ? '实物' : '再抽一次' }}</template></el-table-column>
        <el-table-column prop="prizeName" label="奖品名称" min-width="160" />
        <el-table-column label="奖品图片" width="100"><template #default="{ row }"><el-image v-if="row.prizeImage" :src="row.prizeImage" class="thumb-image" preview-teleported /><span v-else>-</span></template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button size="small" type="primary" @click="selectPrizeForForm(row)">选择</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.mdetail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mdetail-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mdetail-hero__badge {
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

.mdetail-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mdetail-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mdetail-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    padding: 0 16px;
    background: #fff;
    border: 1px solid var(--nf-border);
    border-radius: 8px 8px 0 0;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    display: none;
  }
}

.mdetail-body.nf-table-card {
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

.detail-toolbar,
.stats-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.search-grid {
  margin-bottom: 12px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
    margin-right: 12px;
  }
}

.merchant-info-form {
  margin-top: 0;
}

.merchant-info-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

.permission-form {
  margin-top: 12px;
}

.form-section-title {
  margin: 8px 0 4px;
  padding-left: 12px;
  border-left: 3px solid var(--nf-brand);
  font-weight: 600;
  color: var(--nf-text-primary);
}

.upload-block {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 110px;
}

.image-preview {
  width: 96px;
  height: 96px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--el-border-color-light);
}

.bind-user-row {
  display: flex;
  width: 100%;
  gap: 8px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  width: 100%;
  align-items: start;
  gap: 8px;
}

.action-buttons.single-line {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.action-buttons :deep(.el-button) {
  width: 100%;
  margin-left: 0;
}

.address-actions-row {
  display: flex;
  width: 100%;
  gap: 8px;
}

.thumb-image {
  width: 56px;
  height: 56px;
  border-radius: 10px;
}

.link-text {
  color: var(--nf-brand-deep);
  cursor: pointer;
}

.order-total-money {
  margin-top: 10px;
  color: #b94a48;
  font-size: 20px;
  display: inline-block;
}

.text-danger {
  color: var(--el-color-danger);
}

.stat-card {
  height: 108px;
  padding: 16px;
  border: 1px solid var(--nf-border);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 12px;
  background: #fff;
}

.stat-card span {
  color: var(--nf-text-secondary);
  font-size: 13px;
}

.stat-card strong {
  color: var(--nf-brand-deep);
  font-size: 22px;
  font-weight: 600;
}

.goods-stats-section {
  margin-bottom: 20px;
}

.goods-stats-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.goods-stats-picker {
  width: 320px;
  max-width: 320px;
}

.goods-stats-head :deep(.goods-stats-picker.el-date-editor) {
  width: 320px;
  max-width: 320px;
}

.revenue-day-card {
  margin-top: 16px;
}

.revenue-day-title {
  margin-bottom: 12px;
  color: var(--el-text-color-regular);
}

.revenue-day-title span {
  margin-left: 16px;
  color: #d97706;
}

.qr-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.qr-image {
  width: 240px;
  height: 240px;
  object-fit: contain;
}

.qr-actions {
  display: flex;
  gap: 12px;
}

.map-dialog-layout {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.map-dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.map-hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.map-container {
  width: 100%;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-light);
}

.rich-preview {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.prize-editor {
  width: 100%;
}

.prize-editor :deep(.ql-container) {
  min-height: 240px;
}

.prize-editor :deep(.ql-editor) {
  min-height: 200px;
}

@media (max-width: 900px) {
  .bind-user-row,
  .address-actions-row,
  .detail-toolbar,
  .stats-toolbar,
  .map-dialog-toolbar {
    flex-wrap: wrap;
  }
}
</style>
