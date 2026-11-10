import {
	createRouter,
	createWebHistory
} from 'vue-router'
import {
	useAuthStore
} from '@/stores/auth'
import {
	useAppStore
} from '@/stores/app'
import {
	protectedRoutes,
	whiteRoutes
} from '@/router/routes'

function normalizeRedirect(target) {
	if (!target || typeof target !== 'string' || target.startsWith('/login')) {
		return '/dashboard'
	}
	return target
}

const routes = [
	...whiteRoutes,
	{
		path: '/',
		component: () => import('@/layouts/AdminLayout.vue'),
		redirect: '/dashboard',
		children: protectedRoutes,
	},
	{
		path: '/:pathMatch(.*)*',
		redirect: '/404',
	},
]

const router = createRouter({
	history: createWebHistory(),
	routes,
})

function stopPageLoading() {
	try {
		useAppStore().setPageLoading(false)
	} catch (e) {
		// pinia 未就绪时忽略
	}
}

router.beforeEach(async (to, from) => {
	const authStore = useAuthStore()
	const appStore = useAppStore()

	if (to.fullPath !== from.fullPath && to.name !== 'login') {
		appStore.setPageLoading(true)
	}

	if (to.name === 'login' && authStore.isLoggedIn) {
		return normalizeRedirect(to.query.redirect)
	}

	if (to.name !== 'login' && !authStore.token) {
		appStore.setPageLoading(false)
		return {
			path: '/login',
			query: {
				redirect: normalizeRedirect(to.fullPath)
			}
		}
	}

	if (to.name !== 'login' && authStore.token && !authStore.isLoggedIn) {
		appStore.setPageLoading(false)
		return {
			path: '/login',
			query: {
				redirect: normalizeRedirect(to.fullPath)
			}
		}
	}

	// 有 token 时每次刷新页面都会重新拉菜单（navLoaded 初始为 false），
	// 避免 DB 新增菜单后侧栏仍读 sessionStorage 旧缓存
	if (to.name !== 'login' && authStore.token && !authStore.navLoaded) {
		try {
			await authStore.fetchNavData()
		} catch (error) {
			appStore.setPageLoading(false)
			return {
				path: '/login',
				query: {
					redirect: normalizeRedirect(to.fullPath)
				}
			}
		}
	}

	if (to.meta?.permission && !authStore.hasPermission(to.meta.permission)) {
		appStore.setPageLoading(false)
		return '/dashboard'
	}
})

router.afterEach((to) => {
	document.title = `${to.meta?.title || '后台管理'} | 和它交个朋友管理台`
	// 稍延后关闭，避免极快切换时无反馈
	setTimeout(() => {
		stopPageLoading()
	}, 180)
})

router.onError(() => {
	stopPageLoading()
})

export default router
