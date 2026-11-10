<script setup>
	import {
		getCurrentInstance
	} from 'vue';
	const {
		proxy
	} = getCurrentInstance();
	import {
		onLaunch,
		onShow,
		onHide
	} from '@dcloudio/uni-app';
	import { ensureLogin } from '@/common/auth-guard.js';
	import { syncMessageTabBadge } from '@/common/message-badge.js';

	const APP_TITLE = '和它交朋友';
	const BAD_TITLES = new Set(['IM聊天', '图片']);

	function syncAppTitle() {
		// #ifdef H5
		// previewImage 关闭后可能把浏览器标题还原成旧包名「IM聊天」
		const title = String(document?.title || '').trim();
		if (!title || BAD_TITLES.has(title)) {
			document.title = APP_TITLE;
		}
		// #endif
	}

	onLaunch(() => {
		syncAppTitle();
		['navigateTo', 'redirectTo', 'reLaunch'].forEach(method => {
			uni.addInterceptor(method, {
				invoke(args) {
					if (!ensureLogin(args.url || '')) {
						return false;
					}
				}
			});
		});
		// //企业微信客服链接
		// proxy.$http.get('/app/common/type/274').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefu', res.data.value)
		// 	}
		// })
		// //企业微信客服appid
		// proxy.$http.get('/app/common/type/275').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefuAppId', res.data.value)
		// 	}
		// })
		// //客服类型  328
		// proxy.$http.get('/app/common/type/328').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('SelKeFu', res.data.value)
		// 	}
		// })
		// //客服电话  329
		// proxy.$http.get('/app/common/type/329').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefuPhone', res.data.value)
		// 	}
		// })
	});
	onShow(() => {
		syncAppTitle();
		syncMessageTabBadge();
	});
	onHide(() => {
		console.log('onHide')
	});
</script>

<style lang="scss">
	/*每个页面公共css */
	//uview-plus公共样式
	@import "@/uni_modules/uview-plus/index.scss";
	//自定义公共样式
	@import "@/static/css/main.css";
	@import "@/static/css/pet-theme.scss";

	page {
		background-color: #f9f1e7;
		font-size: 28rpx;
	}
</style>
