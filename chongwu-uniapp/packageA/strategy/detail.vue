<template>
	<view class="pet-page strategy-detail-page">
		<view class="pet-shell">
			<view v-if="loading" class="state-box">
				<text class="pet-muted">加载中...</text>
			</view>
			<view v-else-if="errorMsg" class="state-box">
				<text class="pet-muted">{{ errorMsg }}</text>
				<view class="pet-button retry-btn" @tap="loadDetail">重新加载</view>
			</view>
			<template v-else>
				<view class="pet-card article-card">
					<view v-if="tagList.length" class="tag-row">
						<text
							v-for="(tag, index) in tagList"
							:key="tag"
							class="article-tag"
							:class="'tag-' + (index % 3)"
						>{{ tag }}</text>
					</view>
					<text class="article-title">{{ article.title }}</text>
					<text v-if="article.subTitle" class="article-sub">{{ article.subTitle }}</text>
					<image
						v-if="coverUrl"
						class="article-cover"
						:src="coverUrl"
						mode="aspectFill"
						@tap="previewImage(coverUrl)"
					></image>
					<view class="article-body">
						<rich-text class="article-content" :nodes="contentNodes"></rich-text>
					</view>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchStrategyInfo } from '@/common/api/strategy.js';
import { resolveMediaUrl } from '@/common/media.js';

const loading = ref(false);
const errorMsg = ref('');
const article = ref({});
const strategyId = ref(null);

const IMG_STYLE = 'width:100%;border-radius:16px;margin:16px 0;display:block;';

const tagList = computed(() => {
	const raw = String(article.value.tag || '').trim();
	if (!raw) return ['养宠攻略'];
	return raw.split(/[,，、|/]/).map(s => s.trim()).filter(Boolean).slice(0, 3);
});

const coverUrl = computed(() => {
	const url = article.value.coverUrl;
	if (!url) return '';
	return resolveMediaUrl(url, '');
});

const contentNodes = computed(() => {
	let content = article.value.content || '';
	if (!content) return '<p>暂无正文内容</p>';
	if (!content.includes('<')) {
		content = content.split('\n').map(line => `<p>${line || '&nbsp;'}</p>`).join('');
	}
	content = enhanceImages(content);
	// 封面已单独展示时，去掉正文开头重复的首图
	if (coverUrl.value) {
		content = content.replace(
			/^\s*<img[^>]+src=["'][^"']+["'][^>]*>\s*/i,
			''
		);
	}
	return content;
});

function enhanceImages(html) {
	return String(html).replace(/<img\b([^>]*)>/gi, (full, attrs) => {
		let next = attrs;
		if (/src=["'](?!https?:\/\/|\/)/i.test(next)) {
			next = next.replace(/src=["']([^"']+)["']/i, (_, src) => `src="${resolveMediaUrl(src)}"`);
		} else if (/src=["'](\/static\/[^"']+)["']/i.test(next)) {
			next = next.replace(/src=["'](\/static\/[^"']+)["']/i, (_, src) => `src="${src}"`);
		}
		if (!/style=/i.test(next)) {
			next += ` style="${IMG_STYLE}"`;
		} else if (!/width\s*:/i.test(next)) {
			next = next.replace(/style=(["'])(.*?)\1/i, (_, q, style) => `style=${q}${style};${IMG_STYLE}${q}`);
		}
		if (!/class=/i.test(next)) {
			next += ' class="article-img"';
		}
		return `<img${next}>`;
	});
}

function previewImage(url) {
	if (!url) return;
	uni.previewImage({ urls: [url], current: url });
}

async function loadDetail() {
	if (!strategyId.value) return;
	loading.value = true;
	errorMsg.value = '';
	try {
		article.value = await fetchStrategyInfo(strategyId.value) || {};
	} catch (e) {
		article.value = {};
		errorMsg.value = e?.msg || e?.message || '加载攻略详情失败';
		uni.showToast({ title: errorMsg.value, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad((options) => {
	strategyId.value = Number(options?.id || 0) || null;
	loadDetail();
});
</script>

<style lang="scss">
.strategy-detail-page {
	background: #fdf8f2;
}

.article-card {
	display: flex;
	flex-direction: column;
	gap: 18rpx;
	margin-top: 8rpx;
}

.tag-row {
	display: flex;
	flex-wrap: wrap;
	gap: 10rpx;
}

.article-tag {
	display: inline-flex;
	align-items: center;
	height: 42rpx;
	padding: 0 18rpx;
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

.article-title {
	font-size: 40rpx;
	font-weight: 700;
	line-height: 1.45;
	color: #5f4a3a;
}

.article-sub {
	font-size: 28rpx;
	color: #8f7f70;
	line-height: 1.6;
}

.article-cover {
	width: 100%;
	height: 360rpx;
	border-radius: 28rpx;
	background: #f4e8d8;
}

.article-body {
	margin-top: 8rpx;
}

.article-content {
	font-size: 30rpx;
	line-height: 1.85;
	color: #5f4f42;
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
