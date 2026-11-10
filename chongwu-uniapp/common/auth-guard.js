import { clearAuthSession } from '@/common/auth-session.js';

const LOGIN_PATH = '/packageA/login/index';

const AUTH_PAGES = [
	'/packageA/post/create',
	'/packageA/post/mine',
	'/packageA/adopt/form',
	'/packageA/feed/index',
	'/packageA/home/setup',
	'/packageA/tasks/center',
	'/packageA/market/list',
	'/packageA/market/records',
	'/packageA/settings/index',
	'/packageA/coupon/list',
	'/packageA/exchange/list',
	'/packageA/gift/list',
	'/packageA/assist/list'
];

const AUTH_TABS = ['/pages/my/my'];

export function needAuth(url = '') {
	const path = url.split('?')[0];
	return AUTH_PAGES.some(p => path.includes(p)) || AUTH_TABS.some(p => path === p);
}

export function getCurrentPageUrl() {
	const pages = getCurrentPages();
	if (!pages.length) return '';
	const page = pages[pages.length - 1];
	const route = page.route ? `/${page.route}` : '';
	if (!route) return '';
	const options = page.options || {};
	const query = Object.keys(options)
		.filter(key => options[key] !== undefined && options[key] !== null && options[key] !== '')
		.map(key => `${key}=${encodeURIComponent(options[key])}`)
		.join('&');
	return query ? `${route}?${query}` : route;
}

export function clearAuth() {
	clearAuthSession();
}

export function ensureLogin(url = '', force = false) {
	const token = uni.getStorageSync('token');
	if (token) return true;
	const redirectTarget = url || getCurrentPageUrl() || '/pages/my/my';
	if (force || needAuth(redirectTarget)) {
		uni.showToast({ title: '请先登录', icon: 'none' });
		uni.navigateTo({ url: `${LOGIN_PATH}?redirect=${encodeURIComponent(redirectTarget)}` });
		return false;
	}
	return true;
}

export function getTerminal() {
	// #ifdef MP-WEIXIN
	return 'mini';
	// #endif
	// #ifdef H5
	return 'h5';
	// #endif
	// #ifdef APP-PLUS
	return 'app';
	// #endif
	return 'mini';
}

export function getIdempotentKey(prefix = 'req') {
	return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
}
