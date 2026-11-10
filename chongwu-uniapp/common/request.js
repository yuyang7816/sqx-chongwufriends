import config from './config';
import { clearAuthSession } from './auth-session.js';

const SUCCESS_CODES = new Set([0, 200]);
const AUTH_CODES = new Set([401, 50002]);

function getToken() {
	return uni.getStorageSync('token') || '';
}

function clearAuth() {
	clearAuthSession();
}

function buildUrl(path) {
	const base = config.APIHOST || '';
	const normalizedPath = path.startsWith('/') ? path : `/${path}`;
	return `${base}${normalizedPath}`;
}

function getClientType() {
	// #ifdef MP-WEIXIN
	return 'MP-WEIXIN';
	// #endif
	// #ifdef APP-PLUS
	return 'APP';
	// #endif
	// #ifdef H5
	return 'H5';
	// #endif
	return 'UNKNOWN';
}

function request(path, { method = 'GET', data = {}, header = 'application/json', needAuth = true, resolveFullResponse = false, silentError = false } = {}) {
	return new Promise((resolve, reject) => {
		const token = needAuth ? getToken() : '';
		const idempotentKey = data && data.idempotentKey ? data.idempotentKey : '';
		uni.request({
			url: buildUrl(path),
			data,
			method,
			header: {
				'content-type': header,
				clientType: getClientType(),
				...(token ? { token } : {}),
				...(idempotentKey ? { 'Idempotent-Key': idempotentKey } : {})
			},
			success: (res) => {
				const body = res.data || {};
				if (AUTH_CODES.has(body.code) && token) {
					clearAuth();
					if (!silentError) {
						uni.showToast({ title: body.msg || '请重新登录', icon: 'none' });
					}
					reject(body);
					return;
				}
				if (!SUCCESS_CODES.has(body.code)) {
					if (!silentError) {
						uni.showToast({ title: body.msg || '请求失败', icon: 'none' });
					}
					reject(body);
					return;
				}
				resolve(resolveFullResponse ? body : body.data);
			},
			fail: (err) => {
				if (!silentError) {
					uni.showToast({ title: '网络异常', icon: 'none' });
				}
				reject(err);
			}
		});
	});
}

export function get(path, data = {}, options = {}) {
	return request(path, { method: 'GET', data, ...options });
}

export function post(path, data = {}, options = {}) {
	return request(path, { method: 'POST', data, header: 'application/json', ...options });
}

export function postForm(path, data = {}, options = {}) {
	return request(path, {
		method: 'POST',
		data,
		header: 'application/x-www-form-urlencoded',
		...options
	});
}

export default { get, post, postForm };
