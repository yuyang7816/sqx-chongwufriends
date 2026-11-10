<template>
	<view class="pet-page ranking-page">
		<view class="pet-shell">
			<!-- 顶部提示条 -->
			<view class="tip-banner">
				<text class="tip-icon">📅</text>
				<text class="tip-text">每周更新·为你喜欢的宠物打call</text>
			</view>

			<!-- 本周 / 上周 -->
			<view class="week-tabs">
				<view
					v-for="item in weekTabs"
					:key="item.value"
					class="week-tab"
					:class="{ 'is-active': currentWeekOffset === item.value }"
					@tap="switchWeek(item.value)"
				>{{ item.label }}</view>
			</view>

			<view v-if="currentWeekOffset === 0" class="quota-banner">
				<view class="quota-banner__icon">🎟️</view>
				<view class="quota-banner__body">
					<text class="quota-banner__title">{{ quotaTitle }}</text>
					<text class="quota-banner__desc">{{ quotaDesc }}</text>
				</view>
			</view>

			<!-- 列表 -->
			<view v-if="loading" class="state-box"><text class="pet-muted">加载中...</text></view>
			<view v-else-if="errorMsg" class="state-box">
				<text class="pet-muted">{{ errorMsg }}</text>
				<view class="pet-button retry-btn" @tap="loadRanking">重新加载</view>
			</view>
			<template v-else>
				<view v-if="!rankList.length" class="state-box"><text class="pet-muted">暂无榜单数据</text></view>
				<view v-else class="rank-list">
					<view v-for="item in rankList" :key="item.id" class="rank-card">
						<!-- 名次：前三奖牌，其余数字 -->
						<view class="rank-badge">
							<view v-if="item.rank <= 3" class="medal" :class="'medal-' + item.rank">
								<text class="medal-emoji">{{ medalEmoji(item.rank) }}</text>
							</view>
							<text v-else class="rank-num">{{ item.rank }}</text>
						</view>

						<image class="rank-avatar" :src="item.avatar" mode="aspectFill"></image>

						<view class="rank-main">
							<view class="rank-title-row">
								<text class="rank-name">{{ item.name }}</text>
								<text v-if="item.store" class="store-tag">{{ item.store }}</text>
							</view>
							<view class="likes-row">
								<text class="likes-heart">❤️</text>
								<text class="likes-num">{{ formatLikes(item.likes) }}</text>
							</view>
							<view class="rank-progress">
								<view class="rank-value" :style="{ width: progressWidth(item) }"></view>
							</view>
						</view>

						<view
							class="boost-btn"
							:class="{ 'is-disabled': currentWeekOffset !== 0 }"
							@tap="support(item)"
						>打榜💪</view>
					</view>
				</view>
			</template>

			<!-- 打榜规则 -->
			<view class="rules-card" @tap="showRules = true">
				<view class="rules-head">
					<text class="rules-doc">📄</text>
					<text class="rules-title">打榜规则</text>
				</view>
				<text class="rules-desc">{{ rankingRulesBrief }}</text>
			</view>
		</view>

		<view v-if="showRules" class="rules-mask" @tap="showRules = false">
			<view class="rules-panel" @tap.stop>
				<text class="rules-panel-title">打榜规则</text>
				<text class="rules-panel-content">{{ rankingRules }}</text>
				<view class="pet-button is-block" @tap="showRules = false">我知道了</view>
			</view>
		</view>

		<!-- 积分 / 金币打榜选择 -->
		<view v-if="showPaySheet" class="pay-mask" @tap="closePaySheet">
			<view class="pay-sheet" @tap.stop>
				<view class="pay-sheet-handle"></view>
				<view class="pay-sheet-head">
					<text class="pay-sheet-title">选择打榜方式</text>
					<text class="pay-sheet-sub">今日免费次数已用完，请选择积分或金币继续为爱宠加油</text>
				</view>

				<view v-if="payTarget" class="pay-target">
					<image class="pay-target-avatar" :src="payTarget.avatar" mode="aspectFill"></image>
					<view class="pay-target-info">
						<text class="pay-target-name">{{ payTarget.name }}</text>
						<text class="pay-target-meta">当前 ❤️ {{ formatLikes(payTarget.likes) }}</text>
					</view>
				</view>

				<view class="pay-options">
					<view
						v-for="opt in payOptions"
						:key="opt.value"
						class="pay-option"
						:class="[
							`is-${opt.value}`,
							{ 'is-active': selectedPayType === opt.value, 'is-disabled': opt.disabled }
						]"
						@tap="selectPayOption(opt)"
					>
						<view class="pay-option-icon" :class="`is-${opt.value}`">
							<view class="pay-option-glyph" :class="`is-${opt.value}`">
								<text class="pay-option-glyph-text">{{ opt.mark }}</text>
							</view>
						</view>
						<view class="pay-option-main">
							<view class="pay-option-title-row">
								<text class="pay-option-title">{{ opt.title }}</text>
								<text v-if="opt.disabled" class="pay-option-tag is-warn">余额不足</text>
								<text v-else-if="selectedPayType === opt.value" class="pay-option-tag">已选</text>
							</view>
							<text class="pay-option-desc">{{ opt.desc }}</text>
						</view>
						<view class="pay-option-price">
							<text class="pay-option-cost">{{ opt.cost }}</text>
							<text class="pay-option-unit">{{ opt.unit }}</text>
						</view>
					</view>
				</view>

				<view class="pay-actions">
					<view class="pay-cancel" @tap="closePaySheet">取消</view>
					<view
						class="pay-confirm"
						:class="{ disabled: supporting || !selectedPayType || selectedPayDisabled }"
						@tap="confirmPaySheet"
					>
						{{ supporting ? '打榜中...' : '确认打榜' }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { onPullDownRefresh } from '@dcloudio/uni-app';
import {
	fetchRankingPage,
	fetchRankingPeriods,
	fetchRankingAssistQuota,
	assistPet
} from '@/common/api/ranking.js';
import { ensureLogin } from '@/common/auth-guard.js';
import { resolveMediaUrl } from '@/common/media.js';

const loading = ref(false);
const supporting = ref(false);
const errorMsg = ref('');
const rankList = ref([]);
const rankingRules = ref('每周一0点重置 · 每月TOP 3宠物获得专属周边礼包 🎁');
const showRules = ref(false);
const showPaySheet = ref(false);
const payTarget = ref(null);
const selectedPayType = ref('');
const currentWeekOffset = ref(0);
const currentPeriodId = ref(null);
const assistQuota = ref({
	dailyFreeLimit: 10,
	todayUsed: 0,
	freeLeft: 10,
	pointsCost: 10,
	coinsCost: 10,
	points: 0,
	coins: 0
});
const weekTabs = [
	{ label: '本周', value: 0 },
	{ label: '上周', value: -1 }
];

const maxLikes = computed(() => {
	const nums = rankList.value.map((i) => Number(i.likes) || 0);
	return Math.max(...nums, 1);
});

const rankingRulesBrief = computed(() => {
	const raw = String(rankingRules.value || '').replace(/\n+/g, ' · ');
	return raw.length > 48 ? `${raw.slice(0, 48)}…` : raw;
});

const quotaTitle = computed(() => {
	const freeLeft = Number(assistQuota.value?.freeLeft || 0);
	if (freeLeft > 0) return `今日免费剩余 ${freeLeft} 次`;
	return '今日免费次数已用完';
});

const quotaDesc = computed(() => {
	const q = assistQuota.value || {};
	const pointsCost = Number(q.pointsCost || 0);
	const coinsCost = Number(q.coinsCost || 0);
	const freeLeft = Number(q.freeLeft || 0);
	if (freeLeft > 0) {
		return `用完后可用 ${pointsCost} 积分或 ${coinsCost} 金币继续打榜`;
	}
	return `可使用 ${pointsCost} 积分或 ${coinsCost} 金币继续为爱宠打榜`;
});

const payOptions = computed(() => {
	const q = assistQuota.value || {};
	const pointsCost = Number(q.pointsCost || 0);
	const coinsCost = Number(q.coinsCost || 0);
	const points = Number(q.points || 0);
	const coins = Number(q.coins || 0);
	const options = [];
	if (pointsCost > 0) {
		options.push({
			value: 'points',
			mark: '积',
			title: '积分打榜',
			desc: `当前余额 ${points}`,
			cost: pointsCost,
			unit: '积分',
			disabled: points < pointsCost
		});
	}
	if (coinsCost > 0) {
		options.push({
			value: 'coins',
			mark: '币',
			title: '金币打榜',
			desc: `当前余额 ${coins}`,
			cost: coinsCost,
			unit: '金币',
			disabled: coins < coinsCost
		});
	}
	return options;
});

const selectedPayDisabled = computed(() => {
	const opt = payOptions.value.find((item) => item.value === selectedPayType.value);
	return !opt || opt.disabled;
});

function medalEmoji(rank) {
	if (rank === 1) return '🥇';
	if (rank === 2) return '🥈';
	if (rank === 3) return '🥉';
	return '';
}

function formatLikes(n) {
	const num = Number(n) || 0;
	return num.toLocaleString('en-US');
}

function progressWidth(item) {
	const likes = Number(item.likes) || 0;
	const pct = Math.round((likes / maxLikes.value) * 100);
	return `${Math.max(pct, item.rank <= 3 ? 36 : 18)}%`;
}

function mapRankItem(item, idx) {
	return {
		id: item.petId || item.id,
		rank: item.rankNo || idx + 1,
		name: item.petName || item.name || '未命名',
		store: item.storeName || item.store || '',
		likes: item.likeCount || item.likes || 0,
		avatar: resolveMediaUrl(item.avatarUrl || item.avatar || '/static/logo.png')
	};
}

async function loadRanking() {
	loading.value = true;
	errorMsg.value = '';
	try {
		const res = await fetchRankingPage({ weekOffset: currentWeekOffset.value, page: 1, limit: 50 });
		const records = res?.records || res?.list || (Array.isArray(res) ? res : []);
		rankList.value = records.map(mapRankItem);
	} catch (e) {
		rankList.value = [];
		errorMsg.value = e?.msg || e?.message || '加载榜单失败';
		uni.showToast({ title: errorMsg.value, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function loadMeta() {
	try {
		const overview = await fetchRankingPeriods();
		if (overview?.rules) rankingRules.value = overview.rules;
		const current = overview?.currentWeek || overview?.currentPeriod;
		if (current?.id) currentPeriodId.value = current.id;
		const settings = overview?.assistSettings;
		if (settings) {
			assistQuota.value = {
				...assistQuota.value,
				dailyFreeLimit: Number(settings.dailyFreeLimit ?? 10),
				pointsCost: Number(settings.pointsCost ?? 10),
				coinsCost: Number(settings.coinsCost ?? 10),
				freeLeft: Number(settings.dailyFreeLimit ?? assistQuota.value.freeLeft ?? 10)
			};
		}
	} catch (e) {
		// 规则加载失败不阻断榜单
	}
}

async function loadQuota() {
	const token = uni.getStorageSync('token');
	if (!token) return;
	try {
		const data = await fetchRankingAssistQuota();
		if (!data) return;
		assistQuota.value = {
			dailyFreeLimit: Number(data.dailyFreeLimit ?? 10),
			todayUsed: Number(data.todayUsed ?? 0),
			freeLeft: Number(data.freeLeft ?? 0),
			pointsCost: Number(data.pointsCost ?? 10),
			coinsCost: Number(data.coinsCost ?? 10),
			points: Number(data.points ?? 0),
			coins: Number(data.coins ?? 0)
		};
	} catch (e) {
		// 额度加载失败不阻断榜单
	}
}

function switchWeek(offset) {
	if (currentWeekOffset.value === offset) return;
	currentWeekOffset.value = offset;
	loadRanking();
}

/** 本地 +1 爱心并按爱心值重排名次，不重新请求列表 */
function applyLocalBoost(petId) {
	const list = rankList.value.map((row) => ({
		...row,
		likes: Number(row.id) === Number(petId) ? Number(row.likes || 0) + 1 : Number(row.likes || 0)
	}));
	list.sort((a, b) => {
		if (b.likes !== a.likes) return b.likes - a.likes;
		return (a.rank || 0) - (b.rank || 0);
	});
	list.forEach((row, idx) => {
		row.rank = idx + 1;
	});
	rankList.value = list;
}

function openPaySheet(item) {
	const options = payOptions.value;
	if (!options.length) {
		uni.showToast({ title: '暂未配置积分/金币打榜价格', icon: 'none' });
		return;
	}
	payTarget.value = item;
	const preferred = options.find((opt) => !opt.disabled) || options[0];
	selectedPayType.value = preferred?.value || '';
	showPaySheet.value = true;
}

function closePaySheet() {
	if (supporting.value) return;
	showPaySheet.value = false;
	payTarget.value = null;
	selectedPayType.value = '';
}

function selectPayOption(opt) {
	if (!opt || opt.disabled) {
		uni.showToast({ title: `${opt?.unit || '资产'}余额不足`, icon: 'none' });
		return;
	}
	selectedPayType.value = opt.value;
}

async function confirmPaySheet() {
	if (!payTarget.value || !selectedPayType.value || selectedPayDisabled.value || supporting.value) return;
	supporting.value = true;
	try {
		await doAssist(payTarget.value, selectedPayType.value);
		showPaySheet.value = false;
		payTarget.value = null;
		selectedPayType.value = '';
	} catch (e) {
		uni.showToast({ title: e?.msg || e?.message || '打榜失败', icon: 'none' });
		loadQuota();
	} finally {
		supporting.value = false;
	}
}

async function doAssist(item, payType) {
	await assistPet(item.id, currentPeriodId.value, payType);
	applyLocalBoost(item.id);
	if (!payType) {
		assistQuota.value = {
			...assistQuota.value,
			todayUsed: Number(assistQuota.value.todayUsed || 0) + 1,
			freeLeft: Math.max(Number(assistQuota.value.freeLeft || 0) - 1, 0)
		};
	} else if (payType === 'points') {
		assistQuota.value = {
			...assistQuota.value,
			todayUsed: Number(assistQuota.value.todayUsed || 0) + 1,
			points: Math.max(Number(assistQuota.value.points || 0) - Number(assistQuota.value.pointsCost || 0), 0)
		};
	} else if (payType === 'coins') {
		assistQuota.value = {
			...assistQuota.value,
			todayUsed: Number(assistQuota.value.todayUsed || 0) + 1,
			coins: Math.max(Number(assistQuota.value.coins || 0) - Number(assistQuota.value.coinsCost || 0), 0)
		};
	}
	uni.showToast({ title: `已为${item.name}打榜`, icon: 'success' });
}

async function support(item) {
	if (currentWeekOffset.value !== 0) {
		uni.showToast({ title: '仅支持为本周榜单打榜', icon: 'none' });
		return;
	}
	if (!ensureLogin('/packageA/ranking/list', true)) return;
	if (supporting.value || showPaySheet.value) return;
	supporting.value = true;
	try {
		await loadQuota();
		if (Number(assistQuota.value.freeLeft || 0) <= 0) {
			supporting.value = false;
			openPaySheet(item);
			return;
		}
		await doAssist(item, '');
	} catch (e) {
		uni.showToast({ title: e?.msg || e?.message || '打榜失败', icon: 'none' });
		loadQuota();
	} finally {
		supporting.value = false;
	}
}

onMounted(async () => {
	await loadMeta();
	await Promise.all([loadRanking(), loadQuota()]);
});

onPullDownRefresh(async () => {
	try {
		await Promise.all([loadMeta(), loadRanking(), loadQuota()]);
	} finally {
		uni.stopPullDownRefresh();
	}
});
</script>

<style lang="scss">
.ranking-page {
	padding-top: 8rpx;
}

/* —— 顶部提示 —— */
.tip-banner {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 22rpx 28rpx;
	margin-top: 8rpx;
	border-radius: 24rpx;
	background: #f3e6d4;
	border: 2rpx solid rgba(220, 196, 168, 0.55);
}

.quota-banner {
	display: flex;
	align-items: flex-start;
	gap: 16rpx;
	margin-top: 8rpx;
	margin-bottom: 28rpx;
	padding: 22rpx 24rpx;
	border-radius: 22rpx;
	background: rgba(196, 154, 108, 0.12);
	border: 1rpx solid rgba(196, 154, 108, 0.28);
}

.quota-banner__icon {
	font-size: 32rpx;
	line-height: 1.2;
	flex-shrink: 0;
}

.quota-banner__body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.quota-banner__title {
	font-size: 26rpx;
	font-weight: 700;
	color: #8b5e3c;
	line-height: 1.35;
}

.quota-banner__desc {
	font-size: 22rpx;
	line-height: 1.5;
	color: #a88463;
}

.tip-icon {
	font-size: 30rpx;
	line-height: 1;
	flex-shrink: 0;
}

.tip-text {
	font-size: 26rpx;
	font-weight: 600;
	color: #8b613d;
	line-height: 1.4;
}

/* —— 周切换 —— */
.week-tabs {
	display: flex;
	align-items: center;
	gap: 28rpx;
	margin: 28rpx 0 20rpx;
	padding-left: 4rpx;
}

.week-tab {
	font-size: 30rpx;
	font-weight: 600;
	color: #8b613d;
	padding: 0 28rpx;
	height: 60rpx;
	line-height: 60rpx;
	border-radius: 999rpx;
}

.week-tab.is-active {
	background: #c79662;
	color: #fff;
	box-shadow: 0 8rpx 20rpx rgba(199, 150, 98, 0.28);
}

/* —— 列表 —— */
.rank-list {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.rank-card {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 22rpx 20rpx;
	background: #fffdf9;
	border-radius: 28rpx;
	border: 2rpx solid rgba(236, 215, 191, 0.55);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.06);
}

.rank-badge {
	width: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.medal {
	width: 52rpx;
	height: 52rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.medal-emoji {
	font-size: 40rpx;
	line-height: 1;
}

.rank-num {
	font-size: 36rpx;
	font-weight: 700;
	color: #c4b5a5;
	text-align: center;
	line-height: 1;
}

.rank-avatar {
	width: 84rpx;
	height: 84rpx;
	border-radius: 50%;
	border: 3rpx solid #f0e0ce;
	background: linear-gradient(135deg, #f6d57c, #d0eedf);
	flex-shrink: 0;
}

.rank-main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.rank-title-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	min-width: 0;
}

.rank-name {
	font-size: 30rpx;
	font-weight: 700;
	color: #6d4c38;
	max-width: 180rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.store-tag {
	flex-shrink: 0;
	padding: 0 14rpx;
	height: 36rpx;
	line-height: 36rpx;
	border-radius: 999rpx;
	background: #f0ebe3;
	font-size: 20rpx;
	color: #9f8a77;
}

.likes-row {
	display: flex;
	align-items: center;
	gap: 6rpx;
}

.likes-heart {
	font-size: 22rpx;
	line-height: 1;
}

.likes-num {
	font-size: 24rpx;
	font-weight: 600;
	color: #c45c6a;
}

.rank-progress {
	width: 100%;
	height: 12rpx;
	border-radius: 999rpx;
	background: #f3ebe3;
	overflow: hidden;
}

.rank-value {
	height: 100%;
	border-radius: 999rpx;
	background: linear-gradient(90deg, #f8c4cc 0%, #e86b7a 100%);
}

.boost-btn {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 64rpx;
	padding: 0 22rpx;
	border-radius: 999rpx;
	background: #c79662;
	color: #fff;
	font-size: 24rpx;
	font-weight: 700;
	box-shadow: 0 8rpx 18rpx rgba(199, 150, 98, 0.28);
}

.boost-btn.is-disabled {
	opacity: 0.42;
}

/* —— 规则 —— */
.rules-card {
	margin-top: 28rpx;
	padding: 28rpx 24rpx;
	border-radius: 24rpx;
	background: rgba(255, 253, 249, 0.72);
}

.rules-head {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 12rpx;
}

.rules-doc {
	font-size: 28rpx;
	line-height: 1;
}

.rules-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #6d4c38;
}

.rules-desc {
	display: block;
	font-size: 24rpx;
	line-height: 1.7;
	color: #9f8a77;
}

/* —— 状态 / 弹层 —— */
.state-box {
	padding: 80rpx 0;
	text-align: center;
}

.retry-btn {
	margin: 24rpx auto 0;
	width: 240rpx;
}

.rules-mask {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.45);
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
	z-index: 99;
}

.rules-panel {
	width: 100%;
	max-width: 640rpx;
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding: 36rpx 32rpx;
	background: #fffdf9;
	border-radius: 32rpx;
	border: 2rpx solid rgba(236, 215, 191, 0.55);
}

.rules-panel-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #6d4c38;
}

.rules-panel-content {
	font-size: 28rpx;
	line-height: 1.8;
	color: #5f4f42;
	white-space: pre-wrap;
}

/* —— 打榜方式弹层 —— */
.pay-mask {
	position: fixed;
	inset: 0;
	z-index: 1000;
	background: rgba(64, 42, 24, 0.42);
	display: flex;
	align-items: flex-end;
	justify-content: center;
}

.pay-sheet {
	width: 100%;
	padding: 16rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
	border-radius: 36rpx 36rpx 0 0;
	background: linear-gradient(180deg, #fffaf3 0%, #f7efe4 100%);
	box-shadow: 0 -12rpx 40rpx rgba(91, 66, 49, 0.12);
	animation: pay-sheet-up 0.28s ease-out;
}

.pay-sheet-handle {
	width: 64rpx;
	height: 8rpx;
	margin: 0 auto 20rpx;
	border-radius: 999rpx;
	background: rgba(188, 146, 109, 0.35);
}

.pay-sheet-head {
	margin-bottom: 22rpx;
}

.pay-sheet-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #5b4231;
	line-height: 1.3;
}

.pay-sheet-sub {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	line-height: 1.5;
	color: #b38a63;
}

.pay-target {
	display: flex;
	align-items: center;
	gap: 18rpx;
	padding: 18rpx 20rpx;
	margin-bottom: 22rpx;
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.86);
	border: 1rpx solid rgba(188, 146, 109, 0.14);
}

.pay-target-avatar {
	width: 84rpx;
	height: 84rpx;
	border-radius: 50%;
	border: 3rpx solid #f0e0ce;
	background: linear-gradient(135deg, #f6d57c, #d0eedf);
	flex-shrink: 0;
}

.pay-target-info {
	flex: 1;
	min-width: 0;
}

.pay-target-name {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #5b4231;
	line-height: 1.3;
}

.pay-target-meta {
	display: block;
	margin-top: 6rpx;
	font-size: 24rpx;
	color: #c45c6a;
}

.pay-options {
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.pay-option {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 22rpx 20rpx;
	border-radius: 24rpx;
	background: rgba(255, 255, 255, 0.92);
	border: 2rpx solid rgba(188, 146, 109, 0.16);
	transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.pay-option.is-active {
	border-color: rgba(199, 150, 98, 0.7);
	box-shadow: 0 8rpx 22rpx rgba(199, 150, 98, 0.16);
	background: #fffdf8;
}

.pay-option.is-points.is-active {
	border-color: rgba(122, 145, 173, 0.72);
	box-shadow: 0 8rpx 22rpx rgba(122, 145, 173, 0.16);
}

.pay-option.is-disabled {
	opacity: 0.55;
}

.pay-option-icon {
	width: 68rpx;
	height: 68rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	background: #f6eee3;
}

.pay-option-icon.is-points {
	background: linear-gradient(145deg, #e8eef6, #d5e0ec);
}

.pay-option-icon.is-coins {
	background: linear-gradient(145deg, #f8ead0, #efd6aa);
}

.pay-option-glyph {
	width: 44rpx;
	height: 44rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}

.pay-option-glyph.is-points {
	background: linear-gradient(145deg, #a8bfd8, #7a91ad);
	box-shadow:
		0 4rpx 10rpx rgba(122, 145, 173, 0.28),
		inset 0 2rpx 0 rgba(255, 255, 255, 0.35);
}

.pay-option-glyph.is-coins {
	background: linear-gradient(145deg, #f3d089, #d4a04a);
	border: 3rpx solid rgba(236, 196, 120, 0.95);
	box-shadow:
		0 4rpx 10rpx rgba(201, 150, 78, 0.3),
		inset 0 2rpx 0 rgba(255, 255, 255, 0.45);
}

.pay-option-glyph.is-coins::after {
	content: '';
	position: absolute;
	inset: 6rpx;
	border-radius: 50%;
	border: 2rpx solid rgba(255, 236, 190, 0.55);
	pointer-events: none;
}

.pay-option-glyph-text {
	font-size: 22rpx;
	font-weight: 700;
	line-height: 1;
	color: #fff;
	position: relative;
	z-index: 1;
}

.pay-option.is-points .pay-option-tag:not(.is-warn) {
	color: #6a7f9c;
	background: rgba(122, 145, 173, 0.14);
}

.pay-option.is-points .pay-option-cost,
.pay-option.is-points .pay-option-unit {
	color: #6a7f9c;
}

.pay-option-main {
	flex: 1;
	min-width: 0;
}

.pay-option-title-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
	flex-wrap: wrap;
}

.pay-option-title {
	font-size: 28rpx;
	font-weight: 700;
	color: #5b4231;
}

.pay-option-tag {
	padding: 0 12rpx;
	height: 34rpx;
	line-height: 34rpx;
	border-radius: 999rpx;
	font-size: 20rpx;
	font-weight: 600;
	color: #c79662;
	background: rgba(199, 150, 98, 0.14);
}

.pay-option-tag.is-warn {
	color: #c45c6a;
	background: rgba(196, 92, 106, 0.12);
}

.pay-option-desc {
	display: block;
	margin-top: 6rpx;
	font-size: 22rpx;
	color: #a88463;
}

.pay-option-price {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
	flex-shrink: 0;
}

.pay-option-cost {
	font-size: 34rpx;
	font-weight: 700;
	color: #c79662;
	line-height: 1.1;
}

.pay-option-unit {
	margin-top: 4rpx;
	font-size: 20rpx;
	color: #b38a63;
}

.pay-actions {
	display: flex;
	gap: 16rpx;
	margin-top: 28rpx;
}

.pay-cancel,
.pay-confirm {
	flex: 1;
	height: 84rpx;
	border-radius: 999rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 700;
}

.pay-cancel {
	color: #8b613d;
	background: rgba(255, 255, 255, 0.88);
	border: 2rpx solid rgba(188, 146, 109, 0.28);
}

.pay-confirm {
	color: #fff;
	background: #c79662;
	box-shadow: 0 10rpx 24rpx rgba(199, 150, 98, 0.28);
}

.pay-confirm.disabled {
	opacity: 0.45;
}

@keyframes pay-sheet-up {
	from {
		transform: translateY(40rpx);
		opacity: 0.6;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}
</style>
