<template>
	<view class="pet-page task-page">
		<view class="pet-shell task-shell">
			<view v-if="pageLoading" class="state-box"><text class="state-text">加载中...</text></view>
			<view v-else-if="loadError" class="state-box">
				<text class="state-title">加载失败</text>
				<text class="state-text">{{ loadError }}</text>
				<view class="pet-button state-btn" @tap="loadPage">重试</view>
			</view>
			<template v-else>
				<!-- 资产 + 活跃度 -->
				<view class="asset-card">
					<view class="asset-row">
						<view class="asset-item">
							<text class="asset-icon coins">◉</text>
							<text class="asset-num">{{ user.coins }}</text>
							<text class="asset-label">金币</text>
						</view>
						<view class="asset-divider"></view>
						<view class="asset-item">
							<text class="asset-icon petals">✿</text>
							<text class="asset-num">{{ user.flowers }}</text>
							<text class="asset-label">花瓣</text>
						</view>
					</view>
					<view class="active-section">
						<view class="active-head">
							<text class="active-title">本周活跃度</text>
							<text class="active-percent">{{ activePercent }}%</text>
						</view>
						<view class="active-bar">
							<view class="active-fill" :style="{ width: activePercent + '%' }"></view>
						</view>
						<text class="active-sub">本周完成 {{ completedCount }}/{{ taskList.length }} 项任务</text>
					</view>
				</view>

				<!-- 任务分组 Tabs -->
				<view class="task-tabs">
					<view
						v-for="item in taskTabs"
						:key="item"
						class="task-tab"
						:class="{ 'is-active': currentTab === item }"
						@tap="currentTab = item"
					>
						<text class="task-tab-icon">{{ tabIcon(item) }}</text>
						<text class="task-tab-text">{{ item }}</text>
					</view>
				</view>

				<view v-if="!visibleTasks.length" class="state-box"><text class="state-text">当前分组暂无任务</text></view>

				<!-- 任务卡片 -->
				<view
					v-for="item in visibleTasks"
					:key="item.id"
					class="task-card"
					:class="['is-' + item.status, { 'is-locked': item.locked }]"
				>
					<view class="task-card-icon">
						<text v-if="item.status === 'completed'" class="ico-ok">✓</text>
						<text v-else-if="item.locked" class="ico-lock">🔒</text>
						<text v-else class="ico-hour">⏳</text>
					</view>
					<view class="task-card-main">
						<text class="task-card-title">{{ item.title }}</text>
						<text class="task-card-desc">{{ item.locked ? '等级不足 ' + (item.requiredLevelName || ('Lv.' + item.requiredLevel)) : item.desc }}</text>
						<view class="task-card-reward">
							<text v-if="item.rewardCoins" class="reward-tag coins">+{{ item.rewardCoins }}金币</text>
							<text v-if="item.rewardPoints" class="reward-tag petals">+{{ item.rewardPoints }}花瓣</text>
						</view>
					</view>
					<view class="task-card-action">
						<text v-if="item.status === 'completed'" class="status-done">已完成</text>
						<view v-else-if="item.locked" class="status-locked">未解锁</view>
						<view v-else class="task-btn" @tap="handleTaskAction(item)">去完成</view>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';
import { fetchTaskList, fetchIslandInfo } from '@/common/api/island.js';
import { ensureLogin } from '@/common/auth-guard.js';

const taskTabs = ['家主任务', '社群任务', '每日待办'];
const currentTab = ref('家主任务');
const pageLoading = ref(false);
const loadError = ref('');
const taskList = ref([]);
const user = ref({ points: 0, flowers: 0, coins: 0, level: 1, levelName: 'Lv.1' });

const tabIcon = (name) => {
	if (name === '家主任务') return '🏠';
	if (name === '社群任务') return '💬';
	return '📋';
};

const mapTaskGroup = (group) => {
	if (!group) return '家主任务';
	if (group === 'daily' || group === '每日待办') return '每日待办';
	if (group === '社群任务') return '社群任务';
	return '家主任务';
};

const mapTaskStatus = (recordStatus) => {
	if (recordStatus === 1) return 'completed';
	if (recordStatus === 2) return 'doing';
	return 'pending';
};

const mapTaskItem = (t) => {
	const requiredLevel = Number(t.requiredLevel || 1);
	const userLevel = Number(user.value.level || 1);
	const locked = requiredLevel > userLevel;
	return {
		id: t.id,
		title: t.title,
		desc: t.description || t.desc || '',
		rewardDesc: t.rewardDesc || t.reward || '',
		rewardCoins: Number(t.rewardCoins || 0),
		rewardPoints: Number(t.rewardPoints || 0),
		requiredLevel,
		requiredLevelName: t.requiredLevelName,
		status: mapTaskStatus(t.recordStatus),
		group: mapTaskGroup(t.taskGroup),
		actionType: t.actionType || '',
		locked
	};
};

const completedCount = computed(() => taskList.value.filter((item) => item.status === 'completed').length);
const activePercent = computed(() => {
	if (!taskList.value.length) return 0;
	return Math.round((completedCount.value / taskList.value.length) * 100);
});
const visibleTasks = computed(() => taskList.value.filter((item) => item.group === currentTab.value));

const actionRoute = (item) => {
	const act = (item.actionType || '').toLowerCase();
	switch (act) {
		case 'feed':
		case 'buy':
		case 'resident':
			return { type: 'switchTab', url: '/pages/island/island' };
		case 'post':
			return { type: 'navigate', url: '/packageA/post/create' };
		case 'like':
		case 'comment':
		case 'share':
			return { type: 'switchTab', url: '/pages/msg/msg' };
		default:
			return null;
	}
};

const handleTaskAction = (item) => {
	if (item.locked || item.status === 'completed') return;
	const route = actionRoute(item);
	if (!route) {
		uni.showToast({ title: '请前往对应页面完成任务', icon: 'none' });
		return;
	}
	if (route.type === 'switchTab') {
		uni.switchTab({ url: route.url });
	} else {
		uni.navigateTo({ url: route.url });
	}
};

const loadPage = async () => {
	if (!ensureLogin('/packageA/tasks/center', true)) return;
	pageLoading.value = true;
	loadError.value = '';
	try {
		const [list, info] = await Promise.all([fetchTaskList(), fetchIslandInfo()]);
		if (info) {
			user.value.points = info.points ?? 0;
			user.value.flowers = info.flowers ?? 0;
			user.value.coins = info.coins ?? 0;
			user.value.level = info.level ?? 1;
			user.value.levelName = info.levelName || ('Lv.' + (info.level || 1));
		}
		taskList.value = (list || []).map(mapTaskItem);
	} catch (e) {
		loadError.value = e?.msg || '任务加载失败';
	} finally {
		pageLoading.value = false;
	}
};

onShow(loadPage);
onPullDownRefresh(async () => {
	try {
		await loadPage();
	} finally {
		uni.stopPullDownRefresh();
	}
});
</script>

<style lang="scss">
.task-shell {
	padding-bottom: 60rpx;
}

.state-box {
	min-height: 200rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	padding: 40rpx 0;
}

.state-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #8b613d;
}

.state-text {
	font-size: 24rpx;
	color: #b38a63;
}

.state-btn {
	margin-top: 8rpx;
	padding: 0 32rpx;
}

/* 资产卡 */
.asset-card {
	margin-top: 8rpx;
	padding: 28rpx 28rpx 24rpx;
	border-radius: 28rpx;
	background: linear-gradient(135deg, #fff6e8 0%, #fbe9d2 100%);
	border: 1rpx solid #f0ddc0;
	box-shadow: 0 10rpx 24rpx rgba(199, 150, 98, 0.1);
}

.asset-row {
	display: flex;
	align-items: center;
	justify-content: space-around;
}

.asset-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 6rpx;
}

.asset-icon {
	font-size: 34rpx;
	line-height: 1;
}

.asset-icon.coins {
	color: #e0a458;
}

.asset-icon.petals {
	color: #d38ca1;
}

.asset-num {
	font-size: 48rpx;
	font-weight: 800;
	color: #6d4c38;
	line-height: 1.1;
}

.asset-label {
	font-size: 22rpx;
	color: #9f8a77;
}

.asset-divider {
	width: 2rpx;
	height: 70rpx;
	background: #e8d4ba;
}

.active-section {
	margin-top: 24rpx;
}

.active-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12rpx;
}

.active-title {
	font-size: 26rpx;
	font-weight: 700;
	color: #6d4c38;
}

.active-percent {
	font-size: 26rpx;
	font-weight: 700;
	color: #c79662;
}

.active-bar {
	height: 18rpx;
	border-radius: 999rpx;
	background: #f0e0cb;
	overflow: hidden;
}

.active-fill {
	height: 100%;
	border-radius: 999rpx;
	background: linear-gradient(90deg, #f0b558, #e08f3a);
	transition: width 0.3s ease;
}

.active-sub {
	display: block;
	margin-top: 12rpx;
	font-size: 22rpx;
	color: #9f8a77;
}

/* Tabs */
.task-tabs {
	display: flex;
	gap: 16rpx;
	margin: 28rpx 0 22rpx;
}

.task-tab {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	padding: 18rpx 0;
	border-radius: 22rpx;
	background: #fff;
	border: 2rpx solid #ecd7bf;
	font-size: 26rpx;
	color: #8b613d;
}

.task-tab.is-active {
	background: linear-gradient(145deg, #c79662, #a67b4f);
	border-color: transparent;
	color: #fff;
	box-shadow: 0 8rpx 20rpx rgba(199, 150, 98, 0.28);
}

.task-tab-icon {
	font-size: 28rpx;
	line-height: 1;
}

.task-tab-text {
	font-weight: 700;
}

/* 任务卡 */
.task-card {
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 24rpx 22rpx;
	border-radius: 26rpx;
	background: #ffffff;
	border: 2rpx solid #ecd7bf;
	margin-bottom: 18rpx;
	box-shadow: 0 8rpx 22rpx rgba(163, 120, 79, 0.06);
}

.task-card.is-completed {
	background: #eaf8e6;
	border-color: #c5e7c9;
}

.task-card.is-pending {
	border-style: dashed;
	border-color: #d8c4ad;
}

.task-card.is-locked {
	background: #f3efe9;
	border-color: #e2d8cb;
	opacity: 0.7;
}

.task-card-icon {
	width: 76rpx;
	height: 76rpx;
	border-radius: 22rpx;
	background: #f7efe2;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	font-size: 36rpx;
}

.task-card.is-completed .task-card-icon {
	background: #d6efcf;
	color: #5fbf7a;
}

.task-card.is-locked .task-card-icon {
	background: #e6e0d6;
	color: #b6a994;
}

.ico-ok,
.ico-lock,
.ico-hour {
	line-height: 1;
}

.task-card-main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.task-card-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #6c4d36;
	line-height: 1.3;
}

.task-card.is-locked .task-card-title {
	color: #a89a86;
}

.task-card-desc {
	font-size: 22rpx;
	color: #b19072;
	line-height: 1.4;
}

.task-card.is-locked .task-card-desc {
	color: #b6a994;
}

.task-card-reward {
	display: flex;
	gap: 12rpx;
	flex-wrap: wrap;
}

.reward-tag {
	font-size: 22rpx;
	font-weight: 700;
	padding: 4rpx 14rpx;
	border-radius: 999rpx;
}

.reward-tag.coins {
	background: #fbe9cf;
	color: #c08a3e;
}

.reward-tag.petals {
	background: #fbdde7;
	color: #c96d8a;
}

.task-card-action {
	flex-shrink: 0;
	display: flex;
	align-items: center;
}

.task-btn {
	padding: 14rpx 30rpx;
	border-radius: 999rpx;
	background: linear-gradient(145deg, #c79662, #a67b4f);
	color: #fff;
	font-size: 26rpx;
	font-weight: 700;
	box-shadow: 0 6rpx 16rpx rgba(199, 150, 98, 0.26);
}

.status-done {
	font-size: 24rpx;
	font-weight: 700;
	color: #5fbf7a;
}

.status-locked {
	font-size: 22rpx;
	color: #b6a994;
	padding: 10rpx 22rpx;
	border-radius: 999rpx;
	background: #e6e0d6;
}
</style>
