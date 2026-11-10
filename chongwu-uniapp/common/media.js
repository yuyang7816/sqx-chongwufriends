import config from './config';

function getUploadHost() {
	return String(config.UPLOADHOST || config.APIHOST || '').replace(/\/+$/, '');
}

function getApiHost() {
	return String(config.APIHOST || '').replace(/\/+$/, '');
}

export function resolveMediaUrl(url, fallback = '/static/pets/island-pet-shiba.png') {
	if (!url) return fallback;
	const trimmed = String(url).trim();
	if (!trimmed) return fallback;
	if (/^https?:\/\//i.test(trimmed)) {
		try {
			const u = new URL(trimmed);
			if (u.pathname.includes('/upload/')) {
				return `${getUploadHost()}${u.pathname.startsWith('/sqx_fast') ? u.pathname.replace(/^\/sqx_fast/, '') : u.pathname}`;
			}
		} catch (e) {
			// ignore
		}
		return trimmed;
	}
	if (trimmed.startsWith('/static/')) return trimmed;
	if (trimmed.startsWith('//')) return `https:${trimmed}`;
	const path = trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
	if (path.includes('/upload/')) {
		const normalized = path.replace(/^\/sqx_fast(?=\/|$)/, '');
		return `${getUploadHost()}${normalized.startsWith('/') ? normalized : `/${normalized}`}`;
	}
	return `${getApiHost()}${path}`;
}
