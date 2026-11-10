<template>
	<view class="pet-page">
		<view class="pet-shell">
			<text class="pet-page-title">常驻宠物设</text>
			<text class="pet-page-note">从你已领养的宠物中选择 1 只常驻岛宅，可随时切换。</text>
			<view v-if="pageLoading" class="state-box"><text class="state-text">加载中...</text></view>
			<view v-else-if="loadError" class="state-box">
				<text class="state-title">加载失败</text>
				<text class="state-text">{{ loadError }}</text>
				<view class="pet-button state-btn" @tap="loadPage">重试</view>
			</view>
			<template v-else>
			<text class="pet-section-chip">当前常驻</text>
			<view class="pet-card pet-dashed selected-card">
				<text class="selected-title">当前常驻（{{ selectedId ? 1 : 0 }}/1）</text>
				<view class="selected-row">
					<view class="selected-slot">
						<template v-if="selectedPet">
							<view class="selected-avatar" :style="{ background: selectedPet.tone }">
								<image :src="selectedPet.avatar" mode="aspectFit"></image>
							</view>
							<text class="selected-name">{{ selectedPet.name }}</text>
						</template>
						<template v-else>
							<up-icon name="plus" size="22" color="#d3a271"></up-icon>
							<text>添加常驻</text>
						</template>
					</view>
				</view>
			</view>

			<text class="pet-section-chip">可选宠物</text>
			<view v-if="!petsList.length" class="state-box">
				<text class="state-text">暂无已领养宠物，请先去申请领养并通过审核</text>
				<view class="pet-button state-btn" @tap="goAdopt">去领养</view>
			</view>
			<view v-else class="pet-grid-2 setup-grid">
				<view
					v-for="pet in petsList"
					:key="pet.id"
					class="setup-card"
					:class="{ 'is-active': isSelected(pet.id) }"
				>
					<view class="setup-avatar" :style="{ background: pet.tone }">
						<image :src="pet.avatar" mode="aspectFit"></image>
					</view>
					<text class="setup-name">{{ pet.name }}</text>
					<text class="setup-breed">{{ pet.breed || '品种待完善' }}</text>
					<text class="setup-store">{{ pet.store }}</text>
					<view
						class="setup-btn"
						:class="{ 'is-active': isSelected(pet.id), 'is-disabled': saving }"
						@tap="togglePet(pet)"
					>{{ isSelected(pet.id) ? '取消常驻' : '设为常驻' }}</view>
				</view>
			</view>

			<view class="pet-button is-block save-btn" :class="{ 'is-disabled': saving }" @tap="saveSetting">
				{{ saving ? '保存中...' : '完成并返回' }}
			</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { fetchIslandInfo, fetchResidentCandidates, saveResidents } from '@/common/api/island.js';
import { resolveMediaUrl } from '@/common/media.js';
import { ensureLogin } from '@/common/auth-guard.js';

const toneMap = ['#f7d97f', '#cdeedd', '#f7d5db', '#efe7da', '#d9efe7'];

const petsList = ref([]);
const selectedId = ref(null);
const pageLoading = ref(false);
const loadError = ref('');
const saving = ref(false);

const toId = (value) => {
	const n = Number(value);
	return Number.isFinite(n) && n > 0 ? n : null;
};

const mapPet = (item, index) => ({
	id: toId(item.id),
	name: item.name,
	breed: item.breed || '',
	gender: item.gender || '',
	store: item.storeName || '门店小伙伴',
	avatar: resolveMediaUrl(item.avatarUrl || item.avatar || '/static/logo.png'),
	tone: toneMap[index % toneMap.length]
});

const isSelected = (id) => toId(id) != null && toId(id) === selectedId.value;

const selectedPet = computed(() => petsList.value.find((item) => isSelected(item.id)) || null);

const loadPage = async () => {
	if (!ensureLogin('/packageA/home/setup', true)) return;
	pageLoading.value = true;
	loadError.value = '';
	try {
		const [pageRes, info] = await Promise.all([fetchResidentCandidates(), fetchIslandInfo()]);
		const records = pageRes?.records || pageRes?.list || [];
		petsList.value = records.map(mapPet).filter((item) => item.id != null);
		const fromResidents = (info?.residents || []).map((item) => toId(item.id)).find(Boolean);
		const ownedIds = new Set(petsList.value.map((item) => item.id));
		let current = fromResidents || toId(info?.residentPetId) || null;
		// 常驻不在自有列表时，默认第一只
		if (current != null && !ownedIds.has(current)) {
			current = null;
		}
		if (current == null && petsList.value.length) {
			current = petsList.value[0].id;
		}
		selectedId.value = current;
	} catch (e) {
		loadError.value = e?.msg || '加载失败';
	} finally {
		pageLoading.value = false;
	}
};

const goAdopt = () => {
	uni.navigateTo({ url: '/packageA/adopt/list' });
};

const persistResident = async (petIds, { navigateBack = false } = {}) => {
	if (saving.value) return;
	saving.value = true;
	try {
		const info = await saveResidents(petIds);
		const nextId = toId(info?.residentPetId) || toId(info?.residents?.[0]?.id) || (petIds[0] ?? null);
		selectedId.value = nextId;
		if (info?.petName) {
			uni.showToast({ title: `已设为 ${info.petName}`, icon: 'success' });
		} else if (!petIds.length) {
			uni.showToast({ title: '已取消常驻', icon: 'success' });
		} else {
			uni.showToast({ title: '设置已保存', icon: 'success' });
		}
		if (navigateBack) {
			setTimeout(() => uni.navigateBack(), 600);
		}
	} catch (e) {
		uni.showToast({ title: e?.msg || '保存失败', icon: 'none' });
		throw e;
	} finally {
		saving.value = false;
	}
};

const togglePet = async (pet) => {
	if (!ensureLogin('/packageA/home/setup', true)) return;
	if (saving.value) return;
	const id = toId(pet.id);
	if (!id) return;
	const nextIds = isSelected(id) ? [] : [id];
	const prev = selectedId.value;
	selectedId.value = nextIds[0] || null;
	try {
		await persistResident(nextIds);
	} catch (e) {
		selectedId.value = prev;
	}
};

const saveSetting = async () => {
	if (!ensureLogin('/packageA/home/setup', true)) return;
	try {
		await persistResident(selectedId.value ? [selectedId.value] : [], { navigateBack: true });
	} catch (e) {
		// toast 已在 persistResident 中处理
	}
};

onShow(loadPage);
</script>

<style lang="scss">
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

.selected-card {
	margin: 20rpx 0 8rpx;
}

.selected-title {
	display: block;
	font-size: 28rpx;
	font-weight: 700;
	color: #8b613d;
	margin-bottom: 18rpx;
}

.selected-row {
	display: flex;
}

.selected-slot {
	width: 100%;
	min-height: 180rpx;
	border-radius: 26rpx;
	border: 3rpx dashed #d7b28a;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	color: #b48e67;
	padding: 20rpx 0;
}

.selected-avatar {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
}

.selected-avatar image {
	width: 100%;
	height: 100%;
}

.selected-name {
	font-size: 28rpx;
	font-weight: 700;
	color: #8b613d;
}

.setup-grid {
	margin: 16rpx 0 28rpx;
}

.setup-card {
	text-align: center;
	padding: 28rpx 20rpx 22rpx;
	border-radius: 32rpx;
	background: #ffffff;
	border: 2rpx solid rgba(236, 215, 191, 0.55);
	box-shadow: 0 8rpx 20rpx rgba(163, 120, 79, 0.06);
}

.setup-card.is-active {
	border-color: #c79662;
	background: #fffaf4;
	box-shadow: 0 10rpx 24rpx rgba(199, 150, 98, 0.14);
}

.setup-avatar {
	width: 120rpx;
	height: 120rpx;
	margin: 0 auto 16rpx;
	border-radius: 50%;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
}

.setup-avatar image {
	width: 100%;
	height: 100%;
}

.setup-name {
	display: block;
	font-size: 36rpx;
	font-weight: 700;
	color: #8b613d;
	line-height: 1.3;
	margin-bottom: 8rpx;
}

.setup-breed {
	display: block;
	font-size: 24rpx;
	color: #b6a08c;
	line-height: 1.4;
	margin-bottom: 12rpx;
}

.setup-store {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	max-width: 100%;
	padding: 0 18rpx;
	height: 42rpx;
	border-radius: 999rpx;
	background: #f7eee3;
	font-size: 22rpx;
	color: #bf946d;
	margin-bottom: 18rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.setup-btn {
	width: 100%;
	height: 72rpx;
	border-radius: 999rpx;
	background: #c79662;
	color: #ffffff;
	font-size: 26rpx;
	font-weight: 600;
	display: flex;
	align-items: center;
	justify-content: center;
}

.setup-btn.is-active {
	background: #f3eadc;
	color: #b48e67;
}

.setup-btn.is-disabled,
.is-disabled {
	opacity: 0.6;
	pointer-events: none;
}

.save-btn {
	margin-bottom: 24rpx;
}
</style>
