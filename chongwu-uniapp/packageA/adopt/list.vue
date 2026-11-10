<template>
	<view class="pet-page adopt-page">
		<view class="pet-shell">
			<!-- 顶部横幅 -->
			<view class="adopt-banner">
				<view class="adopt-banner__paws">
					<text class="adopt-banner__paw">🐾</text>
					<text class="adopt-banner__paw is-right">🐾</text>
				</view>
				<text class="adopt-banner__title">理性养宠 · 不离不弃</text>
				<text class="adopt-banner__desc">它们曾陪伴无数顾客度过欢乐时光，如今光荣退役，等待一个温暖的家</text>
			</view>

			<!-- 分类筛选 -->
			<view class="adopt-tabs">
				<view
					v-for="item in filters"
					:key="item.value"
					class="adopt-tab"
					:class="{ 'is-active': currentType === item.value }"
					@tap="switchType(item.value)"
				>
					<text v-if="item.icon" class="adopt-tab__icon">{{ item.icon }}</text>
					<text class="adopt-tab__text">{{ item.label }}</text>
				</view>
			</view>

			<!-- 搜索 -->
			<view class="adopt-search">
				<text class="adopt-search__icon">🔍</text>
				<input v-model="keyword" class="adopt-search__input" placeholder="搜索退役宠物名字..." @confirm="loadPets" />
			</view>
			<view class="adopt-meta-row">
				<text class="adopt-count">共 {{ visiblePets.length }} 只退役宠物等待领养</text>
				<text class="adopt-mine-link" @tap="goMine">我的领养申请 ›</text>
			</view>

			<!-- 列表 -->
			<view v-if="loading" class="adopt-state">加载中...</view>
			<view v-else-if="!visiblePets.length" class="adopt-state">暂无等待领养的退役宠物</view>

			<view v-else class="adopt-list">
				<view v-for="pet in visiblePets" :key="pet.id" class="adopt-card" @tap="goDetail(pet.id)">
					<view class="adopt-card__head">
						<view class="adopt-avatar">
							<image :src="pet.avatar || '/static/logo.png'" mode="aspectFill"></image>
						</view>
						<view class="adopt-main">
							<view class="adopt-name-row">
								<text class="adopt-name">{{ pet.name }}</text>
								<view class="adopt-honor">
									<text class="adopt-honor__star">★</text>
									<text class="adopt-honor__text">{{ pet.honor }}</text>
								</view>
							</view>
							<text class="adopt-info">{{ pet.breed }} · {{ pet.gender }} · {{ pet.age }} · {{ pet.weight }}</text>
							<view class="adopt-tags">
								<text v-for="tag in pet.tags" :key="tag" class="adopt-tag">{{ tag }}</text>
							</view>
						</view>
					</view>

					<view class="adopt-quote">“{{ pet.desc }}”</view>

					<view class="adopt-foot" @tap.stop>
						<view class="adopt-store">
							<text class="adopt-store__icon">🏠</text>
							<text class="adopt-store__text">{{ pet.store }}<text v-if="pet.storeIndex"> · 服役 {{ pet.storeIndex }} 年</text></text>
						</view>
						<view
							v-if="pet.applying"
							class="adopt-apply-btn is-pending"
							@tap="goMine"
						>申请中</view>
						<view
							v-else
							class="adopt-apply-btn"
							@tap="applyForPet(pet.id)"
						>申请领养</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import {
	computed,
	ref
} from 'vue';
import { fetchAdoptPetPage, fetchMyActiveApplyMap } from '@/common/api/adopt.js';
import { resolveMediaUrl } from '@/common/media.js';
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app';

const keyword = ref('');
const currentType = ref('all');
const loading = ref(false);
const pets = ref([]);
const applyMap = ref({});
const filters = [
	{ label: '全部', value: 'all', icon: '' },
	{ label: '汪星人', value: 'dog', icon: '🐶' },
	{ label: '喵星人', value: 'cat', icon: '🐱' }
];

const parseJsonArray = (val) => {
	if (Array.isArray(val)) return val;
	if (!val) return [];
	try {
		return JSON.parse(val);
	} catch (e) {
		return String(val).split(/[,、]/).filter(Boolean);
	}
};

const calcServiceYears = (createTime) => {
	if (!createTime) return 0;
	const year = new Date(String(createTime).replace(/-/g, '/')).getFullYear();
	if (Number.isNaN(year)) return 0;
	return Math.max(1, new Date().getFullYear() - year + 1);
};

const mapPet = (item = {}) => {
	const id = item.id;
	return {
		id,
		name: item.name || '',
		type: item.petType || item.type || '',
		breed: item.breed || '',
		gender: item.gender || '',
		age: item.ageDesc || item.age || '',
		weight: item.weightDesc || item.weight || '',
		store: item.storeName || item.store || '',
		honor: item.levelDesc || '优秀员工',
		desc: item.description || item.desc || '它正在等待一个温暖的新家。',
		tags: parseJsonArray(item.tags),
		avatar: resolveMediaUrl(item.avatarUrl || item.avatar, '/static/logo.png'),
		storeIndex: calcServiceYears(item.createTime),
		applying: !!applyMap.value[Number(id)]
	};
};

const loadPets = async () => {
	loading.value = true;
	try {
		const params = {
			page: 1,
			limit: 50,
			adoptable: 1,
			auditStatus: 1
		};
		if (currentType.value !== 'all') {
			params.petType = currentType.value;
		}
		if (keyword.value.trim()) {
			params.name = keyword.value.trim();
		}
		const [res, activeMap] = await Promise.all([
			fetchAdoptPetPage(params),
			fetchMyActiveApplyMap()
		]);
		applyMap.value = activeMap || {};
		pets.value = (res?.records || []).map(mapPet);
	} catch (e) {
		pets.value = [];
		uni.showToast({ title: e?.msg || e?.message || '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
};

const switchType = (value) => {
	currentType.value = value;
	loadPets();
};

const visiblePets = computed(() => pets.value);

const goDetail = id => {
	uni.navigateTo({
		url: `/packageA/adopt/detail?id=${id}`
	});
};

const applyForPet = id => {
	uni.navigateTo({
		url: `/packageA/adopt/form?id=${id}`
	});
};

const goMine = () => {
	uni.navigateTo({ url: '/packageA/adopt/mine' });
};

onShow(() => {
	loadPets();
});

onPullDownRefresh(async () => {
	try {
		await loadPets();
	} finally {
		uni.stopPullDownRefresh();
	}
});
</script>

<style lang="scss">
.adopt-page {
	--adopt-brown: #8b5e3c;
	--adopt-brown-soft: #c79662;
	--adopt-border: rgba(236, 215, 191, 0.55);
}

/* 顶部横幅 */
.adopt-banner {
	margin-top: 8rpx;
	padding: 30rpx 28rpx 28rpx;
	border-radius: 28rpx;
	background: linear-gradient(145deg, #fffaf4 0%, #fff3e6 100%);
	border: 2rpx solid rgba(188, 146, 109, 0.18);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.05);
	position: relative;
}

.adopt-banner__paws {
	display: flex;
	justify-content: space-between;
	margin-bottom: 6rpx;
}

.adopt-banner__paw {
	font-size: 30rpx;
	opacity: 0.6;
}

.adopt-banner__paw.is-right {
	transform: scaleX(-1);
}

.adopt-banner__title {
	display: block;
	font-size: 34rpx;
	font-weight: 800;
	color: var(--adopt-brown);
	line-height: 1.3;
}

.adopt-banner__desc {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	color: #a78f78;
	line-height: 1.5;
}

/* 分类 tabs */
.adopt-tabs {
	display: flex;
	gap: 18rpx;
	margin: 24rpx 0 18rpx;
}

.adopt-tab {
	flex: 1;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	height: 64rpx;
	padding: 0 20rpx;
	border-radius: 999rpx;
	border: 2rpx solid #e8d4ba;
	background: var(--pet-panel, #fffdf9);
	color: var(--adopt-brown-soft);
	font-size: 26rpx;
}

.adopt-tab.is-active {
	background: var(--adopt-brown);
	border-color: var(--adopt-brown);
	color: #fff;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(139, 94, 60, 0.22);
}

.adopt-tab__icon {
	font-size: 26rpx;
}

/* 搜索 */
.adopt-search {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 0 22rpx;
	height: 76rpx;
	border-radius: 999rpx;
	background: var(--pet-panel, #fffdf9);
	border: 2rpx solid var(--adopt-border);
}

.adopt-search__icon {
	font-size: 26rpx;
	opacity: 0.7;
}

.adopt-search__input {
	flex: 1;
	font-size: 26rpx;
	color: #3a2c20;
}

.adopt-meta-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin: 16rpx 4rpx 18rpx;
}

.adopt-count {
	font-size: 24rpx;
	color: #a78f78;
}

.adopt-mine-link {
	font-size: 24rpx;
	color: var(--adopt-brown-soft);
}

.adopt-state {
	text-align: center;
	padding: 50rpx 0;
	font-size: 26rpx;
	color: #a78f78;
}

/* 列表 */
.adopt-list > view + view {
	margin-top: 20rpx;
}

.adopt-card {
	background: var(--pet-panel, #fffdf9);
	border-radius: 28rpx;
	border: 2rpx solid var(--adopt-border);
	box-shadow: 0 10rpx 28rpx rgba(163, 120, 79, 0.05);
	padding: 24rpx;
}

.adopt-card__head {
	display: flex;
	gap: 20rpx;
}

.adopt-avatar {
	width: 110rpx;
	height: 110rpx;
	border-radius: 28rpx;
	overflow: hidden;
	flex-shrink: 0;
	background: #f5dfb1;
}

.adopt-avatar image {
	width: 100%;
	height: 100%;
}

.adopt-main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.adopt-name-row {
	display: flex;
	align-items: center;
	gap: 14rpx;
}

.adopt-name {
	font-size: 40rpx;
	font-weight: 800;
	color: #3a2c20;
	line-height: 1.2;
}

.adopt-honor {
	display: inline-flex;
	align-items: center;
	gap: 4rpx;
	padding: 4rpx 14rpx;
	border-radius: 999rpx;
	border: 2rpx solid #e6c878;
	background: #fff7e0;
}

.adopt-honor__star {
	font-size: 20rpx;
	color: #d9a93a;
}

.adopt-honor__text {
	font-size: 20rpx;
	font-weight: 700;
	color: #9a7c31;
}

.adopt-info {
	font-size: 24rpx;
	color: #8a7a68;
	line-height: 1.4;
}

.adopt-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.adopt-tag {
	padding: 4rpx 16rpx;
	border-radius: 999rpx;
	background: #f2efe9;
	font-size: 22rpx;
	color: #6b5a4a;
}

.adopt-quote {
	margin: 20rpx 0;
	padding: 22rpx;
	border-radius: 22rpx;
	border: 2rpx dashed #e2c39d;
	background: #fff8ef;
	line-height: 1.7;
	font-size: 25rpx;
	color: #7a5a45;
}

.adopt-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.adopt-store {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	min-width: 0;
}

.adopt-store__icon {
	font-size: 24rpx;
}

.adopt-store__text {
	font-size: 24rpx;
	color: #8a7a68;
}

.adopt-apply-btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 0 34rpx;
	height: 64rpx;
	border-radius: 999rpx;
	background: var(--adopt-brown);
	color: #fff;
	font-size: 26rpx;
	font-weight: 700;
	box-shadow: 0 10rpx 22rpx rgba(139, 94, 60, 0.22);
}

.adopt-apply-btn.is-pending {
	background: #f3e6d6;
	color: #9a7c5a;
	box-shadow: none;
	border: 2rpx solid #e2c39d;
}
</style>
