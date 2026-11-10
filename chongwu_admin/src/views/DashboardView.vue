<script setup>
	import * as echarts from 'echarts'
	import {
		nextTick,
		onBeforeUnmount,
		onMounted,
		reactive,
		ref
	} from 'vue'
	import {
		getDashboardCouponCountTrend,
		getDashboardCouponStats,
		getDashboardDynamicStats,
	getDashboardIncomeStats,
	getDashboardLoveWalletStats,
	getDashboardLoveOrderStats,
		getDashboardOrderCountTrend,
		getDashboardOrderTrend,
		getDashboardOverview,
		getDashboardPayCountTrend,
		getDashboardPaymentStats,
		getDashboardRepeatRateTrend,
		getDashboardStoreGoodsTrend,
		getDashboardTearoomList,
		getDashboardUserCountTrend,
		getDashboardUserGrowthTrend,
		getDashboardUserPlatform,
		getDashboardVenueAnalysis,
		getDashboardWriteOffStats,
	} from '@/api/modules/dashboard'

	const loading = reactive({
		page: false,
		venue: false,
		loveWallet: false,
		orderTrend: false,
		growthTrend: false,
		userCount: false,
		payCount: false,
		repeatRate: false,
		orderCount: false,
		couponCount: false,
		storeGoods: false,
	})

	const periodOptions = [{
			label: '按天查询',
			value: 1
		},
		{
			label: '按月查询',
			value: 2
		},
		{
			label: '按年查询',
			value: 3
		},
	]

	const activeTab = ref('user')
	const summaryFilters = reactive({
		flag: 1,
		date: today()
	})
	const venuePager = reactive({
		page: 1,
		limit: 5,
		total: 0
	})
	const loveWalletPager = reactive({
		page: 1,
		limit: 10,
		total: 0
	})
	const venueTable = ref([])
	const loveWalletTable = ref([])
	const overview = ref({})
	const userStat = ref({
		vipList: []
	})
	const paymentStat = ref({})
	const incomeStat = ref({})
	const couponStat = ref({})
	const loveOrderStat = ref({})
	const dynamicStat = ref({})
	const writeOffStat = ref({})
	const pendingAuditStat = ref({})
	const tearoomOptions = ref([])

	const chartFilters = reactive({
		orderRange: lastDaysRange(6),
		growthRange: lastDaysRange(6),
		userCountFlag: 2,
		userCountRange: lastMonthsRange(6),
		payCountFlag: 2,
		payCountRange: lastMonthsRange(6),
		repeatRateFlag: 2,
		repeatRateRange: lastMonthsRange(6),
		orderCountFlag: 2,
		orderCountRange: lastMonthsRange(6),
		couponCountFlag: 2,
		couponCountRange: lastMonthsRange(6),
		storeGoodsFlag: 2,
		storeGoodsRange: lastMonthsRange(6),
		tearoomId: '',
	})

	const orderTrendRef = ref()
	const growthTrendRef = ref()
	const userCountRef = ref()
	const payCountRef = ref()
	const repeatRateRef = ref()
	const orderCountRef = ref()
	const couponCountRef = ref()
	const storeGoodsRef = ref()

	const chartMap = new Map()
	let pendingAuditTimer = null

	function pad(value) {
		return `${value}`.padStart(2, '0')
	}

	function today() {
		const date = new Date()
		return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
	}

	function shiftDate(days) {
		const date = new Date()
		date.setDate(date.getDate() - days)
		return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
	}

	function shiftMonth(offset) {
		const date = new Date()
		date.setMonth(date.getMonth() - offset)
		return `${date.getFullYear()}-${pad(date.getMonth() + 1)}`
	}

	function shiftYear(offset) {
		return `${new Date().getFullYear() - offset}`
	}

	function lastDaysRange(days) {
		return [shiftDate(days), today()]
	}

	function lastMonthsRange(months) {
		return [shiftMonth(months), today().slice(0, 7)]
	}

	function lastYearsRange(years) {
		return [shiftYear(years), `${new Date().getFullYear()}`]
	}

	function pickerType(flag) {
		return flag === 1 ? 'date' : flag === 2 ? 'month' : 'year'
	}

	function pickerRangeType(flag) {
		return flag === 1 ? 'daterange' : flag === 2 ? 'monthrange' : 'yearrange'
	}

	function valueFormat(flag) {
		return flag === 1 ? 'YYYY-MM-DD' : flag === 2 ? 'YYYY-MM' : 'YYYY'
	}

	function getPresetRange(flag) {
		return flag === 1 ? lastDaysRange(6) : flag === 2 ? lastMonthsRange(6) : lastYearsRange(6)
	}

	function normalizePeriodValue(value, flag) {
		if (!value) {
			return ''
		}
		return flag === 1 ? value : flag === 2 ? value.slice(0, 7) : value.slice(0, 4)
	}

	function chartParams(start, end, flag, extras = {}) {
		return {
			startTime: normalizePeriodValue(start, flag),
			endTime: normalizePeriodValue(end, flag),
			type: flag,
			...extras,
		}
	}

	function amount(value) {
		return Number(value || 0).toLocaleString('zh-CN', {
			maximumFractionDigits: 2
		})
	}

	function count(value) {
		return Number(value || 0).toLocaleString('zh-CN')
	}

	function getCookieValue(name) {
		const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
		return match ? decodeURIComponent(match[1]) : ''
	}

	function getCookieNumber(name) {
		const value = Number(getCookieValue(name))
		return Number.isFinite(value) ? value : 0
	}

	function getChart(el, key) {
		if (!el) {
			return null
		}
		const cached = chartMap.get(key)
		if (cached) {
			return cached
		}
		const chart = echarts.init(el)
		chartMap.set(key, chart)
		return chart
	}

	function rangeToParams(range, flag, extras = {}) {
		return chartParams(range?.[0], range?.[1], flag, extras)
	}

	function fallbackAxisData(axisData = []) {
		return Array.isArray(axisData) && axisData.length ? axisData : ['暂无数据']
	}

	function fallbackSeriesData(data = [], axisData = []) {
		if (Array.isArray(data) && data.length) {
			return data
		}
		return fallbackAxisData(axisData).map(() => 0)
	}

	function fallbackDataset(dataset) {
		if (dataset?.source?.length) {
			return dataset
		}
		return {
			source: [
				['项目', '暂无数据'],
				['数量', 0],
			],
		}
	}

	function renderLineChart(key, el, legend, xAxis, series) {
		const chart = getChart(el, key)
		if (!chart) return
		const axisData = fallbackAxisData(xAxis)
		const normalizedSeries = (series || []).map((item) => ({
			...item,
			data: fallbackSeriesData(item?.data, axisData)
		}))
		chart.setOption({
			color: ['#c86c35', '#4f7cff', '#1f9d8b', '#ef6c57', '#ffc145', '#7a8cff', '#6f8196'],
			tooltip: {
				trigger: 'axis'
			},
			legend: {
				top: 4,
				data: legend
			},
			grid: {
				left: 40,
				right: 24,
				top: 52,
				bottom: 24,
				containLabel: true
			},
			xAxis: {
				type: 'category',
				boundaryGap: false,
				data: axisData
			},
			yAxis: {
				type: 'value'
			},
			series: normalizedSeries,
		})
	}

	function renderBarChart(key, el, xAxis, seriesName, data, color = '#c86c35') {
		const chart = getChart(el, key)
		if (!chart) return
		const axisData = fallbackAxisData(xAxis)
		chart.setOption({
			color: [color],
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'shadow'
				}
			},
			grid: {
				left: 40,
				right: 24,
				top: 24,
				bottom: 24,
				containLabel: true
			},
			xAxis: {
				type: 'category',
				data: axisData,
				axisTick: {
					alignWithLabel: true
				}
			},
			yAxis: {
				type: 'value'
			},
			series: [{
				name: seriesName,
				type: 'bar',
				barMaxWidth: 28,
				data: fallbackSeriesData(data, axisData)
			}],
		})
	}

	function renderDatasetChart(key, el, dataset) {
		const chart = getChart(el, key)
		if (!chart) return
		const normalizedDataset = fallbackDataset(dataset)
		const seriesCount = Math.max(normalizedDataset.source.length - 1, 0)
		chart.setOption({
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'shadow'
				}
			},
			legend: {
				top: 4
			},
			dataset: normalizedDataset,
			grid: {
				left: 40,
				right: 24,
				top: 52,
				bottom: 24,
				containLabel: true
			},
			xAxis: {
				type: 'category'
			},
			yAxis: {
				type: 'value'
			},
			series: Array.from({
				length: seriesCount
			}).map(() => ({
				type: 'bar',
				seriesLayoutBy: 'row',
				barMaxWidth: 22
			})),
		})
	}

	function resizeCharts() {
		chartMap.forEach((chart) => chart.resize())
	}

	async function loadOverviewBundle() {
		loading.page = true
		try {
			const [
				overviewData,
				userPlatformData,
				paymentData,
				incomeData,
				couponData,
				loveOrderData,
				dynamicData,
				writeOffData,
			] = await Promise.all([
				getDashboardOverview(),
				getDashboardUserPlatform({
					data: summaryFilters.date,
					type: summaryFilters.flag
				}),
				getDashboardPaymentStats({
					time: summaryFilters.date,
					flag: summaryFilters.flag
				}),
				getDashboardIncomeStats({
					time: summaryFilters.date,
					type: summaryFilters.flag
				}),
				getDashboardCouponStats({
					time: summaryFilters.date,
					type: summaryFilters.flag
				}),
				getDashboardLoveOrderStats({
					time: summaryFilters.date,
					type: summaryFilters.flag
				}),
				getDashboardDynamicStats({
					date: summaryFilters.date,
					way: summaryFilters.flag
				}),
				getDashboardWriteOffStats({
					time: summaryFilters.date,
					flag: summaryFilters.flag
				}),
			])
			overview.value = overviewData || {}
			userStat.value = {
				vipList: [],
				...(userPlatformData || {})
			}
			paymentStat.value = paymentData || {}
			incomeStat.value = incomeData || {}
			couponStat.value = couponData || {}
			loveOrderStat.value = loveOrderData || {}
			dynamicStat.value = dynamicData || {}
			writeOffStat.value = writeOffData || {}
			loadPendingAuditStats()
			await loadVenueAnalysis()
			await loadLoveWalletStats()
		} finally {
			loading.page = false
		}
	}

	async function loadVenueAnalysis() {
		loading.venue = true
		try {
			const data = await getDashboardVenueAnalysis({
				time: summaryFilters.date,
				flag: summaryFilters.flag,
				page: venuePager.page,
				limit: venuePager.limit,
			})
			venueTable.value = data?.list || []
			venuePager.total = data?.totalCount || 0
		} finally {
			loading.venue = false
		}
	}

	async function loadLoveWalletStats() {
		loading.loveWallet = true
		try {
			const data = await getDashboardLoveWalletStats({
				time: summaryFilters.date,
				type: summaryFilters.flag,
				tearoomName: '',
				page: loveWalletPager.page,
				limit: loveWalletPager.limit,
			})
			loveWalletTable.value = data?.list || []
			loveWalletPager.total = data?.totalCount || 0
		} finally {
			loading.loveWallet = false
		}
	}

	function loadPendingAuditStats() {
		pendingAuditStat.value = {
			activity: getCookieNumber('numberHd'),
			auth: getCookieNumber('numberRz'),
			dynamic: getCookieNumber('numberDt'),
			withdraw: getCookieNumber('numberTx'),
			logout: getCookieNumber('numberZx'),
			complaint: getCookieNumber('numberTs'),
			selfOrder: getCookieNumber('numberZyDfh') + getCookieNumber('numberZyDtk'),
		}
	}

	async function loadOrderTrend() {
		loading.orderTrend = true
		try {
			const data = await getDashboardOrderTrend({
				startTime: chartFilters.orderRange[0],
				endTime: chartFilters.orderRange[1]
			})
			await nextTick()
			renderLineChart('orderTrend', orderTrendRef.value, ['订单总量', '待支付', '待使用', '使用中', '已完成', '已取消', '待评价'], data
				?.year, [{
						name: '订单总量',
						type: 'line',
						smooth: true,
						data: data?.ordersCountList || []
					},
					{
						name: '待支付',
						type: 'line',
						smooth: true,
						data: data?.ordersDzfCountList || []
					},
					{
						name: '待使用',
						type: 'line',
						smooth: true,
						data: data?.ordersDsyCountList || []
					},
					{
						name: '使用中',
						type: 'line',
						smooth: true,
						data: data?.ordersSyzCountList || []
					},
					{
						name: '已完成',
						type: 'line',
						smooth: true,
						data: data?.ordersYwcCountList || []
					},
					{
						name: '已取消',
						type: 'line',
						smooth: true,
						data: data?.ordersYqxCountList || []
					},
					{
						name: '待评价',
						type: 'line',
						smooth: true,
						data: data?.ordersDpjCountList || []
					},
				])
		} finally {
			loading.orderTrend = false
		}
	}

	async function loadGrowthTrend() {
		loading.growthTrend = true
		try {
			const data = await getDashboardUserGrowthTrend({
				startTime: chartFilters.growthRange[0],
				endTime: chartFilters.growthRange[1]
			})
			await nextTick()
			renderLineChart('growthTrend', growthTrendRef.value, ['用户增长量'], data?.year, [{
				name: '用户增长量',
				type: 'line',
				smooth: true,
				areaStyle: {
					opacity: 0.12
				},
				data: data?.userCountList || []
			}, ])
		} finally {
			loading.growthTrend = false
		}
	}

	async function loadUserCountTrend() {
		loading.userCount = true
		try {
			const data = await getDashboardUserCountTrend(rangeToParams(chartFilters.userCountRange, chartFilters
				.userCountFlag))
			await nextTick()
			renderBarChart('userCount', userCountRef.value, data?.date, '用户量', data?.userCountList, '#c86c35')
		} finally {
			loading.userCount = false
		}
	}

	async function loadPayCountTrend() {
		loading.payCount = true
		try {
			const data = await getDashboardPayCountTrend(rangeToParams(chartFilters.payCountRange, chartFilters
				.payCountFlag, {
					classify: 16
				}))
			await nextTick()
			renderBarChart('payCount', payCountRef.value, data?.date, '支付统计', data?.payCountList, '#4f7cff')
		} finally {
			loading.payCount = false
		}
	}

	async function loadRepeatRateTrend() {
		loading.repeatRate = true
		try {
			const data = await getDashboardRepeatRateTrend(rangeToParams(chartFilters.repeatRateRange, chartFilters
				.repeatRateFlag))
			await nextTick()
			renderBarChart('repeatRate', repeatRateRef.value, data?.dateList, '复购率', data?.repeatRateList, '#1f9d8b')
		} finally {
			loading.repeatRate = false
		}
	}

	async function loadOrderCountTrend() {
		loading.orderCount = true
		try {
			const data = await getDashboardOrderCountTrend(rangeToParams(chartFilters.orderCountRange, chartFilters
				.orderCountFlag, {
					classify: 1
				}))
			await nextTick()
			renderBarChart('orderCount', orderCountRef.value, data?.date, '开台统计', data?.payCountList, '#ef6c57')
		} finally {
			loading.orderCount = false
		}
	}

	async function loadCouponCountTrend() {
		loading.couponCount = true
		try {
			const data = await getDashboardCouponCountTrend(rangeToParams(chartFilters.couponCountRange, chartFilters
				.couponCountFlag, {
					classify: 1
				}))
			await nextTick()
			renderBarChart('couponCount', couponCountRef.value, data?.date, '核销统计', data?.couponCountList, '#ffc145')
		} finally {
			loading.couponCount = false
		}
	}

	async function loadStoreGoodsTrend() {
		if (!chartFilters.tearoomId) {
			return
		}
		loading.storeGoods = true
		try {
			const data = await getDashboardStoreGoodsTrend(rangeToParams(chartFilters.storeGoodsRange, chartFilters
				.storeGoodsFlag, {
					classify: 1,
					tearoomId: chartFilters.tearoomId,
				}))
			await nextTick()
			renderDatasetChart('storeGoods', storeGoodsRef.value, data?.dataset)
		} finally {
			loading.storeGoods = false
		}
	}

	async function loadTearoomOptions() {
		const data = await getDashboardTearoomList({
			page: 1,
			limit: 1000,
			tearoomName: '',
			city: '',
			status: 1
		})
		tearoomOptions.value = data?.list || []
		chartFilters.tearoomId = tearoomOptions.value[0]?.tearoomId || ''
	}

	async function loadCharts() {
		await Promise.all([
			loadOrderTrend(),
			loadGrowthTrend(),
			loadUserCountTrend(),
			loadPayCountTrend(),
			loadRepeatRateTrend(),
			loadOrderCountTrend(),
			loadCouponCountTrend(),
			loadStoreGoodsTrend(),
		])
	}

	async function handleSummarySearch() {
		venuePager.page = 1
		loveWalletPager.page = 1
		await loadOverviewBundle()
	}

	async function handleChartSearch(type) {
		const actions = {
			order: loadOrderTrend,
			growth: loadGrowthTrend,
			userCount: loadUserCountTrend,
			payCount: loadPayCountTrend,
			repeatRate: loadRepeatRateTrend,
			orderCount: loadOrderCountTrend,
			couponCount: loadCouponCountTrend,
			storeGoods: loadStoreGoodsTrend,
		}
		await actions[type]?.()
	}

	async function handleChartRangeChange(type) {
		await handleChartSearch(type)
	}

	function handleChartFlagChange(type, flagKey, rangeKey) {
		chartFilters[rangeKey] = getPresetRange(chartFilters[flagKey])
		handleChartSearch(type)
	}

	onMounted(async () => {
		window.addEventListener('resize', resizeCharts)
		loadPendingAuditStats()
		pendingAuditTimer = window.setInterval(loadPendingAuditStats, 1000)
		await loadTearoomOptions()
		await Promise.all([loadOverviewBundle(), loadCharts()])
	})

	onBeforeUnmount(() => {
		window.removeEventListener('resize', resizeCharts)
		if (pendingAuditTimer) {
			window.clearInterval(pendingAuditTimer)
		}
		chartMap.forEach((chart) => chart.dispose())
		chartMap.clear()
	})
</script>

<template>
	<el-space direction="vertical" fill size="20">
		<el-row :gutter="16" class="dashboard-summary-row">
			<el-col v-for="item in [
        ['总用户数', count(overview.totalUsers), '人'],
        ['今日新增', count(overview.newToday), '人'],
        ['本月新增', count(overview.newMonth), '人'],
        ['本年新增', count(overview.newYear), '人'],
        ['总充值', amount(overview.totalRevenue), '元'],
        ['今日充值', amount(overview.todayRevenue), '元'],
        ['本月充值', amount(overview.monthRevenue), '元'],
        ['本年充值', amount(overview.yearRevenue), '元'],
        ['剩余会员卡余额', amount(overview.sumMoney), '元'],
        ['订单支付金额', amount(overview.ordersMoney), '元'],
        ['抖音核销金额', amount(overview.dyMoney), '元'],
        ['美团核销金额', amount(overview.mtMoney), '元'],
        ['快手核销金额', amount(overview.ksMoney), '元'],
      ]" :key="item[0]" class="dashboard-summary-col" :xl="6" :lg="6" :md="8" :sm="12" :xs="24">
				<el-card shadow="hover" class="metric-card dashboard-metric">
					<span class="metric-card__label">{{ item[0] }}</span>
					<strong class="metric-card__value">{{ item[1] }}</strong>
					<span class="dashboard-metric__unit">{{ item[2] }}</span>
				</el-card>
			</el-col>
		</el-row>

		<el-card shadow="never" class="page-card">
			<div class="dashboard-toolbar dashboard-toolbar--panel">
				<div>
					<strong>数据中心</strong>
				</div>
				<el-space wrap class="dashboard-toolbar__actions">
					<el-select v-model="summaryFilters.flag" class="dashboard-toolbar__select">
						<el-option v-for="item in periodOptions" :key="item.value" :label="item.label"
							:value="item.value" />
					</el-select>
					<el-date-picker v-model="summaryFilters.date" :type="pickerType(summaryFilters.flag)"
						:format="summaryFilters.flag === 1 ? 'YYYY-MM-DD' : summaryFilters.flag === 2 ? 'YYYY-MM' : 'YYYY'"
						:value-format="summaryFilters.flag === 1 ? 'YYYY-MM-DD' : 'YYYY-MM-DD'"
						class="dashboard-toolbar__date" />
					<el-button type="primary" :loading="loading.page" @click="handleSummarySearch">刷新数据</el-button>
				</el-space>
			</div>

			<el-tabs v-model="activeTab">
				<el-tab-pane label="用户分析" name="user">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['总用户统计', count(userStat.userCount), '人'],
              ['手机号用户', count(userStat.userPhoneCount), '人'],
              ['未绑定手机号用户', count(userStat.userNullPhoneCount), '人'],
              ['小程序用户统计', count(userStat.wxCount), '人'],
              ['羽毛球馆用户统计', count(userStat.tearoomCount), '人'],
            ]" :key="item[0]" :lg="6" :md="8" :sm="12" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card">
								<span class="metric-card__label">{{ item[0] }}</span>
								<strong class="dashboard-sub-card__value">{{ item[1] }}</strong>
								<span class="dashboard-metric__unit">{{ item[2] }}</span>
							</el-card>
						</el-col>
						<el-col v-for="item in userStat.vipList || []" :key="item.vipName" :lg="6" :md="8" :sm="12" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card">
								<span class="metric-card__label">{{ item.vipName }}用户</span>
								<strong class="dashboard-sub-card__value">{{ count(item.vipCount) }}</strong>
								<span class="dashboard-metric__unit">人</span>
							</el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="羽毛球馆分析" name="venue">
					<el-table v-loading="loading.venue" :data="venueTable" border>
						<el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="160" />
						<el-table-column label="羽毛球馆图片" min-width="120">
							<template #default="{ row }">
								<el-image v-if="row.tearoomCoverImage" :src="row.tearoomCoverImage.split(',')[0]"
									fit="cover" style="width: 60px; height: 60px; border-radius: 12px" />
								<span v-else>暂无图片</span>
							</template>
						</el-table-column>
						<el-table-column label="地址" min-width="260">
							<template
								#default="{ row }">{{ `${row.tearoomProvince || ''}${row.tearoomCity || ''}${row.tearoomDistrict || ''}${row.tearoomAddress || ''}` }}</template>
						</el-table-column>
						<el-table-column prop="sumMoney" label="收益" min-width="120" />
					</el-table>
					<div class="base-pagination">
						<el-pagination :current-page="venuePager.page" :page-size="venuePager.limit"
							:page-sizes="[5, 10, 15, 20]" :total="venuePager.total" background
							layout="total, sizes, prev, pager, next, jumper"
							@update:current-page="venuePager.page = $event; loadVenueAnalysis()"
							@update:page-size="venuePager.limit = $event; venuePager.page = 1; loadVenueAnalysis()" />
					</div>
				</el-tab-pane>

				<el-tab-pane label="支付统计" name="payment">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['支付总金额', amount(paymentStat.sumMoney)],
              ['微信小程序支付金额', amount(paymentStat.weiXinXCXMoney)],
              ['微信APP支付金额', amount(paymentStat.weiXinAppMoney)],
              ['微信公众号支付金额', amount(paymentStat.weiXinGZHMoney)],
              ['支付宝APP支付金额', amount(paymentStat.zhiFuBaoAppMoney)],
              ['支付宝H5支付金额', amount(paymentStat.zhiFuBaoH5Money)],
              ['系统充值', amount(paymentStat.xiTongMoney)],
              ['退款金额', amount(paymentStat.refundMoney)],
            ]" :key="item[0]" :lg="6" :md="8" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong><span
									class="dashboard-metric__unit">元</span></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="收益统计" name="income">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['订单支付金额', amount(incomeStat.ordersPayMoney)],
              ['商家收益', amount(incomeStat.ordersShopMoney)],
              ['分销金额', amount(incomeStat.ordersFenMoney)],
              ['平台收益', amount(incomeStat.ordersPingMoney)],
            ]" :key="item[0]" :lg="6" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong><span
									class="dashboard-metric__unit">元</span></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="公益订单统计" name="love-order">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['总订单数量', count(loveOrderStat.ordersCount)],
              ['会员卡订单数量', count(loveOrderStat.ordersMoneyCount)],
              ['团购订单数量', count(loveOrderStat.ordersCouponCount)],
            ]" :key="item[0]" :lg="6" :md="8" :sm="12" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong><span
									class="dashboard-metric__unit">单</span></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="爱心钱包统计" name="love-wallet">
					<el-table v-loading="loading.loveWallet" :data="loveWalletTable" border>
						<el-table-column type="index" label="编号" width="80" />
						<el-table-column prop="tearoomName" label="门店名称" min-width="180" />
						<el-table-column prop="money" label="爱心钱包" min-width="120" />
						<el-table-column prop="donationMoney" label="已捐款" min-width="120" />
					</el-table>
					<div class="base-pagination">
						<el-pagination
							:current-page="loveWalletPager.page"
							:page-size="loveWalletPager.limit"
							:page-sizes="[10, 20, 30, 50, 100]"
							:total="loveWalletPager.total"
							background
							layout="total, sizes, prev, pager, next"
							@update:current-page="loveWalletPager.page = $event; loadLoveWalletStats()"
							@update:page-size="loveWalletPager.limit = $event; loveWalletPager.page = 1; loadLoveWalletStats()"
						/>
					</div>
				</el-tab-pane>

				<el-tab-pane label="优惠券统计" name="coupon">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['新用户领取数量', count(couponStat.xyhCount)],
              ['邀请新用户数量', count(couponStat.yqxhCount)],
              ['邀请用户下单数量', count(couponStat.yqxdCount)],
              ['支付购买数量', count(couponStat.zfgmCount)],
              ['积分兑换数量', count(couponStat.jfdhCount)],
              ['上架赠送数量', count(couponStat.sjzsCount)],
            ]" :key="item[0]" :lg="6" :md="8" :sm="12" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="动态统计" name="dynamic">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['动态总数', count(dynamicStat.totalCount), '个'],
              ['待审核数量', count(dynamicStat.auditIng), '个'],
              ['已发布数量', count(dynamicStat.auditEd), '个'],
              ['已下架数量', count(dynamicStat.auditOut), '个'],
            ]" :key="item[0]" :lg="6" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong><span
									class="dashboard-metric__unit">{{ item[2] }}</span></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="核销统计" name="writeoff">
					<el-row :gutter="16">
						<el-col v-for="item in [
              ['美团核销总数', count(writeOffStat.mtCount), '个'],
              ['美团核销金额', amount(writeOffStat.mtMoney), '元'],
              ['抖音核销总数', count(writeOffStat.dyCount), '个'],
              ['抖音核销金额', amount(writeOffStat.dyMoney), '元'],
              ['快手核销总数', count(writeOffStat.ksCount), '个'],
              ['快手核销金额', amount(writeOffStat.ksMoney), '元'],
              ['购买核销总数', count(writeOffStat.payCount), '个'],
              ['购买核销金额', amount(writeOffStat.payMoney), '元'],
              ['赠送核销总数', count(writeOffStat.zsCount), '个'],
              ['赠送核销金额', amount(writeOffStat.zsMoney), '元'],
            ]" :key="item[0]" :lg="6" :md="8" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card"><span
									class="metric-card__label">{{ item[0] }}</span><strong
									class="dashboard-sub-card__value">{{ item[1] }}</strong><span
									class="dashboard-metric__unit">{{ item[2] }}</span></el-card>
						</el-col>
					</el-row>
				</el-tab-pane>

				<el-tab-pane label="待审核统计" name="pending-audit">
					<el-row :gutter="16">
						<el-col v-for="item in [
							['活动待审核', count(pendingAuditStat.activity), '个'],
							['认证待审核', count(pendingAuditStat.auth), '个'],
							['动态待审核', count(pendingAuditStat.dynamic), '个'],
							['提现待审核', count(pendingAuditStat.withdraw), '个'],
							['注销待审核', count(pendingAuditStat.logout), '个'],
							['投诉待审核', count(pendingAuditStat.complaint), '个'],
							['自营订单待处理', count(pendingAuditStat.selfOrder), '个'],
						]" :key="item[0]" :lg="6" :md="8" :xs="24">
							<el-card shadow="hover" class="dashboard-sub-card">
								<span class="metric-card__label">{{ item[0] }}</span>
								<strong class="dashboard-sub-card__value dashboard-sub-card__value--alert">{{ item[1] }}</strong>
								<span class="dashboard-metric__unit">{{ item[2] }}</span>
							</el-card>
						</el-col>
					</el-row>
				</el-tab-pane>
			</el-tabs>
		</el-card>

		<el-row :gutter="16">
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header">
							<strong>订单折线图</strong>
							<el-date-picker v-model="chartFilters.orderRange" type="daterange" value-format="YYYY-MM-DD" :clearable="false"
								format="YYYY-MM-DD" @change="handleChartSearch('order')" />
						</div>
					</template>
					<div ref="orderTrendRef" v-loading="loading.orderTrend" class="dashboard-chart" />
				</el-card>
			</el-col>
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header">
							<strong>用户增长折线图</strong>
							<el-date-picker v-model="chartFilters.growthRange" type="daterange" :clearable="false"
								value-format="YYYY-MM-DD" format="YYYY-MM-DD" @change="handleChartSearch('growth')" />
						</div>
					</template>
					<div ref="growthTrendRef" v-loading="loading.growthTrend" class="dashboard-chart" />
				</el-card>
			</el-col>
		</el-row>

		<el-row :gutter="16">
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>用户量统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.userCountFlag" class="chart-filter-select"
									@change="handleChartFlagChange('userCount', 'userCountFlag', 'userCountRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.userCountRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.userCountFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.userCountFlag)"
									:format="valueFormat(chartFilters.userCountFlag)"
									@change="handleChartRangeChange('userCount')" />
							</el-space>
						</div>
					</template>
					<div ref="userCountRef" v-loading="loading.userCount" class="dashboard-chart" />
				</el-card>
			</el-col>
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>复购率统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.repeatRateFlag" class="chart-filter-select"
									@change="handleChartFlagChange('repeatRate', 'repeatRateFlag', 'repeatRateRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.repeatRateRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.repeatRateFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.repeatRateFlag)"
									:format="valueFormat(chartFilters.repeatRateFlag)"
									@change="handleChartRangeChange('repeatRate')" />
							</el-space>
						</div>
					</template>
					<div ref="repeatRateRef" v-loading="loading.repeatRate" class="dashboard-chart" />
				</el-card>
			</el-col>
		</el-row>

		<el-row :gutter="16">
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>支付统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.payCountFlag" class="chart-filter-select"
									@change="handleChartFlagChange('payCount', 'payCountFlag', 'payCountRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.payCountRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.payCountFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.payCountFlag)"
									:format="valueFormat(chartFilters.payCountFlag)"
									@change="handleChartRangeChange('payCount')" />
							</el-space>
						</div>
					</template>
					<div ref="payCountRef" v-loading="loading.payCount" class="dashboard-chart" />
				</el-card>
			</el-col>
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>开台统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.orderCountFlag" class="chart-filter-select"
									@change="handleChartFlagChange('orderCount', 'orderCountFlag', 'orderCountRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.orderCountRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.orderCountFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.orderCountFlag)"
									:format="valueFormat(chartFilters.orderCountFlag)"
									@change="handleChartRangeChange('orderCount')" />
							</el-space>
						</div>
					</template>
					<div ref="orderCountRef" v-loading="loading.orderCount" class="dashboard-chart" />
				</el-card>
			</el-col>
		</el-row>

		<el-row :gutter="16">
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>核销统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.couponCountFlag" class="chart-filter-select"
									@change="handleChartFlagChange('couponCount', 'couponCountFlag', 'couponCountRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.couponCountRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.couponCountFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.couponCountFlag)"
									:format="valueFormat(chartFilters.couponCountFlag)"
									@change="handleChartRangeChange('couponCount')" />
							</el-space>
						</div>
					</template>
					<div ref="couponCountRef" v-loading="loading.couponCount" class="dashboard-chart" />
				</el-card>
			</el-col>
			<el-col :lg="12" :xs="24">
				<el-card shadow="never" class="page-card chart-card">
					<template #header>
						<div class="chart-header chart-header--wrap">
							<strong>门店场地统计</strong>
							<el-space wrap class="chart-filter-group">
								<el-select v-model="chartFilters.tearoomId" placeholder="请选择羽毛球馆"
									class="chart-filter-store" @change="handleChartSearch('storeGoods')">
									<el-option v-for="item in tearoomOptions" :key="item.tearoomId"
										:label="item.tearoomName" :value="item.tearoomId" />
								</el-select>
								<el-select v-model="chartFilters.storeGoodsFlag" class="chart-filter-select"
									@change="handleChartFlagChange('storeGoods', 'storeGoodsFlag', 'storeGoodsRange')"><el-option
										v-for="item in periodOptions" :key="item.value" :label="item.label"
										:value="item.value" /></el-select>
								<el-date-picker v-model="chartFilters.storeGoodsRange" class="chart-filter-range"
									:type="pickerRangeType(chartFilters.storeGoodsFlag)"
									:clearable="false"
									:value-format="valueFormat(chartFilters.storeGoodsFlag)"
									:format="valueFormat(chartFilters.storeGoodsFlag)"
									@change="handleChartRangeChange('storeGoods')" />
							</el-space>
						</div>
					</template>
					<div ref="storeGoodsRef" v-loading="loading.storeGoods"
						class="dashboard-chart dashboard-chart--large" />
				</el-card>
			</el-col>
		</el-row>
	</el-space>
</template>

<style scoped>
	.dashboard-toolbar p,
	.dashboard-metric__unit {
		color: var(--muted);
	}

	.dashboard-toolbar,
	.chart-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16px;
	}

	.chart-header--wrap {
		align-items: flex-start;
	}

	.chart-filter-group {
		justify-content: flex-end;
		flex: 0 1 auto;
	}

	.chart-filter-select {
		width: 100px;
	}

	.chart-filter-range {
		width: 100px;
	}

	.chart-filter-store {
		width: 132px;
	}

	.dashboard-toolbar--panel {
		margin-bottom: 20px;
	}

	.dashboard-toolbar__actions {
		flex: 0 1 auto;
	}

	.dashboard-toolbar__select {
		width: 112px;
	}

	.dashboard-toolbar__date {
		width: 128px;
	}

	.dashboard-toolbar p {
		margin: 6px 0 0;
	}

	.dashboard-metric {
		min-height: 148px;
	}

	.dashboard-summary-row {
		margin-bottom: 20px;
	}

	.dashboard-summary-col {
		margin-bottom: 16px;
	}

	.dashboard-sub-card {
		min-height: 126px;
		margin-bottom: 16px;
	}

	.dashboard-sub-card__value {
		display: block;
		margin: 12px 0 10px;
		font-size: 28px;
		color: var(--ink);
	}

	.dashboard-sub-card__value--alert {
		color: #d84f3f;
	}

	.chart-card :deep(.el-card__body) {
		padding: 12px 12px 4px;
	}

	.dashboard-chart {
		width: 100%;
		height: 320px;
	}

	.dashboard-chart--large {
		height: 360px;
	}

	@media (max-width: 992px) {

		.dashboard-toolbar,
		.chart-header,
		.chart-header--wrap {
			flex-direction: column;
			align-items: stretch;
		}

		.chart-filter-group {
			width: 100%;
			justify-content: flex-start;
		}

		.dashboard-toolbar__actions,
		.dashboard-toolbar__date {
			width: 100%;
		}

		.chart-filter-range,
		.chart-filter-store {
			width: 100%;
		}
	}
</style>
