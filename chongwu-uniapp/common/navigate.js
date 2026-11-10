const TAB_PAGES = ['/pages/index/index', '/pages/msg/msg', '/pages/island/island', '/pages/shop/shop', '/pages/my/my'];

export function openAppLink(url = '') {
	if (!url) return;
	const trimmed = String(url).trim();
	if (!trimmed) return;

	if (/^https?:\/\//i.test(trimmed)) {
		uni.navigateTo({
			url: `/pages/webView/webView?url=${encodeURIComponent(trimmed)}`
		});
		return;
	}

	const normalized = trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
	const path = normalized.split('?')[0];
	if (TAB_PAGES.includes(path)) {
		uni.switchTab({ url: path });
		return;
	}
	uni.navigateTo({ url: normalized });
}
