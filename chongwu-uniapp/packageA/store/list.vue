<template>
	<view class="pet-page">
		<view class="pet-shell">
			<view class="switch-bar">
				<view class="switch-item" :class="{ active: currentType === 'dog' }" @tap="currentType = 'dog'">汪星人🐶
				</view>
				<view class="switch-item" :class="{ active: currentType === 'cat' }" @tap="currentType = 'cat'">喵星人🐱
				</view>
			</view>

			<view class="pet-grid-2 store-grid">
				<view v-for="pet in visiblePets" :key="pet.id" class="pet-card store-card" @tap="goDetail(pet.id)">
					<view class="store-avatar" :style="{ background: pet.tone }">
						<image :src="pet.avatar || '/static/logo.png'" mode="aspectFit"></image>
					</view>
					<text class="store-pet-name">{{ pet.name }}</text>
					<text class="breed-chip">{{ pet.breed }}</text>
					<text class="meta-line"
						:class="pet.gender === '女' ? 'female' : 'male'">{{ pet.gender === '女' ? '♀' : '♂' }}
						{{ pet.gender }} · {{ pet.age }}</text>
					<text class="store-line">{{ pet.store }}</text>
					<view class="tag-row">
						<text v-for="tag in pet.tags.slice(0, 2)" :key="tag" class="trait-chip"
							:class="chipClass(tag)">{{ tag }}</text>
					</view>
					<view class="pet-button is-block look-btn">去看看👉</view>
				</view>
			</view>

			<view v-if="loading" class="pet-muted center-text">加载中...</view>
			<view v-else-if="loadError" class="empty-box">
				<text class="empty-title">加载失败</text>
				<text class="empty-hint">{{ loadError }}</text>
				<view class="pet-button empty-retry" @tap="loadPets">重新加载</view>
			</view>
			<view v-else-if="!visiblePets.length" class="empty-box">
				<text class="empty-title">暂无{{ currentType === 'dog' ? '汪星人' : '喵星人' }}小伙伴</text>
				<text class="empty-hint">试试切换上方「{{ currentType === 'dog' ? '喵星人' : '汪星人' }}」Tab 看看其他小伙伴</text>
			</view>
			<view class="foot-paws">
				<image src="/packageA/static/flmz.png" mode="widthFix"></image>
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
	import {
		fetchPets
	} from '@/common/api/home.js';
	import {
		resolveMediaUrl
	} from '@/common/media.js';
	import {
		onLoad,
		onPullDownRefresh
	} from '@dcloudio/uni-app';

	const currentType = ref('dog');
	const storeId = ref(null);
	const storeName = ref('');
	const pets = ref([]);
	const loading = ref(false);
	const loadError = ref('');

	const toneMap = ['#f7d97f', '#cdeedd', '#f7d5db', '#efe7da', '#d9efe7'];

	const parseJsonArray = (val) => {
		if (Array.isArray(val)) return val;
		if (!val) return [];
		try {
			return JSON.parse(val);
		} catch (e) {
			return String(val).split(/[,、]/).filter(Boolean);
		}
	};

	const mapPet = (item, index) => ({
		id: item.id,
		name: item.name,
		breed: item.breed,
		gender: item.gender,
		age: item.ageDesc || item.age,
		store: item.storeName || storeName.value,
		type: item.petType || item.type,
		tags: parseJsonArray(item.tags),
		avatar: resolveMediaUrl(item.avatarUrl || item.avatar, '/static/logo.png'),
		tone: toneMap[index % toneMap.length]
	});

	const loadPets = async () => {
		if (!storeId.value) return;
		loading.value = true;
		loadError.value = '';
		try {
			const list = await fetchPets(storeId.value, currentType.value);
			pets.value = (list || []).map(mapPet);
		} catch (e) {
			pets.value = [];
			loadError.value = e?.msg || '加载小伙伴失败';
			uni.showToast({
				title: loadError.value,
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	};

	watch(currentType, () => {
		loadPets();
	});

	const visiblePets = computed(() => pets.value);

	const chipClass = tag => {
		if (['活泼', '贪吃', '爱笑'].includes(tag)) return 'yellow';
		if (['温柔', '粘人', '亲人', '爱睡觉'].includes(tag)) return 'pink';
		return 'green';
	};

	const goDetail = id => {
		uni.navigateTo({
			url: `/packageA/archive/detail?id=${id}`
		});
	};

	onLoad(option => {
		storeId.value = Number(option.storeId || option.store || 0) || null;
		storeName.value = option.storeName ? decodeURIComponent(option.storeName) : (option.store || '');
		if (storeName.value) {
			uni.setNavigationBarTitle({ title: storeName.value });
		}
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
	.switch-bar {
		display: flex;
		padding: 8rpx;
		border-radius: 999rpx;
		background: #f3eadc;
		margin-top: 8rpx;
	}

	.switch-item {
		flex: 1;
		height: 72rpx;
		border-radius: 999rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 30rpx;
		font-weight: 700;
		color: #bc956d;
	}

	.switch-item.active {
		background: #c99767;
		color: #fff;
	}

	.store-grid {
		margin-top: 26rpx;
	}

	.store-card {
		text-align: center;
		padding: 30rpx 22rpx 24rpx;
		border-radius: 34rpx;
		background: rgba(255, 255, 255, 0.96);
	}

	.store-avatar {
		width: 118rpx;
		height: 118rpx;
		margin: 0 auto;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		overflow: hidden;
	}

	.store-avatar image {
		width: 118rpx;
		height: 118rpx;
		border-radius: 50%;

	}

	.store-pet-name {
		display: block;
		font-size: 42rpx;
		font-weight: 700;
		line-height: 1.3;
		margin-bottom: 14rpx;
	}

	.breed-chip {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 0 20rpx;
		height: 42rpx;
		border-radius: 999rpx;
		background: #f7eee3;
		font-size: 24rpx;
		color: #bf946d;
		margin-bottom: 12rpx;
	}

	.meta-line,
	.store-line {
		display: block;
		font-size: 26rpx;
		line-height: 1.6;
	}

	.meta-line.female {
		color: #f199a5;
	}

	.meta-line.male {
		color: #6da9ff;
	}

	.store-line {
		color: #a39288;
		margin-bottom: 16rpx;
	}

	.tag-row {
		display: flex;
		justify-content: center;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-bottom: 18rpx;
	}

	.trait-chip {
		padding: 0 16rpx;
		height: 38rpx;
		line-height: 38rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
	}

	.trait-chip.yellow {
		background: #fff3d6;
		color: #c99767;
	}

	.trait-chip.pink {
		background: #ffe8ec;
		color: #f199a5;
	}

	.trait-chip.green {
		background: #e8f6ef;
		color: #7ab89a;
	}

	.look-btn {
		margin-top: 8rpx;
	}

	.foot-paws {
		text-align: center;
		margin: 40rpx 0 20rpx;

		image {
			width: 200rpx;
			height: 60rpx;
		}
	}

	.center-text {
		text-align: center;
		margin: 24rpx 0;
	}

	.empty-box {
		text-align: center;
		padding: 48rpx 32rpx 24rpx;
	}

	.empty-title {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: #8d6b4b;
		margin-bottom: 12rpx;
	}

	.empty-hint {
		display: block;
		font-size: 26rpx;
		color: #b2a08e;
		line-height: 1.6;
	}

	.empty-retry {
		display: inline-block;
		margin-top: 24rpx;
		padding: 0 40rpx;
		height: 72rpx;
		line-height: 72rpx;
	}
</style>
