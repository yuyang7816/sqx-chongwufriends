<template>
	<view class="pet-page island-page">

		<view class="island-shell">
			<view v-if="pageLoading" class="island-state-box">
				<text class="island-state-text">岛宅加载中...</text>
			</view>
			<view v-else-if="!isLoggedIn" class="island-state-box">
				<text class="island-state-title">登录后查看岛宅</text>
				<text class="island-state-text">登录即可查看常驻宠物、今日进度与投喂状态</text>
				<view class="pet-button island-login-btn" @tap="goLogin">立即登录</view>
			</view>
			<view v-else-if="loadError" class="island-state-box">
				<text class="island-state-title">加载失败</text>
				<text class="island-state-text">{{ loadError }}</text>
				<view class="pet-button island-login-btn" @tap="loadIslandData">重新加载</view>
			</view>
			<template v-else>
			<view class="top-ornaments">
				<view class="clock-tile">
					<view class="clock-ring"></view>
				</view>
				<view class="mini-tile paw">🐾</view>
				<view class="mini-tile star">⭐</view>
				<view class="mini-tile bone">🦴</view>
				<view class="resource-row">
					<view class="resource-pill swirl">
						<text class="resource-icon">🌀</text>
						<text>{{ displayPoints }}</text>
					</view>
					<view class="resource-pill flower">
						<text class="resource-icon">🌸</text>
						<text>{{ displayFlowers }}</text>
					</view>
					<view class="resource-pill love" :class="{ bump: loveBump }">
						<text class="resource-icon">❤️</text>
						<text>{{ displayLove }}</text>
					</view>
				</view>
				<view class="gift-block">
					<view class="gift-ribbon"></view>
					<view class="gift-ribbon horizontal"></view>
					<view class="gift-sun">☀️</view>
				</view>
			</view>

			<view class="playfield">
				<view class="side-actions left">
					<view v-for="item in leftActionList" :key="item.title" class="side-action" @tap="handleAction(item)">
						<view class="side-icon left-tone" :class="{ disabled: item.enabled === false }">{{ item.emoji }}</view>
						<text class="side-text">{{ item.title }}</text>
					</view>
				</view>

				<view class="side-actions right">
					<view v-for="item in rightActionList" :key="item.title" class="side-action" @tap="handleAction(item)">
						<view class="side-icon right-tone" :class="{ disabled: item.enabled === false }">{{ item.emoji }}</view>
						<text class="side-text">{{ item.title }}</text>
					</view>
				</view>

				<view class="center-stage">
					<view class="stage-topline">
						<text class="mood-face">{{ displayMoodEmoji }}</text>
						<text class="affection-label">📊 今日进度</text>
						<text class="affection-level">Lv.{{ intimacyLevel }}</text>
					</view>
					<view class="affection-track">
						<view class="affection-value" :style="{ width: dailyProgress + '%' }"></view>
					</view>
					<view class="speech-bubble" :class="{ chewing: petAction !== 'idle' }">
						<text class="speech-emoji">{{ petActionEmoji }}</text>
						<text>{{ eatingStatusText }}</text>
					</view>
					<view class="stage-sparkle left">✨</view>
					<view class="stage-sparkle right">✨</view>
					<view class="floating-flower left">🌸</view>
					<view class="floating-flower right">🌸</view>
					<view v-if="petAction !== 'idle'" :key="feedFxToken" class="feed-fx-layer" :class="`fx-${feedFxType}`">
						<template v-if="feedFxType === 'water'">
							<text class="feed-crumb crumb-a water">💧</text>
							<text class="feed-crumb crumb-b water">💦</text>
							<text class="feed-crumb crumb-c water">💧</text>
							<view class="water-ripple r1"></view>
							<view class="water-ripple r2"></view>
							<view class="water-ripple r3"></view>
							<text class="pet-heart-burst">❤</text>
						</template>
						<template v-else>
							<text class="feed-crumb crumb-a food">🍘</text>
							<text class="feed-crumb crumb-b food">🍖</text>
							<text class="feed-crumb crumb-c food">🍘</text>
							<text class="pet-heart-burst">❤</text>
						</template>
					</view>
					<view v-if="loveFloatVisible" :key="loveFloatToken" class="love-float">❤️+{{ loveFloatAmount }}</view>
					<view class="pet-actor" :class="[`kind-${petAnimKind}`, `species-${petSpecies}`, `action-${petAction}`]">
						<view class="pet-shadow"></view>
						<view class="pet-body">
							<image
								class="pet-image"
								:key="`pet-${residentPetId || petDisplayName}`"
								:src="petAvatar"
								mode="aspectFit"
								@error="onPetImageError"
							></image>
						</view>
					</view>
					<view class="pet-name-pill">{{ petDisplayName }}</view>
				</view>

				<view class="scene-decor">
					<view class="floor-line"></view>
					<view class="decor-card tall">
						<view class="plant">🪴</view>
						<view class="lamp"></view>
						<view class="pig">🐷</view>
					</view>
					<view class="decor-card shelf">
						<view class="flowerpot">🌷</view>
						<view class="shelf-grid">
							<text>⚽</text>
							<text>🧴</text>
							<text>🧃</text>
							<text>🎀</text>
							<text>🍡</text>
							<text>🐾</text>
						</view>
					</view>
					<view class="yarn-ball">🧶</view>
					<view class="leaf-mark">🍃</view>
				</view>
			</view>

			<view class="feed-strip">
				<view class="feed-tools">
					<view class="feed-tool water-tool" :class="{ filled: waterBowlFilled, feeding: petAction === 'drink' }" @tap="openBowlPicker('water')">
						<view class="bowl-stage water-bowl">
							<view class="bowl-body">
								<view class="bowl-inner"></view>
								<view v-if="waterBowlFilled" class="bowl-fill water-fill">
									<view class="water-shine"></view>
								</view>
								<view class="bowl-badge drop">💧</view>
							</view>
						</view>
						<text class="feed-tool-text">{{ waterBowlFilled ? '有水' : '空碗' }}</text>
					</view>
					<view class="feed-tool food-tool" :class="{ filled: foodBowlFilled, feeding: petAction === 'eat' }" @tap="openBowlPicker('food')">
						<view class="bowl-stage food-bowl">
							<view class="bowl-body">
								<view class="bowl-inner"></view>
								<view v-if="foodBowlFilled" class="bowl-fill food-fill">
									<view class="food-mound"></view>
									<view class="food-leaf"></view>
								</view>
								<text class="bowl-badge dog-tag">DOG</text>
							</view>
						</view>
						<text class="feed-tool-text">{{ foodBowlFilled ? '有粮' : '空盆' }}</text>
					</view>
				</view>
				<view class="feed-status">
					<view class="floating-weather">
						<view class="weather-thermo" aria-hidden="true">
							<view class="weather-thermo__bulb"></view>
							<view class="weather-thermo__stem">
								<view class="weather-thermo__level"></view>
							</view>
						</view>
						<text class="weather-copy">{{ weatherText }}</text>
					</view>
					<view v-if="showFeedCountdown" class="countdown-pill bowl-countdown" @tap="goTo('/packageA/feed/index')">
						<view class="countdown-timer-icon" aria-hidden="true">
							<view class="countdown-timer-icon__crown"></view>
							<view class="countdown-timer-icon__body">
								<view class="countdown-timer-icon__hand"></view>
							</view>
						</view>
						<text class="countdown-copy">距下次投喂</text>
						<text class="countdown-time">{{ countdownText }}</text>
					</view>
				</view>
			</view>

			<view class="stat-card">
				<view v-for="item in meters" :key="item.label" class="stat-row">
					<text class="stat-icon">{{ item.icon }}</text>
					<text class="stat-label">{{ item.label }}</text>
					<view class="stat-track">
						<view class="stat-value" :style="{ width: item.value + '%', background: item.color }"></view>
					</view>
					<text class="stat-score">{{ item.value }}%</text>
				</view>
			</view>
			</template>
		</view>

		<view v-if="showBowlPicker" class="item-picker-mask" @tap="closeBowlPicker">
			<view class="item-picker-card" @tap.stop>
				<text class="item-picker-title">选择物品</text>
				<view class="item-picker-row">
					<view class="item-picker-arrow">‹</view>
					<view class="item-card" @tap="goFeedWelfare">
						<view class="item-icon-wrap feed-entry">
							<image class="item-icon-img" src="/static/island/bowl-food.png" mode="aspectFit"></image>
						</view>
						<text class="item-card-name">投喂</text>
					</view>
					<view class="item-card primary" @tap="confirmBowlItem">
						<view class="item-badge love">❤+1</view>
						<view class="item-badge stock">∞</view>
						<view class="item-icon-wrap">
							<text class="item-emoji">{{ bowlPickerType === 'water' ? '💧' : '🍚' }}</text>
						</view>
						<text class="item-card-name">{{ bowlPickerType === 'water' ? '凉白开' : '剩饭菜' }}</text>
					</view>
					<view class="item-picker-arrow">›</view>
				</view>
			</view>
			<view class="item-picker-close" @tap="closeBowlPicker">✕</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref, onUnmounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { fetchIslandInfo, feedPet } from '@/common/api/island.js';
import { ensureLogin } from '@/common/auth-guard.js';
import { resolveMediaUrl } from '@/common/media.js';
import { resolveIslandPetVisual } from '@/common/pet-avatar.js';

const defaultLeftActions = [{
	title: '投喂',
	emoji: '🍗',
	path: '/packageA/feed/index',
	actionType: 'navigate',
	enabled: true
}, {
	title: '家养',
	emoji: '🏠',
	path: '/packageA/home/setup',
	actionType: 'navigate',
	enabled: true
}, {
	title: '任务',
	emoji: '📋',
	path: '/packageA/tasks/center',
	actionType: 'navigate',
	enabled: true
}, {
	title: '集市',
	emoji: '🛍️',
	path: '/packageA/market/list',
	actionType: 'navigate',
	enabled: true
}];

const defaultRightActions = [{
	title: '发现新朋友',
	emoji: '🤝',
	path: '/packageA/post/create',
	actionType: 'navigate',
	enabled: true
}, {
	title: '退休领养',
	emoji: '🏆',
	path: '/packageA/adopt/list',
	actionType: 'navigate',
	enabled: true
}, {
	title: '人气打榜',
	emoji: '🎖️',
	path: '/packageA/ranking/list',
	actionType: 'navigate',
	enabled: true
}, {
	title: '添加企微',
	emoji: '💼',
	path: '/packageA/wecom/index',
	actionType: 'navigate',
	enabled: true
}, {
	title: '养宠攻略',
	emoji: '📖',
	path: '/packageA/strategy/list',
	actionType: 'navigate',
	enabled: true
}];

const isLoggedIn = ref(false);
const pageLoading = ref(false);
const loadError = ref('');
const island = ref({});
const countdownText = ref('0:00');
const countdownSeconds = ref(0);
const moodEmoji = ref('🥰');
const statusText = ref('等待你的投喂');
const petDisplayName = ref('岛宠');
const petAvatar = ref('/static/pets/island-pet-shiba.png');
const petAvatarFallback = ref('/static/pets/island-pet-shiba.png');
const residentPetId = ref(null);
const petAnimKind = ref('shiba');
const petSpecies = ref('dog');
const weatherText = ref('室内 24℃ · 湿度 55%');
const intimacyLevel = ref(1);
const intimacyPercent = ref(0);
const dailyProgress = ref(50);
const displayPoints = ref(0);
const displayFlowers = ref(0);
const displayLove = ref(0);
const leftActionList = ref([...defaultLeftActions]);
const rightActionList = ref([...defaultRightActions]);
const feeding = ref(false);
/** @type {import('vue').Ref<'idle'|'eat'|'drink'>} */
const petAction = ref('idle');
const feedFxType = ref('food');
const feedFxToken = ref(0);
const foodBowlFilled = ref(false);
const waterBowlFilled = ref(false);
const showBowlPicker = ref(false);
/** @type {import('vue').Ref<'food'|'water'>} */
const bowlPickerType = ref('food');
const loveBump = ref(false);
const loveFloatVisible = ref(false);
const loveFloatAmount = ref(1);
const loveFloatToken = ref(0);
let countdownTimer = null;
let eatingTimer = null;
let loveBumpTimer = null;
let loveFloatTimer = null;

const BOWL_STATE_KEY = 'island_bowl_filled_state';

const readBowlState = () => {
	try {
		const raw = uni.getStorageSync(BOWL_STATE_KEY);
		if (!raw) return null;
		const data = typeof raw === 'string' ? JSON.parse(raw) : raw;
		if (!data || !data.expireAt || Date.now() > Number(data.expireAt)) return null;
		return data;
	} catch (e) {
		return null;
	}
};

const writeBowlState = (food, water, cooldownSeconds = 0) => {
	// 冷却中：按冷却时长过期；仅喂一边时：缓存到次日，避免刷新丢失
	const ttlMs = Math.max(0, Number(cooldownSeconds) || 0) * 1000;
	const expireAt = ttlMs > 0 ? Date.now() + ttlMs : Date.now() + 24 * 60 * 60 * 1000;
	uni.setStorageSync(BOWL_STATE_KEY, {
		food: !!food,
		water: !!water,
		expireAt
	});
};

const clearBowlState = () => {
	uni.removeStorageSync(BOWL_STATE_KEY);
	foodBowlFilled.value = false;
	waterBowlFilled.value = false;
};

const statusPriorityList = [{
	key: 'hunger',
	text: '等待喂食中'
}, {
	key: 'clean',
	text: '等待清洁'
}, {
	key: 'mood',
	text: '等待陪伴'
}, {
	key: 'vitality',
	text: '等待玩耍'
}];

const statusByLowestMeter = computed(() => {
	const meterValues = {
		hunger: Number(meters.value?.[0]?.value ?? 0),
		clean: Number(meters.value?.[1]?.value ?? 0),
		mood: Number(meters.value?.[2]?.value ?? 0),
		vitality: Number(meters.value?.[3]?.value ?? 0)
	};
	let current = null;
	statusPriorityList.forEach(item => {
		const value = meterValues[item.key];
		if (current === null || value < current.value) {
			current = {
				value,
				text: item.text
			};
		}
	});
	return current?.text || statusText.value;
});

const showFeedCountdown = computed(() => countdownSeconds.value > 0);
const eatingStatusText = computed(() => {
	if (petAction.value === 'eat') return '正在吃饭…';
	if (petAction.value === 'drink') return '正在喝水…';
	return statusByLowestMeter.value;
});
const petActionEmoji = computed(() => {
	if (petAction.value === 'eat') return '😋';
	if (petAction.value === 'drink') return '😗';
	return '😊';
});
const displayMoodEmoji = computed(() => {
	const hungerValue = Number(meters.value?.[0]?.value ?? 0);
	if (petAction.value === 'eat') return '😋';
	if (petAction.value === 'drink') return '😌';
	if (hungerValue <= 20) return '😫';
	if (hungerValue <= 45) return '🥺';
	return moodEmoji.value || '🥰';
});

const meters = ref([{
	icon: '🍽️',
	label: '饱食度',
	value: 0,
	color: '#f2cb64'
}, {
	icon: '🛁',
	label: '清洁度',
	value: 0,
	color: '#c6e0ef'
}, {
	icon: '😊',
	label: '心情值',
	value: 0,
	color: '#f7c0cb'
}, {
	icon: '⚡',
	label: '活力值',
	value: 0,
	color: '#bfe0c8'
}]);

const applyIslandInfo = (info = {}) => {
	island.value = info;
	meters.value[0].value = info.hunger ?? 0;
	meters.value[1].value = info.clean ?? 0;
	meters.value[2].value = info.mood ?? 0;
	if (meters.value[3]) meters.value[3].value = info.vitality ?? 0;
	countdownText.value = info.feedCooldownText || '0:00';
	countdownSeconds.value = Math.max(0, Number(info.feedCooldownSeconds) || 0);
	statusText.value = info.statusText || '等待你的投喂';
	moodEmoji.value = info.moodEmoji || '🥰';
	petDisplayName.value = info.petName || '岛宠';
	residentPetId.value = info.residentPetId || info.residents?.[0]?.id || null;
	const visual = resolveIslandPetVisual({
		petAvatar: info.petAvatar,
		breed: info.breed || info.residents?.[0]?.breed,
		petType: info.petType || info.residents?.[0]?.petType
	}, resolveMediaUrl);
	petAvatar.value = visual.src;
	petAvatarFallback.value = visual.fallbackSrc || visual.src;
	petAnimKind.value = visual.kind;
	petSpecies.value = visual.species;
	weatherText.value = info.weatherText || '室内 24℃ · 湿度 55%';
	intimacyLevel.value = info.intimacyLevel ?? 1;
	dailyProgress.value = Math.max(0, Math.min(100, Number(info.dailyProgress ?? info.intimacyPercent ?? 50)));
	intimacyPercent.value = dailyProgress.value;
	displayPoints.value = info.points ?? 0;
	displayFlowers.value = info.flowers ?? 0;
	displayLove.value = info.loveValue ?? 0;
	if (info.leftActions?.length) leftActionList.value = info.leftActions;
	if (info.rightActions?.length) rightActionList.value = info.rightActions;

	const cooling = countdownSeconds.value > 0;
	const cached = readBowlState();
	if (typeof info.foodBowlFilled === 'boolean') {
		foodBowlFilled.value = !!info.foodBowlFilled;
	} else if (cached) {
		foodBowlFilled.value = !!cached.food;
	} else {
		foodBowlFilled.value = false;
	}
	if (typeof info.waterBowlFilled === 'boolean') {
		waterBowlFilled.value = !!info.waterBowlFilled;
	} else if (cached) {
		waterBowlFilled.value = !!cached.water;
	} else {
		waterBowlFilled.value = false;
	}
	if (!cooling && !foodBowlFilled.value && !waterBowlFilled.value) {
		clearBowlState();
	} else {
		writeBowlState(foodBowlFilled.value, waterBowlFilled.value, countdownSeconds.value);
	}
	startCountdown(info.feedCooldownSeconds || 0);
};

const onPetImageError = () => {
	const fallback = petAvatarFallback.value || '/static/pets/island-pet-shiba.png';
	if (petAvatar.value !== fallback) {
		petAvatar.value = fallback;
	}
};

const formatCooldown = (seconds) => {
	const sec = Math.max(0, Number(seconds) || 0);
	const min = Math.floor(sec / 60);
	const remain = sec % 60;
	return `${min}:${String(remain).padStart(2, '0')}`;
};

const startCountdown = (seconds) => {
	clearCountdown();
	let remain = Math.max(0, Number(seconds) || 0);
	countdownSeconds.value = remain;
	countdownText.value = formatCooldown(remain);
	if (remain <= 0) {
		if (island.value) {
			island.value.canFeed = true;
			island.value.canFeedFood = !foodBowlFilled.value;
			island.value.canFeedWater = !waterBowlFilled.value;
		}
		// 无冷却时不清空碗状态（可能只喂了一边）
		return;
	}
	countdownTimer = setInterval(() => {
		remain -= 1;
		countdownSeconds.value = Math.max(0, remain);
		if (remain <= 0) {
			countdownText.value = '0:00';
			if (island.value) {
				island.value.canFeed = true;
				island.value.canFeedFood = true;
				island.value.canFeedWater = true;
			}
			clearBowlState();
			clearCountdown();
			return;
		}
		countdownText.value = formatCooldown(remain);
	}, 1000);
};

const clearCountdown = () => {
	if (countdownTimer) {
		clearInterval(countdownTimer);
		countdownTimer = null;
	}
};

const triggerLoveFx = (amount = 1) => {
	const gain = Math.max(1, Number(amount) || 1);
	loveFloatAmount.value = gain;
	loveFloatToken.value += 1;
	loveFloatVisible.value = true;
	loveBump.value = true;
	if (loveBumpTimer) clearTimeout(loveBumpTimer);
	if (loveFloatTimer) clearTimeout(loveFloatTimer);
	loveBumpTimer = setTimeout(() => {
		loveBump.value = false;
		loveBumpTimer = null;
	}, 700);
	loveFloatTimer = setTimeout(() => {
		loveFloatVisible.value = false;
		loveFloatTimer = null;
	}, 1200);
};

const triggerFeedAnimation = (feedType = 'food') => {
	const isWater = feedType === 'water';
	feedFxType.value = isWater ? 'water' : 'food';
	feedFxToken.value += 1;
	petAction.value = isWater ? 'drink' : 'eat';
	if (eatingTimer) clearTimeout(eatingTimer);
	eatingTimer = setTimeout(() => {
		petAction.value = 'idle';
		eatingTimer = null;
	}, isWater ? 1600 : 1500);
};

const loadIslandData = async () => {
	const token = uni.getStorageSync('token');
	isLoggedIn.value = !!token;
	if (!token) {
		pageLoading.value = false;
		loadError.value = '';
		return;
	}
	pageLoading.value = true;
	loadError.value = '';
	try {
		const info = await fetchIslandInfo();
		if (info) applyIslandInfo(info);
	} catch (e) {
		loadError.value = e?.msg || '岛宅数据加载失败';
	} finally {
		pageLoading.value = false;
	}
};

const goLogin = () => {
	ensureLogin('/pages/island/island', true);
};

const openBowlPicker = (feedType) => {
	if (!ensureLogin('/pages/island/island', true)) return;
	const type = feedType === 'water' ? 'water' : 'food';
	if (countdownSeconds.value > 0) {
		uni.showToast({ title: `请等待 ${countdownText.value}`, icon: 'none' });
		return;
	}
	if (type === 'water' && (waterBowlFilled.value || island.value?.canFeedWater === false)) {
		uni.showToast({ title: '水盆本轮已喂，请先喂饭或等待冷却', icon: 'none' });
		return;
	}
	if (type === 'food' && (foodBowlFilled.value || island.value?.canFeedFood === false)) {
		uni.showToast({ title: '饭盆本轮已喂，请先喂水或等待冷却', icon: 'none' });
		return;
	}
	bowlPickerType.value = type;
	showBowlPicker.value = true;
};

const closeBowlPicker = () => {
	showBowlPicker.value = false;
};

const goFeedWelfare = () => {
	closeBowlPicker();
	goTo('/packageA/feed/index');
};

const confirmBowlItem = () => {
	const type = bowlPickerType.value;
	closeBowlPicker();
	doFeed(type);
};

const doFeed = async (feedType = '') => {
	if (!ensureLogin('/pages/island/island', true)) return;
	if (feeding.value) return;
	if (countdownSeconds.value > 0) {
		uni.showToast({ title: `请等待 ${countdownText.value}`, icon: 'none' });
		return;
	}
	if (feedType === 'water' && (waterBowlFilled.value || island.value?.canFeedWater === false)) {
		uni.showToast({ title: '水盆本轮已喂', icon: 'none' });
		return;
	}
	if (feedType === 'food' && (foodBowlFilled.value || island.value?.canFeedFood === false)) {
		uni.showToast({ title: '饭盆本轮已喂', icon: 'none' });
		return;
	}
	feeding.value = true;
	const loveBefore = Number(displayLove.value || 0);
	try {
		const info = await feedPet(feedType);
		if (info) {
			triggerFeedAnimation(feedType || 'food');
			const loveGain = Number(info.loveGain || (Number(info.loveValue) - loveBefore) || 1);
			if (feedType === 'water') {
				waterBowlFilled.value = true;
			} else if (feedType === 'food') {
				foodBowlFilled.value = true;
			} else {
				foodBowlFilled.value = true;
				waterBowlFilled.value = true;
			}
			applyIslandInfo({
				...info,
				foodBowlFilled: foodBowlFilled.value || !!info.foodBowlFilled,
				waterBowlFilled: waterBowlFilled.value || !!info.waterBowlFilled
			});
			writeBowlState(foodBowlFilled.value, waterBowlFilled.value, info.feedCooldownSeconds || 0);
			triggerLoveFx(loveGain);
			const bothDone = foodBowlFilled.value && waterBowlFilled.value;
			uni.showToast({
				title: bothDone ? `都喂完啦 爱心+${loveGain}` : `喂养成功 爱心+${loveGain}`,
				icon: 'none'
			});
		}
	} catch (e) {
		uni.showToast({ title: e?.msg || '投喂失败', icon: 'none' });
	} finally {
		feeding.value = false;
	}
};

const handleAction = (item) => {
	if (!item) return;
	if (item.enabled === false) {
		uni.showToast({ title: '暂不可用', icon: 'none' });
		return;
	}
	if (item.actionType === 'feed' || item.title === '投喂') {
		goTo(item.path || '/packageA/feed/index');
		return;
	}
	if (item.actionType === 'feed_water') {
		openBowlPicker('water');
		return;
	}
	if (item.actionType === 'feed_food') {
		openBowlPicker('food');
		return;
	}
	if (item.path) goTo(item.path);
};

const goTo = url => {
	if (!ensureLogin(url)) return;
	uni.navigateTo({ url });
};

onShow(loadIslandData);
onUnmounted(() => {
	clearCountdown();
	if (eatingTimer) clearTimeout(eatingTimer);
	if (loveBumpTimer) clearTimeout(loveBumpTimer);
	if (loveFloatTimer) clearTimeout(loveFloatTimer);
});
</script>

<style lang="scss">
	.island-page {
		background: linear-gradient(180deg, #f9efe2 0%, #f8efe4 45%, #f7eadb 100%);
	}

	.island-shell {
		position: relative;
		min-height: calc(100vh - 88rpx);
		padding: 18rpx 20rpx 34rpx;
		box-sizing: border-box;
		overflow: hidden;
	}

	.island-state-box {
		min-height: 60vh;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		padding: 40rpx;
	}

	.island-state-title {
		font-size: 32rpx;
		font-weight: 700;
		color: #8b613d;
	}

	.island-state-text {
		font-size: 24rpx;
		color: #b38a63;
		text-align: center;
		line-height: 1.6;
	}

	.island-login-btn {
		margin-top: 12rpx;
		padding: 0 40rpx;
	}

	.top-ornaments {
		position: relative;
		height: 146rpx;
		margin-bottom: 6rpx;
	}

	.clock-tile {
		position: absolute;
		left: 20rpx;
		top: 22rpx;
		width: 70rpx;
		height: 70rpx;
		border-radius: 50%;
		border: 4rpx solid #d29d68;
		background: #fffaf2;
		box-shadow: 0 10rpx 20rpx rgba(182, 143, 101, 0.1);
	}

	.clock-ring {
		position: relative;
		width: 100%;
		height: 100%;
	}

	.clock-ring::before,
	.clock-ring::after {
		content: '';
		position: absolute;
		left: 50%;
		top: 50%;
		background: #b78a5d;
		transform-origin: center bottom;
		border-radius: 999rpx;
	}

	.clock-ring::before {
		width: 4rpx;
		height: 18rpx;
		transform: translate(-50%, -100%) rotate(0deg);
		animation: clock-minute 30s linear infinite;
	}

	.clock-ring::after {
		width: 4rpx;
		height: 14rpx;
		transform: translate(-50%, -100%) rotate(90deg);
		animation: clock-hour 120s linear infinite;
	}

	.clock-tile::after {
		content: '';
		position: absolute;
		left: 50%;
		top: 50%;
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: #b78a5d;
		transform: translate(-50%, -50%);
	}

	.mini-tile {
		position: absolute;
		width: 46rpx;
		height: 46rpx;
		border-radius: 10rpx;
		border: 3rpx solid #d7a777;
		background: #fffaf3;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 22rpx;
		color: #8f6642;
		box-shadow: 0 8rpx 16rpx rgba(182, 143, 101, 0.1);
	}

	.mini-tile.paw {
		left: 108rpx;
		top: 18rpx;
	}

	.mini-tile.star {
		left: 166rpx;
		top: 18rpx;
	}

	.mini-tile.bone {
		left: 108rpx;
		top: 76rpx;
	}

	.resource-row {
		position: absolute;
		right: 86rpx;
		top: 12rpx;
		display: flex;
		gap: 12rpx;
		align-items: center;
		z-index: 2;
	}

	.resource-pill {
		height: 44rpx;
		padding: 0 14rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.96);
		box-shadow: 0 8rpx 18rpx rgba(179, 143, 103, 0.1);
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 20rpx;
		font-weight: 700;
		color: #b58554;
	}

	.resource-pill.love.bump {
		animation: love-bump 0.65s ease;
	}

	.resource-icon {
		font-size: 24rpx;
	}

	.gift-block {
		position: absolute;
		right: 10rpx;
		top: 8rpx;
		width: 74rpx;
		height: 108rpx;
		border-radius: 10rpx;
		background: #f6cfd6;
		overflow: hidden;
		box-shadow: 0 12rpx 22rpx rgba(211, 145, 158, 0.18);
	}

	.gift-ribbon {
		position: absolute;
		left: 26rpx;
		top: 0;
		width: 22rpx;
		height: 100%;
		background: #bfe0ef;
	}

	.gift-ribbon.horizontal {
		left: 0;
		top: 42rpx;
		width: 100%;
		height: 18rpx;
	}

	.gift-sun {
		position: absolute;
		left: 24rpx;
		top: 64rpx;
		font-size: 22rpx;
	}

	.playfield {
		position: relative;
		min-height: 760rpx;
	}

	.side-actions {
		position: absolute;
		top: 120rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		z-index: 5;
	}

	.side-actions.left {
		left: 2rpx;
	}

	.side-actions.right {
		right: 2rpx;
	}

	.side-action {
		width: 112rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
	}

	.side-icon {
		width: 76rpx;
		height: 76rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 38rpx;
		box-shadow: 0 12rpx 20rpx rgba(186, 146, 102, 0.16);
	}

	.left-tone {
		background: linear-gradient(180deg, #c78c57, #a66d42);
	}

	.right-tone {
		background: linear-gradient(180deg, #f2d365, #e1b542);
	}

	.side-icon.disabled {
		opacity: 0.45;
	}

	.side-text {
		font-size: 20rpx;
		line-height: 1.3;
		text-align: center;
		color: #b38152;
		font-weight: 700;
	}

	.center-stage {
		position: relative;
		width: 100%;
		padding: 96rpx 120rpx 0;
		box-sizing: border-box;
		text-align: center;
		z-index: 2;
	}

	.stage-topline {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		margin-bottom: 6rpx;
	}

	.mood-face {
		font-size: 36rpx;
	}

	.affection-label {
		font-size: 20rpx;
		color: #d16874;
		font-weight: 700;
	}

	.affection-level {
		font-size: 20rpx;
		color: #b38a63;
		font-weight: 700;
	}

	.affection-track {
		width: 220rpx;
		height: 10rpx;
		margin: 0 auto 16rpx;
		border-radius: 999rpx;
		background: #eadfda;
		overflow: hidden;
	}

	.affection-value {
		height: 100%;
		background: linear-gradient(90deg, #f7c0c9, #f6d9de);
		border-radius: inherit;
	}

	.speech-bubble {
		width: 270rpx;
		height: 68rpx;
		margin: 0 auto 16rpx;
		border-radius: 999rpx;
		background: #fff;
		box-shadow: 0 12rpx 22rpx rgba(179, 143, 103, 0.12);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		font-size: 24rpx;
		color: #8e6848;
	}

	.speech-bubble.chewing {
		animation: bubble-bounce 0.7s ease;
	}

	.speech-emoji {
		font-size: 28rpx;
	}

	.stage-sparkle,
	.floating-flower {
		position: absolute;
	}

	.stage-sparkle {
		font-size: 22rpx;
		color: #f0ce6a;
	}

	.stage-sparkle.left {
		left: 156rpx;
		top: 220rpx;
	}

	.stage-sparkle.right {
		right: 152rpx;
		top: 312rpx;
	}

	.floating-flower {
		font-size: 24rpx;
	}

	.floating-flower.left {
		left: 170rpx;
		top: 338rpx;
	}

	.floating-flower.right {
		right: 126rpx;
		top: 184rpx;
	}

	.pet-actor {
		position: relative;
		width: 260rpx;
		height: 260rpx;
		margin: 0 auto;
		display: flex;
		align-items: flex-end;
		justify-content: center;
	}

	.pet-shadow {
		position: absolute;
		left: 50%;
		bottom: 8rpx;
		width: 132rpx;
		height: 28rpx;
		border-radius: 50%;
		background: rgba(160, 120, 80, 0.22);
		transform: translateX(-50%) scaleX(1);
		z-index: 0;
		pointer-events: none;
	}

	.pet-body {
		position: relative;
		z-index: 1;
		width: 236rpx;
		height: 236rpx;
		transform-origin: center bottom;
		will-change: transform;
	}

	.pet-image {
		width: 100%;
		height: 100%;
		display: block;
	}

	/* —— 待机：不同形象不同动效 —— */
	.pet-actor.action-idle .pet-body {
		animation: pet-idle-dog 2.4s ease-in-out infinite;
	}

	.pet-actor.action-idle .pet-shadow {
		animation: shadow-idle-dog 2.4s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-corgi .pet-body,
	.pet-actor.action-idle.kind-frenchie .pet-body {
		animation: pet-idle-bounce 1.8s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-corgi .pet-shadow,
	.pet-actor.action-idle.kind-frenchie .pet-shadow {
		animation: shadow-idle-bounce 1.8s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-golden .pet-body,
	.pet-actor.action-idle.kind-samoyed .pet-body {
		animation: pet-idle-fluffy 2.8s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-golden .pet-shadow,
	.pet-actor.action-idle.kind-samoyed .pet-shadow {
		animation: shadow-idle-fluffy 2.8s ease-in-out infinite;
	}

	.pet-actor.action-idle.species-cat .pet-body {
		animation: pet-idle-cat 3s ease-in-out infinite;
	}

	.pet-actor.action-idle.species-cat .pet-shadow {
		animation: shadow-idle-cat 3s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-ragdoll .pet-body {
		animation: pet-idle-ragdoll 3.4s ease-in-out infinite;
	}

	.pet-actor.action-idle.kind-ragdoll .pet-shadow {
		animation: shadow-idle-ragdoll 3.4s ease-in-out infinite;
	}

	/* —— 吃饭 —— */
	.pet-actor.action-eat .pet-body {
		animation: pet-eat 0.55s ease-in-out 2;
	}

	.pet-actor.action-eat .pet-shadow {
		animation: shadow-eat 0.55s ease-in-out 2;
	}

	.pet-actor.action-eat.species-cat .pet-body {
		animation: pet-eat-cat 0.6s ease-in-out 2;
	}

	/* —— 喝水 —— */
	.pet-actor.action-drink .pet-body {
		animation: pet-drink 0.7s ease-in-out 2;
	}

	.pet-actor.action-drink .pet-shadow {
		animation: shadow-drink 0.7s ease-in-out 2;
	}

	.pet-actor.action-drink.species-cat .pet-body {
		animation: pet-drink-cat 0.75s ease-in-out 2;
	}

	.feed-fx-layer {
		position: absolute;
		left: 50%;
		top: 180rpx;
		width: 300rpx;
		height: 260rpx;
		transform: translateX(-50%);
		pointer-events: none;
		z-index: 6;
	}

	.feed-crumb,
	.pet-heart-burst {
		position: absolute;
		display: block;
	}

	.feed-crumb {
		left: 50%;
		bottom: 8rpx;
		font-size: 28rpx;
		opacity: 0;
		transform: translate3d(-50%, 0, 0) scale(0.8);
	}

	.feed-crumb.food {
		text-shadow: 0 6rpx 12rpx rgba(209, 160, 98, 0.18);
	}

	.feed-crumb.water {
		text-shadow: 0 6rpx 12rpx rgba(107, 170, 208, 0.18);
	}

	.feed-crumb.crumb-a {
		animation: crumb-fly-a 0.92s ease-out forwards;
	}

	.feed-crumb.crumb-b {
		animation: crumb-fly-b 0.96s ease-out forwards;
	}

	.feed-crumb.crumb-c {
		animation: crumb-fly-c 1s ease-out forwards;
	}

	.fx-water .feed-crumb.crumb-a {
		animation: drop-fly-a 1.05s ease-out forwards;
	}

	.fx-water .feed-crumb.crumb-b {
		animation: drop-fly-b 1.1s ease-out forwards;
	}

	.fx-water .feed-crumb.crumb-c {
		animation: drop-fly-c 1.15s ease-out forwards;
	}

	.water-ripple {
		position: absolute;
		left: 50%;
		bottom: 36rpx;
		width: 36rpx;
		height: 36rpx;
		margin-left: -18rpx;
		border: 3rpx solid rgba(110, 180, 220, 0.55);
		border-radius: 50%;
		opacity: 0;
		transform: scale(0.4);
	}

	.water-ripple.r1 {
		animation: ripple-expand 1s ease-out 0.15s forwards;
	}

	.water-ripple.r2 {
		animation: ripple-expand 1s ease-out 0.35s forwards;
	}

	.water-ripple.r3 {
		animation: ripple-expand 1s ease-out 0.55s forwards;
	}

	.pet-heart-burst {
		left: 50%;
		top: 12rpx;
		font-size: 34rpx;
		opacity: 0;
		transform: translateX(-50%) scale(0.5);
		animation: pet-burst 0.7s ease-out 0.38s forwards;
	}

	.love-float {
		position: absolute;
		left: 50%;
		top: 120rpx;
		z-index: 8;
		font-size: 30rpx;
		font-weight: 800;
		color: #e86b7a;
		text-shadow: 0 4rpx 10rpx rgba(232, 107, 122, 0.28);
		transform: translateX(-50%);
		pointer-events: none;
		animation: love-float-up 1.15s ease-out forwards;
	}

	.pet-name-pill {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		min-width: 120rpx;
		height: 44rpx;
		padding: 0 24rpx;
		margin-top: 4rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.92);
		color: #b17e4f;
		font-size: 22rpx;
		font-weight: 700;
		box-shadow: 0 8rpx 18rpx rgba(179, 143, 103, 0.08);
	}

	.scene-decor {
		position: relative;
		height: 200rpx;
		margin-top: 88rpx;
	}

	.floor-line {
		position: absolute;
		left: 88rpx;
		right: 88rpx;
		bottom: 48rpx;
		height: 6rpx;
		background: linear-gradient(90deg, rgba(214, 187, 152, 0) 0%, rgba(214, 187, 152, 0.9) 18%, rgba(214, 187, 152, 0.9) 82%, rgba(214, 187, 152, 0) 100%);
		border-radius: 999rpx;
	}

	.decor-card {
		position: absolute;
		border: 3rpx solid #d0b18f;
		background: rgba(236, 219, 201, 0.5);
		border-radius: 8rpx;
	}

	.decor-card.tall {
		left: 8rpx;
		bottom: 22rpx;
		width: 86rpx;
		height: 118rpx;
	}

	.plant {
		position: absolute;
		left: 6rpx;
		top: -24rpx;
		font-size: 30rpx;
	}

	.lamp {
		position: absolute;
		left: 50rpx;
		top: 34rpx;
		width: 8rpx;
		height: 44rpx;
		background: #cfa36e;
		border-radius: 999rpx;
	}

	.lamp::before {
		content: '';
		position: absolute;
		left: -11rpx;
		top: -14rpx;
		width: 30rpx;
		height: 18rpx;
		background: #f3d269;
		border-radius: 18rpx 18rpx 8rpx 8rpx;
	}

	.pig {
		position: absolute;
		left: 4rpx;
		bottom: -6rpx;
		font-size: 28rpx;
	}

	.decor-card.shelf {
		left: 126rpx;
		bottom: 26rpx;
		width: 112rpx;
		height: 70rpx;
		padding: 8rpx;
		box-sizing: border-box;
	}

	.flowerpot {
		position: absolute;
		left: -10rpx;
		top: -26rpx;
		font-size: 26rpx;
	}

	.shelf-grid {
		width: 100%;
		height: 100%;
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		grid-template-rows: repeat(2, 1fr);
		gap: 4rpx;
		font-size: 16rpx;
		align-items: center;
		justify-items: center;
		color: #876447;
	}

	.yarn-ball {
		position: absolute;
		right: 132rpx;
		bottom: 34rpx;
		font-size: 28rpx;
	}

	.leaf-mark {
		position: absolute;
		right: 56rpx;
		bottom: 30rpx;
		font-size: 28rpx;
	}

	.floating-weather {
		position: relative;
		right: auto;
		bottom: auto;
		display: inline-flex;
		align-items: center;
		gap: 10rpx;
		max-width: 420rpx;
		padding: 10rpx 20rpx 10rpx 14rpx;
		border-radius: 999rpx;
		background: rgba(214, 230, 239, 0.92);
		box-shadow: 0 8rpx 16rpx rgba(154, 192, 214, 0.14);
	}

	.weather-thermo {
		position: relative;
		width: 18rpx;
		height: 28rpx;
		flex-shrink: 0;
	}

	.weather-thermo__stem {
		position: absolute;
		left: 50%;
		top: 0;
		width: 8rpx;
		height: 18rpx;
		margin-left: -4rpx;
		border-radius: 999rpx;
		border: 2rpx solid #8aa3b3;
		background: #f7fbfd;
		box-sizing: border-box;
		overflow: hidden;
		z-index: 1;
	}

	.weather-thermo__level {
		position: absolute;
		left: 1rpx;
		right: 1rpx;
		bottom: 0;
		height: 70%;
		border-radius: 999rpx;
		background: #e86b5a;
	}

	.weather-thermo__bulb {
		position: absolute;
		left: 50%;
		bottom: 0;
		width: 14rpx;
		height: 14rpx;
		margin-left: -7rpx;
		border-radius: 50%;
		background: #e86b5a;
		border: 2rpx solid #8aa3b3;
		box-sizing: border-box;
		z-index: 2;
	}

	.weather-copy {
		font-size: 20rpx;
		line-height: 1.2;
		color: #7a93a3;
		white-space: nowrap;
	}

	.feed-strip {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
		padding: 0 8rpx;
		position: relative;
		z-index: 3;
	}

	.feed-tools {
		display: flex;
		gap: 18rpx;
		align-items: flex-end;
	}

	.feed-status {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		justify-content: flex-end;
		gap: 12rpx;
		padding-top: 0;
		flex-shrink: 0;
	}

	.feed-tool {
		width: 120rpx;
		position: relative;
		text-align: center;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
	}

	.bowl-stage {
		width: 108rpx;
		height: 96rpx;
		position: relative;
		display: flex;
		align-items: flex-end;
		justify-content: center;
		filter: drop-shadow(0 8rpx 12rpx rgba(160, 120, 80, 0.16));
		transition: transform 0.25s ease;
	}

	.feed-tool.feeding .bowl-stage {
		animation: bowl-bounce 0.55s ease-in-out 2;
	}

	.bowl-body {
		width: 92rpx;
		height: 58rpx;
		border-radius: 8rpx 8rpx 28rpx 28rpx;
		position: relative;
		overflow: hidden;
		border: 3rpx solid rgba(90, 70, 50, 0.35);
		box-sizing: border-box;
	}

	.water-bowl .bowl-body {
		background: linear-gradient(180deg, #8ec8ef 0%, #5aa4d8 100%);
	}

	.food-bowl .bowl-body {
		background: linear-gradient(180deg, #f3d27a 0%, #e2b24a 100%);
	}

	.bowl-inner {
		position: absolute;
		left: 8rpx;
		right: 8rpx;
		top: 10rpx;
		bottom: 8rpx;
		border-radius: 6rpx 6rpx 22rpx 22rpx;
		background: rgba(255, 255, 255, 0.35);
	}

	.feed-tool:not(.filled) .bowl-inner {
		background: rgba(255, 255, 255, 0.18);
	}

	.bowl-fill {
		position: absolute;
		left: 10rpx;
		right: 10rpx;
		bottom: 10rpx;
		border-radius: 6rpx 6rpx 18rpx 18rpx;
		overflow: hidden;
		animation: fill-pop 0.45s ease-out;
	}

	.water-fill {
		height: 28rpx;
		background: linear-gradient(180deg, #dff4ff 0%, #7fc3ef 100%);
	}

	.water-shine {
		position: absolute;
		left: 12rpx;
		top: 6rpx;
		width: 26rpx;
		height: 8rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.75);
	}

	.food-fill {
		height: 34rpx;
		background: linear-gradient(180deg, #fff8ea 0%, #f0d9a2 100%);
	}

	.food-mound {
		position: absolute;
		left: 50%;
		top: -10rpx;
		width: 54rpx;
		height: 28rpx;
		margin-left: -27rpx;
		border-radius: 50%;
		background: radial-gradient(circle at 40% 40%, #fffdf8, #f2dfb5 70%);
		border: 2rpx solid rgba(180, 140, 90, 0.25);
		box-sizing: border-box;
	}

	.food-leaf {
		position: absolute;
		right: 14rpx;
		top: 4rpx;
		width: 14rpx;
		height: 10rpx;
		border-radius: 50%;
		background: #7fc07a;
		transform: rotate(-18deg);
	}

	.bowl-badge {
		position: absolute;
		z-index: 2;
	}

	.bowl-badge.drop {
		left: 50%;
		bottom: 10rpx;
		transform: translateX(-50%);
		font-size: 22rpx;
		opacity: 0.85;
	}

	.feed-tool.filled .bowl-badge.drop {
		opacity: 0;
	}

	.bowl-badge.dog-tag {
		left: 50%;
		bottom: 12rpx;
		transform: translateX(-50%);
		font-size: 14rpx;
		font-weight: 800;
		letter-spacing: 0.04em;
		color: rgba(120, 80, 30, 0.7);
	}

	.feed-tool-text {
		font-size: 20rpx;
		font-weight: 700;
		color: #b48354;
	}

	.feed-tool.filled .feed-tool-text {
		color: #d08a45;
	}

	@keyframes bowl-bounce {
		0%,
		100% {
			transform: translateY(0) scale(1);
		}
		40% {
			transform: translateY(-8rpx) scale(1.04);
		}
		70% {
			transform: translateY(2rpx) scale(0.98);
		}
	}

	@keyframes fill-pop {
		0% {
			opacity: 0;
			transform: scaleY(0.3);
			transform-origin: bottom center;
		}
		100% {
			opacity: 1;
			transform: scaleY(1);
			transform-origin: bottom center;
		}
	}

	.countdown-pill {
		min-height: 56rpx;
		padding: 12rpx 22rpx;
		border-radius: 999rpx;
		border: 2rpx solid #ead2b3;
		background: rgba(255, 251, 245, 0.98);
		display: inline-flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		box-shadow: 0 8rpx 16rpx rgba(182, 143, 101, 0.08);
	}

	.countdown-timer-icon {
		position: relative;
		width: 28rpx;
		height: 30rpx;
		flex-shrink: 0;
	}

	.countdown-timer-icon__crown {
		position: absolute;
		left: 50%;
		top: 0;
		width: 10rpx;
		height: 6rpx;
		margin-left: -5rpx;
		border-radius: 2rpx 2rpx 0 0;
		background: #c08a55;
	}

	.countdown-timer-icon__crown::before,
	.countdown-timer-icon__crown::after {
		content: '';
		position: absolute;
		top: 2rpx;
		width: 6rpx;
		height: 4rpx;
		border-radius: 999rpx;
		background: #c08a55;
	}

	.countdown-timer-icon__crown::before {
		left: -6rpx;
	}

	.countdown-timer-icon__crown::after {
		right: -6rpx;
	}

	.countdown-timer-icon__body {
		position: absolute;
		left: 50%;
		bottom: 0;
		width: 24rpx;
		height: 24rpx;
		margin-left: -12rpx;
		border-radius: 50%;
		border: 3rpx solid #c08a55;
		box-sizing: border-box;
		background: transparent;
	}

	.countdown-timer-icon__hand {
		position: absolute;
		left: 50%;
		top: 4rpx;
		width: 2rpx;
		height: 8rpx;
		margin-left: -1rpx;
		border-radius: 999rpx;
		background: #c08a55;
		transform-origin: center bottom;
		transform: rotate(28deg);
	}

	.countdown-copy {
		font-size: 22rpx;
		font-weight: 600;
		color: #b78456;
		line-height: 1;
	}

	.countdown-time {
		font-size: 24rpx;
		font-weight: 700;
		color: #b07a48;
		line-height: 1;
		letter-spacing: 0.02em;
	}

	.stat-card {
		margin-top: 40rpx;
		padding: 24rpx 20rpx;
		background: rgba(255, 252, 247, 0.96);
		border-radius: 28rpx;
		border: 2rpx solid rgba(236, 215, 191, 0.82);
		box-shadow: 0 12rpx 24rpx rgba(179, 143, 103, 0.08);
	}

	.stat-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 16rpx;
	}

	.stat-row:last-child {
		margin-bottom: 0;
	}

	.stat-icon {
		width: 30rpx;
		text-align: center;
		font-size: 24rpx;
	}

	.stat-label {
		width: 88rpx;
		font-size: 24rpx;
		color: #8f795f;
		font-weight: 600;
	}

	.stat-track {
		flex: 1;
		height: 10rpx;
		background: #eae0d5;
		border-radius: 999rpx;
		overflow: hidden;
	}

	.stat-value {
		height: 100%;
		border-radius: inherit;
	}

	.stat-score {
		width: 60rpx;
		text-align: right;
		font-size: 22rpx;
		font-weight: 700;
		color: #b07f53;
	}

	@keyframes clock-minute {
		from {
			transform: translate(-50%, -100%) rotate(0deg);
		}
		to {
			transform: translate(-50%, -100%) rotate(360deg);
		}
	}

	@keyframes clock-hour {
		from {
			transform: translate(-50%, -100%) rotate(90deg);
		}
		to {
			transform: translate(-50%, -100%) rotate(450deg);
		}
	}

	@keyframes crumb-fly-a {
		0% {
			opacity: 0;
			transform: translate3d(-50%, 10rpx, 0) scale(0.75);
		}
		15% {
			opacity: 1;
		}
		75% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(-88rpx, -186rpx, 0) scale(1.06);
		}
	}

	@keyframes crumb-fly-b {
		0% {
			opacity: 0;
			transform: translate3d(-50%, 12rpx, 0) scale(0.75);
		}
		15% {
			opacity: 1;
		}
		75% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(-14rpx, -210rpx, 0) scale(1.08);
		}
	}

	@keyframes crumb-fly-c {
		0% {
			opacity: 0;
			transform: translate3d(-50%, 8rpx, 0) scale(0.75);
		}
		15% {
			opacity: 1;
		}
		75% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(58rpx, -176rpx, 0) scale(1.02);
		}
	}

	@keyframes drop-fly-a {
		0% {
			opacity: 0;
			transform: translate3d(-50%, -120rpx, 0) scale(0.7);
		}
		20% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(-70rpx, 8rpx, 0) scale(1.05);
		}
	}

	@keyframes drop-fly-b {
		0% {
			opacity: 0;
			transform: translate3d(-50%, -140rpx, 0) scale(0.65);
		}
		18% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(-8rpx, 14rpx, 0) scale(1.1);
		}
	}

	@keyframes drop-fly-c {
		0% {
			opacity: 0;
			transform: translate3d(-50%, -110rpx, 0) scale(0.7);
		}
		22% {
			opacity: 1;
		}
		100% {
			opacity: 0;
			transform: translate3d(56rpx, 6rpx, 0) scale(1);
		}
	}

	@keyframes ripple-expand {
		0% {
			opacity: 0.7;
			transform: scale(0.35);
		}
		100% {
			opacity: 0;
			transform: scale(2.4);
		}
	}

	@keyframes pet-idle-dog {
		0%,
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		35% {
			transform: translateY(-10rpx) rotate(-1.5deg) scale(1.02, 0.98);
		}
		70% {
			transform: translateY(-4rpx) rotate(1.2deg) scale(0.99, 1.015);
		}
	}

	@keyframes shadow-idle-dog {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.9;
		}
		35% {
			transform: translateX(-50%) scaleX(0.86);
			opacity: 0.65;
		}
		70% {
			transform: translateX(-50%) scaleX(0.94);
			opacity: 0.78;
		}
	}

	@keyframes pet-idle-bounce {
		0%,
		100% {
			transform: translateY(0) scale(1, 1);
		}
		40% {
			transform: translateY(-14rpx) scale(1.03, 0.96);
		}
		55% {
			transform: translateY(0) scale(0.97, 1.04);
		}
		70% {
			transform: translateY(-6rpx) scale(1.01, 0.99);
		}
	}

	@keyframes shadow-idle-bounce {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.9;
		}
		40% {
			transform: translateX(-50%) scaleX(0.78);
			opacity: 0.55;
		}
		55% {
			transform: translateX(-50%) scaleX(1.08);
			opacity: 1;
		}
	}

	@keyframes pet-idle-fluffy {
		0%,
		100% {
			transform: translateY(0) rotate(0deg) scale(1);
		}
		50% {
			transform: translateY(-12rpx) rotate(2deg) scale(1.035);
		}
	}

	@keyframes shadow-idle-fluffy {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.88;
		}
		50% {
			transform: translateX(-50%) scaleX(0.82);
			opacity: 0.6;
		}
	}

	@keyframes pet-idle-cat {
		0%,
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		25% {
			transform: translateY(-6rpx) rotate(-2.5deg) scale(1.01, 0.99);
		}
		50% {
			transform: translateY(-2rpx) rotate(0deg) scale(1.02, 0.98);
		}
		75% {
			transform: translateY(-8rpx) rotate(2.2deg) scale(1.01, 0.99);
		}
	}

	@keyframes shadow-idle-cat {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.85;
		}
		50% {
			transform: translateX(-50%) scaleX(0.9);
			opacity: 0.68;
		}
	}

	@keyframes pet-idle-ragdoll {
		0%,
		100% {
			transform: translateY(0) scale(1, 1) rotate(0deg);
		}
		40% {
			transform: translateY(-8rpx) scale(1.02, 0.985) rotate(-1deg);
		}
		70% {
			transform: translateY(-3rpx) scale(0.995, 1.01) rotate(1deg);
		}
	}

	@keyframes shadow-idle-ragdoll {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.85;
		}
		40% {
			transform: translateX(-50%) scaleX(0.88);
			opacity: 0.62;
		}
	}

	@keyframes pet-eat {
		0% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		22% {
			transform: translateY(10rpx) rotate(-6deg) scale(1.04, 0.92);
		}
		45% {
			transform: translateY(4rpx) rotate(4deg) scale(0.97, 1.04);
		}
		68% {
			transform: translateY(12rpx) rotate(-4deg) scale(1.03, 0.94);
		}
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
	}

	@keyframes pet-eat-cat {
		0% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		30% {
			transform: translateY(12rpx) rotate(-8deg) scale(1.02, 0.94);
		}
		55% {
			transform: translateY(6rpx) rotate(3deg) scale(0.98, 1.03);
		}
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
	}

	@keyframes shadow-eat {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.88;
		}
		22%,
		68% {
			transform: translateX(-50%) scaleX(1.12);
			opacity: 1;
		}
		45% {
			transform: translateX(-50%) scaleX(0.95);
			opacity: 0.75;
		}
	}

	@keyframes pet-drink {
		0% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		25% {
			transform: translateY(16rpx) rotate(8deg) scale(1.02, 0.93);
		}
		50% {
			transform: translateY(12rpx) rotate(10deg) scale(1.01, 0.95);
		}
		70% {
			transform: translateY(18rpx) rotate(6deg) scale(1.03, 0.92);
		}
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
	}

	@keyframes pet-drink-cat {
		0% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
		28% {
			transform: translateY(14rpx) rotate(-10deg) scale(1.01, 0.94);
		}
		55% {
			transform: translateY(18rpx) rotate(-7deg) scale(1.02, 0.92);
		}
		100% {
			transform: translateY(0) rotate(0deg) scale(1, 1);
		}
	}

	@keyframes shadow-drink {
		0%,
		100% {
			transform: translateX(-50%) scaleX(1);
			opacity: 0.88;
		}
		30%,
		70% {
			transform: translateX(-50%) scaleX(1.14);
			opacity: 1;
		}
	}

	@keyframes bubble-bounce {
		0% {
			transform: scale(1);
		}
		35% {
			transform: scale(1.06);
		}
		70% {
			transform: scale(0.98);
		}
		100% {
			transform: scale(1);
		}
	}

	@keyframes pet-burst {
		0% {
			opacity: 0;
			transform: translateX(-50%) scale(0.5);
		}
		40% {
			opacity: 1;
			transform: translateX(-50%) scale(1.1);
		}
		100% {
			opacity: 0;
			transform: translateX(-50%) translateY(-20rpx) scale(0.88);
		}
	}

	@keyframes love-bump {
		0%,
		100% {
			transform: scale(1);
		}
		35% {
			transform: scale(1.12);
		}
		60% {
			transform: scale(0.96);
		}
	}

	@keyframes love-float-up {
		0% {
			opacity: 0;
			transform: translateX(-50%) translateY(12rpx) scale(0.8);
		}
		20% {
			opacity: 1;
			transform: translateX(-50%) translateY(0) scale(1.05);
		}
		100% {
			opacity: 0;
			transform: translateX(-50%) translateY(-56rpx) scale(0.95);
		}
	}

	.item-picker-mask {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		z-index: 1000;
		background: rgba(60, 40, 20, 0.42);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding-bottom: 80rpx;
		box-sizing: border-box;
	}

	.item-picker-card {
		width: 620rpx;
		padding: 36rpx 24rpx 42rpx;
		border-radius: 36rpx;
		background: #fff8ef;
		border: 6rpx solid #c9955d;
		box-shadow: 0 18rpx 40rpx rgba(140, 100, 60, 0.22), inset 0 0 0 4rpx #f3d7b2;
		position: relative;
		box-sizing: border-box;
	}

	.item-picker-title {
		display: block;
		text-align: center;
		font-size: 34rpx;
		font-weight: 800;
		color: #b07a48;
		margin-bottom: 28rpx;
	}

	.item-picker-row {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
	}

	.item-picker-arrow {
		width: 40rpx;
		text-align: center;
		font-size: 44rpx;
		color: rgba(170, 150, 130, 0.7);
		font-weight: 300;
	}

	.item-card {
		width: 210rpx;
		height: 230rpx;
		border-radius: 24rpx;
		background: #fff;
		border: 3rpx solid #ecd7bf;
		box-shadow: 0 10rpx 20rpx rgba(179, 143, 103, 0.1);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		position: relative;
		box-sizing: border-box;
	}

	.item-icon-wrap {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background: linear-gradient(180deg, #fff7ea, #f6e4cc);
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.item-icon-img {
		width: 88rpx;
		height: 88rpx;
	}

	.item-emoji {
		font-size: 56rpx;
		line-height: 1;
	}

	.item-card-name {
		font-size: 28rpx;
		font-weight: 700;
		color: #a87445;
	}

	.item-badge {
		position: absolute;
		z-index: 2;
		font-size: 20rpx;
		font-weight: 800;
		line-height: 1;
	}

	.item-badge.love {
		left: 12rpx;
		top: 12rpx;
		color: #e86b7a;
		background: rgba(255, 240, 244, 0.95);
		border-radius: 999rpx;
		padding: 6rpx 10rpx;
	}

	.item-badge.stock {
		right: 14rpx;
		top: 12rpx;
		color: #b07a48;
		font-size: 28rpx;
	}

	.item-picker-close {
		margin-top: 28rpx;
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.92);
		border: 4rpx solid #d7b48a;
		color: #c08a55;
		font-size: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 10rpx 22rpx rgba(140, 100, 60, 0.16);
	}

	@media screen and (max-width: 360px) {
		.center-stage {
			padding-left: 104rpx;
			padding-right: 104rpx;
		}

		.side-action {
			width: 100rpx;
		}

		.side-icon {
			width: 68rpx;
			height: 68rpx;
			font-size: 34rpx;
		}

		.side-text {
			font-size: 18rpx;
		}

		.resource-row {
			gap: 8rpx;
			right: 80rpx;
		}

		.resource-pill {
			padding: 0 10rpx;
			font-size: 18rpx;
		}
	}
</style>
