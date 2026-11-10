import { get, post } from '@/common/request';

export function phoneCodeLogin(phoneNo, verifyCode) {
	return post('/app/base/auth/phoneCodeLogin', { phoneNo, verifyCode }, { needAuth: false });
}

export function phonePasswordLogin(phoneNo, password) {
	return post('/app/base/auth/phonePasswordLogin', { phoneNo, password }, { needAuth: false });
}

export function phoneRegister(phoneNo, code, password, superiorInvitationCode = '') {
	const payload = { phoneNo, code, password };
	const invite = String(superiorInvitationCode || '').trim();
	if (invite) payload.superiorInvitationCode = invite;
	return post('/app/base/auth/phoneRegister', payload, { needAuth: false });
}

export function wxJsRegister(payload) {
	return post('/app/base/auth/wxJsRegister', payload, { needAuth: false });
}

export function resetPassword(phone, code, password) {
	return post('/app/base/auth/resetPassword', { phone, code, password }, { needAuth: false });
}

export function wxLogin(code) {
	return get('/app/base/auth/wxLogin', { code }, { needAuth: false });
}

export function wxPhoneLogin(loginCode, phoneCode, superiorInvitationCode = '') {
	const payload = { loginCode, phoneCode };
	const invite = String(superiorInvitationCode || '').trim();
	if (invite) payload.superiorInvitationCode = invite;
	return post('/app/base/auth/wxPhoneLogin', payload, { needAuth: false });
}

export function getWxUserPhoneNumber(code, openId) {
	return get('/app/base/auth/getWxUserPhoneNumber', { code, openId }, { needAuth: false });
}

export function sendPhoneCode(phone, bizCode = 'login') {
	return post('/app/base/verifyCode/sendPhoneCode', { phone, bizCode }, { needAuth: false });
}

export function fetchUserInfo() {
	return get('/app/base/user/info');
}

export function updateUserInfo(data) {
	return post('/app/base/user/update', data);
}
