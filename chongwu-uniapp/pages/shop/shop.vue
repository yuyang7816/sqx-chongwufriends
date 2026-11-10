<template>
	<view class="pet-page">
		<view class="pet-shell">
			<view v-if="shopBanners.length" class="shop-banner-wrap">
				<swiper class="shop-banner-swiper" :autoplay="shopBanners.length > 1" :interval="4000">
					<swiper-item v-for="item in shopBanners" :key="item.id" @tap="openBanner(item)">
						<image :src="item.image || '/static/logo.png'" mode="aspectFill"></image>
					</swiper-item>
				</swiper>
			</view>

		<up-sticky offset-top="0" bg-color="#fbf4eb" :z-index="20">
			<view class="tab-sticky">
				<scroll-view scroll-x class="tab-scroll" show-scrollbar="false">
					<view class="tab-scroll-inner">
						<view v-for="item in tabs" :key="item.id" class="shop-tab"
							:class="{ active: currentTabId === item.id }" @tap="switchTab(item)">
							{{ item.name }}
						</view>
					</view>
				</scroll-view>
			</view>
		</up-sticky>

			<view class="exchange-card" @tap="goTo('/packageA/exchange/list')">
				<view class="exchange-left">
					<view class="exchange-icon">
						<up-icon name="coupon" size="20" color="#ffffff"></up-icon>
					</view>
					<view class="exchange-copy">
						<text class="exchange-title">积分兑换区</text>
						<text class="exchange-desc">当前可用：{{ formatPoints(user.points) }} 花瓣</text>
					</view>
				</view>
				<up-icon name="arrow-right" size="16" color="#cfc3b6"></up-icon>
			</view>

			<view v-if="loading" class="pet-muted" style="text-align:center;padding:40rpx 0;">加载中...</view>
			<view v-else-if="!visibleProducts.length" class="pet-muted" style="text-align:center;padding:40rpx 0;">
				{{ loadError || '暂无商品' }}
			</view>
			<view v-else class="pet-grid-2 product-grid">
				<view v-for="product in visibleProducts" :key="product.id" class="pet-card product-card"
					@tap="goTo(`/packageA/product/detail?id=${product.id}`)">
					<view class="pet-thumb product-thumb">
						<image :src="product.coverUrl || '/static/logo.png'" mode="aspectFill"></image>
					</view>
					<text class="product-name">{{ product.name }}</text>
					<text class="product-subtitle">{{ product.description || '门店同款 · 宠友热推' }}</text>
				<view class="product-foot">
					<view class="product-price">
						<text class="product-price__symbol">¥</text>
						<text class="product-price__num">{{ formatPrice(product.price) }}</text>
					</view>
				</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import {
		computed,
		ref,
		onMounted
	} from 'vue';
	import {
		onShow
	} from '@dcloudio/uni-app';
	import {
		fetchMallCategories,
		fetchMallPage
	} from '@/common/api/mall.js';
	import {
		fetchUserAsset
	} from '@/common/api/user-pet.js';
	import {
		fetchBanners
	} from '@/common/api/home.js';
	import { resolveMediaUrl } from '@/common/media.js';

	const user = ref({ points: 0 });
	const tabs = ref([{ id: 0, name: '全部分类' }]);
	const currentTabId = ref(0);
	const products = ref([]);
	const shopBanners = ref([]);
	const loading = ref(false);
	const loadError = ref('');

	const loadAsset = async () => {
		const token = uni.getStorageSync('token');
		if (!token) return;
		try {
			const asset = await fetchUserAsset();
			if (asset) user.value.points = asset.points ?? 0;
		} catch (e) {
			/* ignore */
		}
	};

	onMounted(async () => {
		loading.value = true;
		loadError.value = '';
		try {
			const cats = await fetchMallCategories();
			if (cats?.length) {
				tabs.value = cats;
				currentTabId.value = cats[0].id;
			}
		} catch (e) {
			loadError.value = e?.message || '分类加载失败';
			uni.showToast({ title: loadError.value, icon: 'none' });
		}
		try {
			const banners = await fetchBanners('shop');
			shopBanners.value = (banners || []).map((banner, index) => ({
				id: banner.id || index,
				title: banner.name || '精选好物',
				subtitle: banner.subTitle || banner.subtitle || '精选好物',
				image: resolveMediaUrl(banner.imageUrl),
				jumpUrl: banner.jumpUrl || ''
			}));
		} catch (e) {
			shopBanners.value = [];
		}
		await loadAsset();
		await loadProducts();
		loading.value = false;
	});

	onShow(() => {
		loadAsset();
	});

	const loadProducts = async () => {
		try {
			const params = { page: 1, limit: 20 };
			if (currentTabId.value) {
				params.categoryId = currentTabId.value;
			}
			const res = await fetchMallPage(params);
			const list = res?.records || res?.list || [];
			products.value = list.map(p => ({
				id: p.id,
				name: p.name,
				price: p.price ?? p.activityPrice ?? 0,
				originalPrice: p.originalPrice,
				point: p.pointPrice ?? p.point ?? 0,
				coverUrl: resolveMediaUrl(p.coverUrl),
				description: p.description || '',
				category: p.category || ''
			}));
			loadError.value = '';
		} catch (e) {
			products.value = [];
			loadError.value = e?.message || '商品加载失败';
			uni.showToast({ title: loadError.value, icon: 'none' });
		}
	};

	const formatPrice = value => {
		const num = Number(value);
		return Number.isFinite(num) ? num.toFixed(2) : '0.00';
	};

	const formatPoints = value => {
		const num = Number(value);
		if (!Number.isFinite(num)) return '0';
		return num.toLocaleString('en-US');
	};

	const switchTab = tab => {
		currentTabId.value = tab.id;
		loadProducts();
	};

	const visibleProducts = computed(() => products.value);

	const openBanner = (banner) => {
		const url = banner?.jumpUrl;
		if (!url) return;
		if (url.startsWith('/pages/')) {
			uni.switchTab({
				url
			});
			return;
		}
		goTo(url);
	};

	const goTo = url => {
		uni.navigateTo({
			url
		});
	};
</script>

<style lang="scss">
	.page-title {
		display: block;
		font-size: 52rpx;
		font-weight: 700;
		color: #c39668;
		margin: 6rpx 0 16rpx;
	}

	.shop-banner-wrap {
		margin-top: 4rpx;
		margin-bottom: 4rpx;
		border-radius: 28rpx;
		overflow: hidden;
	}

	.shop-banner-swiper {
		width: 100%;
		height: 220rpx;
		border-radius: 28rpx;
		overflow: hidden;

		image {
			width: 100%;
			height: 100%;
			border-radius: 28rpx;
			object-fit: cover;
			object-position: center 30%;
		}
	}

	.shop-banner-swiper :deep(.uni-swiper-wrapper),
	.shop-banner-swiper :deep(.uni-swiper-slide),
	.shop-banner-swiper :deep(swiper-item) {
		border-radius: 28rpx;
		overflow: hidden;
	}

	.shop-banner-swiper :deep(.uni-swiper-dot) {
		bottom: 12rpx;
	}

	.shop-banner {
		display: flex;
		align-items: center;
		justify-content: space-between;
		background: linear-gradient(135deg, #f5d57f, #ffe7b5);
		margin-top: 8rpx;
		padding: 34rpx 28rpx;
		border-radius: 34rpx;
	}

	.banner-image {
		width: 180rpx;
		height: 180rpx;
		border-radius: 24rpx;
		overflow: hidden;
	}

	.banner-image image {
		width: 100%;
		height: 100%;
	}

	.tab-sticky {
		background: #fbf4eb;
		box-shadow: 0 6rpx 14rpx rgba(163, 120, 79, 0.06);
	}

	.tab-scroll {
		white-space: nowrap;
		width: 100%;
		overflow: hidden;
		padding: 14rpx 0 16rpx;
	}

	.tab-scroll-inner {
		display: inline-flex;
		align-items: center;
		gap: 34rpx;
		padding-right: 28rpx;
	}

	.shop-tab {
		position: relative;
		font-size: 26rpx;
		line-height: 40rpx;
		color: #a9a5a1;
		padding: 0 0 12rpx;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		text-align: center;
		flex-shrink: 0;
		transition: color 0.2s ease;
	}

	.shop-tab.active {
		color: #c89a68;
		font-weight: 600;
	}

	.exchange-card {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
		margin: 8rpx 0 28rpx;
		padding: 28rpx 26rpx;
		border-radius: 28rpx;
		background: #ffffff;
		box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.06);
	}

	.exchange-left {
		display: flex;
		align-items: center;
		gap: 20rpx;
		min-width: 0;
		flex: 1;
	}

	.exchange-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
		background: #ffdde2;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.exchange-copy {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		min-width: 0;
	}

	.exchange-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3b32;
		line-height: 1.3;
	}

	.exchange-desc {
		font-size: 24rpx;
		color: #9a9088;
		line-height: 1.4;
	}

	.product-grid {
		padding-bottom: 30rpx;
	}

	.product-card {
		padding: 0 0 20rpx;
		overflow: hidden;
		border-radius: 30rpx;
	}

	.product-thumb {
		height: 280rpx;
		border-radius: 28rpx 28rpx 0 0;
	}

	.product-name {
		display: block;
		padding: 18rpx 20rpx 8rpx;
		font-size: 30rpx;
		font-weight: 700;
		line-height: 1.5;
	}

	.product-subtitle {
		display: block;
		padding: 0 20rpx 10rpx;
		font-size: 22rpx;
		color: #aa9885;
	}

	.product-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 20rpx;
	}

	.product-price {
		display: inline-flex;
		align-items: baseline;
		color: #ce8f4c;
	}

	.product-price__symbol {
		font-size: 26rpx;
		font-weight: 700;
		margin-right: 2rpx;
	}

	.product-price__num {
		font-size: 38rpx;
		font-weight: 800;
	}
</style>
