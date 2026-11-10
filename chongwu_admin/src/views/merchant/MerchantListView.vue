<script setup>
	import {
		computed,
		nextTick,
		onMounted,
		reactive,
		ref
	} from 'vue'
	import {
		useRouter
	} from 'vue-router'
	import AppDialog from '@/components/common/AppDialog.vue'
	import BasePagination from '@/components/common/BasePagination.vue'
	import {
		createMerchantApi,
		createMerchantRoomApi,
		deleteMerchantRoomApi,
		getMerchantCenterPageApi,
		getMerchantKsRefreshTokenApi,
		getMerchantRoomPageApi,
		uploadMerchantImageApi,
		updateMerchantApi,
		updateMerchantRoomApi,
	} from '@/api/modules/merchant-center'
	import {
		getUserCenterPage
	} from '@/api/modules/user-center'
	import {
		validateForm
	} from '@/utils/form'
	import {
		confirmAction,
		showError,
		showSuccess
	} from '@/utils/modal'
	import {
		getApiBaseUrl,
		isAuth
	} from '@/utils/tool'

	const router = useRouter()
	const QQ_MAP_KEY = 'PSPBZ-Q4IY3-ABH3Q-O4RTI-OEMVS-NUFIP'

	const loading = ref(false)
	const dialogVisible = ref(false)
	const dialogLoading = ref(false)
	const userPickerVisible = ref(false)
	const userPickerLoading = ref(false)
	const pageNum = ref(1)
	const pageSize = ref(10)
	const total = ref(0)
	const tableData = ref([])
	const qrDialogVisible = ref(false)
	const qrDialogTitle = ref('领水二维码')
	const qrImageUrl = ref('')
	const qrImageName = ref('')
	const formRef = ref()
	const pickerData = ref([])
	const pickerPage = ref(1)
	const pickerSize = ref(10)
	const pickerTotal = ref(0)
	const editingId = ref(0)
	const mapDialogVisible = ref(false)
	const mapLoading = ref(false)
	const mapInitializing = ref(false)
	const imageUploadingField = ref('')
	const mapContainerRef = ref()
	const roomDialogVisible = ref(false)
	const roomLoading = ref(false)
	const roomPage = ref(1)
	const roomSize = ref(10)
	const roomTotal = ref(0)
	const roomList = ref([])
	const roomMerchantId = ref(0)
	const roomMerchantName = ref('')
	const roomFormVisible = ref(false)
	const roomFormLoading = ref(false)
	const roomEditingId = ref(0)
	const ksDialogVisible = ref(false)
	const ksDialogLoading = ref(false)
	const ksCode = ref('')
	const ksTearoomId = ref(0)
	let qqMapScriptPromise = null
	let qqMapInstance = null
	let qqMapMarker = null

	const filters = reactive({
		tearoomName: '',
		phone: '',
		userName: '',
		shopType: '',
		status: '',
		isVipPrivilege: '',
	})

	const pickerFilters = reactive({
		phone: '',
		userName: '',
		invitationCode: '',
	})

	const initialForm = {
		tearoomId: 0,
		tearoomName: '',
		phone: '',
		tearoomPhone: '',
		userId: 0,
		userName: '',
		shopType: 1,
		tearoomProvince: '',
		tearoomCity: '',
		tearoomDistrict: '',
		tearoomAddress: '',
		longitude: '',
		latitude: '',
		tearoomLongitude: '',
		tearoomLatitude: '',
		tearoomCoverImage: '',
		tearoomTitleImage: '',
		qunImg: '',
		wxCode: '',
		siteImg: '',
		welfareImg: '',
		keyCode: '',
		rate: '',
		yyToken: '',
		yyId: '',
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
		moneyScope: '',
		isReserve: 2,
		isOpen: 1,
		isQrCode: 2,
		isVipPrivilege: 2,
		isWelfare: 2,
		welfareContent: '',
		isFaceRecognition: 2,
		isMusic: 1,
		luckySort: '',
		luckyCouponIds: '',
		luckyContent: '',
		isRefund: 1,
		isWxPay: 1,
		broadcastContent: '',
		cameraId: '',
		startTime: '',
		endTime: '',
		openTime1: '',
		closeTime1: '',
		startTime2: '',
		endTime2: '',
		remark: '',
		status: 1,
	}

	const form = reactive({
		...initialForm
	})

	const rules = {
		tearoomName: [{
			required: true,
			message: '请输入羽毛球馆名称',
			trigger: 'blur'
		}],
		tearoomPhone: [{
				required: true,
				message: '请输入联系电话',
				trigger: 'blur'
			},
			{
				pattern: /^1\d{10}$/,
				message: '手机号格式错误',
				trigger: 'blur'
			},
		],
		rate: [{
			required: true,
			message: '请输入佣金比例',
			trigger: 'blur'
		}],
	}

	const statusOptions = [{
			label: '全部',
			value: ''
		},
		{
			label: '正常',
			value: 1
		},
		{
			label: '打烊',
			value: 2
		},
		{
			label: '今日已满',
			value: 3
		},
	]

	const shopTypeOptions = [{
			label: '全部',
			value: ''
		},
		{
			label: '自营',
			value: 1
		},
		{
			label: '三方',
			value: 2
		},
	]

	const yesNoOptions = [{
			label: '是',
			value: 1
		},
		{
			label: '否',
			value: 2
		},
	]

	const moneyScopeOptions = [{
			label: '有加减',
			value: 1
		},
		{
			label: '有加',
			value: 2
		},
		{
			label: '有减',
			value: 3
		},
		{
			label: '没有',
			value: 4
		},
	]

	const isVipPrivilegeOptions = [{
			label: '全部',
			value: ''
		},
		{
			label: '是',
			value: 1
		},
		{
			label: '否',
			value: 2
		},
	]

	const dialogTitle = computed(() => (editingId.value ? '编辑羽毛球馆' : '新增羽毛球馆'))
	const roomDialogTitle = computed(() => (roomEditingId.value ? '修改' : '添加'))

	const roomForm = reactive({
		roomType: 1,
		roomName: '',
		roomNo: '',
		deviceId: '',
		serveCode: '',
		isOpen: 2,
	})

	function resetForm() {
		Object.assign(form, initialForm)
	}

	function syncFormLocationFields() {
		form.tearoomPhone = form.tearoomPhone || form.phone
		form.phone = form.tearoomPhone
		form.tearoomLongitude = form.tearoomLongitude || form.longitude
		form.tearoomLatitude = form.tearoomLatitude || form.latitude
		form.longitude = form.tearoomLongitude
		form.latitude = form.tearoomLatitude
	}

	function firstImage(value) {
		return String(value || '')
			.split(',')
			.map((item) => item.trim())
			.find(Boolean) || ''
	}

	function splitImages(value) {
		return String(value || '')
			.split(',')
			.map((item) => item.trim())
			.filter(Boolean)
	}

	function normalizeStatus(value) {
		const normalized = Number(value)
		if (normalized === 1) return 1
		if (normalized === 2) return 2
		if (normalized === 3) return 3
		if (normalized === 0) return 2
		return 1
	}

	function statusText(value) {
		return statusOptions.find((item) => item.value === Number(value))?.label || '-'
	}

	function shopTypeText(value) {
		return Number(value) === 2 ? '三方' : '自营'
	}

	function yesNoText(value) {
		return Number(value) === 1 ? '是' : '否'
	}

	function moneyScopeText(value) {
		return moneyScopeOptions.find((item) => item.value === Number(value))?.label || '-'
	}

	function roomTypeText(value) {
		return Number(value) === 8 ? '温度设备' : '设备'
	}

	function normalizeMerchantRow(item = {}) {
		return {
			...item,
			tearoomId: Number(item.tearoomId || item.id || 0),
			tearoomName: item.tearoomName || item.name || '',
			phone: item.tearoomPhone || item.phone || item.mobile || item.userPhone || '',
			tearoomPhone: item.tearoomPhone || item.phone || item.mobile || item.userPhone || '',
			userId: Number(item.userId || item.uid || 0),
			userName: item.userName || item.nickName || item.linkUserName || '',
			shopType: Number(item.shopType || 1),
			tearoomProvince: item.tearoomProvince || item.province || '',
			tearoomCity: item.tearoomCity || item.city || '',
			tearoomDistrict: item.tearoomDistrict || item.district || '',
			tearoomAddress: item.tearoomAddress || item.address || '',
			longitude: item.tearoomLongitude || item.longitude || '',
			latitude: item.tearoomLatitude || item.latitude || '',
			tearoomLongitude: item.tearoomLongitude || item.longitude || '',
			tearoomLatitude: item.tearoomLatitude || item.latitude || '',
			tearoomCoverImage: item.tearoomCoverImage || item.coverImage || '',
			tearoomTitleImage: item.tearoomTitleImage || item.titleImage || '',
			qunImg: item.qunImg || '',
			wxCode: item.wxCode || '',
			siteImg: item.siteImg || '',
			welfareImg: item.welfareImg || '',
			keyCode: item.keyCode || '',
			rate: item.rate || '',
			yyToken: item.yyToken || '',
			yyId: item.yyId || '',
			shopPoiId: item.shopPoiId || '',
			ksAppId: item.ksAppId || '',
			ksAppSecret: item.ksAppSecret || '',
			ksRefreshToken: item.ksRefreshToken || '',
			ksRefreshTokenTime: item.ksRefreshTokenTime || '',
			mtEmail: item.mtEmail || '',
			mtPassword: item.mtPassword || '',
			mtStoreId: item.mtStoreId || '',
			dyPoiId: item.dyPoiId || '',
			dyAppId: item.dyAppId || '',
			dySecret: item.dySecret || '',
			wifiNo: item.wifiNo || '',
			wifiPwd: item.wifiPwd || '',
			moneyScope: Number(item.moneyScope || ''),
			isReserve: Number(item.isReserve || 2),
			isOpen: Number(item.isOpen || 1),
			isQrCode: Number(item.isQrCode || 2),
			isVipPrivilege: Number(item.isVipPrivilege || 2),
			isWelfare: Number(item.isWelfare || 2),
			welfareContent: item.welfareContent || '',
			isFaceRecognition: Number(item.isFaceRecognition || 2),
			isMusic: Number(item.isMusic || 1),
			luckySort: item.luckySort || '',
			luckyCouponIds: item.luckyCouponIds || '',
			luckyContent: item.luckyContent || '',
			isRefund: Number(item.isRefund || 1),
			isWxPay: Number(item.isWxPay || 1),
			broadcastContent: item.broadcastContent || '',
			cameraId: item.cameraId || '',
			startTime: item.startTime || '',
			endTime: item.endTime || '',
			remark: item.remark || '',
			sales: item.sales || 0,
			tearoomDutyTime: item.tearoomDutyTime || '',
			tearoomTimerDoor: item.tearoomTimerDoor || '',
			status: normalizeStatus(item.status ?? item.isEnable ?? 1),
			openTime1: String(item.tearoomDutyTime || '').split('-')[0] || '',
			closeTime1: String(item.tearoomDutyTime || '').split('-')[1] || '',
			startTime2: String(item.tearoomTimerDoor || '').split('-')[0] || '',
			endTime2: String(item.tearoomTimerDoor || '').split('-')[1] || '',
			createTime: item.createTime || item.createdAt || '',
		}
	}

	function buildAddress(row) {
		return [row.tearoomProvince, row.tearoomCity, row.tearoomDistrict, row.tearoomAddress].filter(Boolean).join('') ||
			'-'
	}

	function buildQueryParams() {
		const params = {
			page: pageNum.value,
			limit: pageSize.value,
			tearoomName: filters.tearoomName.trim(),
			phone: filters.phone.trim(),
			userName: filters.userName.trim(),
			shopType: filters.shopType,
			status: filters.status,
			isVipPrivilege: filters.isVipPrivilege,
		}
		Object.keys(params).forEach((key) => {
			if (params[key] === '' || params[key] === null || typeof params[key] === 'undefined') {
				delete params[key]
			}
		})
		return params
	}

	function buildSubmitPayload() {
		const payload = {
			tearoomId: editingId.value || undefined,
			tearoomName: form.tearoomName.trim(),
			phone: form.tearoomPhone.trim(),
			tearoomPhone: form.tearoomPhone.trim(),
			userId: form.userId || undefined,
			userName: form.userName.trim(),
			shopType: Number(form.shopType || 1),
			tearoomProvince: form.tearoomProvince.trim(),
			tearoomCity: form.tearoomCity.trim(),
			tearoomDistrict: form.tearoomDistrict.trim(),
			tearoomAddress: form.tearoomAddress.trim(),
			longitude: form.tearoomLongitude.trim(),
			latitude: form.tearoomLatitude.trim(),
			tearoomLongitude: form.tearoomLongitude.trim(),
			tearoomLatitude: form.tearoomLatitude.trim(),
			tearoomCoverImage: form.tearoomCoverImage.trim(),
			tearoomTitleImage: form.tearoomTitleImage.trim(),
			qunImg: form.qunImg.trim(),
			wxCode: form.wxCode.trim(),
			siteImg: form.siteImg.trim(),
			welfareImg: form.welfareImg.trim(),
			keyCode: form.keyCode.trim(),
			rate: form.rate.trim(),
			yyToken: form.yyToken.trim(),
			yyId: form.yyId.trim(),
			shopPoiId: form.shopPoiId.trim(),
			ksAppId: form.ksAppId.trim(),
			ksAppSecret: form.ksAppSecret.trim(),
			mtEmail: form.mtEmail.trim(),
			mtPassword: form.mtPassword.trim(),
			mtStoreId: form.mtStoreId.trim(),
			dyPoiId: form.dyPoiId.trim(),
			dyAppId: form.dyAppId.trim(),
			dySecret: form.dySecret.trim(),
			wifiNo: form.wifiNo.trim(),
			wifiPwd: form.wifiPwd.trim(),
			moneyScope: Number(form.moneyScope || 0),
			isReserve: Number(form.isReserve || 2),
			isOpen: Number(form.isOpen || 1),
			isQrCode: Number(form.isQrCode || 2),
			isVipPrivilege: Number(form.isVipPrivilege || 2),
			isWelfare: Number(form.isWelfare || 2),
			welfareContent: form.welfareContent.trim(),
			isFaceRecognition: Number(form.isFaceRecognition || 2),
			isMusic: Number(form.isMusic || 1),
			luckySort: form.luckySort.trim(),
			luckyCouponIds: form.luckyCouponIds.trim(),
			luckyContent: form.luckyContent.trim(),
			isRefund: Number(form.isRefund || 1),
			isWxPay: Number(form.isWxPay || 1),
			broadcastContent: form.broadcastContent.trim(),
			cameraId: form.cameraId.trim(),
			startTime: form.startTime,
			endTime: form.endTime,
			tearoomDutyTime: form.openTime1 && form.closeTime1 ? `${form.openTime1}-${form.closeTime1}` : '',
			tearoomTimerDoor: form.startTime2 && form.endTime2 ? `${form.startTime2}-${form.endTime2}` : '',
			remark: form.remark.trim(),
			status: form.status,
		}
		Object.keys(payload).forEach((key) => {
			if (payload[key] === '' || payload[key] === null || typeof payload[key] === 'undefined') {
				delete payload[key]
			}
		})
		return payload
	}

	async function loadData() {
		loading.value = true
		try {
			const data = await getMerchantCenterPageApi(buildQueryParams())
			tableData.value = (data.list || []).map(normalizeMerchantRow)
			total.value = Number(data.total || 0)
		} finally {
			loading.value = false
		}
	}

	function handleSearch() {
		pageNum.value = 1
		loadData()
	}

	function handleReset() {
		filters.tearoomName = ''
		filters.phone = ''
		filters.userName = ''
		filters.shopType = ''
		filters.status = ''
		filters.isVipPrivilege = ''
		handleSearch()
	}

	function openCreate() {
		editingId.value = 0
		resetForm()
		syncFormLocationFields()
		dialogVisible.value = true
	}

	function openEdit(row) {
		const normalized = normalizeMerchantRow(row)
		editingId.value = normalized.tearoomId
		Object.assign(form, initialForm, normalized)
		syncFormLocationFields()
		dialogVisible.value = true
	}

	function handleDialogVisible(value) {
		dialogVisible.value = value
		if (!value) {
			editingId.value = 0
			resetForm()
		}
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

	function updateMapMarker(lat, lng) {
		const maps = window.qq?.maps
		if (!maps || !qqMapInstance) return
		const position = createQQMapLatLng(lat, lng)
		qqMapInstance.setCenter(position)
		if (!qqMapMarker) {
			qqMapMarker = new maps.Marker({
				map: qqMapInstance,
				position
			})
			return
		}
		qqMapMarker.setPosition(position)
	}

	function createQQMapLatLng(lat, lng) {
		const maps = window.qq?.maps
		if (!maps?.LatLng) {
			throw new Error('腾讯地图初始化失败')
		}
		const latitude = Number(lat)
		const longitude = Number(lng)
		try {
			return new maps.LatLng(latitude, longitude)
		} catch (error) {
			return maps.LatLng(latitude, longitude)
		}
	}

	async function initMap() {
		mapInitializing.value = true
		try {
			const maps = await ensureQQMaps()
			await nextTick()
			if (!mapContainerRef.value) return
			const latitude = Number(form.tearoomLatitude || 34.34281541842994)
			const longitude = Number(form.tearoomLongitude || 108.93970884382725)
			const center = createQQMapLatLng(latitude, longitude)
			qqMapInstance = new maps.Map(mapContainerRef.value, {
				center,
				zoom: 13,
				mapTypeId: maps.MapTypeId.ROADMAP,
			})
			qqMapMarker = new maps.Marker({
				map: qqMapInstance,
				position: center
			})
			maps.event.addListener(qqMapInstance, 'click', async (event) => {
				form.tearoomLongitude = String(event.latLng.getLng())
				form.tearoomLatitude = String(event.latLng.getLat())
				form.longitude = form.tearoomLongitude
				form.latitude = form.tearoomLatitude
				updateMapMarker(form.tearoomLatitude, form.tearoomLongitude)
				const response = await requestJsonp(
					`https://apis.map.qq.com/ws/geocoder/v1/?location=${form.tearoomLatitude},${form.tearoomLongitude}&key=${QQ_MAP_KEY}&get_poi=1`
					)
				const result = response?.result
				if (!result) return
				form.tearoomAddress = result.formatted_addresses?.recommend || form.tearoomAddress
				form.tearoomProvince = result.ad_info?.province || form.tearoomProvince
				form.tearoomCity = result.ad_info?.city || form.tearoomCity
				form.tearoomDistrict = result.ad_info?.district || form.tearoomDistrict
			})
		} catch (error) {
			showError(error.message || '地图初始化失败')
		} finally {
			mapInitializing.value = false
		}
	}

	function openMapDialog() {
		mapDialogVisible.value = true
	}

	async function locateByAddress() {
		const address = [form.tearoomProvince, form.tearoomCity, form.tearoomDistrict, form.tearoomAddress].filter(
			Boolean).join('')
		if (!address) {
			showError('请先填写省市区和详细地址')
			return
		}
		mapLoading.value = true
		try {
			const response = await requestJsonp(
				`https://apis.map.qq.com/ws/geocoder/v1/?address=${encodeURIComponent(address)}&key=${QQ_MAP_KEY}&get_poi=1`
				)
			const location = response?.result?.location
			if (!location) {
				showError('未找到对应坐标')
				return
			}
			form.tearoomLongitude = String(location.lng)
			form.tearoomLatitude = String(location.lat)
			form.longitude = form.tearoomLongitude
			form.latitude = form.tearoomLatitude
			if (mapDialogVisible.value) {
				await initMap()
				updateMapMarker(form.tearoomLatitude, form.tearoomLongitude)
			}
		} finally {
			mapLoading.value = false
		}
	}

	async function handleImageUpload(field, option) {
		imageUploadingField.value = field
		try {
			const data = await uploadMerchantImageApi(option.file)
			const url = typeof data === 'string' ? data : data?.url || data?.data || data?.src || data?.path || ''
			form[field] = String(url || '')
			option.onSuccess?.(data)
		} catch (error) {
			option.onError?.(error)
		} finally {
			imageUploadingField.value = ''
		}
	}

	async function submitDialog() {
		const valid = await validateForm(formRef)
		if (!valid) return
		syncFormLocationFields()
		if (!form.tearoomTitleImage) {
			showError('请添加羽毛球馆logo')
			return
		}
		if (!form.tearoomAddress || !form.tearoomLongitude || !form.tearoomLatitude) {
			showError('请完善羽毛球馆位置,经纬度进行选择')
			return
		}
		if (!form.startTime || !form.endTime) {
			showError('请选择营业时间')
			return
		}
		if (!form.rate) {
			showError('佣金比例不能为空')
			return
		}
		if (!form.moneyScope) {
			showError('请选择零钱操作权限')
			return
		}
		if (!form.openTime1 || !form.closeTime1) {
			showError('请选择人员值守时间')
			return
		}
		if ((form.startTime2 && !form.endTime2) || (!form.startTime2 && form.endTime2)) {
			showError('定时开关门时间需要成对填写')
			return
		}
		dialogLoading.value = true
		try {
			const payload = buildSubmitPayload()
			if (editingId.value) {
				await updateMerchantApi(payload)
				showSuccess('羽毛球馆已更新')
			} else {
				await createMerchantApi(payload)
				showSuccess('羽毛球馆已创建')
			}
			dialogVisible.value = false
			await loadData()
		} finally {
			dialogLoading.value = false
		}
	}

	async function updateMerchantStatus(row, status) {
		await confirmAction('确定执行此操作?')
		await updateMerchantApi({
			tearoomId: row.tearoomId,
			status
		})
		showSuccess('操作成功')
		await loadData()
	}

	function goMerchantDetail(row, tab = 'info') {
		router.push({
			path: `/merchants/${row.tearoomId}`,
			query: {
				tab
			}
		})
	}

	function goUserDetail(row) {
		if (!row?.userId) {
			showError('当前门店未绑定用户')
			return
		}
		router.push(`/users/${row.userId}`)
	}

	function openWaterQr(row) {
		qrDialogTitle.value = '领水二维码'
		qrImageName.value = `${row.tearoomName || '门店'}-领水二维码`
		qrImageUrl.value =
			`${getApiBaseUrl()}app/invite/mpCreateQr?invitationCode=${row.tearoomId}&page=teaching/getUserWater/getUserWater`
		qrDialogVisible.value = true
	}

	function openCourse(row) {
		router.push({
			path: `/merchants/${row.tearoomId}/curriculum`,
			query: {
				storeName: row.tearoomName
			}
		})
	}


	function openCabinetQr(row) {
		qrDialogTitle.value = '柜子二维码'
		qrImageName.value = `${row.tearoomName || '门店'}-柜子二维码`
		qrImageUrl.value =
			`${getApiBaseUrl()}invite/mpCreateQr?relation=${row.tearoomId}&page=box/boxGoodsList/boxGoodsList`
		qrDialogVisible.value = true
	}

	function downloadQr() {
		if (!qrImageUrl.value) return
		const image = new Image()
		image.setAttribute('crossOrigin', 'anonymous')
		image.src = qrImageUrl.value
		image.onload = () => {
			const canvas = document.createElement('canvas')
			canvas.width = image.width
			canvas.height = image.height
			const ctx = canvas.getContext('2d')
			ctx.drawImage(image, 0, 0, image.width, image.height)
			canvas.toBlob((blob) => {
				if (!blob) return
				const href = URL.createObjectURL(blob)
				const link = document.createElement('a')
				link.href = href
				link.download = `${qrImageName.value || qrDialogTitle.value}.png`
				link.click()
				URL.revokeObjectURL(href)
				showSuccess('下载完成')
			})
		}
	}

	function openQrWindow() {
		if (!qrImageUrl.value) return
		window.open(qrImageUrl.value, '_blank')
	}

	function openUserPicker() {
		userPickerVisible.value = true
		loadUserPickerData()
	}

	function buildUserPickerParams() {
		const params = {
			page: pickerPage.value,
			limit: pickerSize.value,
			phone: pickerFilters.phone.trim(),
			userName: pickerFilters.userName.trim(),
			invitationCode: pickerFilters.invitationCode.trim(),
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
			pickerData.value = data.list || []
			pickerTotal.value = Number(data.total || 0)
		} finally {
			userPickerLoading.value = false
		}
	}

	function handleUserPickerSearch() {
		pickerPage.value = 1
		loadUserPickerData()
	}

	function handleUserPickerReset() {
		pickerFilters.phone = ''
		pickerFilters.userName = ''
		pickerFilters.invitationCode = ''
		handleUserPickerSearch()
	}

	function selectUser(row) {
		form.userId = Number(row.userId || 0)
		form.userName = row.userName || ''
		if (!form.tearoomPhone && row.phone) {
			form.tearoomPhone = row.phone
			form.phone = row.phone
		}
		userPickerVisible.value = false
	}

	function clearBindUser() {
		form.userId = 0
		form.userName = ''
	}

	function resetRoomForm() {
		roomEditingId.value = 0
		roomForm.roomType = 1
		roomForm.roomName = ''
		roomForm.roomNo = ''
		roomForm.deviceId = ''
		roomForm.serveCode = ''
		roomForm.isOpen = 2
	}

	async function loadRoomData() {
		if (!roomMerchantId.value) return
		roomLoading.value = true
		try {
			const data = await getMerchantRoomPageApi({
				page: roomPage.value,
				limit: roomSize.value,
				roomName: '',
				tearoomId: roomMerchantId.value,
			})
			roomList.value = data.list || []
			roomTotal.value = Number(data.total || 0)
		} finally {
			roomLoading.value = false
		}
	}

	function openRoomDialog(row) {
		roomMerchantId.value = Number(row.tearoomId || 0)
		roomMerchantName.value = row.tearoomName || ''
		roomPage.value = 1
		roomDialogVisible.value = true
		loadRoomData()
	}

	function openCreateRoom() {
		resetRoomForm()
		roomFormVisible.value = true
	}

	function openEditRoom(row) {
		roomEditingId.value = Number(row.roomId || 0)
		roomForm.roomType = Number(row.roomType || 1)
		roomForm.roomName = row.roomName || ''
		roomForm.roomNo = row.roomNo || ''
		roomForm.deviceId = row.deviceId || ''
		roomForm.serveCode = row.serveCode || ''
		roomForm.isOpen = Number(row.isOpen || 2)
		roomFormVisible.value = true
	}

	async function submitRoom() {
		if (Number(roomForm.roomType) === 8) {
			if (!roomForm.deviceId.trim()) {
				showError('请输入设备ID')
				return
			}
			if (!roomForm.serveCode.trim()) {
				showError('请输入服务编号')
				return
			}
		} else {
			if (!roomForm.roomName.trim()) {
				showError('请输入设备名称')
				return
			}
			if (!roomForm.roomNo.trim()) {
				showError('请输入设备编号')
				return
			}
		}
		roomFormLoading.value = true
		try {
			const payload = {
				tearoomId: roomMerchantId.value,
				roomId: roomEditingId.value || undefined,
				roomType: Number(roomForm.roomType || 1),
				roomName: roomForm.roomName.trim(),
				roomNo: roomForm.roomNo.trim(),
				isOpen: Number(roomForm.isOpen || 2),
				deviceId: roomForm.deviceId.trim(),
				serveCode: roomForm.serveCode.trim(),
			}
			if (roomEditingId.value) {
				await updateMerchantRoomApi(payload)
				showSuccess('操作成功')
			} else {
				await createMerchantRoomApi(payload)
				showSuccess('操作成功')
			}
			roomFormVisible.value = false
			await loadRoomData()
		} finally {
			roomFormLoading.value = false
		}
	}

	async function toggleRoomStatus(row) {
		await updateMerchantRoomApi({
			tearoomId: row.tearoomId,
			goodsId: row.goodsId,
			roomId: row.roomId,
			roomType: row.roomType,
			roomName: row.roomName,
			roomNo: row.roomNo,
			isOpen: row.isOpen,
			deviceId: row.deviceId,
			serveCode: row.serveCode,
		})
		showSuccess('操作成功')
		await loadRoomData()
	}

	async function deleteRoom(row) {
		await confirmAction('确定删除此条信息?')
		await deleteMerchantRoomApi(row.roomId)
		showSuccess('删除成功')
		await loadRoomData()
	}

	function openKsDialog(row) {
		ksTearoomId.value = Number(row.tearoomId || 0)
		ksCode.value = ''
		ksDialogVisible.value = true
	}

	async function submitKsCode() {
		if (!ksCode.value.trim()) {
			showError('请输入CODE')
			return
		}
		ksDialogLoading.value = true
		try {
			const result = await getMerchantKsRefreshTokenApi({
				tearoomId: ksTearoomId.value,
				code: ksCode.value.trim(),
			})
			if (Number(result?.result) === 1 || Number(result?.code) === 0) {
				ksDialogVisible.value = false
				showSuccess('操作成功')
				await loadData()
				return
			}
			showError(result?.error_msg || result?.msg || '操作失败')
		} finally {
			ksDialogLoading.value = false
		}
	}

	function canEdit() {
		return isAuth('merchantList:add') || isAuth('merchantList:update') || isAuth('merchantList:edit')
	}

	function canToggle() {
		return isAuth('merchantList:update') || isAuth('merchantList:status') || isAuth('merchantList:edit')
	}

	onMounted(() => {
		loadData()
	})
</script>

<template>
	<div class="mc">
		<header class="mc-hero nf-page-hero">
			<span class="mc-hero__badge">羽毛球馆</span>
			<h1 class="mc-hero__title">羽毛球馆中心</h1>
			<p class="mc-hero__desc">管理羽毛球馆门店信息、设备绑定与营业状态，支持多条件筛选与增删改操作</p>
		</header>

		<div class="mc-filter nf-filter-card">
			<div class="nf-filter-card__head">
				<span>筛选条件</span>
			</div>
			<el-form :inline="true" class="mc-filter__form" @submit.prevent>
			<el-form-item label="店铺类型">
				<el-select v-model="filters.shopType" clearable placeholder="请选择" @change="handleSearch">
					<el-option v-for="item in shopTypeOptions" :key="item.value" :label="item.label"
						:value="item.value" />
				</el-select>
			</el-form-item>
			<el-form-item label="羽毛球馆名称">
				<el-input v-model="filters.tearoomName" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="handleSearch" />
			</el-form-item>
			<el-form-item label="羽毛球馆电话">
				<el-input v-model="filters.phone" clearable placeholder="请输入羽毛球馆电话" @keyup.enter="handleSearch" />
			</el-form-item>
			<el-form-item label="用户昵称">
				<el-input v-model="filters.userName" clearable placeholder="请输入用户昵称" @keyup.enter="handleSearch" />
			</el-form-item>
			<el-form-item label="是否享受会员权益">
				<el-select v-model="filters.isVipPrivilege" clearable placeholder="请选择" @change="handleSearch">
					<el-option v-for="item in isVipPrivilegeOptions" :key="item.value" :label="item.label"
						:value="item.value" />
				</el-select>
			</el-form-item>
			<el-form-item class="mc-filter__actions">
				<el-space wrap>
					<el-button type="primary" @click="handleSearch">查询</el-button>
					<el-button @click="handleReset">重置</el-button>
					<el-button v-if="canEdit()" type="primary" @click="openCreate">添加</el-button>
				</el-space>
			</el-form-item>
		</el-form>
		</div>

		<div class="mc-table nf-table-card">
		<el-table v-loading="loading" :data="tableData" border>
			<el-table-column prop="tearoomId" label="编号" width="80" fixed="left" />
			<el-table-column label="店铺类型" width="90">
				<template #default="{ row }">
					<span
						:class="Number(row.shopType) === 1 ? 'danger-text' : 'primary-text'">{{ shopTypeText(row.shopType) }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="tearoomName" label="羽毛球馆名称" width="120" fixed="left" />
			<el-table-column prop="tearoomPhone" label="羽毛球馆电话" width="140" />
			<el-table-column label="羽毛球馆logo" width="120">
				<template #default="{ row }">
					<span v-if="!row.tearoomTitleImage">暂无图片</span>
					<el-image v-else :src="row.tearoomTitleImage" :preview-src-list="splitImages(row.tearoomTitleImage)"
						fit="cover" class="thumb-image" preview-teleported />
				</template>
			</el-table-column>
			<el-table-column label="用户昵称" width="120">
				<template #default="{ row }">
					<span class="link-text" @click="goUserDetail(row)">{{ row.userName || '未绑定' }}</span>
				</template>
			</el-table-column>
			<el-table-column label="羽毛球馆地址" width="260">
				<template #default="{ row }">
					<div>{{ buildAddress(row) }}</div>
					<div>经度：{{ row.tearoomLongitude || '未绑定' }}</div>
					<div>纬度：{{ row.tearoomLatitude || '未绑定' }}</div>
				</template>
			</el-table-column>
			<el-table-column prop="rate" label="佣金比例" width="100" />
			<el-table-column label="微信二维码" width="160">
				<template #default="{ row }">
					<div v-if="splitImages(row.wxCode).length" class="image-list-cell">
						<el-image v-for="item in splitImages(row.wxCode)" :key="item" :src="item"
							:preview-src-list="splitImages(row.wxCode)" fit="cover" class="thumb-image mini-thumb"
							preview-teleported />
					</div>
				</template>
			</el-table-column>
			<el-table-column label="群二维码" width="200">
				<template #default="{ row }">
					<div v-if="splitImages(row.qunImg).length" class="image-list-cell">
						<el-image v-for="item in splitImages(row.qunImg)" :key="item" :src="item"
							:preview-src-list="splitImages(row.qunImg)" fit="cover" class="thumb-image mini-thumb"
							preview-teleported />
					</div>
				</template>
			</el-table-column>
			<el-table-column label="场地图" width="200">
				<template #default="{ row }">
					<div v-if="splitImages(row.siteImg).length" class="image-list-cell">
						<el-image v-for="item in splitImages(row.siteImg)" :key="item" :src="item"
							:preview-src-list="splitImages(row.siteImg)" fit="cover" class="thumb-image mini-thumb"
							preview-teleported />
					</div>
				</template>
			</el-table-column>
			<el-table-column prop="shopPoiId" label="快手门店ID" width="200" />
			<el-table-column prop="ksAppId" label="快手APPID" width="200" />
			<el-table-column prop="ksAppSecret" label="快手AppSecret" width="200" />
			<el-table-column label="快手核销优惠券token" width="300">
				<template #default="{ row }">
					<el-popover placement="top-start" trigger="hover" width="320">
						<template #reference>
							<div class="ellipsis-text">{{ row.ksRefreshToken }}</div>
						</template>
						<div class="break-text">{{ row.ksRefreshToken }}</div>
					</el-popover>
					<el-button size="small" type="primary" plain :disabled="!canEdit()"
						@click="openKsDialog(row)">获取</el-button>
				</template>
			</el-table-column>
			<el-table-column prop="ksRefreshTokenTime" label="快手核销优惠券token 失效时间" width="180" />
			<el-table-column prop="mtEmail" label="美团Email" width="200" />
			<el-table-column prop="mtPassword" label="美团密码" width="200" />
			<el-table-column prop="mtStoreId" label="美团门店ID" width="200" />
			<el-table-column prop="dyPoiId" label="抖音门店ID" width="200" />
			<el-table-column prop="dyAppId" label="抖音AppID" width="200" />
			<el-table-column prop="dySecret" label="抖音Secret" width="200" />
			<el-table-column prop="luckySort" label="幸运用户序号" width="200" />
			<el-table-column prop="luckyCouponIds" label="幸运用户奖励优惠券" width="200" />
			<el-table-column prop="luckyContent" label="幸运用户播报内容" width="200" />
			<el-table-column label="是否允许退款" width="100">
				<template #default="{ row }">{{ yesNoText(row.isRefund) }}</template>
			</el-table-column>
			<el-table-column label="是否开启微信支付" width="120">
				<template #default="{ row }">{{ yesNoText(row.isWxPay) }}</template>
			</el-table-column>
			<el-table-column label="扫码器是否自动开台" width="140">
				<template #default="{ row }">{{ yesNoText(row.isQrCode) }}</template>
			</el-table-column>
			<el-table-column prop="wifiNo" label="wifi账号" width="160" />
			<el-table-column prop="wifiPwd" label="wifi密码" width="160" />
			<el-table-column prop="yyToken" label="语音播报token" width="180" />
			<el-table-column prop="yyId" label="语音播报ID" width="140" />
			<el-table-column label="播报内容" width="200">
				<template #default="{ row }">
					<el-popover placement="top-start" trigger="hover" width="320">
						<template #reference>
							<div class="ellipsis-text">{{ row.broadcastContent }}</div>
						</template>
						<div class="break-text">{{ row.broadcastContent }}</div>
					</el-popover>
				</template>
			</el-table-column>
			<el-table-column prop="cameraId" label="摄像头配置" width="160" />
			<el-table-column label="营业时间" width="120">
				<template #default="{ row }">{{ row.startTime }}--{{ row.endTime }}</template>
			</el-table-column>
			<el-table-column prop="tearoomDutyTime" label="人员值守时间" width="140" />
			<el-table-column prop="tearoomTimerDoor" label="定时开门开关灯时间" width="170" />
			<el-table-column label="销量" width="80">
				<template #default="{ row }">{{ row.sales || '0' }}</template>
			</el-table-column>
			<el-table-column label="是否开启人脸识别" width="130">
				<template #default="{ row }">{{ yesNoText(row.isFaceRecognition) }}</template>
			</el-table-column>
			<el-table-column label="门店是否支持点歌" width="120">
				<template #default="{ row }">{{ yesNoText(row.isMusic) }}</template>
			</el-table-column>
			<el-table-column label="是否开启预定" width="110">
				<template #default="{ row }">{{ yesNoText(row.isReserve) }}</template>
			</el-table-column>
			<el-table-column label="是否开启爱心活动" width="120">
				<template #default="{ row }">{{ yesNoText(row.isWelfare) }}</template>
			</el-table-column>
			<el-table-column label="爱心活动介绍" width="300">
				<template #default="{ row }">
					<el-popover placement="top-start" trigger="hover" width="320">
						<template #reference>
							<div class="ellipsis-text">{{ row.welfareContent }}</div>
						</template>
						<div class="break-text">{{ row.welfareContent }}</div>
					</el-popover>
				</template>
			</el-table-column>
			<el-table-column label="爱心活动背景图" width="200">
				<template #default="{ row }">
					<div v-if="splitImages(row.welfareImg).length" class="image-list-cell">
						<el-image v-for="item in splitImages(row.welfareImg)" :key="item" :src="item"
							:preview-src-list="splitImages(row.welfareImg)" fit="cover" class="thumb-image mini-thumb"
							preview-teleported />
					</div>
				</template>
			</el-table-column>
			<el-table-column label="零钱操作权限" width="110" fixed="right">
				<template #default="{ row }">{{ moneyScopeText(row.moneyScope) }}</template>
			</el-table-column>
			<el-table-column label="是否享受会员权益" width="120">
				<template #default="{ row }">
					<span
						:class="Number(row.isVipPrivilege) === 1 ? 'danger-text' : 'primary-text'">{{ yesNoText(row.isVipPrivilege) }}</span>
				</template>
			</el-table-column>
			<el-table-column label="领水二维码" width="100" fixed="right">
				<template #default="{ row }">
					<el-button size="small" type="primary" @click="openWaterQr(row)">查看</el-button>
				</template>
			</el-table-column>
			<el-table-column label="柜子二维码" width="100">
				<template #default="{ row }">
					<el-button size="small" type="primary" @click="openCabinetQr(row)">查看</el-button>
				</template>
			</el-table-column>
			<el-table-column label="状态" width="80" fixed="right">
				<template #default="{ row }">
					<span
						:class="Number(row.status) === 1 ? 'success-text' : Number(row.status) === 2 ? 'danger-text' : 'primary-text'">{{ statusText(row.status) }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="createTime" label="创建时间" width="160" />
			<el-table-column label="操作" width="280" fixed="right">
				<template #default="{ row }">
					<el-space wrap>
						<el-button size="small" type="primary" @click="goMerchantDetail(row)">详情</el-button>
						<el-button size="small" type="warning" @click="openRoomDialog(row)">设备管理</el-button>
						<el-button v-if="Number(row.status) === 1" size="small" type="primary" plain
							:disabled="!canToggle()" @click="updateMerchantStatus(row, 2)">打烊</el-button>
						<el-button v-if="Number(row.status) === 1" size="small" type="danger" :disabled="!canToggle()"
							@click="updateMerchantStatus(row, 3)">今日已满</el-button>
						<el-button v-if="Number(row.status) === 2 || Number(row.status) === 3" size="small"
							type="danger" :disabled="!canToggle()"
							@click="updateMerchantStatus(row, 1)">正常营业</el-button>
						<el-button size="small" type="warning" plain @click="openCourse(row)">课程</el-button>
					</el-space>
				</template>
			</el-table-column>
		</el-table>

		<BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 30, 50, 100]" :total="total"
			@update:currentPage="pageNum = $event; loadData()"
			@update:pageSize="pageSize = $event; pageNum = 1; loadData()" />
		</div>

		<AppDialog v-model="dialogVisible" :title="dialogTitle" width="1200px" :loading="dialogLoading"
			@confirm="submitDialog">
			<el-form ref="formRef" :model="form" :rules="rules" label-width="150px" class="merchant-form"
				@submit.prevent>
				<el-row :gutter="16">
					<el-col :span="24">
						<div class="section-title">图片信息</div>
					</el-col>
					<el-col :span="12">
						<el-form-item label="羽毛球馆logo">
							<div class="upload-box-row">
								<img v-if="form.tearoomTitleImage" :src="form.tearoomTitleImage"
									class="thumb-image large-thumb" />
								<el-empty v-else description="暂无图片" :image-size="64" />
								<el-upload :show-file-list="false"
									:http-request="(option) => handleImageUpload('tearoomTitleImage', option)">
									<el-button :loading="imageUploadingField === 'tearoomTitleImage'">上传</el-button>
								</el-upload>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="群二维码">
							<div class="upload-box-row">
								<img v-if="form.qunImg" :src="form.qunImg" class="thumb-image large-thumb" />
								<el-empty v-else description="暂无图片" :image-size="64" />
								<el-upload :show-file-list="false"
									:http-request="(option) => handleImageUpload('qunImg', option)">
									<el-button :loading="imageUploadingField === 'qunImg'">上传</el-button>
								</el-upload>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="微信二维码">
							<div class="upload-box-row">
								<img v-if="form.wxCode" :src="form.wxCode" class="thumb-image large-thumb" />
								<el-empty v-else description="暂无图片" :image-size="64" />
								<el-upload :show-file-list="false"
									:http-request="(option) => handleImageUpload('wxCode', option)">
									<el-button :loading="imageUploadingField === 'wxCode'">上传</el-button>
								</el-upload>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="场地图">
							<div class="upload-box-row">
								<img v-if="form.siteImg" :src="form.siteImg" class="thumb-image large-thumb" />
								<el-empty v-else description="暂无图片" :image-size="64" />
								<el-upload :show-file-list="false"
									:http-request="(option) => handleImageUpload('siteImg', option)">
									<el-button :loading="imageUploadingField === 'siteImg'">上传</el-button>
								</el-upload>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<div class="section-title">基础信息</div>
					</el-col>
					<el-col :span="12">
						<el-form-item label="羽毛球馆名称" prop="tearoomName">
							<el-input v-model="form.tearoomName" maxlength="50" placeholder="请输入羽毛球馆名称" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="联系电话" prop="tearoomPhone">
							<el-input v-model="form.tearoomPhone" maxlength="11" placeholder="请输入联系电话" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="绑定用户">
							<el-input :model-value="form.userName" readonly placeholder="请选择绑定用户">
								<template #append>
									<el-space spacer="|">
										<el-button @click="openUserPicker">选择</el-button>
										<el-button @click="clearBindUser">清空</el-button>
									</el-space>
								</template>
							</el-input>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="店铺类型">
							<el-radio-group v-model="form.shopType">
								<el-radio :value="1">自营</el-radio>
								<el-radio :value="2">三方</el-radio>
							</el-radio-group>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="状态">
							<el-radio-group v-model="form.status">
								<el-radio :value="1">正常</el-radio>
								<el-radio :value="2">打烊</el-radio>
								<el-radio :value="3">今日已满</el-radio>
							</el-radio-group>
						</el-form-item>
					</el-col>
					<el-col :span="12"><el-form-item label="佣金比例" prop="rate"><el-input
								v-model="form.rate" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="设备标识码"><el-input
								v-model="form.keyCode" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="快手门店ID"><el-input
								v-model="form.shopPoiId" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="快手APPID"><el-input
								v-model="form.ksAppId" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="快手AppSecret"><el-input
								v-model="form.ksAppSecret" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="美团Email"><el-input
								v-model="form.mtEmail" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="美团密码"><el-input
								v-model="form.mtPassword" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="美团门店ID"><el-input
								v-model="form.mtStoreId" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="抖音门店ID"><el-input
								v-model="form.dyPoiId" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="抖音AppID"><el-input
								v-model="form.dyAppId" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="抖音Secret"><el-input
								v-model="form.dySecret" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="wifi账号"><el-input
								v-model="form.wifiNo" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="wifi密码"><el-input
								v-model="form.wifiPwd" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="语音播报token"><el-input
								v-model="form.yyToken" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="语音播报ID"><el-input
								v-model="form.yyId" /></el-form-item></el-col>
					<el-col :span="24"><el-form-item label="播报内容"><el-input v-model="form.broadcastContent"
								type="textarea" :rows="2" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="摄像头配置"><el-input
								v-model="form.cameraId" /></el-form-item></el-col>
					<el-col :span="24">
						<div class="section-title">位置信息</div>
					</el-col>
					<el-col :span="8">
						<el-form-item label="省份">
							<el-input v-model="form.tearoomProvince" maxlength="20" placeholder="请输入省份" />
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="城市">
							<el-input v-model="form.tearoomCity" maxlength="20" placeholder="请输入城市" />
						</el-form-item>
					</el-col>
					<el-col :span="8">
						<el-form-item label="区县">
							<el-input v-model="form.tearoomDistrict" maxlength="20" placeholder="请输入区县" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="详细地址">
							<div class="address-row">
								<el-input v-model="form.tearoomAddress" maxlength="120" placeholder="请输入详细地址" />
								<el-button :loading="mapLoading" @click="locateByAddress">获取经纬度</el-button>
								<el-button type="primary" plain @click="openMapDialog">地图选点</el-button>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="经度">
							<el-input v-model="form.tearoomLongitude" placeholder="请输入经度" />
						</el-form-item>
					</el-col>
					<el-col :span="12">
						<el-form-item label="纬度">
							<el-input v-model="form.tearoomLatitude" placeholder="请输入纬度" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<div class="section-title">营业与功能</div>
					</el-col>
					<el-col :span="12"><el-form-item label="开店时间"><el-time-select v-model="form.startTime"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="闭店时间"><el-time-select v-model="form.endTime"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="零钱操作权限"><el-radio-group v-model="form.moneyScope"><el-radio
									v-for="item in moneyScopeOptions" :key="item.value"
									:value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="是否开启预定"><el-radio-group v-model="form.isReserve"><el-radio
									v-for="item in yesNoOptions" :key="item.value"
									:value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="是否线下开台"><el-radio-group v-model="form.isOpen"><el-radio
									:value="1">是</el-radio><el-radio
									:value="2">否</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="扫码器自动开台"><el-radio-group v-model="form.isQrCode"><el-radio
									:value="1">是</el-radio><el-radio
									:value="2">否</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="会员权益"><el-radio-group
								v-model="form.isVipPrivilege"><el-radio v-for="item in yesNoOptions" :key="item.value"
									:value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="爱心活动"><el-radio-group v-model="form.isWelfare"><el-radio
									v-for="item in yesNoOptions" :key="item.value"
									:value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="爱心活动介绍"><el-input
								v-model="form.welfareContent" /></el-form-item></el-col>
					<el-col :span="12">
						<el-form-item label="爱心背景图">
							<div class="upload-box-row">
								<img v-if="form.welfareImg" :src="form.welfareImg" class="thumb-image large-thumb" />
								<el-empty v-else description="暂无图片" :image-size="64" />
								<el-upload :show-file-list="false"
									:http-request="(option) => handleImageUpload('welfareImg', option)">
									<el-button :loading="imageUploadingField === 'welfareImg'">上传</el-button>
								</el-upload>
							</div>
						</el-form-item>
					</el-col>
					<el-col :span="12"><el-form-item label="人脸识别"><el-radio-group
								v-model="form.isFaceRecognition"><el-radio v-for="item in yesNoOptions"
									:key="item.value"
									:value="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="支持点歌"><el-radio-group v-model="form.isMusic"><el-radio
									:value="1">是</el-radio><el-radio
									:value="2">否</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="允许退款"><el-radio-group v-model="form.isRefund"><el-radio
									:value="1">是</el-radio><el-radio
									:value="2">否</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="微信支付"><el-radio-group v-model="form.isWxPay"><el-radio
									:value="1">是</el-radio><el-radio
									:value="2">否</el-radio></el-radio-group></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="值守开始"><el-time-select v-model="form.openTime1"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="值守结束"><el-time-select v-model="form.closeTime1"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="开门开灯时间"><el-time-select v-model="form.startTime2"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="关门关灯时间"><el-time-select v-model="form.endTime2"
								:picker-options="{ start: '00:00', step: '00:15', end: '23:59' }" /></el-form-item></el-col>
					<el-col :span="24">
						<div class="section-title">扩展信息</div>
					</el-col>
					<el-col :span="12"><el-form-item label="幸运用户序号"><el-input
								v-model="form.luckySort" /></el-form-item></el-col>
					<el-col :span="12"><el-form-item label="幸运用户奖励优惠券"><el-input
								v-model="form.luckyCouponIds" /></el-form-item></el-col>
					<el-col :span="24"><el-form-item label="幸运用户播报内容"><el-input
								v-model="form.luckyContent" /></el-form-item></el-col>
					<el-col :span="24">
						<el-form-item label="封面图">
							<el-input v-model="form.tearoomCoverImage" placeholder="请输入封面图地址，多个用英文逗号分隔" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="标题图">
							<el-input v-model="form.tearoomTitleImage" placeholder="请输入标题图地址，多个用英文逗号分隔" />
						</el-form-item>
					</el-col>
					<el-col :span="24">
						<el-form-item label="备注">
							<el-input v-model="form.remark" type="textarea" :rows="3" maxlength="200" show-word-limit
								placeholder="请输入备注" />
						</el-form-item>
					</el-col>
				</el-row>
			</el-form>
		</AppDialog>

		<el-dialog v-model="mapDialogVisible" title="地图选点" width="70%" destroy-on-close @opened="initMap" class="nf-pro-dialog" align-center>
<div v-loading="mapInitializing" class="map-dialog-layout">
				<div ref="mapContainerRef" class="map-container" />
			</div>
		</el-dialog>

		<el-dialog v-model="userPickerVisible" title="选择绑定用户" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid search-grid--compact" @submit.prevent>
				<el-form-item label="手机号">
					<el-input v-model="pickerFilters.phone" clearable placeholder="请输入手机号"
						@keyup.enter="handleUserPickerSearch" />
				</el-form-item>
				<el-form-item label="昵称">
					<el-input v-model="pickerFilters.userName" clearable placeholder="请输入昵称"
						@keyup.enter="handleUserPickerSearch" />
				</el-form-item>
				<el-form-item label="邀请码">
					<el-input v-model="pickerFilters.invitationCode" clearable placeholder="请输入邀请码"
						@keyup.enter="handleUserPickerSearch" />
				</el-form-item>
				<el-form-item>
					<el-space>
						<el-button type="primary" @click="handleUserPickerSearch">查询</el-button>
						<el-button @click="handleUserPickerReset">重置</el-button>
					</el-space>
				</el-form-item>
			</el-form>

			<el-table v-loading="userPickerLoading" :data="pickerData" border>
				<el-table-column prop="userId" label="编号" width="90" />
				<el-table-column prop="userName" label="昵称" min-width="180" />
				<el-table-column prop="phone" label="手机号" min-width="150" />
				<el-table-column prop="invitationCode" label="邀请码" min-width="120" />
				<el-table-column prop="createTime" label="创建时间" min-width="180" />
				<el-table-column label="操作" width="100" fixed="right">
					<template #default="{ row }">
						<el-button link type="primary" @click="selectUser(row)">选择</el-button>
					</template>
				</el-table-column>
			</el-table>

			<BasePagination :current-page="pickerPage" :page-size="pickerSize" :page-sizes="[10, 20, 30, 40]"
				:total="pickerTotal" @update:currentPage="pickerPage = $event; loadUserPickerData()"
				@update:pageSize="pickerSize = $event; pickerPage = 1; loadUserPickerData()" />
		</el-dialog>

		<el-dialog v-model="roomDialogVisible" :title="`${roomMerchantName || '门店'}设备列表`" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<div class="room-dialog-toolbar">
				<el-button v-if="canEdit()" size="small" type="primary" @click="openCreateRoom">添加</el-button>
			</div>
			<el-table v-loading="roomLoading" :data="roomList" border>
				<el-table-column prop="roomId" label="编号" width="80" />
				<el-table-column label="设备类型" min-width="100">
					<template #default="{ row }">{{ roomTypeText(row.roomType) }}</template>
				</el-table-column>
				<el-table-column prop="roomName" label="设备名称" min-width="140" />
				<el-table-column prop="roomNo" label="设备编号" min-width="140" />
				<el-table-column prop="deviceId" label="设备ID" min-width="140" />
				<el-table-column prop="serveCode" label="服务编号" min-width="140" />
				<el-table-column label="是否开启" width="100">
					<template #default="{ row }">
						<el-switch v-model="row.isOpen" :active-value="1" :inactive-value="2" :disabled="!canEdit()"
							@change="toggleRoomStatus(row)" />
					</template>
				</el-table-column>
				<el-table-column label="操作" width="180">
					<template #default="{ row }">
						<el-space>
							<el-button size="small" type="primary" :disabled="!canEdit()"
								@click="openEditRoom(row)">修改</el-button>
							<el-button size="small" type="danger" :disabled="!canEdit()"
								@click="deleteRoom(row)">删除</el-button>
						</el-space>
					</template>
				</el-table-column>
			</el-table>
			<BasePagination :current-page="roomPage" :page-size="roomSize" :page-sizes="[10, 20, 30, 40]"
				:total="roomTotal" @update:currentPage="roomPage = $event; loadRoomData()"
				@update:pageSize="roomSize = $event; roomPage = 1; loadRoomData()" />
		</el-dialog>

		<AppDialog v-model="roomFormVisible" :title="roomDialogTitle" width="680px" :loading="roomFormLoading"
			@confirm="submitRoom">
			<el-form label-width="120px" @submit.prevent>
				<el-form-item label="设备类型">
					<el-radio-group v-model="roomForm.roomType">
						<el-radio :value="1">设备</el-radio>
						<el-radio :value="8">温度设备</el-radio>
					</el-radio-group>
				</el-form-item>
				<el-form-item v-if="Number(roomForm.roomType) !== 8" label="设备名称">
					<el-input v-model="roomForm.roomName" placeholder="请输入设备名称" />
				</el-form-item>
				<el-form-item v-if="Number(roomForm.roomType) !== 8" label="设备编号">
					<el-input v-model="roomForm.roomNo" placeholder="请输入设备编号" />
				</el-form-item>
				<el-form-item v-if="Number(roomForm.roomType) === 8" label="设备ID">
					<el-input v-model="roomForm.deviceId" placeholder="请输入设备ID" />
				</el-form-item>
				<el-form-item v-if="Number(roomForm.roomType) === 8" label="服务编号">
					<el-input v-model="roomForm.serveCode" placeholder="请输入服务编号" />
				</el-form-item>
				<el-form-item label="是否开启">
					<el-radio-group v-model="roomForm.isOpen">
						<el-radio :value="1">是</el-radio>
						<el-radio :value="2">否</el-radio>
					</el-radio-group>
				</el-form-item>
			</el-form>
		</AppDialog>

		<AppDialog v-model="ksDialogVisible" title="获取快手核销优惠券token" width="560px" :loading="ksDialogLoading"
			@confirm="submitKsCode">
			<el-form label-width="100px" @submit.prevent>
				<el-form-item label="CODE">
					<el-input v-model="ksCode" placeholder="请输入CODE" />
				</el-form-item>
			</el-form>
		</AppDialog>

		<el-dialog v-model="qrDialogVisible" :title="qrDialogTitle" width="420px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="qr-dialog-body">
				<img :src="qrImageUrl" alt="领水二维码" class="qr-image" />
				<el-space>
					<el-button @click="openQrWindow">打开窗口查看</el-button>
					<el-button type="primary" @click="downloadQr">下载</el-button>
				</el-space>
			</div>
		</el-dialog>
	</div>
</template>

<style scoped lang="scss">
	.mc {
		display: flex;
		flex-direction: column;
		gap: 12px;
		padding: 0 0 8px;
		overflow-x: hidden;
	}

	.mc-hero.nf-page-hero {
		margin-bottom: 0;
		display: flex;
		flex-direction: column;
		gap: 6px;
	}

	.mc-hero__badge {
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

	.mc-hero__title {
		margin: 0;
		font-size: 22px;
		font-weight: 700;
		color: var(--nf-text-primary);
		line-height: 1.3;
	}

	.mc-hero__desc {
		margin: 0;
		font-size: 13px;
		color: var(--nf-text-secondary);
		line-height: 1.5;
	}

	.mc-filter.nf-filter-card {
		margin-bottom: 0;
	}

	.mc-filter__form {
		margin-bottom: 0;

  :deep(.el-select),
		:deep(.el-input) {
			width: 180px;
		}
	}

	.mc-filter__actions {
		:deep(.el-form-item__content) {
			flex-wrap: wrap;
		}
	}

	.mc-table.nf-table-card {
		margin-bottom: 0;
		padding-bottom: 12px;

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

	.search-grid {
		margin-bottom: 16px;
	}

	.merchant-form :deep(.el-form-item) {
		align-items: flex-start;
	}

	.merchant-form :deep(.el-form-item__label) {
		line-height: 20px;
		white-space: normal;
		word-break: break-word;
	}

	.merchant-form :deep(.el-radio-group) {
		display: flex;
		flex-wrap: wrap;
		gap: 8px 16px;
	}

	.section-title {
		margin-bottom: 8px;
		padding-left: 10px;
		border-left: 4px solid var(--nf-brand);
		font-weight: 600;
	}

	.thumb-image {
		width: 56px;
		height: 56px;
		border-radius: 10px;
	}

	.mini-thumb {
		width: 50px;
		height: 50px;
		border-radius: 6px;
	}

	.large-thumb {
		width: 96px;
		height: 96px;
	}

	.upload-box-row {
		display: flex;
		align-items: center;
		gap: 12px;
		min-height: 110px;
	}

	.address-row {
		display: flex;
		width: 100%;
		gap: 8px;
	}

	.link-text {
		color: var(--nf-brand-deep);
		cursor: pointer;
	}

	.image-list-cell {
		display: flex;
		flex-wrap: wrap;
		gap: 6px;
	}

	.ellipsis-text {
		display: -webkit-box;
		overflow: hidden;
		max-height: 80px;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
	}

	.break-text {
		width: 300px;
		word-break: break-all;
		overflow-wrap: break-word;
	}

	.room-dialog-toolbar {
		display: flex;
		justify-content: flex-end;
		margin-bottom: 10px;
	}

	.danger-text {
		color: #f56c6c;
	}

	.primary-text {
		color: var(--nf-brand-deep);
	}

	.success-text {
		color: #13ce66;
	}

	.status-cell {
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.qr-dialog-body {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16px;
	}

	.qr-image {
		width: 220px;
		height: 220px;
		object-fit: contain;
	}

	.map-dialog-layout {
		min-height: 480px;
	}

	.map-container {
		width: 100%;
		height: 480px;
		border: 1px solid var(--el-border-color-light);
		border-radius: 12px;
	}

	@media (max-width: 900px) {
		.mc-filter__form {
			:deep(.el-select),
			:deep(.el-input) {
				width: 100%;
			}
		}

		.address-row,
		.upload-box-row {
			flex-wrap: wrap;
		}
	}
</style>
