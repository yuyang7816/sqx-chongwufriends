<template>
	<view class="pet-page detail-page">
		<view v-if="pageLoading" class="detail-state-box">
			<view class="detail-skeleton hero-skeleton"></view>
			<view class="detail-skeleton card-skeleton"></view>
			<text class="detail-state-text">档案加载中...</text>
		</view>
		<view v-else-if="loadError" class="detail-state-box">
			<text class="detail-state-title">加载失败</text>
			<text class="detail-state-text">{{ loadError }}</text>
			<view class="pet-button detail-retry-btn" @tap="loadPet">重新加载</view>
		</view>
		<template v-else>
		<view class="detail-hero-wrap">
			<view class="detail-navbar">
				<up-navbar
					title=""
					:placeholder="false"
					:border="false"
					bgColor="transparent"
					leftIcon="arrow-left"
					leftIconColor="#8b613d"
					:autoBack="true"
				></up-navbar>
			</view>
			<image class="detail-hero" :src="pet.avatar || '/static/logo.png'" mode="aspectFill"></image>
			<view class="detail-hero-mask"></view>
		</view>
		<view class="detail-shell">
			<view class="detail-info-card pet-card">
				<view class="detail-title-row">
					<view class="detail-title-main">
						<view class="name-line">
							<text class="pet-title">{{ pet.name }}</text>
							<text class="gender-mark"
								:class="pet.gender === '男' ? 'is-male' : 'is-female'">{{ pet.gender === '男' ? '♂' : '♀' }}</text>
						</view>
						<text class="pet-subtitle">{{ pet.breed }} · {{ pet.age || '年龄待更新' }}</text>
						<text v-if="pet.likeCount > 0" class="pet-like-line">❤️ {{ formatLikeCount(pet.likeCount) }} 人喜欢</text>
					</view>
					<text class="pet-pill detail-level-pill">{{ pet.level || pet.statusLabel || '明星宠物' }}</text>
				</view>
				<view class="detail-meta-list">
					<view v-for="item in detailMeta" :key="item.label" class="detail-meta-item">
						<view class="detail-meta-icon">
							<image class="detail-meta-icon-image" :src="item.short" mode="aspectFit"></image>
						</view>
						<text class="detail-meta-label">{{ item.label }}：</text>
						<text class="detail-meta-value">{{ item.value }}</text>
					</view>
				</view>
			</view>

			<view class="detail-content-block">
				<view class="detail-pair-grid">
					<view class="pair-card pair-card-green">
						<text class="pair-title">爱做的事 ❤️</text>
						<text v-for="item in goodAtList" :key="item" class="pair-line">· {{ item }}</text>
					</view>
					<view class="pair-card pair-card-pink">
						<text class="pair-title">讨厌的事 💔</text>
						<text v-for="item in avoidList" :key="item" class="pair-line">· {{ item }}</text>
					</view>
				</view>

				<view v-if="pet.description" class="pet-card detail-desc">
					<text class="detail-desc-title">关于 {{ pet.name }}</text>
					<text class="detail-desc-text">{{ pet.description }}</text>
				</view>

				<view class="detail-album-section">
					<view class="detail-album-tabs">
						<view v-for="tab in albumTabs" :key="tab.value" class="detail-album-tab"
							:class="{ 'is-active': currentAlbum === tab.value }" @tap="currentAlbum = tab.value">
							{{ tab.label }}
						</view>
					</view>
					<view class="detail-album-grid">
						<view v-for="(item, index) in currentAlbumImages" :key="`${currentAlbum}-${index}`"
							class="detail-album-card">
							<image :src="item" mode="aspectFill"></image>
						</view>
					</view>
					<view v-if="!currentAlbumImages.length" class="album-empty">暂无相册照片</view>
				</view>
			</view>
		</view>
		<view class="detail-floating-bar">
			<view class="pet-button is-light action-btn" style="background: #FFFFFF;" @tap="goCommunity">社群互动</view>
			<view v-if="pet.adoptable" class="pet-button action-btn" @tap="goAdoptForm">申请领养</view>
		</view>
		</template>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchArchiveInfo } from '@/common/api/archive.js';
import { resolveMediaUrl } from '@/common/media.js';

const petId = ref(0);
const pageLoading = ref(true);
const loadError = ref('');
const albumTabs = [
	{ label: '随拍相册', value: 'daily' },
	{ label: '社群相册', value: 'social' }
];
const currentAlbum = ref('daily');
const pet = ref({
	id: 0,
	name: '',
	breed: '',
	gender: '',
	age: '',
	level: '',
	statusLabel: '',
	store: '',
	code: '',
	tags: [],
	goodAt: [],
	avoid: [],
	description: '',
	adoptable: 0,
	likeCount: 0,
	avatar: '/static/logo.png',
	dailyAlbum: [],
	socialAlbum: []
});

const parseJsonArray = (val) => {
	if (Array.isArray(val)) return val;
	if (!val) return [];
	try {
		return JSON.parse(val);
	} catch (e) {
		return String(val).split(/[,、]/).filter(Boolean);
	}
};

const mapPet = (data = {}) => ({
	id: data.id,
	name: data.name || '',
	breed: data.breed || '',
	gender: data.gender || '',
	age: data.ageDesc || data.age || '',
	level: data.levelDesc || data.level || '',
	statusLabel: data.statusLabel || '',
	store: data.storeName || data.store || '',
	code: data.petCode || data.code || '',
	tags: parseJsonArray(data.tags),
	goodAt: parseJsonArray(data.goodAt),
	avoid: parseJsonArray(data.avoidItems || data.avoid),
	description: data.description || data.desc || '',
	adoptable: Number(data.adoptable || 0),
	likeCount: Number(data.likeCount || 0),
	avatar: resolveMediaUrl(data.avatarUrl || data.avatar, '/static/logo.png'),
	dailyAlbum: parseJsonArray(data.dailyAlbum || data.albumImages).map((url) => resolveMediaUrl(url)),
	socialAlbum: parseJsonArray(data.socialAlbum || data.communityImages).map((url) => resolveMediaUrl(url))
});

const detailMeta = computed(() => [
	{
		label: '年龄',
		short: '/packageA/static/shengri.png',
		value: pet.value.age || '-'
	},
	{
		label: '性格',
		short: '/packageA/static/xingge.png',
		value: pet.value.tags.join('、') || '-'
	},
	{
		label: '所属',
		short: '/packageA/static/suoshu.png',
		value: pet.value.store || '-'
	},
	{
		label: '编号',
		short: '/packageA/static/bianhao.png',
		value: pet.value.code || '-'
	}
]);

const goodAtList = computed(() => (pet.value.goodAt.length ? pet.value.goodAt : ['喜欢陪你散步']));
const avoidList = computed(() => (pet.value.avoid.length ? pet.value.avoid : ['暂时还没有记录']));
const currentAlbumImages = computed(() => {
	const source = currentAlbum.value === 'daily' ? pet.value.dailyAlbum : pet.value.socialAlbum;
	return (source || []).filter(Boolean);
});

const loadPet = async () => {
	if (!petId.value) {
		pageLoading.value = false;
		loadError.value = '宠物不存在';
		return;
	}
	pageLoading.value = true;
	loadError.value = '';
	try {
		const data = await fetchArchiveInfo(petId.value);
		if (data) {
			pet.value = mapPet(data);
		} else {
			loadError.value = '档案不存在';
		}
	} catch (e) {
		loadError.value = e?.msg || e?.message || '加载档案失败';
	} finally {
		pageLoading.value = false;
	}
};

const formatLikeCount = (count) => {
	const num = Number(count || 0);
	if (num >= 10000) return `${(num / 10000).toFixed(1)}万`;
	return String(num);
};

onLoad((option) => {
	petId.value = Number(option.id || 0);
	loadPet();
});

const goCommunity = () => {
	uni.navigateTo({
		url: `/packageA/post/create?petId=${pet.value.id}`
	});
};

const goAdoptForm = () => {
	uni.navigateTo({
		url: `/packageA/adopt/form?id=${pet.value.id}`
	});
};
</script>

<style lang="scss">
.detail-page {
	min-height: 100vh;
	padding-bottom: 210rpx;
	background: linear-gradient(180deg, #fbf3ea 0%, #fff8f0 44%, #f8efdf 100%);
}

.detail-hero-wrap {
	position: relative;
	z-index: 0;
}

.detail-navbar {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	z-index: 3;
}

.detail-hero {
	width: 100%;
	height: 640rpx;
	display: block;
}

.detail-hero-mask {
	position: absolute;
	left: 0;
	right: 0;
	bottom: 0;
	height: 160rpx;
	background: linear-gradient(180deg, rgba(251, 243, 234, 0) 0%, rgba(251, 243, 234, 0.95) 100%);
}

.detail-shell {
	position: relative;
	z-index: 2;
	margin-top: -92rpx;
	padding: 0 24rpx;
}

.detail-info-card {
	padding: 34rpx 30rpx;
	border-radius: 38rpx;
	background: rgba(255, 252, 247, 0.98);
	box-shadow: 0 22rpx 46rpx rgba(159, 113, 71, 0.12);
}

.detail-content-block {
	padding-top: 22rpx;
}

.name-line {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.gender-mark {
	width: 38rpx;
	height: 38rpx;
	border-radius: 50%;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	font-weight: 700;
}

.is-male {
	color: #6b97ff;
	background: rgba(107, 151, 255, 0.14);
}

.is-female {
	color: #f2859a;
	background: rgba(242, 133, 154, 0.14);
}

.detail-title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 26rpx;
}

.detail-title-main {
	flex: 1;
	min-width: 0;
}

.detail-level-pill {
	flex-shrink: 0;
	height: 54rpx;
	padding: 0 24rpx;
	font-size: 22rpx;
	font-weight: 700;
	background: linear-gradient(135deg, #f7d99a, #e4b85e);
	color: #7d5027;
	border: none;
	box-shadow: 0 10rpx 20rpx rgba(228, 184, 94, 0.22);
}

.detail-meta-list {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 18rpx 20rpx;
}

.detail-meta-item {
	display: flex;
	align-items: center;
	min-width: 0;
	font-size: 24rpx;
	color: #8d755e;
}

.detail-meta-icon {
	width: 34rpx;
	height: 34rpx;
	margin-right: 12rpx;
	border-radius: 12rpx;
	background: #fff1df;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.detail-meta-icon-image {
	width: 22rpx;
	height: 22rpx;
	display: block;
}

.detail-meta-label {
	color: #a48b73;
	flex-shrink: 0;
}

.detail-meta-value {
	flex: 1;
	min-width: 0;
	color: #7a614f;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.detail-pair-grid {
	display: grid;
	grid-template-columns: repeat(2, minmax(0, 1fr));
	gap: 20rpx;
}

.pair-card {
	padding: 26rpx 24rpx;
	border-radius: 28rpx;
	min-height: 210rpx;
	border: 2rpx dashed rgba(205, 176, 145, 0.55);
	box-shadow: 0 16rpx 32rpx rgba(177, 130, 87, 0.08);
}

.pair-card-green {
	background: linear-gradient(180deg, #daf3e7 0%, #f4fbf7 100%);
}

.pair-card-pink {
	background: linear-gradient(180deg, #ffe5e8 0%, #fff6f6 100%);
}

.pair-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	margin-bottom: 18rpx;
	color: #6d4c38;
}

.pair-line {
	display: block;
	font-size: 24rpx;
	color: #8b735f;
	line-height: 1.9;
}

.detail-desc {
	margin-top: 20rpx;
	padding: 28rpx;
	background: rgba(255, 250, 244, 0.96);
}

.detail-desc-title {
	display: block;
	margin-bottom: 14rpx;
	font-size: 30rpx;
	font-weight: 700;
	color: #6d4c38;
}

.detail-desc-text {
	font-size: 26rpx;
	line-height: 1.8;
	color: #7d6756;
}

.detail-album-section {
	margin-top: 26rpx;
}

.detail-album-tabs {
	display: flex;
	justify-content: center;
	gap: 84rpx;
	margin-bottom: 22rpx;
}

.detail-album-tab {
	position: relative;
	padding-bottom: 18rpx;
	font-size: 28rpx;
	font-weight: 600;
	color: #b2a08e;
	flex: 1;
	text-align: center;
}

.detail-album-tab.is-active {
	color: #94653d;
}

.detail-album-tab.is-active::after {
	content: '';
	position: absolute;
	left: 50%;
	bottom: 0;
	width: 100%;
	height: 5rpx;
	border-radius: 999rpx;
	background: #c89461;
	transform: translateX(-50%);
}

.detail-album-grid {
	display: grid;
	grid-template-columns: repeat(3, minmax(0, 1fr));
	gap: 16rpx;
}

.detail-album-card {
	overflow: hidden;
	border-radius: 24rpx;
	background: #f3ebe2;
}

.detail-album-card image {
	width: 100%;
	height: 200rpx;
	display: block;
}

.album-empty {
	text-align: center;
	color: #b2a08e;
	font-size: 26rpx;
	padding: 40rpx 0;
}

.detail-floating-bar {
	position: fixed;
	left: 24rpx;
	right: 24rpx;
	z-index: 999;
	bottom: calc(24rpx + env(safe-area-inset-bottom));
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 18rpx;
	border-radius: 999rpx;
	backdrop-filter: blur(16rpx);
}

.action-btn {
	flex: 1;
	height: 88rpx;
	line-height: 88rpx;
}

.pet-like-line {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #e08b9a;
}

.detail-state-box {
	padding: 120rpx 48rpx 48rpx;
	text-align: center;
}

.detail-state-title {
	display: block;
	font-size: 32rpx;
	font-weight: 700;
	color: #8d6b4b;
	margin-bottom: 12rpx;
}

.detail-state-text {
	display: block;
	font-size: 26rpx;
	color: #b2a08e;
	line-height: 1.6;
}

.detail-retry-btn {
	display: inline-block;
	margin-top: 28rpx;
	padding: 0 48rpx;
	height: 76rpx;
	line-height: 76rpx;
}

.detail-skeleton {
	border-radius: 28rpx;
	background: linear-gradient(90deg, #f3eadc 25%, #faf4ea 50%, #f3eadc 75%);
	background-size: 200% 100%;
	animation: detail-shimmer 1.2s infinite;
}

.hero-skeleton {
	height: 420rpx;
	margin-bottom: 24rpx;
}

.card-skeleton {
	height: 220rpx;
}

@keyframes detail-shimmer {
	0% { background-position: 200% 0; }
	100% { background-position: -200% 0; }
}
</style>
