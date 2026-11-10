<template>
	<view class="pet-page post-rules-page">
		<view class="pet-shell">
			<view class="rules-hero pet-card">
				<text class="rules-hero-badge">发帖前请先阅读</text>
				<text class="rules-hero-title">一起维护温柔、友好的宠物社区氛围</text>
				<text class="rules-hero-desc">分享真实养宠日常，尊重每一位用户与每一只毛孩子，让内容更有爱也更安全。</text>
			</view>

			<view v-for="section in ruleSections" :key="section.title" class="rules-section pet-card">
				<view class="rules-section-head">
					<text class="rules-section-icon">{{ section.icon }}</text>
					<text class="rules-section-title">{{ section.title }}</text>
				</view>
				<text v-for="item in section.items" :key="item" class="rules-item">{{ item }}</text>
			</view>

			<view class="rules-tip-box">
				<text class="rules-tip-title">温馨提示</text>
				<text class="rules-tip-text">如发布内容涉及广告引流、虚假信息、攻击性言论或不当图片，平台有权下架内容并限制账号功能。</text>
			</view>
		</view>
	</view>
</template>

<script setup>

	const ruleSections = [
		{
			icon: '🐾',
			title: '内容发布原则',
			items: [
				'1. 分享真实、积极的养宠生活，不发布虚构或误导性内容。',
				'2. 图片、视频和文字需与宠物主题相关，避免无关刷屏。',
				'3. 涉及领养、救助、送养的信息，请确保来源真实、表达清晰。'
			]
		},
		{
			icon: '💬',
			title: '互动与评论规范',
			items: [
				'1. 尊重他人，不辱骂、不阴阳怪气、不人身攻击。',
				'2. 不恶意引战，不散布焦虑，不对宠物健康作无依据判断。',
				'3. 讨论不同养宠方式时，请保持礼貌，理性交流。'
			]
		},
		{
			icon: '🛡️',
			title: '禁止内容',
			items: [
				'1. 禁止发布暴力虐宠、血腥不适或令人反感的画面。',
				'2. 禁止发布违法违规、涉黄涉赌、侵权盗图或恶意营销内容。',
				'3. 禁止泄露他人隐私，包括手机号、住址、聊天记录等敏感信息。'
			]
		}
	];
</script>

<style lang="scss">
	.post-rules-page {
		background: linear-gradient(180deg, #fff8f0 0%, #f8efe2 100%);
	}

	.rules-hero,
	.rules-section,
	.rules-tip-box {
		margin-top: 20rpx;
	}

	.rules-hero {
		padding: 34rpx 30rpx;
		background: linear-gradient(180deg, #fffdf8 0%, #fff5e6 100%);
	}

	.rules-hero-badge {
		display: inline-flex;
		align-items: center;
		height: 44rpx;
		padding: 0 20rpx;
		border-radius: 999rpx;
		background: #f9e2a7;
		color: #8b613d;
		font-size: 22rpx;
		font-weight: 700;
		margin-bottom: 18rpx;
	}

	.rules-hero-title {
		display: block;
		font-size: 38rpx;
		font-weight: 700;
		line-height: 1.4;
		color: #6d4c38;
	}

	.rules-hero-desc {
		display: block;
		margin-top: 16rpx;
		font-size: 24rpx;
		line-height: 1.8;
		color: #9b846f;
	}

	.rules-section {
		padding: 28rpx;
	}

	.rules-section-head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 18rpx;
	}

	.rules-section-icon {
		font-size: 28rpx;
	}

	.rules-section-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #6d4c38;
	}

	.rules-item {
		display: block;
		font-size: 25rpx;
		line-height: 1.9;
		color: #826b57;
	}

	.rules-tip-box {
		padding: 26rpx 28rpx;
		border-radius: 28rpx;
		background: rgba(255, 247, 231, 0.92);
		border: 2rpx dashed rgba(215, 170, 119, 0.75);
	}

	.rules-tip-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #8b613d;
		margin-bottom: 10rpx;
	}

	.rules-tip-text {
		font-size: 24rpx;
		line-height: 1.8;
		color: #977d67;
	}
</style>
