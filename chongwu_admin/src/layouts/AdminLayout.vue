<script setup>
	import {
		computed,
		nextTick,
		onBeforeUnmount,
		onMounted,
		ref,
		watch
	} from 'vue'
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ArrowDown,
		Bell,
		CollectionTag,
		DataBoard,
		Document,
		Edit,
		Expand,
		Files,
		Fold,
		Goods,
		Grid,
		Guide,
		Link,
		List,
		MagicStick,
		Memo,
		Message,
		Money,
		Odometer,
		Picture,
		Present,
		Rank,
		Setting,
		ShoppingBag,
		Star,
		Ticket,
		Tickets,
		TrendCharts,
		Management,
		User,
		UserFilled,
		Warning,
		Location,
		Shop,
		Tools
	} from '@element-plus/icons-vue'
	import IconSvg from '@/components/icon-svg.vue'
	import brandLogo from '@/assets/brand-logo.png'
	import {
		confirmAction
	} from '@/utils/modal'
	import {
		useAppStore
	} from '@/stores/app'
	import {
		useAuthStore
	} from '@/stores/auth'

	// 数据库 sys_menu.icon 存储的是逻辑名称，这里统一映射到 Element Plus 图标组件。
	// 未命中的名称再回退到 IconSvg 自定义 SVG 图标。
	const iconMap = {
		dashboard: Odometer,
		user: User,
		peoples: UserFilled,
		picture: Picture,
		link: Link,
		location: Location,
		shop: Shop,
		documentation: Document,
		money: Money,
		message: Message,
		warning: Warning,
		edit: Edit,
		shopping: ShoppingBag,
		star: Star,
		form: Memo,
		chart: TrendCharts,
		skill: MagicStick,
		rank: Rank,
		guide: Guide,
		log: Document,
		list: List,
		goods: Goods,
		order: Tickets,
		system: Tools,
		'tree-table': Management,
		ticket: Ticket,
		present: Present,
		bell: Bell,
		'collection-tag': CollectionTag,
		category: Grid,
		// 兼容 routes.js meta 中 PascalCase 写法
		DataBoard,
		User,
		Setting,
		Files,
		Picture,
	}
	const route = useRoute()
	const router = useRouter()
	const appStore = useAppStore()
	const authStore = useAuthStore()
	const tabListRef = ref()
	const canScrollLeft = ref(false)
	const canScrollRight = ref(false)
	const tabItemRefs = new Map()

	const menuList = computed(() => authStore.menuList)

	// 优先使用 Element Plus 图标，未命中则交给 IconSvg 渲染自定义 SVG。
	function resolveIcon(name) {
		return iconMap[name] || null
	}

	const activeMenu = computed(() => route.meta?.activeMenu || route.path)
	const routeTitleMap = computed(() => {
		const map = new Map()
		router.getRoutes().forEach((item) => {
			if (item.meta?.title) {
				map.set(item.path, item.meta.title)
			}
		})
		return map
	})

	function getTitleByPath(path) {
		return routeTitleMap.value.get(path) || ''
	}

	const headerDisplay = computed(() => {
		const currentTitle = route.meta?.title || '工作台'
		if (!route.meta?.title) {
			return {
				title: '工作台',
				subtitle: ''
			}
		}

		if (route.meta?.hidden && route.meta?.activeMenu) {
			const parentTitle = getTitleByPath(route.meta.activeMenu)
			return {
				title: parentTitle || currentTitle,
				subtitle: currentTitle,
			}
		}

		const matchedWithTitles = route.matched.filter((item) => item.meta?.title && item.path !== '/')
		if (matchedWithTitles.length > 1) {
			return {
				title: matchedWithTitles[0].meta.title,
				subtitle: matchedWithTitles[matchedWithTitles.length - 1].meta.title,
			}
		}

		const segments = route.path.split('/').filter(Boolean)
		if (segments.length > 1) {
			const basePath = `/${segments[0]}`
			const baseTitle = getTitleByPath(basePath)
			if (baseTitle && baseTitle !== currentTitle) {
				return {
					title: baseTitle,
					subtitle: currentTitle,
				}
			}
		}

		return {
			title: route.meta?.headerTitle || currentTitle,
			subtitle: '',
		}
	})

	watch(
		() => route.fullPath,
		async () => {
			if (!route.meta?.title || route.meta.hidden) {
				return
			}
			appStore.addTab({
				path: route.fullPath,
				title: route.meta.title,
				affix: Boolean(route.meta.affix)
			})
			await nextTick()
			scrollActiveTabIntoView()
			updateTabScrollState()
		}, {
			immediate: true
		},
	)

	watch(
		() => appStore.tabs.length,
		async () => {
			await nextTick()
			updateTabScrollState()
		},
	)

	function navigate(path) {
		if (!path || !String(path).startsWith('/')) {
			return
		}
		if (path === route.path) {
			return
		}
		appStore.setPageLoading(true)
		router.push(path).catch(() => {
			appStore.setPageLoading(false)
		})
	}

	async function handleLogout() {
		await confirmAction('确认退出当前账号吗？')
		await authStore.logout()
		appStore.clearTabs(false)
		router.replace('/login')
	}

	function closeTab(path) {
		const current = route.fullPath === path
		appStore.removeTab(path)
		if (current) {
			const fallback = appStore.tabs[appStore.tabs.length - 1]?.path || '/dashboard'
			router.push(fallback)
		}
	}

	function closeAllTabs() {
		const current = route.fullPath
		const shouldRedirect = appStore.tabs.some((item) => item.path === current && !item.affix)
		appStore.clearTabs(true)
		if (shouldRedirect) {
			const fallback = appStore.tabs[appStore.tabs.length - 1]?.path || '/dashboard'
			router.push(fallback)
		}
	}

	function closeOtherTabs() {
		appStore.closeOtherTabs(route.fullPath)
	}

	function closeRightTabs() {
		appStore.closeRightTabs(route.fullPath)
	}

	function handleTabAction(command) {
		if (command === 'all') {
			closeAllTabs()
			return
		}
		if (command === 'right') {
			closeRightTabs()
			return
		}
		if (command === 'other') {
			closeOtherTabs()
		}
	}

	function setTabItemRef(path, el) {
		if (el) {
			tabItemRefs.set(path, el)
			return
		}
		tabItemRefs.delete(path)
	}

	function updateTabScrollState() {
		const el = tabListRef.value
		if (!el) {
			canScrollLeft.value = false
			canScrollRight.value = false
			return
		}
		canScrollLeft.value = el.scrollLeft > 4
		canScrollRight.value = el.scrollLeft + el.clientWidth < el.scrollWidth - 4
	}

	function scrollTabsBy(offset) {
		const el = tabListRef.value
		if (!el) return
		el.scrollBy({
			left: offset,
			behavior: 'smooth'
		})
		window.setTimeout(updateTabScrollState, 220)
	}

	function scrollActiveTabIntoView() {
		const el = tabItemRefs.get(route.fullPath)
		el?.scrollIntoView({
			behavior: 'smooth',
			inline: 'nearest',
			block: 'nearest'
		})
	}

	function handleTabWheel(event) {
		const el = tabListRef.value
		if (!el) return
		if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return
		event.preventDefault()
		el.scrollLeft += event.deltaY
		updateTabScrollState()
	}

	onMounted(() => {
		updateTabScrollState()
		window.addEventListener('resize', updateTabScrollState)
	})

	onBeforeUnmount(() => {
		window.removeEventListener('resize', updateTabScrollState)
	})
</script>

<template>
	<div class="admin-shell">
		<el-container class="admin-frame">
			<el-aside :width="appStore.collapsed ? '64px' : '230px'"
				:class="['admin-aside', 'site-sidebar', { 'is-collapsed': appStore.collapsed }]">
				<div class="site-sidebar__head" :class="{ 'is-collapsed': appStore.collapsed }">
					<img class="site-sidebar__head-logo" :src="brandLogo" alt="和它交个朋友" />
					<div v-show="!appStore.collapsed" class="site-sidebar__head-text">
						<strong>和它交个朋友</strong>
						<span class="site-sidebar__head-sub">管理系统</span>
					</div>
				</div>
				<el-menu :default-active="activeMenu" :collapse="appStore.collapsed" :collapse-transition="false"
					unique-opened class="aside-menu">
					<template v-for="item in menuList" :key="item.id">
						<el-sub-menu v-if="item.children?.length" :index="item.index">
							<template #title>
								<el-icon>
									<component :is="resolveIcon(item.icon)" v-if="resolveIcon(item.icon)" />
									<IconSvg v-else-if="item.icon" :name="item.icon" class="site-sidebar__menu-icon" />
									<component :is="Setting" v-else />
								</el-icon>
								<span>{{ item.title }}</span>
							</template>
							<el-menu-item v-for="child in item.children" :key="child.id" :index="child.index"
								@click="navigate(child.path)">
								<el-icon>
									<component :is="resolveIcon(child.icon)" v-if="resolveIcon(child.icon)" />
									<IconSvg v-else-if="child.icon" :name="child.icon" class="site-sidebar__menu-icon" />
									<component :is="DataBoard" v-else />
								</el-icon>
								<span>{{ child.title }}</span>
							</el-menu-item>
						</el-sub-menu>
						<el-menu-item v-else :index="item.index" @click="navigate(item.path)">
							<el-icon>
								<component :is="resolveIcon(item.icon)" v-if="resolveIcon(item.icon)" />
								<IconSvg v-else-if="item.icon" :name="item.icon" class="site-sidebar__menu-icon" />
								<component :is="DataBoard" v-else />
							</el-icon>
							<span>{{ item.title }}</span>
						</el-menu-item>
					</template>
				</el-menu>
				<div v-show="!appStore.collapsed" class="site-sidebar__foot">
					<span class="site-sidebar__foot-dot" />
					<span class="site-sidebar__foot-text">管理系统</span>
				</div>
			</el-aside>
			<el-container>
				<el-header class="admin-header">
					<div class="admin-header__left">
						<el-button circle plain class="admin-header__fold" @click="appStore.toggleCollapsed()">
							<el-icon>
								<component :is="appStore.collapsed ? Expand : Fold" />
							</el-icon>
						</el-button>
						<div class="admin-header__context">
							<span class="admin-header__badge">当前页面</span>
							<div class="admin-header__boss">
								<div class="admin-header__title">{{ headerDisplay.title }}</div>
								<div v-if="headerDisplay.subtitle" class="admin-header__subtitle admin-header__subtitlex">/
								</div>
								<div v-if="headerDisplay.subtitle" class="admin-header__subtitle">
									{{ headerDisplay.subtitle }}
								</div>
							</div>
						</div>
					</div>
					<el-space class="admin-header__toolbar" :size="12">
						<el-dropdown>
							<div class="user-pill">
								<img class="user-pill__logo" :src="brandLogo" alt="和它交个朋友" />
								<div class="user-pill__info">
									<span class="user-pill__name">{{ authStore.profile.name }}</span>
									<span class="user-pill__email">{{ authStore.profile.email }}</span>
								</div>
								<el-icon>
									<ArrowDown />
								</el-icon>
							</div>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item @click="router.push('/dashboard')">返回工作台</el-dropdown-item>
									<el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</el-space>
				</el-header>
				<el-main class="admin-main">
					<div class="tab-bar">
						<el-button text class="tab-bar__scroll" :disabled="!canScrollLeft"
							@click="scrollTabsBy(-240)">‹</el-button>
						<div ref="tabListRef" class="tab-bar__list" @scroll="updateTabScrollState"
							@wheel="handleTabWheel">
							<button v-for="item in appStore.tabs" :key="item.path" class="tab-bar__item"
								:ref="(el) => setTabItemRef(item.path, el)"
								:class="{ 'is-active': item.path === route.fullPath }" @click="navigate(item.path)">
								<span>{{ item.title }}</span>
								<span v-if="!item.affix" class="tab-bar__close"
									@click.stop="closeTab(item.path)">×</span>
							</button>
						</div>
						<el-button text class="tab-bar__scroll" :disabled="!canScrollRight"
							@click="scrollTabsBy(240)">›</el-button>
						<el-dropdown trigger="click" @command="handleTabAction">
							<el-button text class="tab-bar__action">
								标签操作
								<el-icon>
									<ArrowDown />
								</el-icon>
							</el-button>
							<template #dropdown>
								<el-dropdown-menu>
									<el-dropdown-item command="all">全部关闭</el-dropdown-item>
									<el-dropdown-item command="right">关闭右侧</el-dropdown-item>
									<el-dropdown-item command="other">关闭其他</el-dropdown-item>
								</el-dropdown-menu>
							</template>
						</el-dropdown>
					</div>
					<div class="site-wrapper admin-main-body" v-loading="appStore.pageLoading"
						element-loading-text="页面加载中...">
						<div v-if="appStore.pageLoading" class="route-progress" aria-hidden="true"></div>
						<router-view />
					</div>
				</el-main>
			</el-container>
		</el-container>
	</div>
</template>

<style scoped>
	/* Shell U3 · yanglao-admin-v1 · Brand #f8efe2 */
	.admin-header__subtitlex {
		margin: 0 5px;
		color: var(--nf-text-secondary, #909399);
	}

	.admin-header__subtitle {
		font-size: 13px;
		color: var(--nf-text-secondary, #909399);
	}

	.admin-header__boss {
		display: flex;
		align-items: center;
		min-width: 0;
	}

	.admin-header__toolbar {
		display: flex;
		align-items: center;
		flex-shrink: 0;
	}

	.admin-header__toolbar :deep(.el-space__item) {
		display: flex;
		align-items: center;
	}

	.admin-header__fold {
		flex-shrink: 0;
		width: 36px;
		height: 36px;
		padding: 0;
		border-color: var(--nf-border, #e8eaed);
		color: var(--nf-text-regular, #303133);
	}

	.admin-header__fold:hover {
		border-color: var(--nf-brand-deep, #eed8b9);
		color: var(--nf-brand-deep, #eed8b9);
		background: var(--nf-brand-lite, #fefdfc);
	}

	.tab-bar {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		gap: 6px;
		margin: 0 -16px 12px;
		padding: 10px 16px;
		background: #eef1f6;
		border-bottom: 1px solid #e4e7ed;
	}

	.tab-bar__scroll {
		flex-shrink: 0;
		width: 28px;
		height: 30px;
		padding: 0;
		border-radius: 6px;
		color: #909399;
		font-size: 16px;
		line-height: 1;
		background: transparent;
	}

	.tab-bar__scroll:hover:not(:disabled) {
		color: #a67b4f;
		background: rgba(255, 255, 255, 0.85);
	}

	.tab-bar__list {
		display: flex;
		gap: 8px;
		min-width: 0;
		flex: 1;
		overflow: auto;
		scrollbar-width: none;
	}

	.tab-bar__list::-webkit-scrollbar {
		display: none;
	}

	.tab-bar__item {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		flex-shrink: 0;
		height: 34px;
		padding: 0 14px;
		border: 1px solid #e4e7ed;
		border-radius: 8px;
		background: #ffffff;
		color: #606266;
		font-size: 13px;
		font-weight: 500;
		cursor: pointer;
		box-shadow: 0 1px 2px rgba(26, 36, 51, 0.04);
		transition: border-color 0.2s, background 0.2s, color 0.2s, box-shadow 0.2s;
	}

	.tab-bar__item:hover {
		border-color: #d8b892;
		background: #fffdf9;
		color: #303133;
	}

	.tab-bar__item.is-active {
		border-color: #d4a574;
		background: #ffffff;
		color: #c49a6c;
		font-weight: 600;
		box-shadow: 0 0 0 1px rgba(196, 154, 108, 0.18), 0 1px 3px rgba(196, 154, 108, 0.12);
	}

	.tab-bar__close {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		width: 16px;
		height: 16px;
		margin-left: 0;
		border-radius: 50%;
		font-size: 13px;
		line-height: 1;
		color: #c0c4cc;
		transition: background 0.15s, color 0.15s;
	}

	.tab-bar__item:hover .tab-bar__close,
	.tab-bar__item.is-active .tab-bar__close {
		color: #c49a6c;
	}

	.tab-bar__close:hover {
		background: rgba(196, 154, 108, 0.16);
		color: #8b613d;
	}

	.tab-bar__action {
		flex-shrink: 0;
		height: 34px;
		padding: 0 10px;
		border-radius: 8px;
		color: #909399;
		font-size: 13px;
		line-height: 1;
		background: #ffffff;
		border: 1px solid #e4e7ed;
	}

	.tab-bar__action:hover {
		color: #c49a6c;
		border-color: #d4a574;
		background: #fffdf9;
	}

	.admin-main-body {
		position: relative;
		flex: 1;
		min-height: 240px;
		min-width: 0;
		overflow-x: hidden;
	}

	.route-progress {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		z-index: 20;
		height: 3px;
		overflow: hidden;
		background: rgba(238, 216, 185, 0.25);
	}

	.route-progress::after {
		content: '';
		display: block;
		height: 100%;
		width: 40%;
		background: linear-gradient(90deg, transparent, var(--nf-brand-deep, #eed8b9), transparent);
		animation: route-progress-slide 0.9s ease-in-out infinite;
	}

	@keyframes route-progress-slide {
		0% {
			transform: translateX(-100%);
		}
		100% {
			transform: translateX(350%);
		}
	}

	.admin-aside.is-collapsed :deep(.el-menu--collapse) {
		width: 64px;
	}

	.admin-aside.is-collapsed :deep(.el-menu-item),
	.admin-aside.is-collapsed :deep(.el-sub-menu__title) {
		display: flex;
		justify-content: center;
		padding: 0 !important;
	}

	.admin-aside.is-collapsed :deep(.el-menu-item .el-icon),
	.admin-aside.is-collapsed :deep(.el-sub-menu__title .el-icon) {
		margin-right: 0;
	}

	.aside-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
		color: var(--nf-menu-active, #8b613d);
	}

	.aside-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title .el-icon) {
		color: var(--nf-menu-active-soft, #bc926d);
	}
</style>