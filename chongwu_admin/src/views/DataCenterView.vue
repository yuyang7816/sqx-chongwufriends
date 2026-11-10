<script setup>
	import * as echarts from 'echarts'
	import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
	import { useRouter } from 'vue-router'
	import {
		ArrowRight,
		Document,
		Present,
		Refresh,
		ShoppingCart,
		User,
	} from '@element-plus/icons-vue'
	import { post, resolveBusinessResponse } from '@/api/http'

	const router = useRouter()
	const loading = ref(false)
	const overview = ref({})
	const trendRef = ref()
	const chartMap = new Map()

	const REVENUE_COLOR = '#c49a6c'
	const FEED_COLOR = '#8b613d'

	const todayLabel = computed(() => {
		const d = new Date()
		const w = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
		return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · 周${w}`
	})

	function count(v) {
		return Number(v || 0).toLocaleString('zh-CN')
	}

	function money(v) {
		const n = Number(v || 0)
		return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
	}

	function deltaText(today, yesterday) {
		const t = Number(today || 0)
		const y = Number(yesterday || 0)
		const diff = t - y
		if (y === 0 && t === 0) return { text: '持平', tone: 'flat' }
		if (y === 0) return { text: '新增', tone: 'up' }
		const pct = Math.abs((diff / y) * 100).toFixed(1)
		if (diff > 0) return { text: `+${pct}%`, tone: 'up' }
		if (diff < 0) return { text: `-${pct}%`, tone: 'down' }
		return { text: '持平', tone: 'flat' }
	}

	function isNightTheme() {
		return document.documentElement.getAttribute('data-theme') === 'night'
	}

	function chartTextColor() {
		return isNightTheme() ? 'rgba(232, 236, 242, 0.72)' : '#909399'
	}

	function chartAxisLine() {
		return isNightTheme() ? 'rgba(255, 255, 255, 0.12)' : 'rgba(26, 36, 51, 0.08)'
	}

	function getChart(el, key) {
		if (!el) return null
		const cached = chartMap.get(key)
		if (cached) return cached
		const chart = echarts.init(el)
		chartMap.set(key, chart)
		return chart
	}

	const metrics = computed(() => {
		const o = overview.value || {}
		return [
			{
				label: '今日营收',
				value: money(o.todayRevenue),
				unit: '元',
				accent: 'brand',
				path: '/pet/orders',
				icon: ShoppingCart,
				delta: deltaText(o.todayRevenue, o.yesterdayRevenue),
				hint: `昨日 ¥${money(o.yesterdayRevenue)}`,
				featured: true,
			},
			{
				label: '今日订单',
				value: count(o.todayMallOrderCount),
				unit: '单',
				accent: 'ink',
				path: '/pet/orders',
				icon: Document,
				delta: deltaText(o.todayMallOrderCount, o.yesterdayMallOrderCount),
				hint: `客单价 ¥${money(o.todayAvgOrderAmount)}`,
			},
			{
				label: '今日投喂',
				value: money(o.todayFeedAmount),
				unit: '元',
				accent: 'deep',
				path: '/pet/feed',
				icon: Present,
				delta: deltaText(o.todayFeedAmount, o.yesterdayFeedAmount),
				hint: `${count(o.todayFeedCount)} 笔`,
			},
			{
				label: '今日新用户',
				value: count(o.todayNewUserCount),
				unit: '人',
				accent: 'soft',
				path: '/users',
				icon: User,
				delta: deltaText(o.todayNewUserCount, o.yesterdayNewUserCount),
				hint: `昨日 ${count(o.yesterdayNewUserCount)} 人`,
			},
		]
	})

	const todoItems = computed(() => {
		const o = overview.value || {}
		return [
			{ label: '待审核动态', value: Number(o.communityPending || 0), path: '/pet/community' },
			{ label: '待审核领养', value: Number(o.adoptPending || 0), path: '/pet/adopt' },
			{ label: '待反馈投喂', value: Number(o.feedPending || 0), path: '/pet/feed' },
			{ label: '待处理投诉', value: Number(o.complaintPending || 0), path: '/pet/community/complaints' },
			{ label: '即将过期券', value: Number(o.couponExpiring || 0), path: '/pet/coupon-grant' },
		]
	})

	const pendingTotal = computed(() => todoItems.value.reduce((sum, item) => sum + item.value, 0))
	const hasPending = computed(() => pendingTotal.value > 0)

	const bizItems = computed(() => {
		const o = overview.value || {}
		return [
			{ label: '今日动态', value: count(o.communityToday), path: '/pet/community' },
			{ label: '社区总量', value: count(o.communityTotal), path: '/pet/community' },
			{ label: '宠物档案', value: count(o.archiveCount), path: '/pet/archives' },
			{ label: '上架商品', value: count(o.mallOnCount), path: '/pet/mall' },
			{ label: '昨日营收', value: money(o.yesterdayRevenue), prefix: '¥', path: '/pet/orders' },
			{ label: '昨日投喂', value: money(o.yesterdayFeedAmount), prefix: '¥', path: '/pet/feed' },
		]
	})

	function renderTrend() {
		const list = overview.value?.trend7d || []
		const axis = list.map((d) => String(d.date || '').slice(5))
		const revenue = list.map((d) => Number(d.revenue || 0))
		const feed = list.map((d) => Number(d.feedAmount || 0))
		const text = chartTextColor()
		const line = chartAxisLine()
		const night = isNightTheme()
		getChart(trendRef.value, 'trend')?.setOption({
			color: [REVENUE_COLOR, FEED_COLOR],
			tooltip: {
				trigger: 'axis',
				backgroundColor: night ? 'rgba(22, 28, 36, 0.92)' : 'rgba(255,255,255,0.96)',
				borderColor: night ? 'rgba(255,255,255,0.08)' : '#e8eaed',
				textStyle: { color: night ? '#e8ecf2' : '#1a2433', fontSize: 12 },
			},
			legend: {
				top: 4,
				right: 8,
				itemWidth: 12,
				itemHeight: 8,
				icon: 'roundRect',
				textStyle: { color: text, fontSize: 12 },
			},
			grid: { left: 8, right: 12, top: 40, bottom: 4, containLabel: true },
			xAxis: {
				type: 'category',
				data: axis.length ? axis : ['暂无'],
				axisTick: { show: false },
				axisLine: { show: false },
				axisLabel: { color: text, fontSize: 11 },
			},
			yAxis: {
				type: 'value',
				splitNumber: 4,
				splitLine: { lineStyle: { color: line, type: 'dashed' } },
				axisLabel: { color: text, fontSize: 11 },
			},
			series: [
				{
					name: '营收',
					type: 'bar',
					barMaxWidth: 26,
					itemStyle: {
						borderRadius: [6, 6, 0, 0],
						color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
							{ offset: 0, color: '#d4b08a' },
							{ offset: 1, color: '#c49a6c' },
						]),
					},
					data: revenue.length ? revenue : [0],
				},
				{
					name: '投喂',
					type: 'line',
					smooth: true,
					symbol: 'circle',
					symbolSize: 7,
					lineStyle: { width: 2.5 },
					areaStyle: {
						color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
							{ offset: 0, color: 'rgba(139, 97, 61, 0.18)' },
							{ offset: 1, color: 'rgba(139, 97, 61, 0)' },
						]),
					},
					data: feed.length ? feed : [0],
				},
			],
		}, true)
	}

	async function loadAll() {
		loading.value = true
		try {
			const res = await post('admin/pet/datacenter/overview', {}, { silentError: true })
			overview.value = (await resolveBusinessResponse(res)) || {}
			await nextTick()
			renderTrend()
		} catch {
			overview.value = {}
		} finally {
			loading.value = false
		}
	}

	function resize() {
		chartMap.forEach((c) => c.resize())
	}

	function go(path) {
		router.push(path)
	}

	onMounted(async () => {
		window.addEventListener('resize', resize)
		await loadAll()
	})

	onBeforeUnmount(() => {
		window.removeEventListener('resize', resize)
		chartMap.forEach((c) => c.dispose())
		chartMap.clear()
	})
</script>

<template>
	<div class="dc" v-loading="loading">
		<header class="dc-hero">
			<div class="dc-hero__copy">
				<p class="dc-hero__eyebrow">数据中心 · 本店经营</p>
				<h1 class="dc-hero__title">今日经营台</h1>
				<p class="dc-hero__date">{{ todayLabel }}</p>
			</div>
			<div class="dc-hero__actions">
				<p class="dc-hero__hint">按支付成功时间聚合</p>
				<el-button type="primary" :icon="Refresh" :loading="loading" round @click="loadAll">
					刷新数据
				</el-button>
			</div>
		</header>

		<section class="dc-kpi-row">
			<button
				v-for="m in metrics"
				:key="m.label"
				type="button"
				class="dc-kpi"
				:class="[`dc-kpi--${m.accent}`, { 'dc-kpi--featured': m.featured }]"
				@click="go(m.path)"
			>
				<div class="dc-kpi__top">
					<span class="dc-kpi__icon">
						<el-icon :size="18"><component :is="m.icon" /></el-icon>
					</span>
					<span class="dc-kpi__delta" :class="`is-${m.delta.tone}`">{{ m.delta.text }}</span>
				</div>
				<span class="dc-kpi__label">{{ m.label }}</span>
				<div class="dc-kpi__value-row">
					<strong class="dc-kpi__value">{{ m.value }}</strong>
					<span class="dc-kpi__unit">{{ m.unit }}</span>
				</div>
				<span class="dc-kpi__hint">{{ m.hint }}</span>
			</button>
		</section>

		<section class="dc-main">
			<div class="dc-panel dc-panel--trend">
				<div class="dc-panel__head">
					<div>
						<strong>近 7 日趋势</strong>
						<p>营收柱状 · 投喂折线</p>
					</div>
				</div>
				<div ref="trendRef" class="dc-chart" />
			</div>

			<div class="dc-panel dc-panel--todo" :class="{ 'is-idle': !hasPending }">
				<div class="dc-panel__head">
					<div>
						<strong>待办事项</strong>
						<p v-if="hasPending">{{ count(pendingTotal) }} 项待处理</p>
						<p v-else>暂无积压，状态良好</p>
					</div>
					<span v-if="hasPending" class="dc-panel__badge">需关注</span>
					<span v-else class="dc-panel__badge is-ok">空闲</span>
				</div>
				<div class="dc-todo-list">
					<button
						v-for="t in todoItems"
						:key="t.label"
						type="button"
						class="dc-todo"
						:class="{ 'is-active': t.value > 0 }"
						@click="go(t.path)"
					>
						<span class="dc-todo__label">{{ t.label }}</span>
						<strong class="dc-todo__value">{{ count(t.value) }}</strong>
						<span class="dc-todo__go">
							{{ t.value > 0 ? '去处理' : '查看' }}
							<el-icon :size="12"><ArrowRight /></el-icon>
						</span>
					</button>
				</div>
			</div>
		</section>

		<section class="dc-snap">
			<div class="dc-snap__head">
				<strong>业务快览</strong>
				<span>点击进入对应模块</span>
			</div>
			<div class="dc-snap__grid">
				<button
					v-for="item in bizItems"
					:key="item.label"
					type="button"
					class="dc-snap__item"
					@click="go(item.path)"
				>
					<span class="dc-snap__label">{{ item.label }}</span>
					<strong class="dc-snap__value">
						<span v-if="item.prefix" class="dc-snap__prefix">{{ item.prefix }}</span>{{ item.value }}
					</strong>
				</button>
			</div>
		</section>
	</div>
</template>

<style scoped>
	.dc {
		--dc-ink: var(--nf-text-primary, #1a2433);
		--dc-muted: var(--nf-text-secondary, #909399);
		--dc-line: var(--nf-border, #e8eaed);
		--dc-card: var(--nf-card-bg, #ffffff);
		--dc-accent: var(--nf-brand-accent, #c49a6c);
		--dc-accent-deep: var(--nf-brand-accent-deep, #a67b4f);
		--dc-cream: var(--nf-brand, #f8efe2);
		--dc-cream-deep: var(--nf-brand-deep, #eed8b9);
		display: flex;
		flex-direction: column;
		gap: 16px;
		padding-bottom: 12px;
	}

	/* ── Hero ── */
	.dc-hero {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 20px;
		padding: 22px 24px;
		border-radius: 14px;
		border: 1px solid rgba(196, 154, 108, 0.28);
		background:
			radial-gradient(ellipse 80% 120% at 100% 0%, rgba(238, 216, 185, 0.55), transparent 55%),
			radial-gradient(ellipse 60% 100% at 0% 100%, rgba(248, 239, 226, 0.9), transparent 50%),
			linear-gradient(135deg, #fefdfc 0%, #f8efe2 55%, #f3e6d4 100%);
		overflow: hidden;
		position: relative;
	}

	.dc-hero::after {
		content: '';
		position: absolute;
		right: -40px;
		top: -60px;
		width: 180px;
		height: 180px;
		border-radius: 50%;
		border: 1px solid rgba(196, 154, 108, 0.22);
		pointer-events: none;
	}

	.dc-hero__eyebrow {
		margin: 0 0 6px;
		font-size: 12px;
		font-weight: 600;
		letter-spacing: 0.08em;
		color: var(--dc-accent-deep);
	}

	.dc-hero__title {
		margin: 0;
		font-size: 28px;
		font-weight: 750;
		letter-spacing: -0.02em;
		color: var(--dc-ink);
		line-height: 1.15;
	}

	.dc-hero__date {
		margin: 8px 0 0;
		font-size: 13px;
		color: var(--dc-muted);
	}

	.dc-hero__actions {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 10px;
		position: relative;
		z-index: 1;
	}

	.dc-hero__hint {
		margin: 0;
		font-size: 12px;
		color: var(--dc-muted);
	}

	/* ── KPI ── */
	.dc-kpi-row {
		display: grid;
		grid-template-columns: 1.25fr repeat(3, 1fr);
		gap: 12px;
	}

	.dc-kpi {
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 6px;
		padding: 16px 18px;
		border: 1px solid var(--dc-line);
		border-radius: 12px;
		background: var(--dc-card);
		cursor: pointer;
		text-align: left;
		transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
	}

	.dc-kpi:hover {
		transform: translateY(-2px);
		border-color: rgba(196, 154, 108, 0.55);
		box-shadow: 0 10px 24px rgba(26, 36, 51, 0.06);
	}

	.dc-kpi--featured {
		background: linear-gradient(160deg, #1a2433 0%, #2c3a4d 100%);
		border-color: transparent;
		color: #fff;
	}

	.dc-kpi--featured .dc-kpi__label,
	.dc-kpi--featured .dc-kpi__hint,
	.dc-kpi--featured .dc-kpi__unit {
		color: rgba(255, 255, 255, 0.62);
	}

	.dc-kpi--featured .dc-kpi__value {
		color: #fff;
	}

	.dc-kpi--featured .dc-kpi__icon {
		background: rgba(248, 239, 226, 0.16);
		color: #f8efe2;
	}

	.dc-kpi--featured:hover {
		box-shadow: 0 12px 28px rgba(26, 36, 51, 0.22);
	}

	.dc-kpi__top {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 4px;
	}

	.dc-kpi__icon {
		width: 34px;
		height: 34px;
		border-radius: 10px;
		display: inline-flex;
		align-items: center;
		justify-content: center;
		background: var(--dc-cream);
		color: var(--dc-accent-deep);
	}

	.dc-kpi--ink .dc-kpi__icon {
		background: #eef1f5;
		color: #3d4a5c;
	}

	.dc-kpi--deep .dc-kpi__icon {
		background: rgba(139, 97, 61, 0.1);
		color: #8b613d;
	}

	.dc-kpi--soft .dc-kpi__icon {
		background: #f7f8fa;
		color: #738095;
	}

	.dc-kpi__delta {
		padding: 2px 8px;
		border-radius: 999px;
		font-size: 11px;
		font-weight: 650;
		background: #f4f6fa;
		color: var(--dc-muted);
	}

	.dc-kpi__delta.is-up {
		background: rgba(42, 157, 143, 0.12);
		color: #1f8a7c;
	}

	.dc-kpi__delta.is-down {
		background: rgba(232, 93, 76, 0.12);
		color: #d14a3b;
	}

	.dc-kpi--featured .dc-kpi__delta {
		background: rgba(255, 255, 255, 0.1);
		color: rgba(255, 255, 255, 0.78);
	}

	.dc-kpi--featured .dc-kpi__delta.is-up {
		background: rgba(90, 200, 180, 0.2);
		color: #9eebdc;
	}

	.dc-kpi--featured .dc-kpi__delta.is-down {
		background: rgba(255, 140, 120, 0.22);
		color: #ffc4b8;
	}

	.dc-kpi__label {
		font-size: 13px;
		color: var(--dc-muted);
	}

	.dc-kpi__value-row {
		display: flex;
		align-items: baseline;
		gap: 6px;
	}

	.dc-kpi__value {
		font-size: 28px;
		font-weight: 750;
		letter-spacing: -0.03em;
		line-height: 1.05;
		color: var(--dc-ink);
		font-variant-numeric: tabular-nums;
	}

	.dc-kpi--featured .dc-kpi__value {
		font-size: 32px;
	}

	.dc-kpi__unit {
		font-size: 12px;
		color: var(--dc-muted);
	}

	.dc-kpi__hint {
		font-size: 12px;
		color: var(--dc-muted);
	}

	/* ── Main: trend + todo ── */
	.dc-main {
		display: grid;
		grid-template-columns: minmax(0, 1.7fr) minmax(280px, 1fr);
		gap: 12px;
		align-items: stretch;
	}

	.dc-panel {
		padding: 16px 18px 18px;
		border: 1px solid var(--dc-line);
		border-radius: 12px;
		background: var(--dc-card);
		min-height: 0;
	}

	.dc-panel__head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12px;
		margin-bottom: 8px;
	}

	.dc-panel__head strong {
		display: block;
		font-size: 15px;
		color: var(--dc-ink);
	}

	.dc-panel__head p {
		margin: 4px 0 0;
		font-size: 12px;
		color: var(--dc-muted);
	}

	.dc-panel__badge {
		flex-shrink: 0;
		padding: 3px 9px;
		border-radius: 999px;
		font-size: 11px;
		font-weight: 650;
		background: rgba(232, 93, 76, 0.1);
		color: #d14a3b;
	}

	.dc-panel__badge.is-ok {
		background: rgba(42, 157, 143, 0.12);
		color: #1f8a7c;
	}

	.dc-chart {
		width: 100%;
		height: 300px;
	}

	.dc-todo-list {
		display: flex;
		flex-direction: column;
		gap: 8px;
	}

	.dc-todo {
		display: grid;
		grid-template-columns: 1fr auto auto;
		align-items: center;
		gap: 10px;
		padding: 12px 14px;
		border: 1px solid transparent;
		border-radius: 10px;
		background: #f7f8fa;
		cursor: pointer;
		text-align: left;
		transition: background 0.15s ease, border-color 0.15s ease, transform 0.15s ease;
	}

	.dc-todo:hover {
		transform: translateX(2px);
		border-color: rgba(196, 154, 108, 0.35);
		background: var(--dc-cream);
	}

	.dc-todo.is-active {
		background: linear-gradient(90deg, rgba(248, 239, 226, 0.95), #fff 70%);
		border-color: rgba(196, 154, 108, 0.4);
	}

	.dc-todo__label {
		font-size: 13px;
		color: var(--dc-ink);
	}

	.dc-todo__value {
		font-size: 18px;
		font-weight: 700;
		font-variant-numeric: tabular-nums;
		color: var(--dc-ink);
		min-width: 28px;
		text-align: right;
	}

	.dc-todo.is-active .dc-todo__value {
		color: var(--dc-accent-deep);
	}

	.dc-todo__go {
		display: inline-flex;
		align-items: center;
		gap: 2px;
		font-size: 12px;
		color: var(--dc-accent-deep);
		opacity: 0.85;
	}

	.dc-panel--todo.is-idle .dc-todo__value {
		color: var(--dc-muted);
	}

	/* ── Biz snapshot strip ── */
	.dc-snap {
		padding: 14px 18px 16px;
		border: 1px solid var(--dc-line);
		border-radius: 12px;
		background: var(--dc-card);
	}

	.dc-snap__head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		margin-bottom: 12px;
	}

	.dc-snap__head strong {
		font-size: 15px;
		color: var(--dc-ink);
	}

	.dc-snap__head span {
		font-size: 12px;
		color: var(--dc-muted);
	}

	.dc-snap__grid {
		display: grid;
		grid-template-columns: repeat(6, minmax(0, 1fr));
		gap: 0;
	}

	.dc-snap__item {
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 6px;
		padding: 8px 14px;
		border: 0;
		border-right: 1px solid var(--dc-line);
		background: transparent;
		cursor: pointer;
		text-align: left;
		transition: background 0.15s ease;
	}

	.dc-snap__item:last-child {
		border-right: 0;
	}

	.dc-snap__item:hover {
		background: var(--dc-cream);
	}

	.dc-snap__label {
		font-size: 12px;
		color: var(--dc-muted);
	}

	.dc-snap__value {
		font-size: 18px;
		font-weight: 700;
		color: var(--dc-ink);
		font-variant-numeric: tabular-nums;
		letter-spacing: -0.02em;
	}

	.dc-snap__prefix {
		font-size: 12px;
		font-weight: 600;
		margin-right: 1px;
		color: var(--dc-muted);
	}

	@media (max-width: 1100px) {
		.dc-kpi-row {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		.dc-kpi--featured {
			grid-column: 1 / -1;
		}

		.dc-main {
			grid-template-columns: 1fr;
		}

		.dc-snap__grid {
			grid-template-columns: repeat(3, minmax(0, 1fr));
		}

		.dc-snap__item:nth-child(3n) {
			border-right: 0;
		}

		.dc-snap__item:nth-child(n + 4) {
			border-top: 1px solid var(--dc-line);
			padding-top: 14px;
			margin-top: 4px;
		}
	}

	@media (max-width: 720px) {
		.dc-hero {
			flex-direction: column;
			align-items: flex-start;
		}

		.dc-hero__actions {
			align-items: flex-start;
		}

		.dc-kpi-row,
		.dc-snap__grid {
			grid-template-columns: 1fr;
		}

		.dc-kpi--featured {
			grid-column: auto;
		}

		.dc-snap__item {
			border-right: 0;
			border-bottom: 1px solid var(--dc-line);
			padding: 12px 0;
		}

		.dc-snap__item:last-child {
			border-bottom: 0;
		}

		.dc-snap__item:nth-child(n + 4) {
			border-top: 0;
			margin-top: 0;
			padding-top: 12px;
		}
	}
</style>
