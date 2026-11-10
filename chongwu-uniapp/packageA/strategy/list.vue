<template>
	<view class="pet-page strategy-page">
		<view class="pet-shell">
			<view v-if="loading" class="state-box">
				<text class="pet-muted">加载中...</text>
			</view>
			<view v-else-if="errorMsg" class="state-box">
				<text class="pet-muted">{{ errorMsg }}</text>
				<view class="pet-button retry-btn" @tap="loadDetail">重新加载</view>
			</view>
			<template v-else>
				<view class="guide-banner">
					<view class="guide-banner-main">
						<text class="guide-title">养宠攻略</text>
						<view class="guide-sub-row">
							<text class="guide-subtitle">科学养宠，从了解开始</text>
							<text class="guide-paw">🐾</text>
						</view>
					</view>
					<view class="guide-books" aria-hidden="true">
						<view class="book book-a"></view>
						<view class="book book-b"></view>
						<view class="book book-c"></view>
					</view>
				</view>

				<scroll-view scroll-x class="category-scroll" :show-scrollbar="false">
					<view class="category-row">
						<view
							v-for="item in tabs"
							:key="String(item.id)"
							class="category-pill"
							:class="{ 'is-active': currentCategoryId === item.id }"
							@tap="switchCategory(item.id)"
						>
							<text class="category-icon">{{ item.icon }}</text>
							<text class="category-name">{{ item.name }}</text>
						</view>
					</view>
				</scroll-view>

				<view v-if="!strategyList.length" class="state-box">
					<text class="pet-muted">暂无攻略内容</text>
				</view>
				<view
					v-for="(item, index) in strategyList"
					:key="item.id"
					class="strategy-card"
					@tap="readArticle(item)"
				>
					<view class="strategy-dot" :class="'dot-' + (index % 3)"></view>
					<image
						v-if="item.coverUrl"
						class="strategy-cover"
						:src="item.coverUrl"
						mode="aspectFill"
					></image>
					<view v-else class="strategy-icon" :class="iconClass(item)">
						<text class="strategy-icon-emoji">{{ iconEmoji(item) }}</text>
					</view>
					<view class="strategy-main">
						<text class="strategy-title">{{ item.title }}</text>
						<text class="strategy-desc">{{ item.subTitle || '' }}</text>
						<view v-if="parseTags(item).length" class="tag-row">
							<text
								v-for="(tag, tagIndex) in parseTags(item)"
								:key="tag"
								class="strategy-tag"
								:class="'tag-' + (tagIndex % 3)"
							>{{ tag }}</text>
						</view>
						<view class="strategy-meta">
							<view class="read-link">
								<up-icon name="file-text" size="14" color="#c79662"></up-icon>
								<text>阅读全文</text>
							</view>
						</view>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onPullDownRefresh } from '@dcloudio/uni-app';
import { fetchStrategyCategories, fetchStrategyPage } from '@/common/api/strategy.js';
import { resolveMediaUrl } from '@/common/media.js';

const loading = ref(false);
const errorMsg = ref('');
const strategyList = ref([]);
const tabs = ref([{ id: null, name: '全部', icon: '🐾' }]);
const currentCategoryId = ref(null);

const CATEGORY_ICONS = {
	全部: '🐾',
	汪星人: '🐶',
	喵星人: '🐱',
	幼宠: '🍼',
	饮食: '🍖',
	健康: '🩺'
};

function categoryIcon(name) {
	return CATEGORY_ICONS[name] || '📖';
}

function parseTags(item) {
	const raw = String(item?.tag || '').trim();
	if (!raw) return [];
	return raw.split(/[,，、|/]/).map(s => s.trim()).filter(Boolean).slice(0, 3);
}

function detectTopic(item) {
	const text = `${item?.tag || ''} ${item?.title || ''} ${item?.subTitle || ''}`;
	if (/健康|生病|症状|医疗|疫苗|驱虫/.test(text)) return 'health';
	if (/饮食|鲜食|食谱|罐头|喂食|营养/.test(text)) return 'food';
	if (/喵|猫/.test(text)) return 'cat';
	if (/汪|狗|犬|柴犬/.test(text)) return 'dog';
	return 'guide';
}

function iconClass(item) {
	return `topic-${detectTopic(item)}`;
}

function iconEmoji(item) {
	const map = { dog: '🐶', cat: '🐱', food: '🍗', health: '🩺', guide: '📖' };
	return map[detectTopic(item)] || '📖';
}

async function loadCategories() {
	try {
		const list = await fetchStrategyCategories();
		const normalized = Array.isArray(list) ? list : [];
		tabs.value = [
			{ id: null, name: '全部', icon: '🐾' },
			...normalized.map(item => ({
				id: item.id,
				name: item.name,
				icon: categoryIcon(item.name)
			}))
		];
	} catch (e) {
		tabs.value = [{ id: null, name: '全部', icon: '🐾' }];
	}
}

async function loadList() {
	loading.value = true;
	errorMsg.value = '';
	try {
		const res = await fetchStrategyPage({ page: 1, limit: 50 });
		const all = (res?.records || res?.list || []).map(item => ({
			...item,
			coverUrl: item.coverUrl ? resolveMediaUrl(item.coverUrl, '') : ''
		}));
		if (currentCategoryId.value == null) {
			strategyList.value = all;
		} else {
			const tab = tabs.value.find(item => item.id === currentCategoryId.value);
			const categoryName = tab?.name || '';
			strategyList.value = all.filter(item =>
				item.categoryId === currentCategoryId.value
				|| parseTags(item).includes(categoryName)
			);
		}
	} catch (e) {
		strategyList.value = [];
		errorMsg.value = e?.msg || e?.message || '加载攻略失败';
		uni.showToast({ title: errorMsg.value, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function switchCategory(categoryId) {
	currentCategoryId.value = categoryId;
	loadList();
}

function readArticle(item) {
	if (!item?.id) return;
	uni.navigateTo({ url: `/packageA/strategy/detail?id=${item.id}` });
}

async function loadDetail() {
	await loadCategories();
	await loadList();
}

onMounted(loadDetail);

onPullDownRefresh(async () => {
	try {
		await loadDetail();
	} finally {
		uni.stopPullDownRefresh();
	}
});
</script>

<style lang="scss">
.strategy-page {
	background: #fdf8f2;
}

.guide-banner {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 20rpx;
	margin-top: 8rpx;
	margin-bottom: 28rpx;
	padding: 28rpx 30rpx;
	border-radius: 28rpx;
	border: 2rpx dashed #9fd4b8;
	background: linear-gradient(135deg, #d9f0e4 0%, #e8f7ef 100%);
}

.guide-banner-main {
	flex: 1;
	min-width: 0;
}

.guide-title {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #5f4a3a;
	line-height: 1.4;
}

.guide-sub-row {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-top: 10rpx;
}

.guide-subtitle {
	font-size: 24rpx;
	color: #8f7f70;
	line-height: 1.5;
}

.guide-paw {
	font-size: 22rpx;
}

.guide-books {
	position: relative;
	width: 96rpx;
	height: 84rpx;
	flex-shrink: 0;
}

.book {
	position: absolute;
	bottom: 8rpx;
	width: 28rpx;
	height: 64rpx;
	border-radius: 8rpx 8rpx 4rpx 4rpx;
	border: 2rpx solid rgba(95, 74, 58, 0.18);
}

.book-a {
	left: 8rpx;
	background: #f4c96a;
	transform: rotate(-10deg);
}

.book-b {
	left: 34rpx;
	height: 72rpx;
	background: #7eb89a;
	transform: rotate(2deg);
}

.book-c {
	left: 58rpx;
	background: #f3a6a0;
	transform: rotate(12deg);
}

.category-scroll {
	margin-bottom: 8rpx;
	white-space: nowrap;
}

.category-row {
	display: inline-flex;
	gap: 16rpx;
	padding: 4rpx 2rpx 20rpx;
}

.category-pill {
	display: inline-flex;
	align-items: center;
	gap: 8rpx;
	height: 64rpx;
	padding: 0 28rpx;
	border-radius: 999rpx;
	background: #f7f0e6;
	color: #6d4c38;
	font-size: 26rpx;
	flex-shrink: 0;
}

.category-pill.is-active {
	background: #8b613d;
	color: #fff;
	box-shadow: 0 10rpx 22rpx rgba(139, 97, 61, 0.22);
}

.category-icon {
	font-size: 24rpx;
	line-height: 1;
}

.category-name {
	line-height: 1;
}

.strategy-card {
	position: relative;
	display: flex;
	gap: 20rpx;
	margin-bottom: 22rpx;
	padding: 28rpx 26rpx 24rpx;
	border-radius: 32rpx;
	background: #fffdf9;
	border: 2rpx solid rgba(236, 215, 191, 0.45);
	box-shadow: 0 12rpx 32rpx rgba(163, 120, 79, 0.06);
}

.strategy-dot {
	position: absolute;
	left: -10rpx;
	top: 42rpx;
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
}

.dot-0 { background: #f8d57f; }
.dot-1 { background: #f3b6bc; }
.dot-2 { background: #9fd4b8; }

.strategy-icon,
.strategy-cover {
	width: 96rpx;
	height: 96rpx;
	border-radius: 28rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	border: 3rpx solid rgba(255, 255, 255, 0.8);
	overflow: hidden;
	background: #f8efdf;
}

.topic-dog { background: #fff1d6; }
.topic-cat { background: #ffe8ec; }
.topic-food { background: #fff0d8; }
.topic-health { background: #e7f6ee; }
.topic-guide { background: #f8efdf; }

.strategy-icon-emoji {
	font-size: 44rpx;
	line-height: 1;
}

.strategy-main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.strategy-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #5f4a3a;
	line-height: 1.45;
}

.strategy-desc {
	font-size: 24rpx;
	color: #9f8a77;
	line-height: 1.55;
}

.tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
	margin-top: 4rpx;
}

.strategy-tag {
	display: inline-flex;
	align-items: center;
	height: 40rpx;
	padding: 0 16rpx;
	border-radius: 999rpx;
	font-size: 22rpx;
	font-weight: 600;
}

.tag-0 {
	background: #f5d78e;
	color: #8b613d;
}

.tag-1 {
	background: #f8d4d9;
	color: #b56b76;
}

.tag-2 {
	background: #d8ecf6;
	color: #5f86a3;
}

.strategy-meta {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	margin-top: 8rpx;
}

.read-link {
	display: inline-flex;
	align-items: center;
	gap: 6rpx;
	font-size: 22rpx;
	color: #c79662;
	font-weight: 600;
}

.state-box {
	padding: 80rpx 0;
	text-align: center;
}

.retry-btn {
	margin: 24rpx auto 0;
	width: 240rpx;
}
</style>
