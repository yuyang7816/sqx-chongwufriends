<script setup>
	import {
		onMounted,
		reactive,
		ref
	} from 'vue'
	import {
		useRoute,
		useRouter
	} from 'vue-router'
	import {
		ElMessage
	} from 'element-plus'
	import {
		User,
		Lock,
		Key
	} from '@element-plus/icons-vue'
	import {
		useAuthStore
	} from '@/stores/auth'
	import {
		validateForm
	} from '@/utils/form'
	import {
		getCaptchaUrl,
		getUUID
	} from '@/utils/tool'

	const route = useRoute()
	const router = useRouter()
	const authStore = useAuthStore()
	const formRef = ref()
	const loading = ref(false)
	const captchaUrl = ref('')
	const form = reactive({
		username: '',
		password: '',
		captcha: '',
		uuid: ''
	})
	const rules = {
		username: [{
			required: true,
			message: '请输入账号',
			trigger: 'blur'
		}],
		password: [{
			required: true,
			message: '请输入密码',
			trigger: 'blur'
		}],
		captcha: [{
			required: true,
			message: '请输入验证码',
			trigger: 'blur'
		}],
	}

	function getRedirectTarget() {
		const redirect = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect
		if (!redirect || typeof redirect !== 'string' || redirect.startsWith('/login')) {
			return '/dashboard'
		}
		return redirect
	}

	function refreshCaptcha() {
		form.uuid = getUUID()
		captchaUrl.value = getCaptchaUrl(form.uuid)
	}

	async function handleLogin() {
		if (loading.value) {
			return
		}
		const valid = await validateForm(formRef)
		if (!valid) {
			return
		}
		loading.value = true
		try {
			await authStore.login(form)
			ElMessage.success('登录成功')
			router.replace(getRedirectTarget())
		} catch (error) {
			ElMessage.error(error.msg || error.message || '登录失败')
			refreshCaptcha()
			form.captcha = ''
		} finally {
			loading.value = false
		}
	}

	onMounted(refreshCaptcha)
</script>

<template>
	<div class="login-page">
		<div class="login-page__stage" aria-hidden="true">
			<div class="login-page__wash login-page__wash--base" />
			<div class="login-page__wash login-page__wash--warm" />
			<div class="login-page__wash login-page__wash--cool" />
			<div class="login-page__beam" />
			<div class="login-page__grain" />
			<div class="login-page__orbit login-page__orbit--a" />
			<div class="login-page__orbit login-page__orbit--b" />
			<div class="login-page__orbit login-page__orbit--c" />
			<svg class="login-page__ring" viewBox="0 0 640 640" fill="none">
				<circle cx="320" cy="320" r="238" stroke="currentColor" stroke-width="1" stroke-dasharray="4 10" />
				<circle cx="320" cy="320" r="168" stroke="currentColor" stroke-width="1" opacity="0.55" />
			</svg>
		</div>

		<div class="login-page__layout">
			<section class="login-page__brand">
				<p class="login-page__eyebrow">PET OPERATIONS · ADMIN</p>
				<h1 class="login-page__title">
					<span class="login-page__title-mark">和它</span>
					<span class="login-page__title-rest">交个朋友</span>
				</h1>
				<p class="login-page__desc">宠物社交与运营一体化管理平台</p>
				<div class="login-page__meta">
					<span>数据看板</span>
					<span>档案管理</span>
					<span>商城运营</span>
				</div>
			</section>

			<section class="login-page__panel">
				<div class="login-page__panel-inner">
					<header class="login-page__panel-head">
						<span class="login-page__panel-badge">管理后台</span>
						<h2>欢迎回来</h2>
						<p>使用管理员账号登录系统</p>
					</header>

					<el-form
						ref="formRef"
						:model="form"
						:rules="rules"
						label-position="top"
						class="login-page__form"
						@submit.prevent
					>
						<el-form-item prop="username" label="账号">
							<el-input
								v-model="form.username"
								placeholder="请输入账号"
								clearable
								size="large"
								:prefix-icon="User"
							/>
						</el-form-item>
						<el-form-item prop="password" label="密码">
							<el-input
								v-model="form.password"
								type="password"
								show-password
								placeholder="请输入密码"
								size="large"
								:prefix-icon="Lock"
								@keyup.enter="handleLogin"
							/>
						</el-form-item>
						<el-form-item prop="captcha" label="验证码">
							<div class="login-page__captcha">
								<el-input
									v-model="form.captcha"
									placeholder="请输入验证码"
									clearable
									size="large"
									:prefix-icon="Key"
									@keyup.enter="handleLogin"
								/>
								<button
									type="button"
									class="login-page__captcha-btn"
									title="点击刷新"
									@click="refreshCaptcha"
								>
									<img :src="captchaUrl" alt="验证码" class="login-page__captcha-img" />
								</button>
							</div>
						</el-form-item>

						<el-button
							native-type="button"
							type="primary"
							:loading="loading"
							class="nf-login-submit"
							@click="handleLogin"
						>
							登 录
						</el-button>
					</el-form>

					<footer class="login-page__foot">
						<span>© {{ new Date().getFullYear() }} 和它交个朋友</span>
					</footer>
				</div>
			</section>
		</div>
	</div>
</template>

<style lang="scss" scoped>
.login-page {
	--login-ink: var(--nf-text-primary, #1a2433);
	--login-muted: var(--nf-text-secondary, #909399);
	--login-line: var(--nf-border, #e8eaed);
	--login-brand: var(--nf-brand, #f8efe2);
	--login-brand-deep: var(--nf-brand-deep, #eed8b9);
	--login-brand-lite: var(--nf-brand-lite, #fefdfc);
	--login-card: var(--nf-card-bg, #fff);

	position: relative;
	isolation: isolate;
	min-height: 100vh;
	min-width: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: clamp(20px, 4vw, 48px);
	overflow: hidden;
	background: #f3eee6;
	font-family: 'Avenir Next', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
	color: var(--login-ink);

	&__stage {
		position: absolute;
		inset: 0;
		z-index: 0;
		pointer-events: none;
		overflow: hidden;
	}

	&__wash {
		position: absolute;
		inset: -20%;

		&--base {
			background:
				radial-gradient(ellipse 70% 55% at 18% 22%, rgba(254, 253, 252, 0.95) 0%, transparent 58%),
				radial-gradient(ellipse 55% 50% at 82% 78%, rgba(238, 216, 185, 0.55) 0%, transparent 60%),
				linear-gradient(155deg, #faf6f0 0%, #f4efe6 42%, #efe6d8 100%);
		}

		&--warm {
			background:
				radial-gradient(circle at 70% 18%, rgba(248, 239, 226, 0.9) 0%, transparent 42%),
				radial-gradient(circle at 30% 88%, rgba(229, 204, 158, 0.28) 0%, transparent 46%);
			animation: login-breathe 12s ease-in-out infinite;
		}

		&--cool {
			background: radial-gradient(circle at 50% 40%, rgba(244, 246, 250, 0.35) 0%, transparent 55%);
			mix-blend-mode: soft-light;
		}
	}

	&__beam {
		position: absolute;
		top: -18%;
		left: 42%;
		width: 38vw;
		min-width: 280px;
		height: 140%;
		background: linear-gradient(
			108deg,
			transparent 0%,
			rgba(254, 253, 252, 0.08) 36%,
			rgba(248, 239, 226, 0.42) 50%,
			rgba(254, 253, 252, 0.08) 64%,
			transparent 100%
		);
		transform: rotate(18deg);
		filter: blur(2px);
		animation: login-beam 16s ease-in-out infinite;
	}

	&__grain {
		position: absolute;
		inset: 0;
		opacity: 0.35;
		background-image:
			radial-gradient(rgba(26, 36, 51, 0.045) 0.7px, transparent 0.7px),
			radial-gradient(rgba(238, 216, 185, 0.22) 0.8px, transparent 0.8px);
		background-size: 18px 18px, 28px 28px;
		background-position: 0 0, 9px 12px;
		mask-image: radial-gradient(ellipse 80% 70% at 50% 45%, #000 20%, transparent 85%);
	}

	&__orbit {
		position: absolute;
		border-radius: 50%;
		border: 1px solid rgba(238, 216, 185, 0.45);
		background: radial-gradient(circle at 30% 30%, rgba(254, 253, 252, 0.75), rgba(248, 239, 226, 0.2) 55%, transparent 72%);
		filter: blur(0.2px);

		&--a {
			width: min(42vw, 460px);
			height: min(42vw, 460px);
			top: -8%;
			left: -6%;
			animation: login-drift 22s ease-in-out infinite;
		}

		&--b {
			width: min(28vw, 300px);
			height: min(28vw, 300px);
			right: 8%;
			bottom: 6%;
			opacity: 0.8;
			animation: login-drift 18s ease-in-out infinite reverse;
		}

		&--c {
			width: 140px;
			height: 140px;
			left: 38%;
			bottom: 18%;
			opacity: 0.55;
			border-color: rgba(229, 204, 158, 0.35);
			animation: login-float 10s ease-in-out infinite;
		}
	}

	&__ring {
		position: absolute;
		width: min(52vw, 520px);
		height: min(52vw, 520px);
		left: 6%;
		top: 50%;
		transform: translateY(-52%);
		color: rgba(238, 216, 185, 0.55);
		opacity: 0.7;
		animation: login-spin 48s linear infinite;
	}

	&__layout {
		position: relative;
		z-index: 1;
		display: grid;
		grid-template-columns: 1.15fr 0.85fr;
		align-items: center;
		gap: clamp(28px, 5vw, 72px);
		width: min(1080px, 100%);
		animation: login-rise 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
	}

	&__brand {
		display: flex;
		flex-direction: column;
		justify-content: center;
		padding: clamp(8px, 2vw, 24px) 0;
		max-width: 560px;
	}

	&__eyebrow {
		margin: 0 0 18px;
		font-size: 12px;
		font-weight: 600;
		letter-spacing: 0.22em;
		color: #a8927c;
		text-transform: uppercase;
	}

	&__title {
		margin: 0;
		display: flex;
		flex-direction: column;
		gap: 4px;
		font-size: clamp(40px, 5.6vw, 64px);
		font-weight: 700;
		line-height: 1.08;
		letter-spacing: 0.02em;
		color: var(--login-ink);
	}

	&__title-mark {
		display: block;
		background: linear-gradient(120deg, #1a2433 0%, #5b4231 55%, #8b613d 100%);
		-webkit-background-clip: text;
		background-clip: text;
		color: transparent;
	}

	&__title-rest {
		display: block;
		color: #5b4231;
		font-weight: 650;
	}

	&__desc {
		margin: 22px 0 0;
		max-width: 28em;
		font-size: 16px;
		line-height: 1.7;
		color: #8a7460;
	}

	&__meta {
		display: flex;
		flex-wrap: wrap;
		gap: 10px;
		margin-top: 36px;

		span {
			padding: 8px 14px;
			border-radius: 999px;
			font-size: 12px;
			font-weight: 600;
			letter-spacing: 0.04em;
			color: #6f5744;
			background: rgba(254, 253, 252, 0.55);
			border: 1px solid rgba(238, 216, 185, 0.7);
			backdrop-filter: blur(10px);
		}
	}

	&__panel {
		justify-self: end;
		width: min(420px, 100%);
		animation: login-panel-in 0.85s cubic-bezier(0.22, 1, 0.36, 1) 0.12s both;
	}

	&__panel-inner {
		position: relative;
		padding: 36px 34px 28px;
		border-radius: 28px;
		background:
			linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(254, 253, 252, 0.88) 100%);
		border: 1px solid rgba(255, 255, 255, 0.75);
		box-shadow:
			0 1px 0 rgba(255, 255, 255, 0.9) inset,
			0 18px 50px rgba(91, 66, 49, 0.08),
			0 2px 8px rgba(26, 36, 51, 0.04);
		backdrop-filter: blur(18px);
		overflow: hidden;

		&::before {
			content: '';
			position: absolute;
			top: 0;
			left: 18%;
			right: 18%;
			height: 2px;
			border-radius: 999px;
			background: linear-gradient(90deg, transparent, var(--login-brand-deep), transparent);
		}
	}

	&__panel-head {
		margin-bottom: 28px;

		h2 {
			margin: 14px 0 6px;
			font-size: 26px;
			font-weight: 700;
			letter-spacing: 0.02em;
			color: var(--login-ink);
		}

		p {
			margin: 0;
			font-size: 13px;
			color: var(--login-muted);
			line-height: 1.5;
		}
	}

	&__panel-badge {
		display: inline-flex;
		align-items: center;
		padding: 4px 10px;
		border-radius: 999px;
		font-size: 11px;
		font-weight: 700;
		letter-spacing: 0.08em;
		color: #6f5744;
		background: var(--login-brand);
		border: 1px solid var(--login-brand-deep);
	}

	&__form {
		:deep(.el-form-item) {
			margin-bottom: 18px;
		}

		:deep(.el-form-item__label) {
			font-size: 13px;
			font-weight: 600;
			color: #5b4231;
			padding-bottom: 6px;
		}

		:deep(.el-input__wrapper) {
			min-height: 46px;
			border-radius: 14px;
			background: rgba(255, 255, 255, 0.9);
			box-shadow: 0 0 0 1px rgba(232, 234, 237, 0.95) inset;
			transition: box-shadow 0.22s ease, background 0.22s ease;

			&:hover {
				box-shadow: 0 0 0 1px rgba(238, 216, 185, 0.85) inset;
			}

			&.is-focus {
				background: #fff;
				box-shadow:
					0 0 0 1px var(--login-brand-deep) inset,
					0 0 0 4px rgba(238, 216, 185, 0.28);
			}
		}

		:deep(.el-input__prefix) {
			color: #b38a63;
		}
	}

	&__captcha {
		display: flex;
		gap: 10px;
		width: 100%;

		:deep(.el-input) {
			flex: 1;
		}
	}

	&__captcha-btn {
		padding: 0;
		border: 0;
		background: transparent;
		cursor: pointer;
		flex-shrink: 0;
		border-radius: 14px;
		overflow: hidden;
		transition: transform 0.2s ease, box-shadow 0.2s ease;

		&:hover {
			transform: translateY(-1px);
			box-shadow: 0 8px 18px rgba(163, 120, 79, 0.12);
		}

		&:focus-visible {
			outline: 2px solid var(--login-brand-deep);
			outline-offset: 2px;
		}
	}

	&__captcha-img {
		display: block;
		height: 46px;
		width: 122px;
		object-fit: contain;
		border-radius: 14px;
		border: 1px solid var(--login-line);
		background: #fff;
	}

	.nf-login-submit {
		width: 100%;
		height: 48px;
		margin-top: 10px;
		font-size: 15px;
		font-weight: 700;
		letter-spacing: 0.28em;
		text-indent: 0.28em;
		border-radius: 14px;
		background:
			linear-gradient(180deg, #f3e4c8 0%, var(--login-brand-deep) 100%) !important;
		border-color: #e2c89a !important;
		color: var(--login-ink) !important;
		box-shadow:
			0 1px 0 rgba(255, 255, 255, 0.55) inset,
			0 10px 24px rgba(163, 120, 79, 0.18);
		transition: transform 0.18s ease, filter 0.18s ease, box-shadow 0.18s ease;

		&:hover:not(:disabled) {
			filter: brightness(1.02);
			box-shadow:
				0 1px 0 rgba(255, 255, 255, 0.6) inset,
				0 14px 28px rgba(163, 120, 79, 0.22);
		}

		&:active:not(:disabled) {
			transform: translateY(1px) scale(0.99);
		}
	}

	&__foot {
		margin-top: 22px;
		text-align: center;
		font-size: 12px;
		color: var(--login-muted);
		letter-spacing: 0.02em;
	}
}

@media (max-width: 960px) {
	.login-page {
		align-items: flex-start;
		padding-top: 48px;
		padding-bottom: 48px;
		overflow: auto;
	}

	.login-page__layout {
		grid-template-columns: 1fr;
		gap: 28px;
		width: min(440px, 100%);
	}

	.login-page__brand {
		text-align: center;
		align-items: center;
		max-width: none;
		padding: 0;
	}

	.login-page__title {
		font-size: clamp(34px, 9vw, 44px);
	}

	.login-page__desc {
		margin-top: 14px;
	}

	.login-page__meta {
		justify-content: center;
		margin-top: 20px;
	}

	.login-page__panel {
		justify-self: stretch;
		width: 100%;
	}

	.login-page__ring {
		opacity: 0.35;
		left: 50%;
		transform: translate(-50%, -48%);
	}
}

@media (max-width: 480px) {
	.login-page__panel-inner {
		padding: 28px 20px 22px;
		border-radius: 22px;
	}

	.login-page__meta span:last-child {
		display: none;
	}
}

@keyframes login-rise {
	from {
		opacity: 0;
		transform: translateY(18px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes login-panel-in {
	from {
		opacity: 0;
		transform: translateY(24px) scale(0.985);
	}
	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}

@keyframes login-breathe {
	0%,
	100% {
		opacity: 0.85;
		transform: scale(1);
	}
	50% {
		opacity: 1;
		transform: scale(1.04);
	}
}

@keyframes login-beam {
	0%,
	100% {
		opacity: 0.45;
		transform: rotate(18deg) translateX(0);
	}
	50% {
		opacity: 0.75;
		transform: rotate(18deg) translateX(3%);
	}
}

@keyframes login-drift {
	0%,
	100% {
		transform: translate3d(0, 0, 0);
	}
	50% {
		transform: translate3d(18px, -14px, 0);
	}
}

@keyframes login-float {
	0%,
	100% {
		transform: translateY(0);
	}
	50% {
		transform: translateY(-16px);
	}
}

@keyframes login-spin {
	from {
		transform: translateY(-52%) rotate(0deg);
	}
	to {
		transform: translateY(-52%) rotate(360deg);
	}
}

@media (prefers-reduced-motion: reduce) {
	.login-page__wash--warm,
	.login-page__beam,
	.login-page__orbit,
	.login-page__ring,
	.login-page__layout,
	.login-page__panel {
		animation: none !important;
	}
}
</style>
