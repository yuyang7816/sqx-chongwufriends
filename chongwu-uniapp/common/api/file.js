import config from '@/common/config';

const SUCCESS_CODES = new Set([0, 200]);

function getUploadHost() {
	return String(config.UPLOADHOST || config.APIHOST || '').replace(/\/+$/, '');
}

function normalizeUploadPath(pathname = '') {
	let path = String(pathname || '').trim();
	if (!path) return '';
	path = path.replace(/^\/sqx_fast(?=\/|$)/, '');
	if (!path.startsWith('/')) path = `/${path}`;
	return path;
}

function joinUploadFileUrl(pathname) {
	const path = normalizeUploadPath(pathname);
	if (!path) return '';
	return `${getUploadHost()}${path}`;
}

function toPublicFileUrl(input) {
	if (input == null || input === '') return '';
	if (typeof input === 'object') {
		const relative = String(input.relativePath || '').trim();
		if (relative.includes('/upload/')) {
			return joinUploadFileUrl(relative);
		}
	}
	let raw = input;
	if (typeof raw === 'object') {
		raw = raw.url || raw.src || raw.path || raw.data || '';
	}
	const value = String(raw || '').trim();
	if (!value) return '';

	if (/^https?:\/\//i.test(value)) {
		try {
			const u = new URL(value);
			if (u.pathname.includes('/upload/')) {
				return joinUploadFileUrl(u.pathname);
			}
			return value;
		} catch (e) {
			return value;
		}
	}
	if (value.startsWith('//')) return toPublicFileUrl(`https:${value}`);
	if (value.includes('/upload/')) return joinUploadFileUrl(value);
	return joinUploadFileUrl(value);
}

export function uploadImage(filePath) {
	return new Promise((resolve, reject) => {
		if (!filePath) {
			reject(new Error('filePath is required'));
			return;
		}
		if (/^https?:\/\//i.test(filePath) && !/\/upload\//i.test(filePath)) {
			resolve(filePath);
			return;
		}
		const token = uni.getStorageSync('token') || '';
		uni.uploadFile({
			url: `${getUploadHost()}/file/upload`,
			filePath,
			name: 'file',
			header: token ? { token } : {},
			success: (res) => {
				try {
					const body = typeof res.data === 'string' ? JSON.parse(res.data) : (res.data || {});
					if (!SUCCESS_CODES.has(body.code)) {
						uni.showToast({
							title: body.msg || '上传失败',
							icon: 'none'
						});
						reject(body);
						return;
					}
					const data = body.data || {};
					const url = toPublicFileUrl(data);
					if (!url) {
						reject(new Error('upload url empty'));
						return;
					}
					resolve(url);
				} catch (e) {
					reject(e);
				}
			},
			fail: (err) => {
				uni.showToast({
					title: '上传失败',
					icon: 'none'
				});
				reject(err);
			}
		});
	});
}

export { toPublicFileUrl };
