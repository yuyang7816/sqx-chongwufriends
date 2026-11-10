<template>
	<view class="pet-page wecom-page">
		<view class="pet-shell">
			<!-- 顶部头图 -->
			<view class="wecom-hero">
				<view class="wecom-hero__deco wecom-hero__deco--a"></view>
				<view class="wecom-hero__deco wecom-hero__deco--b"></view>
				<view class="wecom-hero__deco wecom-hero__deco--c"></view>
				<view class="wecom-hero__frame">
					<image class="wecom-hero__img" :src="logoUrl" mode="aspectFit"></image>
				</view>
				<text class="wecom-hero__title">{{ pageTitle }}</text>
				<text class="wecom-hero__sub">{{ pageSubtitle }} 🐾</text>
			</view>

			<!-- 福利卡片 -->
			<view class="wecom-benefits">
				<view v-for="(item, idx) in cards" :key="idx" class="wecom-benefit">
					<view class="wecom-benefit__icon" :style="{ background: iconStyle(idx).bg }">
						<text class="wecom-benefit__emoji">{{ iconStyle(idx).emoji }}</text>
					</view>
					<view class="wecom-benefit__text">
						<text class="wecom-benefit__title">{{ item.title }}</text>
						<text class="wecom-benefit__desc">{{ item.desc }}</text>
					</view>
				</view>
			</view>

			<!-- 添加方式 -->
			<view class="wecom-section">
				<view class="wecom-section__bar"></view>
				<text class="wecom-section__title">添加方式</text>
			</view>

			<!-- 方式一：扫码 -->
			<view class="wecom-method wecom-method--qr">
				<text class="wecom-method__label">方式一：扫码添加</text>
				<view class="wecom-qr" @tap="previewQr">
					<image v-if="qrUrl" class="wecom-qr__img" :src="qrUrl" mode="aspectFit"></image>
					<view v-else class="wecom-qr__placeholder">
						<text class="wecom-qr__icon">💼</text>
						<text class="wecom-qr__text">扫码添加企业微</text>
					</view>
				</view>
				<text class="wecom-method__hint">打开微信扫一扫</text>
			</view>

			<!-- 方式二：搜索 -->
			<view class="wecom-method wecom-method--search">
				<text class="wecom-method__label">方式二：搜索添加</text>
				<view class="wecom-search">
					<text class="wecom-search__id">{{ wecomId || '@heta_pet_official' }}</text>
					<view class="wecom-search__copy" @tap="copyWechat">复制</view>
				</view>
				<text class="wecom-method__hint">长按复制微信号</text>
			</view>

			<!-- 主按钮 -->
			<view class="wecom-cta" @tap="copyWechat">一键添加企微 💼</view>
			<view v-if="serviceLink" class="wecom-cta wecom-cta--plain" @tap="openServiceLink">打开客服链接</view>

			<!-- 底部 -->
			<view class="wecom-footer">
				<view class="wecom-footer__paws">
					<text class="wecom-footer__paw">🐾</text>
					<text class="wecom-footer__dash">—</text>
					<text class="wecom-footer__paw">🐾</text>
					<text class="wecom-footer__dash">—</text>
					<text class="wecom-footer__paw">🐾</text>
				</view>
				<text class="wecom-footer__text">已有 12,800+ 宠友在这里等你 🐕</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onMounted } from 'vue';
import { fetchWecomConfig } from '@/common/api/user-pet.js';

const pageTitle = ref('和它交个朋友');
const pageSubtitle = ref('加入我们的宠物大家庭');
const logoUrl = ref('/static/logo.png');
const qrUrl = ref('');
const wecomId = ref('');
const serviceLink = ref('');
const cards = ref([
	{ title: '福利早知道', desc: '第一时间获取门店优惠、新品消息' },
	{ title: '养宠顾问', desc: '专业养宠顾问在线答疑，随时咨询' },
	{ title: '宠友社群', desc: '和 2000+ 宠友交流养宠心得' }
]);

const ICON_PRESETS = [
	{ emoji: '🎁', bg: '#fdf0c8' },
	{ emoji: '🐾', bg: '#d9efe1' },
	{ emoji: '👥', bg: '#fce0e6' }
];
const iconStyle = (idx) => ICON_PRESETS[idx % ICON_PRESETS.length];

function parseBenefitCards(text) {
	if (!text) return [];
	return text.split('\n').map(line => line.trim()).filter(Boolean).map(line => {
		const parts = line.split('|');
		return {
			title: parts[0] || '福利',
			desc: parts[1] || ''
		};
	});
}

async function loadConfig() {
	try {
		const data = await fetchWecomConfig();
		pageTitle.value = data?.pageTitle || data?.communityTitle || pageTitle.value;
		pageSubtitle.value = data?.pageSubtitle || data?.communitySubtitle || pageSubtitle.value;
		qrUrl.value = data?.qrUrl || '';
		wecomId.value = data?.wecomId || '';
		serviceLink.value = data?.serviceLink || '';
		if (data?.logoUrl) logoUrl.value = data.logoUrl;
		const benefitCards = parseBenefitCards(data?.benefitText);
		if (benefitCards.length) cards.value = benefitCards;
	} catch (e) {
		uni.showToast({ title: e?.msg || e?.message || '加载企微配置失败', icon: 'none' });
	}
}

const copyWechat = () => {
	const value = wecomId.value || '@heta_pet_official';
	uni.setClipboardData({
		data: value,
		success: () => {
			uni.showToast({ title: '企微号已复制', icon: 'success' });
		}
	});
};

function openServiceLink() {
	if (!serviceLink.value) return;
	// #ifdef H5
	window.location.href = serviceLink.value;
	// #endif
	// #ifndef H5
	uni.setClipboardData({
		data: serviceLink.value,
		success: () => uni.showToast({ title: '客服链接已复制', icon: 'success' })
	});
	// #endif
};

function previewQr() {
	if (!qrUrl.value) return;
	uni.previewImage({ urls: [qrUrl.value], current: qrUrl.value });
}

onMounted(loadConfig);
</script>

<style lang="scss">
.wecom-page {
	--wecom-brown: #8b5e3c;
	--wecom-brown-soft: #c79662;
	--wecom-border: rgba(211, 162, 113, 0.55);
	--wecom-dashed: #d3a271;
}

/* 顶部头图 */
.wecom-hero {
	position: relative;
	margin-top: 8rpx;
	padding: 48rpx 24rpx 36rpx;
	text-align: center;
	background: var(--pet-panel, #fffdf9);
	border-radius: 32rpx;
	border: 2rpx solid var(--wecom-border);
	box-shadow: var(--pet-shadow);
	overflow: hidden;
}

.wecom-hero__deco {
	position: absolute;
	border-radius: 50%;
	opacity: 0.55;
}

.wecom-hero__deco--a {
	top: 24rpx;
	left: 40rpx;
	width: 60rpx;
	height: 60rpx;
	background: #fdf0c8;
	transform: rotate(20deg);
	border-radius: 16rpx;
}

.wecom-hero__deco--b {
	top: 36rpx;
	right: 50rpx;
	width: 48rpx;
	height: 48rpx;
	background: #d9efe1;
	border-radius: 50%;
}

.wecom-hero__deco--c {
	bottom: 70rpx;
	left: 60rpx;
	width: 36rpx;
	height: 36rpx;
	background: #fce0e6;
	transform: rotate(45deg);
}

.wecom-hero__frame {
	position: relative;
	width: 180rpx;
	height: 180rpx;
	margin: 0 auto 24rpx;
	border-radius: 40rpx;
	background: #fff;
	border: 3rpx dashed var(--wecom-dashed);
	display: flex;
	align-items: center;
	justify-content: center;
}

.wecom-hero__img {
	width: 130rpx;
	height: 130rpx;
}

.wecom-hero__title {
	display: block;
	font-size: 44rpx;
	font-weight: 800;
	color: var(--wecom-brown);
	line-height: 1.3;
}

.wecom-hero__sub {
	display: block;
	margin-top: 10rpx;
	font-size: 26rpx;
	color: #a78f78;
}

/* 福利卡片 */
.wecom-benefits {
	margin-top: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 18rpx;
}

.wecom-benefit {
	display: flex;
	align-items: center;
	gap: 22rpx;
	padding: 24rpx;
	border-radius: 28rpx;
	background: var(--pet-panel, #fffdf9);
	border: 2rpx solid var(--wecom-border);
	box-shadow: var(--pet-shadow);
}

.wecom-benefit__icon {
	width: 92rpx;
	height: 92rpx;
	border-radius: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.wecom-benefit__emoji {
	font-size: 44rpx;
}

.wecom-benefit__text {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.wecom-benefit__title {
	font-size: 32rpx;
	font-weight: 700;
	color: #3a2c20;
	line-height: 1.3;
}

.wecom-benefit__desc {
	font-size: 24rpx;
	color: #a78f78;
	line-height: 1.4;
}

/* 区块标题 */
.wecom-section {
	display: flex;
	align-items: center;
	gap: 14rpx;
	margin: 32rpx 4rpx 20rpx;
}

.wecom-section__bar {
	width: 8rpx;
	height: 32rpx;
	border-radius: 4rpx;
	background: var(--wecom-brown);
}

.wecom-section__title {
	font-size: 32rpx;
	font-weight: 800;
	color: var(--wecom-brown);
}

/* 添加方式 */
.wecom-method {
	padding: 28rpx 24rpx;
	border-radius: 28rpx;
	border: 3rpx dashed var(--wecom-dashed);
	background: rgba(255, 252, 246, 0.6);
	margin-bottom: 20rpx;
}

.wecom-method__label {
	display: block;
	font-size: 28rpx;
	font-weight: 700;
	color: var(--wecom-brown);
	margin-bottom: 20rpx;
}

.wecom-method__hint {
	display: block;
	margin-top: 18rpx;
	font-size: 24rpx;
	color: #a78f78;
	text-align: center;
}

/* 方式一 扫码 */
.wecom-method--qr {
	text-align: center;
}

.wecom-qr {
	width: 320rpx;
	height: 320rpx;
	margin: 0 auto;
	border-radius: 28rpx;
	border: 3rpx dashed var(--wecom-dashed);
	background: #fffdf9;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}

.wecom-qr__img {
	width: 86%;
	height: 86%;
}

.wecom-qr__placeholder {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 14rpx;
}

.wecom-qr__icon {
	font-size: 60rpx;
}

.wecom-qr__text {
	font-size: 24rpx;
	color: var(--wecom-brown-soft);
}

/* 方式二 搜索 */
.wecom-method--search {
	text-align: left;
}

.wecom-search {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	padding: 22rpx 24rpx;
	border-radius: 24rpx;
	background: #fff7ef;
	border: 2rpx solid #f0dfc8;
}

.wecom-search__id {
	flex: 1;
	min-width: 0;
	font-size: 30rpx;
	font-weight: 700;
	color: #b48455;
}

.wecom-search__copy {
	flex-shrink: 0;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	padding: 0 28rpx;
	height: 56rpx;
	border-radius: 999rpx;
	background: var(--wecom-brown);
	color: #fff;
	font-size: 24rpx;
	font-weight: 700;
}

/* 主按钮 */
.wecom-cta {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	height: 92rpx;
	margin-top: 24rpx;
	border-radius: 999rpx;
	background: var(--wecom-brown);
	color: #fff;
	font-size: 32rpx;
	font-weight: 700;
	box-shadow: 0 14rpx 28rpx rgba(139, 94, 60, 0.24);
}

.wecom-cta--plain {
	background: #fff;
	color: var(--wecom-brown-soft);
	border: 2rpx solid #e8d8c8;
	box-shadow: none;
}

/* 底部 */
.wecom-footer {
	margin-top: 36rpx;
	text-align: center;
}

.wecom-footer__paws {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 14rpx;
	margin-bottom: 14rpx;
}

.wecom-footer__paw {
	font-size: 26rpx;
	opacity: 0.7;
}

.wecom-footer__dash {
	font-size: 22rpx;
	color: #c7b9a8;
}

.wecom-footer__text {
	display: block;
	font-size: 24rpx;
	color: #a78f78;
}
</style>
