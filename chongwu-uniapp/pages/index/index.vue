<template>
	<view class="pet-page home-page">
		<up-navbar :placeholder="true" :border="false" bgColor="#FFF8F0" leftIcon="" title="">
			<template #left>
				<view class="home-main-title">
					和它交朋友
				</view>
			</template>
			<template #right>
				<view class="city-chip" @tap="openCityPicker">
					<text>{{ currentCity }}</text>
					<up-icon :name="cityPickerLoading ? 'reload' : 'arrow-down'" size="12" color="#8b613d"></up-icon>
				</view>
			</template>
		</up-navbar>

		<view class="pet-shell">
			<view v-if="pageLoading" class="home-skeleton">
				<view class="skeleton-block hero-skeleton"></view>
				<view class="skeleton-block title-skeleton"></view>
				<view class="skeleton-grid">
					<view v-for="n in 4" :key="n" class="skeleton-block store-skeleton"></view>
				</view>
			</view>

			<view v-else-if="loadError" class="home-error" @tap="retryLoad">
				<text class="home-error-text">加载失败，点击重试</text>
			</view>

			<template v-else>
				<view class="hero-card-wrap">
					<swiper v-if="bannerList.length" class="hero-swiper" circular autoplay interval="4500"
						:current="bannerIndex" @change="onBannerChange">
						<swiper-item v-for="banner in bannerList" :key="banner.id">
							<view class="hero-card" @tap="openBanner(banner)">
								<view class="hero-image">
									<image :src="banner.image" mode="scaleToFill"></image>
								</view>
							</view>
						</swiper-item>
					</swiper>
					
				</view>

				<view class="pet-section-title">
					<view class="section-title-left">
						<image src="/static/image/index/home.png" class="section-icon" mode="widthFix"></image>
						<text>线下门店</text>
					</view>
				</view>
				<view class="store-grid">
					<view v-if="!storeCards.length && !pageLoading" class="pet-muted store-empty">当前城市暂无门店</view>
					<view v-for="store in storeCards" :key="store.id" class="pet-card store-item"
						@tap="openStore(store)">
						<view class="store-icon" :style="{ background: store.tone }">
							<up-icon name="home" size="20" color="#ffffff"></up-icon>
						</view>
						<text class="store-name">{{ store.name }}</text>
					</view>
				</view>
				<view class="pet-pill-row store-filter-row flex align-center justify-between">
					<view v-for="store in quickStores" :key="store.id" class="pet-pill" @tap="openStore(store)">
						{{ store.name }}
					</view>
				</view>

				<view class="pet-card pet-dashed event-card" @tap="openEventStore">
					<view class="event-head">
						<text class="event-badge">{{ eventStore.businessStatus === 1 ? '今日营业中' : '门店动态' }}</text>
						<text class="event-flag">{{ eventStore.name || '摇尾巴认证' }}</text>
					</view>
					<view class="pet-thumb event-image">
						<image :src="resolveMediaUrl(eventStore.coverUrl)" mode="aspectFill"></image>
					</view>
					<text class="event-copy">{{ eventStore.description || '在这里，每一只毛孩子都是我们的家人。欢迎来门店和它们互动。' }}</text>
				</view>

				<view class="social-entry-row">
					<view class="pet-card social-entry xhs" @tap="openXhsQr">
						<text class="social-title">{{ socialXhs.title }}</text>
						<text class="pet-muted">{{ socialXhs.subtitle }}</text>
					</view>
					<view class="pet-card social-entry douyin" @tap="openSocial(socialCommunity)">
						<text class="social-title">{{ socialCommunity.title }}</text>
						<text class="pet-muted">{{ socialCommunity.subtitle }}</text>
					</view>
				</view>
				<view class="home-foot-space"></view>
			</template>
		</view>

		<up-popup :show="cityPickerShow" mode="bottom" :round="20" :safeAreaInsetBottom="true"
			@close="cityPickerShow = false">
			<view class="city-panel">
				<view class="city-panel-head">
					<text class="city-panel-title">选择城市</text>
					<view class="city-panel-close" @tap="cityPickerShow = false">
						<up-icon name="close" size="18" color="#8b613d"></up-icon>
					</view>
				</view>
				<view class="city-panel-sub">当前城市：{{ currentCity }}</view>
				<scroll-view scroll-y class="city-panel-scroll">
					<view class="city-grid">
						<view v-for="city in cityList" :key="city.id"
							class="city-grid-item" :class="{ 'is-active': city.id === currentCityId }"
							@tap="selectCity(city)">
							<text>{{ city.name }}</text>
						</view>
					</view>
					<view v-if="!cityList.length && !cityPickerLoading" class="city-panel-empty">
						暂无开放城市
					</view>
				</scroll-view>
			</view>
		</up-popup>

		<up-popup :show="xhsQrShow" mode="center" :round="24" :safeAreaInsetBottom="false" bgColor="transparent" @close="closeXhsQr">
			<view class="xhs-qr-panel">
				<view class="xhs-qr-head">
					<text class="xhs-qr-title">和它交朋友</text>
					<view class="xhs-qr-close" @tap="closeXhsQr">
						<up-icon name="close" size="16" color="#8b613d"></up-icon>
					</view>
				</view>
				<text class="xhs-qr-sub">{{ socialXhs.subtitle || '扫码关注获取最新动态' }}</text>
				<view class="xhs-qr-box" @tap.stop="previewXhsQr">
					<image v-if="xhsQrUrl" class="xhs-qr-img" :src="xhsQrUrl" mode="aspectFit"></image>
					<view v-else class="xhs-qr-empty">
						<text class="xhs-qr-empty-text">暂未配置二维码</text>
					</view>
				</view>
				<text class="xhs-qr-hint">点击二维码可放大查看</text>
				<view class="xhs-qr-btn" @tap="closeXhsQr">我知道了</view>
			</view>
		</up-popup>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		fetchCities,
		fetchStores,
		fetchBanners,
		fetchHomeConfig,
		trackBannerClick
	} from '@/common/api/home.js';
	import {
		openAppLink
	} from '@/common/navigate.js';
	import {
		resolveMediaUrl
	} from '@/common/media.js';
	import {
		onLoad,
		onShow,
		onPullDownRefresh
	} from '@dcloudio/uni-app';

	const HOME_CITY_KEY = 'homeCityId';
	const HOME_TITLE = '和它交朋友';

	const restoreHomeTitle = () => {
		uni.setNavigationBarTitle({ title: HOME_TITLE });
		// #ifdef H5
		if (typeof document !== 'undefined') {
			document.title = HOME_TITLE;
		}
		// #endif
	};

	const DEFAULT_HERO = {
		title: '理性养宠',
		subtitle: '我们主张降低遗弃，用爱与责任给它一个温暖的家。',
		image: '/static/logo.png',
		jumpUrl: '/packageA/adopt/list'
	};

	const pageLoading = ref(true);
	const loadError = ref(false);
	const currentCity = ref('选择城市');
	const currentCityId = ref(null);
	const storeCards = ref([]);
	const quickStores = ref([]);
	const allStores = ref([]);
	const heroBanner = ref({
		...DEFAULT_HERO
	});
	const bannerList = ref([]);
	const bannerIndex = ref(0);
	const socialXhs = ref({
		title: '关注小红书',
		subtitle: '获取最新动态',
		jumpUrl: '',
		qrUrl: ''
	});
	const socialCommunity = ref({
		title: '加入社群',
		subtitle: '获取养宠咨询',
		jumpUrl: '/packageA/wecom/index'
	});
	const eventStore = ref({
		name: '',
		coverUrl: '/static/logo.png',
		businessStatus: 1,
		description: ''
	});
	const cityList = ref([]);
	const cityPickerShow = ref(false);
	const cityPickerLoading = ref(false);
	const xhsQrShow = ref(false);
	const xhsQrUrl = ref('');

	const normalizeCities = (list = []) => list.map(item => ({
		id: item.id ?? item.cityId ?? item.value,
		name: item.name ?? item.cityName ?? item.label ?? ''
	})).filter(item => item.id !== undefined && item.name);

	const mapStores = (list = []) => {
		const mapped = list.map(item => ({
			id: item.id,
			name: item.name,
			tone: item.tone || '#f6d57c',
			coverUrl: item.coverUrl,
			featured: item.featured === 1 || item.featured === true,
			businessStatus: item.businessStatus,
			address: item.address
		}));
		allStores.value = mapped;
		storeCards.value = mapped.slice(0, 4);
		quickStores.value = mapped.slice(4);
		const eventPick = mapped.find(item => item.featured) ||
			mapped.find(item => item.businessStatus === 1) ||
			mapped[0];
		if (eventPick) {
			eventStore.value = {
				id: eventPick.id,
				name: eventPick.name,
				coverUrl: eventPick.coverUrl || '/static/logo.png',
				businessStatus: eventPick.businessStatus,
				description: eventPick.address ? `${eventPick.name} · ${eventPick.address}` :
					'在这里，每一只毛孩子都是我们的家人。欢迎来门店和它们互动。'
			};
			return;
		}
		eventStore.value = {
			name: '',
			coverUrl: '/static/logo.png',
			businessStatus: 0,
			description: '当前城市暂无门店，试试切换其他城市看看。'
		};
	};

	const syncCurrentCity = (cities = []) => {
		if (!cities.length) return;
		const savedId = Number(uni.getStorageSync(HOME_CITY_KEY) || 0);
		const matched = cities.find(item => item.id === currentCityId.value) ||
			(savedId ? cities.find(item => item.id === savedId) : null) ||
			cities.find(item => item.name === currentCity.value) ||
			cities[0];
		currentCity.value = matched.name;
		currentCityId.value = matched.id;
	};

	const mapBannerItem = (banner = {}) => ({
		id: banner.id,
		title: banner.name || DEFAULT_HERO.title,
		subtitle: banner.subTitle || banner.subtitle || DEFAULT_HERO.subtitle,
		image: resolveMediaUrl(banner.imageUrl, DEFAULT_HERO.image),
		jumpUrl: banner.jumpUrl || DEFAULT_HERO.jumpUrl
	});

	const loadBanners = async () => {
		const banners = await fetchBanners('home');
		if (banners?.length) {
			bannerList.value = banners.map(mapBannerItem);
			bannerIndex.value = 0;
			heroBanner.value = {
				...bannerList.value[0]
			};
			return;
		}
		bannerList.value = [];
		heroBanner.value = {
			...DEFAULT_HERO
		};
	};

	const loadHomeConfig = async () => {
		try {
			const config = await fetchHomeConfig();
			if (config?.xhs) {
				socialXhs.value = {
					...socialXhs.value,
					...config.xhs,
					qrUrl: resolveMediaUrl(config.xhs.qrUrl || '', '')
				};
				xhsQrUrl.value = socialXhs.value.qrUrl || '';
			}
			if (config?.community) socialCommunity.value = {
				...socialCommunity.value,
				...config.community
			};
		} catch (e) {
			/* ignore */ }
	};

	const loadStoresByCity = async (cityId, options = {}) => {
		const {
			showLoading = false
		} = options;
		if (!cityId) {
			mapStores([]);
			return;
		}
		if (showLoading) {
			uni.showLoading({
				title: '切换城市中',
				mask: true
			});
		}
		try {
			const storeData = await fetchStores(cityId);
			mapStores(Array.isArray(storeData) ? storeData : []);
		} finally {
			if (showLoading) {
				uni.hideLoading();
			}
		}
	};

	const loadCities = async (options = {}) => {
		const {
			showPicker = false
		} = options;
		cityPickerLoading.value = true;
		if (showPicker) {
			uni.showLoading({
				title: '加载城市中',
				mask: true
			});
		}
		try {
			const cities = normalizeCities(await fetchCities());
			cityList.value = cities;
			if (!cities.length) {
				uni.showToast({
					title: '暂无开放城市',
					icon: 'none'
				});
				return cities;
			}
			syncCurrentCity(cities);
			if (showPicker) {
				cityPickerShow.value = true;
			}
			return cities;
		} catch (e) {
			if (showPicker && cityList.value.length) {
				cityPickerShow.value = true;
				return cityList.value;
			}
			throw e;
		} finally {
			cityPickerLoading.value = false;
			if (showPicker) {
				uni.hideLoading();
			}
		}
	};

	const loadHomeData = async (options = {}) => {
		const {
			cityId,
			refreshBanner = true
		} = options;
		pageLoading.value = true;
		loadError.value = false;
		try {
			await Promise.all([
				loadCities(),
				loadHomeConfig()
			]);
			const targetCityId = cityId || currentCityId.value;
			if (targetCityId) {
				await loadStoresByCity(targetCityId);
			}
			if (refreshBanner) {
				await loadBanners();
			}
		} catch (e) {
			loadError.value = true;
		} finally {
			pageLoading.value = false;
		}
	};

	const retryLoad = () => loadHomeData({
		cityId: currentCityId.value,
		refreshBanner: true
	});

	onLoad(option => {
		if (option.city) {
			currentCity.value = option.city;
		}
		if (option.cityId) {
			currentCityId.value = Number(option.cityId);
		}
		loadHomeData();
	});

	onShow(() => {
		restoreHomeTitle();
	});

	onPullDownRefresh(async () => {
		await loadHomeData({
			cityId: currentCityId.value,
			refreshBanner: true
		});
		uni.showToast({
			title: '已更新',
			icon: 'none'
		});
		uni.stopPullDownRefresh();
	});

	const openCityPicker = async () => {
		await loadCities({
			showPicker: true
		});
	};

	const selectCity = async city => {
		if (!city?.id || city.id === currentCityId.value) {
			cityPickerShow.value = false;
			return;
		}
		cityPickerShow.value = false;
		currentCity.value = city.name;
		currentCityId.value = city.id;
		uni.setStorageSync(HOME_CITY_KEY, city.id);
		try {
			await loadStoresByCity(city.id, {
				showLoading: true
			});
			await loadBanners();
		} catch (e) {
			/* request.js 已 Toast */
		}
	};

	const goTo = url => {
		openAppLink(url);
	};

	const switchTab = url => {
		openAppLink(url);
	};

	const openHero = () => {
		openAppLink(heroBanner.value.jumpUrl || DEFAULT_HERO.jumpUrl);
	};

	const onBannerChange = e => {
		bannerIndex.value = e.detail?.current || 0;
		heroBanner.value = {
			...bannerList.value[bannerIndex.value]
		};
	};

	const openBanner = banner => {
		if (banner?.id) {
			trackBannerClick(banner.id).catch(() => {});
		}
		openAppLink(banner?.jumpUrl || DEFAULT_HERO.jumpUrl);
	};

	const openSocial = entry => {
		openAppLink(entry?.jumpUrl || '/packageA/wecom/index');
	};

	const openXhsQr = () => {
		xhsQrUrl.value = resolveMediaUrl(socialXhs.value?.qrUrl || '', '');
		if (!xhsQrUrl.value) {
			uni.showToast({
				title: '暂未配置小红书二维码',
				icon: 'none'
			});
			return;
		}
		xhsQrShow.value = true;
	};

	const closeXhsQr = () => {
		xhsQrShow.value = false;
		restoreHomeTitle();
	};

	const previewXhsQr = () => {
		if (!xhsQrUrl.value) return;
		const url = xhsQrUrl.value;
		// 先关弹框，避免预览层被 popup 遮住
		xhsQrShow.value = false;
		restoreHomeTitle();
		setTimeout(() => {
			uni.previewImage({
				urls: [url],
				current: url,
				complete: () => {
					restoreHomeTitle();
					// #ifdef H5
					let n = 0;
					const timer = setInterval(() => {
						const title = String(document?.title || '').trim();
						if (!title || title === 'IM聊天' || title === '图片') {
							document.title = HOME_TITLE;
						}
						if (++n >= 40) clearInterval(timer);
					}, 300);
					// #endif
				}
			});
		}, 50);
	};

	const openEventStore = () => {
		if (eventStore.value.id) {
			openStore(eventStore.value);
			return;
		}
		if (allStores.value.length) {
			openStore(allStores.value[0]);
			return;
		}
		uni.showToast({
			title: '当前城市暂无门店',
			icon: 'none'
		});
	};

	const openStore = store => {
		const storeId = store?.id || store;
		const storeName = store?.name || '';
		uni.navigateTo({
			url: `/packageA/store/list?storeId=${storeId}${storeName ? `&storeName=${encodeURIComponent(storeName)}` : ''}`
		});
	};
</script>

<style lang="scss">
	.pet-shell {
		margin-top: 20rpx;
	}

	.pet-pill {
		background-color: #ffffff;
		color: #5D4037;
		padding: 10rpx 30rpx;
	}

	.home-page {
		padding-bottom: 30rpx;
	}

	.home-heading {
		padding: 8rpx 4rpx 18rpx;
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
	}



	.home-sub-title {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #a7917c;
	}

	.city-chip {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 6rpx 20rpx;
		border-radius: 999rpx;
		background: #F5EDE0;
		border: 2rpx solid #F5EDE0;
		font-size: 24rpx;
		color: #8b613d;

	}

	.city-panel {
		width: 100%;
		box-sizing: border-box;
		padding: 28rpx 28rpx 36rpx;
		background: #fffdf9;
		border-top-left-radius: 32rpx;
		border-top-right-radius: 32rpx;
	}

	.city-panel-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 10rpx;
	}

	.city-panel-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #8b613d;
	}

	.city-panel-close {
		padding: 8rpx 16rpx;
	}

	.city-panel-sub {
		font-size: 24rpx;
		color: #9f8a77;
		margin-bottom: 20rpx;
	}

	.city-panel-scroll {
		max-height: 720rpx;
	}

	.city-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 18rpx;
	}

	.city-grid-item {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 76rpx;
		padding: 0 18rpx;
		border-radius: 20rpx;
		background: #f6eee2;
		color: #6d4c38;
		font-size: 26rpx;
		border: 2rpx solid transparent;
	}

	.city-grid-item.is-active {
		background: #c79662;
		color: #ffffff;
		border-color: #8b613d;
		box-shadow: 0 10rpx 22rpx rgba(199, 150, 98, 0.24);
		font-weight: 700;
	}

	.city-panel-empty {
		padding: 60rpx 0;
		text-align: center;
		color: #9f8a77;
		font-size: 26rpx;
	}

	.hero-card {
		position: relative;
		display: flex;
		padding: 0;
		overflow: hidden;
		height: 396rpx;
		min-height: 396rpx;
		border: 0;
		border-radius: 28rpx;
		box-shadow: none;
		background: transparent;
	}

	.hero-card-wrap {
		position: relative;
		margin-bottom: 0;
	}

	.hero-watermark {
		position: absolute;
		left: 158rpx;
		top: 88rpx;
		font-size: 72rpx;
		font-weight: 700;
		color: rgba(199, 150, 98, 0.1);
		letter-spacing: 8rpx;
		transform: rotate(-18deg);
	}

	.hero-copy {
		flex: 1;
		padding: 40rpx 34rpx;
		background: linear-gradient(135deg, #f8d6d8, #f6e8df);
		display: flex;
		flex-direction: column;
		justify-content: center;
		gap: 16rpx;
	}

	.hero-tag {
		font-size: 28rpx;
		line-height: 1.7;
		color: #7b5d48;
	}

	.hero-image {
		width: 100%;
		height: 396rpx;
	}

	.hero-image image {
		width: 100%;
		height: 100%;
		display: block;
		/* 与容器同为 16:9，scaleToFill 不会裁切主体 */
	}

	.section-title-left {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.section-icon {
		width: 35rpx;
		height: 35rpx;
	}

	.store-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 20rpx;
	}

	.store-empty {
		grid-column: 1 / -1;
		text-align: center;
		padding: 48rpx 24rpx;
		font-size: 26rpx;
	}

	.store-item {
		height: 196rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 18rpx;
		border-radius: 30rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 250, 243, 0.96));
	}

	.store-icon {
		width: 84rpx;
		height: 84rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: inset 0 -10rpx 18rpx rgba(255, 255, 255, 0.26);
	}

	.store-name {
		font-size: 32rpx;
		font-weight: 700;
	}

	.store-filter-row {
		margin-top: 20rpx;
	}

	.event-card {
		margin-top: 28rpx;
		padding-top: 18rpx;
		position: relative;
	}

	.event-card::after {
		content: '';
		position: absolute;
		left: 24rpx;
		right: 24rpx;
		bottom: 18rpx;
		height: 2rpx;
		background: linear-gradient(90deg, transparent, rgba(223, 191, 151, 0.8), transparent);
	}

	.event-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 20rpx;
	}

	.event-badge {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 44rpx;
		padding: 0 18rpx;
		border-radius: 999rpx;
		background: #f2d26d;
		font-size: 22rpx;
		font-weight: 700;
		color: #78583b;
	}

	.event-flag {
		font-size: 22rpx;
		color: #9a8d84;
	}

	.event-image {
		height: 260rpx;
		margin-bottom: 20rpx;
		border-radius: 26rpx;
	}

	.event-copy {
		font-size: 32rpx;
		line-height: 1.7;
		color: #6d4c38;
	}

	.social-entry-row {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 20rpx;
		margin-top: 24rpx;
	}

	.social-entry {
		padding: 28rpx 24rpx;
		min-height: 114rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;
		position: relative;
	}

	.social-entry::before {
		content: '';
		position: absolute;
		left: 0;
		top: 0;
		width: 100%;
		height: 100%;
		opacity: 0.2;
		border-radius: inherit;
	}

	.social-entry.xhs::before {
		background: linear-gradient(135deg, #ffd6d9, transparent 70%);
	}

	.social-entry.douyin::before {
		background: linear-gradient(135deg, #d9efe7, transparent 70%);
	}

	.xhs-qr-panel {
		width: 580rpx;
		padding: 40rpx 36rpx 36rpx;
		border-radius: 36rpx;
		overflow: hidden;
		background: linear-gradient(180deg, #fffdf9 0%, #faf3ea 100%);
		border: 2rpx solid rgba(236, 215, 191, 0.85);
		box-shadow: 0 24rpx 48rpx rgba(109, 76, 56, 0.18);
		box-sizing: border-box;
	}

	.xhs-qr-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.xhs-qr-title {
		flex: 1;
		min-width: 0;
		font-size: 34rpx;
		font-weight: 700;
		color: #6d4c38;
		line-height: 1.3;
	}

	.xhs-qr-close {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: rgba(245, 235, 224, 0.95);
		border: 2rpx solid rgba(236, 215, 191, 0.8);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.xhs-qr-sub {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #9f8a77;
		line-height: 1.5;
	}

	.xhs-qr-box {
		margin-top: 32rpx;
		padding: 28rpx;
		border-radius: 28rpx;
		background: #ffffff;
		border: 2rpx solid rgba(236, 215, 191, 0.75);
		box-shadow: inset 0 0 0 8rpx #faf3ea;
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 380rpx;
	}

	.xhs-qr-img {
		width: 320rpx;
		height: 320rpx;
		border-radius: 20rpx;
		background: #fff;
	}

	.xhs-qr-empty {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 280rpx;
	}

	.xhs-qr-empty-text {
		font-size: 26rpx;
		color: #9f8a77;
	}

	.xhs-qr-hint {
		display: block;
		margin-top: 18rpx;
		text-align: center;
		font-size: 22rpx;
		color: #b39a84;
	}

	.xhs-qr-btn {
		margin-top: 28rpx;
		height: 88rpx;
		border-radius: 999rpx;
		background: linear-gradient(180deg, #c79662 0%, #b88955 100%);
		color: #fff;
		font-size: 30rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 14rpx 28rpx rgba(184, 137, 85, 0.28);
	}

	.home-foot-space {
		height: 16rpx;
	}

	.social-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		margin-bottom: 10rpx;
	}

	.home-skeleton {
		padding-top: 20rpx;
	}

	.skeleton-block {
		background: linear-gradient(90deg, #f3e8dc 25%, #faf4ec 50%, #f3e8dc 75%);
		background-size: 200% 100%;
		animation: skeleton-shimmer 1.2s ease-in-out infinite;
		border-radius: 24rpx;
	}

	.hero-skeleton {
		height: 238rpx;
		margin-top: 38rpx;
	}

	.title-skeleton {
		height: 40rpx;
		width: 40%;
		margin: 28rpx 0 20rpx;
	}

	.skeleton-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 20rpx;
	}

	.store-skeleton {
		height: 196rpx;
	}

	.home-error {
		padding: 120rpx 40rpx;
		text-align: center;
	}

	.home-error-text {
		color: #b58d6a;
		font-size: 28rpx;
	}

	.hero-swiper {
		width: 100%;
		height: 396rpx;
		margin-bottom: 0;
		border-radius: 28rpx;
		overflow: hidden;
	}

	.home-page .pet-section-title {
		margin-top: 16rpx;
		margin-bottom: 14rpx;
	}

	.hero-dots {
		display: flex;
		justify-content: center;
		gap: 10rpx;
		margin-top: 12rpx;
	}

	.hero-dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: #dcc9b5;
	}

	.hero-dot.active {
		width: 28rpx;
		border-radius: 999rpx;
		background: #c99763;
	}

	@keyframes skeleton-shimmer {
		0% {
			background-position: 200% 0;
		}

		100% {
			background-position: -200% 0;
		}
	}
</style>
