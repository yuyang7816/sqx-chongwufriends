export const PHONE_REG = /^1\d{10}$/;

export function sanitizeDigits(value = '', maxLength = Infinity) {
	return String(value).replace(/\D/g, '').slice(0, maxLength);
}

export function sanitizeTrim(value = '') {
	return String(value).trim();
}

export function getApiErrorMessage(error, fallback) {
	return error?.msg || fallback;
}

export function showApiErrorToast(error, fallback) {
	uni.showToast({
		title: getApiErrorMessage(error, fallback),
		icon: 'none'
	});
}

export function validatePhone(phone) {
	if (!phone) {
		return '请输入手机号';
	}

	if (!PHONE_REG.test(phone)) {
		return '请输入正确手机号';
	}

	return '';
}
