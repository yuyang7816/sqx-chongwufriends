<template>
	<view class="pet-page order-page">
		<view class="pet-shell">
			<view class="order-hero">
				<view class="order-hero__icon">🌸</view>
				<text class="order-hero__label">积分</text>
				<text class="order-hero__value">{{ formatPoints(points) }}</text>
			</view>

			<view class="order-tabs">
				<view
					v-for="item in typeTabs"
					:key="item.value"
					class="order-tab"
					:class="{ 'is-active': currentType === item.value }"
					@tap="switchType(item.value)"
				>
					<text class="order-tab__text">{{ item.label }}</text>
				</view>
			</view>

			<view class="order-subtabs">
				<view
					v-for="item in statusTabs"
					:key="item.value"
					class="order-subtab"
					:class="{ 'is-active': currentStatus === item.value }"
					@tap="switchStatus(item.value)"
				>
					<text v-if="item.icon" class="order-subtab__icon">{{ item.icon }}</text>
					<text class="order-subtab__text">{{ item.label }}</text>
				</view>
			</view>

			<view v-if="loading && !recordList.length" class="order-state">
				<text class="order-state__text">加载中...</text>
			</view>
			<view v-else-if="!recordList.length" class="order-state">
				<text class="order-state__title">暂无订单</text>
				<text class="order-state__text">{{ loadError || '去商城看看喜欢的商品吧～' }}</text>
			</view>
			<view v-else class="order-list">
				<view v-for="item in recordList" :key="item.id" class="order-card">
					<view class="order-card__head">
						<text class="order-card__date">{{ item.date }}</text>
						<view class="order-status" :class="statusInfo(item.status).cls">
							<text class="order-status__icon">{{ statusInfo(item.status).icon }}</text>
							<text class="order-status__text">{{ statusInfo(item.status).label }}</text>
						</view>
					</view>

					<view class="order-card__body">
						<view class="order-card__cover">
							<image v-if="item.coverUrl" class="order-card__img" :src="item.coverUrl" mode="aspectFill"></image>
							<view v-else class="order-card__fallback">🎁</view>
						</view>
						<view class="order-card__info">
							<view class="order-card__name-row">
								<text class="order-card__name">{{ item.title }}</text>
								<text class="order-type-tag" :class="item.isExchange ? 'is-exchange' : 'is-product'">
									{{ item.isExchange ? '积分兑换' : '商品订单' }}
								</text>
							</view>
							<text class="order-card__qty">×{{ item.quantity || 1 }}</text>
						</view>
					</view>

					<view class="order-card__foot">
						<view v-if="item.isExchange" class="order-cost is-points">
							<text class="order-cost__icon">🌸</text>
							<text class="order-cost__value">{{ formatPoints(item.points) }}</text>
							<text class="order-cost__unit">积分</text>
						</view>
						<view v-else class="order-cost is-cash">
							<text class="order-cost__symbol">¥</text>
							<text class="order-cost__value">{{ formatPrice(item.payAmount) }}</text>
						</view>
						<text class="order-card__no">单号 {{ item.orderNo || '-' }}</text>
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="order-tip">加载更多...</view>
			<view v-else-if="finished && recordList.length" class="order-tip">没有更多了</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchMyOrders } from '@/common/api/mall.js';
import { fetchUserAsset } from '@/common/api/user-pet.js';
import { resolveMediaUrl } from '@/common/media.js';

const typeTabs = [
	{ value: 'all', label: '全部' },
	{ value: 'product', label: '商品订单' },
	{ value: 'exchange', label: '积分兑换' }
];
const statusTabs = [
	{ value: 'all', label: '全部', icon: '' },
	{ value: 'pending', label: '进行中', icon: '⏳' },
	{ value: 'done', label: '已完成', icon: '✓' }
];
const STATUS_MAP = { all: 'all', pending: 'pending', done: 'done' };

const currentType = ref('all');
const currentStatus = ref('all');
const recordList = ref([]);
const points = ref(0);
const loading = ref(false);
const loadingMore = ref(false);
const finished = ref(false);
const page = ref(1);
const loadError = ref('');

const mapRecord = (item) => {
	const isExchange = item.bizType === 4 || item.payType === 2;
	return {
		id: item.id,
		orderNo: item.orderNo || '',
		date: item.createTime ? String(item.createTime).slice(0, 10) : '',
		title: item.spuName || '商品订单',
		coverUrl: item.coverUrl ? resolveMediaUrl(item.coverUrl) : '',
		quantity: item.quantity ?? 1,
		points: item.pointAmount ?? 0,
		payAmount: item.payAmount ?? 0,
		payType: item.payType,
		bizType: item.bizType,
		status: item.status,
		isExchange
	};
};

const loadAsset = async () => {
	try {
		const asset = await fetchUserAsset();
		if (asset?.points != null) points.value = asset.points;
	} catch (e) {
		/* ignore */
	}
};

const loadData = async (reset = true) => {
	if (reset) {
		page.value = 1;
		finished.value = false;
	}
	if (finished.value && !reset) return;
	if (loading.value || loadingMore.value) return;
	if (reset) loading.value = true;
	else loadingMore.value = true;
	loadError.value = '';
	try {
		const res = await fetchMyOrders({
			page: page.value,
			limit: 20,
			orderType: currentType.value,
			exchangeStatus: STATUS_MAP[currentStatus.value]
		});
		const list = (res?.records || []).map(mapRecord);
		recordList.value = reset ? list : recordList.value.concat(list);
		const total = Number(res?.total || 0);
		finished.value = recordList.value.length >= total || list.length < 20;
		if (!finished.value) page.value += 1;
	} catch (e) {
		if (reset) recordList.value = [];
		loadError.value = e?.msg || e?.message || '订单加载失败';
		uni.showToast({ title: loadError.value, icon: 'none' });
	} finally {
		loading.value = false;
		loadingMore.value = false;
	}
};

const switchType = value => {
	if (currentType.value === value) return;
	currentType.value = value;
	loadData(true);
};

const switchStatus = value => {
	if (currentStatus.value === value) return;
	currentStatus.value = value;
	loadData(true);
};

const statusInfo = (status) => {
	const s = Number(status);
	if (s === 2) return { cls: 'is-success', icon: '✓', label: '已完成' };
	if (s === 3) return { cls: 'is-danger', icon: '✕', label: '已取消' };
	if (s === 4) return { cls: 'is-danger', icon: '↩', label: '已退款' };
	return { cls: 'is-warn', icon: '⏳', label: '进行中' };
};

const formatPrice = value => {
	const num = Number(value);
	return Number.isFinite(num) ? num.toFixed(2) : '0.00';
};

const formatPoints = (value) => {
	const num = Number(value);
	if (!Number.isFinite(num) || num < 1000) return String(num ?? 0);
	const k = num / 1000;
	const rounded = Math.round(k * 10) / 10;
	return `${rounded % 1 === 0 ? rounded.toFixed(0) : rounded.toFixed(1)}k`;
};

onShow(async () => {
	await loadAsset();
	await loadData(true);
});

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadAsset(), loadData(true)]);
	} finally {
		uni.stopPullDownRefresh();
	}
});

onReachBottom(() => {
	if (!loading.value && !loadingMore.value) {
		loadData(false);
	}
});
</script>

<style lang="scss">
.order-page {
	--order-brown: #c79662;
	--order-brown-deep: #8b5e3c;
	--order-border: rgba(236, 215, 191, 0.55);
}

.order-hero {
	background: var(--pet-panel, #fffdf9);
	border-radius: 32rpx;
	box-shadow: var(--pet-shadow);
	border: 2rpx solid var(--order-border);
	padding: 36rpx 24rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: 8rpx;
}

.order-hero__icon {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: #fce7ee;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 44rpx;
	margin-bottom: 14rpx;
}

.order-hero__label {
	font-size: 24rpx;
	color: #a39382;
}

.order-hero__value {
	font-size: 64rpx;
	font-weight: 800;
	color: var(--order-brown-deep);
	line-height: 1.1;
	margin-top: 4rpx;
}

.order-tabs {
	display: flex;
	gap: 18rpx;
	margin: 24rpx 0 16rpx;
}

.order-tab {
	flex: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	height: 64rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	border: 2rpx solid #e8d4ba;
	background: transparent;
	color: var(--order-brown);
	font-size: 26rpx;
}

.order-tab.is-active {
	background: var(--order-brown);
	border-color: var(--order-brown);
	color: #fff;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(199, 150, 98, 0.24);
}

.order-subtabs {
	display: flex;
	gap: 14rpx;
	margin-bottom: 20rpx;
}

.order-subtab {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	height: 52rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: #f6eee2;
	color: #a78f78;
	font-size: 24rpx;
}

.order-subtab.is-active {
	background: #fff3e0;
	color: var(--order-brown-deep);
	font-weight: 700;
}

.order-subtab__icon {
	font-size: 22rpx;
}

.order-state {
	padding: 60rpx 0;
	text-align: center;
}

.order-state__title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: var(--order-brown-deep);
	margin-bottom: 10rpx;
}

.order-state__text {
	display: block;
	font-size: 24rpx;
	color: #a39382;
}

.order-tip {
	text-align: center;
	padding: 20rpx 0;
	font-size: 24rpx;
	color: #a39382;
}

.order-list > view + view {
	margin-top: 18rpx;
}

.order-card {
	background: var(--pet-panel, #fffdf9);
	border-radius: 28rpx;
	box-shadow: var(--pet-shadow);
	border: 2rpx solid var(--order-border);
	padding: 24rpx;
}

.order-card__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.order-card__date {
	font-size: 24rpx;
	color: #a39382;
}

.order-status {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	min-height: 40rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	font-weight: 600;
}

.order-status.is-success { background: #dbf0dc; color: #4f8d5c; }
.order-status.is-warn { background: #fce6aa; color: #9a7c31; }
.order-status.is-danger { background: #f9d7db; color: #be6b76; }

.order-status__icon { font-size: 22rpx; }

.order-card__body {
	display: flex;
	align-items: center;
	gap: 18rpx;
	margin-top: 18rpx;
}

.order-card__cover {
	width: 96rpx;
	height: 96rpx;
	border-radius: 20rpx;
	background: #f6eee2;
	overflow: hidden;
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
}

.order-card__img { width: 100%; height: 100%; }
.order-card__fallback { font-size: 40rpx; }

.order-card__info {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.order-card__name-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.order-card__name {
	flex: 1;
	min-width: 0;
	font-size: 30rpx;
	font-weight: 700;
	color: #3a2c20;
	line-height: 1.35;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.order-type-tag {
	flex-shrink: 0;
	display: inline-flex;
	align-items: center;
	height: 36rpx;
	padding: 0 14rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 700;
}

.order-type-tag.is-exchange { background: #fce8d2; color: #ce8f4c; }
.order-type-tag.is-product { background: #e7f0e8; color: #4f8d5c; }

.order-card__qty {
	font-size: 24rpx;
	color: #9f8a77;
}

.order-card__foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-top: 18rpx;
	gap: 12rpx;
}

.order-cost {
	display: inline-flex;
	align-items: baseline;
	gap: 4rpx;
}

.order-cost.is-points .order-cost__value {
	font-size: 32rpx;
	font-weight: 800;
	color: #ce8f4c;
}

.order-cost.is-points .order-cost__icon { font-size: 24rpx; }
.order-cost.is-points .order-cost__unit { font-size: 22rpx; color: #ce8f4c; }

.order-cost.is-cash .order-cost__symbol {
	font-size: 26rpx;
	font-weight: 700;
	color: #ce8f4c;
}

.order-cost.is-cash .order-cost__value {
	font-size: 38rpx;
	font-weight: 800;
	color: #ce8f4c;
}

.order-card__no {
	flex-shrink: 0;
	font-size: 20rpx;
	color: #b8a594;
	max-width: 240rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
</style>
