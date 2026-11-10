<template>
	<view class="pet-page detail-page">
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
					</view>
					<text class="pet-pill detail-level-pill">{{ pet.level || '明星宠物' }}</text>
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
						<view class="detail-album-card detail-album-card-add" @tap="addAlbumPhoto">
							<up-icon name="plus" color="#999999" size="28"></up-icon>
						</view>
					</view>
				</view>
			</view>
		</view>
		<view class="detail-floating-bar">
			<view class="pet-button is-light action-btn" @tap="choosePhoto" style="background: #FFFFFF;">拍摄</view>
			<view class="pet-button action-btn" @tap="goTo('/packageA/post/create')">社群互动</view>
			<view
				v-if="applying"
				class="detail-side-btn is-pending"
				@tap="goTo('/packageA/adopt/mine')"
			>
				<text class="detail-side-pending">申请中</text>
			</view>
			<view v-else class="detail-side-btn" @tap="goTo(`/packageA/adopt/form?id=${pet.id}`)">
				<image src="/packageA/static/jiaru.png"></image>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		fetchAdoptPetInfo,
		fetchMyActiveApplyMap
	} from '@/common/api/adopt.js';
	import { resolveMediaUrl } from '@/common/media.js';
	import {
		onLoad
	} from '@dcloudio/uni-app';

	const petId = ref(1);
	const applying = ref(false);
	const albumTabs = [{
			label: '随拍相册',
			value: 'daily'
		},
		{
			label: '社群相册',
			value: 'social'
		}
	];
	const currentAlbum = ref('daily');
	const pet = ref({
		id: 0,
		name: '',
		breed: '',
		gender: '',
		age: '',
		weight: '',
		level: '',
		store: '',
		code: '',
		tags: [],
		goodAt: [],
		avoid: [],
		description: '',
		birthday: '',
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
		weight: data.weightDesc || data.weight || '',
		level: data.levelDesc || data.level || '',
		store: data.storeName || data.store || '',
		code: data.petCode || data.code || '',
		tags: parseJsonArray(data.tags),
		goodAt: parseJsonArray(data.goodAt),
		avoid: parseJsonArray(data.avoidItems || data.avoid),
		description: data.description || data.desc || '',
		birthday: data.birthday || data.birthDate || data.birth || '',
		avatar: resolveMediaUrl(data.avatarUrl || data.avatar, '/static/logo.png'),
		dailyAlbum: parseJsonArray(data.dailyAlbum || data.albumImages).map(resolveMediaUrl),
		socialAlbum: parseJsonArray(data.socialAlbum || data.communityImages).map(resolveMediaUrl)
	});

	const buildGallery = (images = [], count = 6) => {
		const list = images.filter(Boolean);
		if (list.length >= count) {
			return list.slice(0, count);
		}
		const fallback = pet.value.avatar || '/static/logo.png';
		const result = list.slice();
		while (result.length < count) {
			result.push(fallback);
		}
		return result;
	};

	const detailMeta = computed(() => [{
			label: '生日',
			short: '/packageA/static/shengri.png',
			value: pet.value.birthday || pet.value.age || '-'
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

	const goodAtList = computed(() => pet.value.goodAt.length ? pet.value.goodAt : ['喜欢陪你散步']);
	const avoidList = computed(() => pet.value.avoid.length ? pet.value.avoid : ['暂时还没有记录']);
	const currentAlbumImages = computed(() => {
		const source = currentAlbum.value === 'daily' ? pet.value.dailyAlbum : pet.value.socialAlbum;
		return buildGallery(source);
	});

	const loadPet = async () => {
		try {
			const [data, activeMap] = await Promise.all([
				fetchAdoptPetInfo(petId.value),
				fetchMyActiveApplyMap()
			]);
			if (data) pet.value = mapPet(data);
			applying.value = !!activeMap[Number(petId.value)];
		} catch (e) {
			uni.showToast({
				title: '加载宠物信息失败',
				icon: 'none'
			});
		}
	};

	onLoad(option => {
		petId.value = Number(option.id || 1);
		loadPet();
	});

	const goTo = url => {
		uni.navigateTo({
			url
		});
	};

	const choosePhoto = () => {
		uni.chooseImage({
			count: 1,
			sourceType: ['camera'],
			sizeType: ['compressed'],
			success: res => {
				const tempFile = res.tempFilePaths?.[0];
				if (tempFile) {
					const targetKey = currentAlbum.value === 'daily' ? 'dailyAlbum' : 'socialAlbum';
					pet.value[targetKey] = [tempFile, ...pet.value[targetKey]].slice(0, 9);
				}
				uni.showToast({
					title: `已为${pet.value.name}拍摄一张照片`,
					icon: 'none'
				});
			},
			fail: err => {
				if (err?.errMsg?.includes('cancel')) return;
				uni.showToast({
					title: '打开相机失败',
					icon: 'none'
				});
			}
		});
	};

	const addAlbumPhoto = () => {
		uni.showToast({
			title: `已打开${pet.value.name}的相册上传入口`,
			icon: 'none'
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
		// width: 136rpx;
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

	.detail-album-card-add {
		height: 200rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(250, 243, 231, 1.0);
		border: 2rpx dashed rgba(200, 148, 97, 0.45);
	}

	.detail-album-add-icon {
		display: flex;
		align-items: center;
		justify-content: center;

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
		// background: rgba(255, 248, 240, 0.94);
		// box-shadow: 0 16rpx 36rpx rgba(136, 98, 65, 0.12);
		backdrop-filter: blur(16rpx);
	}

	.action-btn {
		flex: 1;
		height: 88rpx;
		line-height: 88rpx;
	}

	.detail-side-btn {
		width: 88rpx;
		height: 88rpx;

		image {
			width: 88rpx;
			height: 88rpx;
		}
	}

	.detail-side-btn.is-pending {
		width: auto;
		min-width: 120rpx;
		padding: 0 20rpx;
		border-radius: 999rpx;
		background: #f3e6d6;
		border: 2rpx solid #e2c39d;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.detail-side-pending {
		font-size: 24rpx;
		font-weight: 700;
		color: #9a7c5a;
	}

	@media screen and (max-width: 360px) {
		.detail-title-row {
			align-items: flex-start;
			flex-direction: column;
		}

		.detail-level-pill {
			align-self: flex-start;
		}

		.detail-meta-list,
		.detail-pair-grid,
		.detail-album-grid {
			grid-template-columns: 1fr;
		}

		.detail-album-tabs {
			gap: 48rpx;
		}
	}
</style>
