<template>
	<view class="pet-page coupon-page">
		<view class="pet-shell">
			<!-- 顶部汇总 -->
			<view class="coupon-summary">
				<view class="coupon-summary__item">
					<text class="coupon-summary__num is-active">{{ couponStats['可用'] }}</text>
					<text class="coupon-summary__label">可用</text>
				</view>
				<view class="coupon-summary__divider"></view>
				<view class="coupon-summary__item">
					<text class="coupon-summary__num">{{ couponStats['已用'] }}</text>
					<text class="coupon-summary__label">已用</text>
				</view>
				<view class="coupon-summary__divider"></view>
				<view class="coupon-summary__item">
					<text class="coupon-summary__num">{{ couponStats['已过'] }}</text>
					<text class="coupon-summary__label">已过期</text>
				</view>
			</view>

			<!-- 筛选 tabs -->
			<view class="coupon-tabs">
				<view
					v-for="item in filters"
					:key="item.value"
					class="coupon-tab"
					:class="{ 'is-active': currentFilter === item.value }"
					@tap="currentFilter = item.value"
				>
					<text class="coupon-tab__icon">{{ item.icon }}</text>
					<text class="coupon-tab__text">{{ item.label }}</text>
				</view>
			</view>

			<!-- 列表 -->
			<view v-if="loading" class="coupon-state">加载中...</view>
			<view v-else-if="!visibleCoupons.length" class="coupon-state">
				{{ loadError || '暂无优惠券' }}
			</view>
			<view v-else class="coupon-list">
				<view
					v-for="item in visibleCoupons"
					:key="item.id"
					class="coupon-card"
					:class="{ 'is-disabled': item.status !== '可用' }"
					@tap="handleCoupon(item)"
				>
					<view class="coupon-card__value">
						<text class="coupon-card__amount">{{ item.amount }}</text>
					</view>
					<view class="coupon-card__main">
						<text class="coupon-card__tag" :class="item.type === '积分兑换' ? 'is-exchange' : 'is-store'">{{ item.type }}</text>
						<text class="coupon-card__title">{{ item.title }}</text>
						<text class="coupon-card__desc">{{ item.desc }}</text>
						<text class="coupon-card__foot">{{ scopeText(item) }} · {{ item.expire }}</text>
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
	watch
} from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { fetchCouponList } from '@/common/api/coupon.js';

const filters = [
	{ value: '可用', label: '可用', icon: '🎯' },
	{ value: '已用', label: '已用', icon: '⏳' },
	{ value: '已过', label: '已过期', icon: '⏳' }
];
const currentFilter = ref('可用');
const couponList = ref([]);
const allCoupons = ref([]);
const loading = ref(false);
const loadError = ref('');

const COUPON_STATUS_MAP = {
	'可用': 'available',
	'已用': 'used',
	'已过': 'expired'
};

const mapCoupon = item => ({
	id: item.id,
	status: item.statusText || item.status || '可用',
	title: item.title || '优惠券',
	amount: item.amount || '',
	desc: item.desc || '',
	expire: item.expire || '',
	type: item.type || '门店优惠'
});

const scopeText = (item) => item.type === '积分兑换' ? '指定门店' : '全场门店';

watch(currentFilter, () => {
	loadData();
});

const loadData = async () => {
	loading.value = true;
	loadError.value = '';
	try {
		const statusKey = COUPON_STATUS_MAP[currentFilter.value];
		const list = await fetchCouponList(statusKey);
		couponList.value = (list || []).map(mapCoupon);
	} catch (e) {
		couponList.value = [];
		loadError.value = e?.msg || e?.message || '券包加载失败';
		uni.showToast({ title: loadError.value, icon: 'none' });
	} finally {
		loading.value = false;
	}
};

const loadAllStats = async () => {
	try {
		const list = await fetchCouponList();
		allCoupons.value = (list || []).map(mapCoupon);
	} catch (e) {
		allCoupons.value = [];
	}
};

onShow(() => {
	loadData();
	loadAllStats();
});

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadData(), loadAllStats()]);
	} finally {
		uni.stopPullDownRefresh();
	}
});

const couponStats = computed(() => ({
	'可用': allCoupons.value.filter(item => item.status === '可用').length,
	'已用': allCoupons.value.filter(item => item.status === '已用').length,
	'已过': allCoupons.value.filter(item => item.status === '已过' || item.status === '已过期').length
}));

const visibleCoupons = computed(() => couponList.value);

const handleCoupon = item => {
	if (item.status !== '可用') {
		uni.showToast({
			title: item.status === '已用' ? '该券已使用' : '该券已过期',
			icon: 'none'
		});
		return;
	}
	// 线下使用券：展示使用说明
	uni.showModal({
		title: item.title,
		content: `${item.type} · ${item.desc}\n${scopeText(item)} · ${item.expire}\n\n到店出示券码即可使用`,
		showCancel: false,
		confirmText: '知道了'
	});
};
</script>

<style lang="scss">
.coupon-page {
	--coupon-brown: #8b5e3c;
	--coupon-brown-soft: #c79662;
	--coupon-orange: #d97a3a;
	--coupon-border: rgba(236, 215, 191, 0.55);
}

/* 汇总 */
.coupon-summary {
	margin-top: 8rpx;
	background: var(--pet-panel, #fffdf9);
	border-radius: 28rpx;
	border: 2rpx solid var(--coupon-border);
	box-shadow: var(--pet-shadow);
	padding: 30rpx 0;
	display: flex;
	align-items: center;
}

.coupon-summary__item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.coupon-summary__num {
	font-size: 56rpx;
	font-weight: 800;
	color: #b8a99a;
	line-height: 1;
}

.coupon-summary__num.is-active {
	color: var(--coupon-brown);
}

.coupon-summary__label {
	font-size: 24rpx;
	color: #a39382;
}

.coupon-summary__divider {
	width: 2rpx;
	height: 60rpx;
	background: #ece3d8;
}

/* tabs */
.coupon-tabs {
	display: flex;
	gap: 18rpx;
	margin: 24rpx 0 20rpx;
}

.coupon-tab {
	flex: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 64rpx;
	border-radius: 999rpx;
	background: #f6eee2;
	color: var(--coupon-brown-soft);
	font-size: 26rpx;
}

.coupon-tab.is-active {
	background: var(--coupon-brown);
	color: #fff;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(139, 94, 60, 0.22);
}

.coupon-tab__icon {
	font-size: 24rpx;
}

/* 状态 */
.coupon-state {
	text-align: center;
	padding: 50rpx 0;
	font-size: 26rpx;
	color: #a39382;
}

/* 列表 */
.coupon-list > view + view {
	margin-top: 18rpx;
}

.coupon-card {
	display: flex;
	align-items: stretch;
	gap: 22rpx;
	padding: 24rpx;
	border-radius: 28rpx;
	background: var(--pet-panel, #fffdf9);
	border: 2rpx solid var(--coupon-border);
	box-shadow: var(--pet-shadow);
}

.coupon-card.is-disabled {
	opacity: 0.6;
}

.coupon-card__value {
	width: 140rpx;
	flex-shrink: 0;
	border-radius: 22rpx;
	background: #faf1e5;
	display: flex;
	align-items: center;
	justify-content: center;
}

.coupon-card__amount {
	font-size: 40rpx;
	font-weight: 800;
	color: var(--coupon-orange);
	text-align: center;
}

.coupon-card__main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.coupon-card__tag {
	align-self: flex-start;
	display: inline-flex;
	align-items: center;
	height: 36rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 700;
}

.coupon-card__tag.is-store {
	background: #fce8d2;
	color: var(--coupon-brown);
}

.coupon-card__tag.is-exchange {
	background: #e7f0e8;
	color: #4f8d5c;
}

.coupon-card__title {
	font-size: 32rpx;
	font-weight: 700;
	color: #3a2c20;
	line-height: 1.3;
}

.coupon-card__desc {
	font-size: 24rpx;
	color: #8a7a68;
}

.coupon-card__foot {
	font-size: 22rpx;
	color: #b8a99a;
}
</style>
