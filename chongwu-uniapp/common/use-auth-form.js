import {
	watch
} from 'vue';
import {
	sanitizeDigits,
	sanitizeTrim,
	validatePhone as validatePhoneValue
} from '@/common/validators.js';

export function useAuthForm({
	phone,
	code,
	password,
	confirmPassword
}) {
	if (phone) {
		watch(phone, value => {
			const normalizedValue = sanitizeDigits(value, 11);
			if (value !== normalizedValue) {
				phone.value = normalizedValue;
			}
		});
	}

	if (code) {
		watch(code, value => {
			const normalizedValue = sanitizeDigits(value, 6);
			if (value !== normalizedValue) {
				code.value = normalizedValue;
			}
		});
	}

	const validatePhone = () => {
		const errorMessage = validatePhoneValue(phone?.value);
		if (errorMessage) {
			uni.showToast({
				title: errorMessage,
				icon: 'none'
			});
			return false;
		}

		return true;
	};

	const trimPasswords = () => {
		if (password) {
			password.value = sanitizeTrim(password.value);
		}

		if (confirmPassword) {
			confirmPassword.value = sanitizeTrim(confirmPassword.value);
		}
	};

	return {
		trimPasswords,
		validatePhone
	};
}
