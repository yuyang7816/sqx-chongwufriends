import { clearMessageTabBadge, syncMessageTabBadge } from '@/common/message-badge.js';
import { clearMyInvitationCode, saveMyInvitationCode } from '@/common/invite.js';

const TAB_PAGES = ['/pages/index/index', '/pages/msg/msg', '/pages/island/island', '/pages/shop/shop', '/pages/my/my'];

export function clearAuthSession() {
	uni.removeStorageSync('token');
	uni.removeStorageSync('userId');
	uni.removeStorageSync('phone');
	uni.removeStorageSync('userName');
	uni.removeStorageSync('avatar');
	uni.removeStorageSync('openId');
	clearMyInvitationCode();
	clearMessageTabBadge();
}

export function normalizeLoginPayload(data = {}) {
	if (data?.loginVO?.token) return data.loginVO;
	if (data?.token) return data;
	return data || {};
}

export function saveAuthSession(data = {}) {
	const payload = normalizeLoginPayload(data);
	if (payload.token) uni.setStorageSync('token', payload.token);
	const user = payload.user || {};
	if (user.userId) uni.setStorageSync('userId', user.userId);
	if (user.phone) uni.setStorageSync('phone', user.phone);
	if (user.userName) uni.setStorageSync('userName', user.userName);
	if (user.avatar) uni.setStorageSync('avatar', user.avatar);
	if (user.invitationCode) saveMyInvitationCode(user.invitationCode);
	return payload;
}

export function navigateAfterLogin(redirectUrl = '') {
	const targetUrl = redirectUrl || '/pages/my/my';
	const targetPath = targetUrl.split('?')[0];
	if (TAB_PAGES.includes(targetPath)) {
		uni.switchTab({ url: targetPath });
		return;
	}
	uni.redirectTo({ url: targetUrl.includes('?') ? targetUrl : targetUrl });
}

export function persistLogin(data = {}, redirectUrl = '') {
	saveAuthSession(data);
	syncMessageTabBadge();
	navigateAfterLogin(redirectUrl);
}
