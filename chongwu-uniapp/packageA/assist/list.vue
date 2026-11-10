<template>
	<view class="pet-page assist-page">
		<view class="pet-shell">
			<!-- 顶部双汇总卡 -->
			<view class="assist-summary">
				<view class="assist-summary__card">
					<text class="assist-summary__num">{{ summary.totalCount }}</text>
					<text class="assist-summary__label">累计助力</text>
					<text class="assist-summary__sub">👍 次</text>
				</view>
				<view class="assist-summary__card">
					<text class="assist-summary__num">{{ summary.receivedCount }}</text>
					<text class="assist-summary__label">收获助力</text>
					<text class="assist-summary__sub">❤️ 次</text>
				</view>
			</view>

			<!-- 方向切换 tabs -->
			<view class="assist-tabs">
				<view
					v-for="tab in tabs"
					:key="tab.value"
					class="assist-tab"
					:class="{ 'is-active': currentTab === tab.value }"
					@tap="switchTab(tab.value)"
				>
					<text class="assist-tab__icon">{{ tab.icon }}</text>
					<text class="assist-tab__text">{{ tab.label }}</text>
				</view>
			</view>

			<!-- 列表 -->
			<view v-if="loading && !assistList.length" class="assist-state">加载中...</view>
			<view v-else-if="!assistList.length" class="assist-state">{{ loadError || '暂无助力记录' }}</view>
			<view v-else class="assist-list">
				<view v-for="item in assistList" :key="item.id" class="assist-card">
					<view class="assist-card__icon">👍</view>
					<view class="assist-card__main">
						<text class="assist-card__title">给 {{ item.target }} {{ petIcon(item.petId) }}</text>
						<text class="assist-card__type">{{ item.type }}</text>
						<text class="assist-card__time">{{ item.time }}</text>
					</view>
					<view class="assist-card__status">
						<text class="assist-card__check">✓</text>
						<text class="assist-card__status-text">{{ item.status }}</text>
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="assist-tip">加载更多...</view>
			<view v-else-if="finished && assistList.length" class="assist-tip">没有更多了</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import { fetchAssistPage, fetchAssistSummary } from '@/common/api/assist.js';

const tabs = [
	{ value: 'mine', label: '我助力的', icon: '🎯' },
	{ value: 'received', label: '助力我的', icon: '❤️' }
];
const currentTab = ref('mine');

const assistList = ref([]);
const loading = ref(false);
const loadingMore = ref(false);
const finished = ref(false);
const page = ref(1);
const loadError = ref('');
const summary = ref({ totalCount: 0, receivedCount: 0 });

const petIcon = (petId) => {
	const id = Number(petId || 0);
	return id % 2 === 0 ? '🐕' : '🐈';
};

const mapAssist = item => ({
	id: item.id,
	petId: item.petId,
	target: item.target || item.petName || '',
	type: item.type || item.assistType || '助力',
	time: item.time || '',
	status: item.status || '成功'
});

const loadSummary = async () => {
	try {
		const res = await fetchAssistSummary();
		summary.value = {
			totalCount: Number(res?.totalCount || 0),
			receivedCount: Number(res?.receivedCount || 0)
		};
	} catch (e) {
		// 忽略汇总失败
	}
};

const loadData = async (reset = true) => {
	if (reset) {
		page.value = 1;
		finished.value = false;
		if (!assistList.value.length) assistList.value = [];
	}
	if (finished.value && !reset) return;
	if (loading.value || loadingMore.value) return;
	if (reset) loading.value = true;
	else loadingMore.value = true;
	loadError.value = '';
	try {
		const pageRes = await fetchAssistPage({ page: page.value, limit: 20, direction: currentTab.value });
		const list = (pageRes?.records || []).map(mapAssist);
		assistList.value = reset ? list : assistList.value.concat(list);
		const total = Number(pageRes?.total || 0);
		finished.value = assistList.value.length >= total || list.length < 20;
		if (!finished.value) page.value += 1;
	} catch (e) {
		if (reset) assistList.value = [];
		loadError.value = e?.msg || e?.message || '助力记录加载失败';
		uni.showToast({ title: loadError.value, icon: 'none' });
	} finally {
		loading.value = false;
		loadingMore.value = false;
	}
};

const switchTab = (value) => {
	if (currentTab.value === value) return;
	currentTab.value = value;
	loadData(true);
};

onShow(() => {
	loadSummary();
	loadData(true);
});

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadSummary(), loadData(true)]);
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
.assist-page {
	--assist-brown: #8b5e3c;
	--assist-green: #4f8d5c;
	--assist-border: rgba(236, 215, 191, 0.55);
}

/* 汇总 */
.assist-summary {
	margin-top: 8rpx;
	display: flex;
	gap: 18rpx;
}

.assist-summary__card {
	flex: 1;
	background: var(--pet-panel, #fffdf9);
	border-radius: 28rpx;
	border: 2rpx solid var(--assist-border);
	box-shadow: var(--pet-shadow);
	padding: 30rpx 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.assist-summary__num {
	font-size: 56rpx;
	font-weight: 800;
	color: var(--assist-brown);
	line-height: 1;
}

.assist-summary__label {
	font-size: 26rpx;
	color: #6a5a48;
}

.assist-summary__sub {
	font-size: 22rpx;
	color: #a39382;
}

/* tabs */
.assist-tabs {
	display: flex;
	gap: 18rpx;
	margin: 24rpx 0 20rpx;
}

.assist-tab {
	flex: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 64rpx;
	border-radius: 999rpx;
	background: #f6eee2;
	color: var(--assist-brown);
	font-size: 26rpx;
}

.assist-tab.is-active {
	background: var(--assist-brown);
	color: #fff;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(139, 94, 60, 0.22);
}

.assist-tab__icon {
	font-size: 24rpx;
}

/* 状态 */
.assist-state {
	text-align: center;
	padding: 50rpx 0;
	font-size: 26rpx;
	color: #a39382;
}

.assist-tip {
	text-align: center;
	padding: 20rpx 0;
	font-size: 24rpx;
	color: #b8a99a;
}

/* 列表 */
.assist-list > view + view {
	margin-top: 18rpx;
}

.assist-card {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 24rpx;
	border-radius: 28rpx;
	background: var(--pet-panel, #fffdf9);
	border: 2rpx solid var(--assist-border);
	box-shadow: var(--pet-shadow);
}

.assist-card__icon {
	width: 72rpx;
	height: 72rpx;
	border-radius: 20rpx;
	background: #faf1e5;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 38rpx;
	flex-shrink: 0;
}

.assist-card__main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.assist-card__title {
	font-size: 30rpx;
	font-weight: 700;
	color: #3a2c20;
}

.assist-card__type {
	font-size: 24rpx;
	color: #8a7a68;
}

.assist-card__time {
	font-size: 22rpx;
	color: #b8a99a;
}

.assist-card__status {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	flex-shrink: 0;
}

.assist-card__check {
	width: 32rpx;
	height: 32rpx;
	border-radius: 8rpx;
	background: var(--assist-green);
	color: #fff;
	font-size: 20rpx;
	display: inline-flex;
	align-items: center;
	justify-content: center;
}

.assist-card__status-text {
	font-size: 24rpx;
	font-weight: 700;
	color: var(--assist-green);
}
</style>
