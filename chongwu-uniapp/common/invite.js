const PENDING_KEY = 'superiorInvitationCode';
const MY_CODE_KEY = 'invitationCode';

/** 落地页保存分享带来的邀请人邀请码 */
export function savePendingInvitationCode(code) {
	const value = String(code || '').trim();
	if (!value) return;
	uni.setStorageSync(PENDING_KEY, value);
}

export function getPendingInvitationCode() {
	return String(uni.getStorageSync(PENDING_KEY) || '').trim();
}

export function clearPendingInvitationCode() {
	uni.removeStorageSync(PENDING_KEY);
}

/** 当前登录用户自己的邀请码（用于分享） */
export function saveMyInvitationCode(code) {
	const value = String(code || '').trim();
	if (!value) return;
	uni.setStorageSync(MY_CODE_KEY, value);
}

export function getMyInvitationCode() {
	return String(uni.getStorageSync(MY_CODE_KEY) || '').trim();
}

export function clearMyInvitationCode() {
	uni.removeStorageSync(MY_CODE_KEY);
}
