<script setup>
	import {
		computed,
		reactive,
		ref
	} from 'vue'
	import BasePagination from '@/components/common/BasePagination.vue'
	import {
		uploadMerchantImageApi
	} from '@/api/modules/merchant-center'
	import {
		BANNER_POSITION,
		createOperationBannerApi,
		deleteOperationBannerApi,
		getOperationBannerInfoApi,
		getOperationBannerPageApi,
		updateOperationBannerApi,
	} from '@/api/modules/operation-banner'
	import {
		confirmAction,
		showError,
		showSuccess
	} from '@/utils/modal'
	import {
		isAuth
	} from '@/utils/tool'

	const positionTabs = [{
			label: '商城首页',
			value: BANNER_POSITION.SHOP,
			desc: '用户端「商城」Tab 顶部轮播'
		},
		{
			label: 'APP首页',
			value: BANNER_POSITION.HOME,
			desc: '用户端首页顶部主视觉'
		},
		{
			label: '领养页',
			value: BANNER_POSITION.ADOPT,
			desc: '用户端领养专区轮播'
		},
	]

	const activePosition = ref(BANNER_POSITION.SHOP)
	const loading = ref(false)
	const dialogVisible = ref(false)
	const dialogLoading = ref(false)
	const detailLoading = ref(false)
	const uploadLoading = ref(false)
	const tableData = ref([])
	const total = ref(0)
	const pageNum = ref(1)
	const pageSize = ref(10)
	const editingId = ref(null)

	const filters = reactive({
		name: '',
		status: '',
	})

	const form = reactive({
		name: '',
		subTitle: '',
		imageUrl: '',
		jumpUrl: '',
		sort: 0,
		status: 1,
	})

	const statusOptions = [{
			label: '全部',
			value: ''
		},
		{
			label: '启用',
			value: 1
		},
		{
			label: '禁用',
			value: 2
		},
	]

	const currentTabMeta = computed(() => positionTabs.find((item) => item.value === activePosition.value) || positionTabs[
		0])

	function resetForm() {
		Object.assign(form, {
			name: '',
			subTitle: '',
			imageUrl: '',
			jumpUrl: '',
			sort: 0,
			status: 1,
		})
	}

	function resolveUploadUrl(data) {
		return String(data?.url || data?.data || data?.src || data?.path || data || '')
	}

	function buildQueryParams() {
		return {
			page: pageNum.value,
			limit: pageSize.value,
			positionCode: activePosition.value,
			name: filters.name.trim(),
			status: filters.status,
		}
	}

	function buildSubmitPayload() {
		return {
			name: form.name.trim(),
			subTitle: form.subTitle.trim(),
			imageUrl: form.imageUrl,
			jumpUrl: form.jumpUrl.trim(),
			sort: Number(form.sort || 0),
			status: Number(form.status || 1),
			positionCode: activePosition.value,
		}
	}

	async function loadData() {
		loading.value = true
		try {
			const data = await getOperationBannerPageApi(buildQueryParams())
			tableData.value = data.list || []
			total.value = data.total || 0
		} catch (error) {
			tableData.value = []
			total.value = 0
			showError(error?.msg || error?.message || '加载轮播图失败')
		} finally {
			loading.value = false
		}
	}

	function handlePositionChange() {
		pageNum.value = 1
		loadData()
	}

	function handleSearch() {
		pageNum.value = 1
		loadData()
	}

	function handleReset() {
		filters.name = ''
		filters.status = ''
		pageNum.value = 1
		loadData()
	}

	function openCreate() {
		editingId.value = null
		resetForm()
		dialogVisible.value = true
	}

	async function openEdit(row) {
		editingId.value = row.id
		resetForm()
		dialogVisible.value = true
		detailLoading.value = true
		try {
			const detail = await getOperationBannerInfoApi(row.id)
			Object.assign(form, {
				name: detail.name || '',
				subTitle: detail.subTitle || '',
				imageUrl: detail.imageUrl || '',
				jumpUrl: detail.jumpUrl || '',
				sort: detail.sort ?? 0,
				status: Number(detail.status ?? 1),
			})
		} catch (error) {
			showError(error?.msg || error?.message || '加载轮播详情失败')
			dialogVisible.value = false
		} finally {
			detailLoading.value = false
		}
	}

	async function handleImageUpload(option) {
		uploadLoading.value = true
		try {
			const data = await uploadMerchantImageApi(option.file)
			form.imageUrl = resolveUploadUrl(data)
			if (!form.imageUrl) {
				throw new Error('未获取到图片地址')
			}
			option.onSuccess?.(data)
		} catch (error) {
			showError(error?.message || '图片上传失败')
			option.onError?.(error)
		} finally {
			uploadLoading.value = false
		}
	}

	async function handleSubmit() {
		if (!form.imageUrl) return showError('请上传轮播图')
		if (!form.name.trim()) return showError('请输入轮播图名称')

		dialogLoading.value = true
		try {
			const payload = buildSubmitPayload()
			if (editingId.value) {
				await updateOperationBannerApi({
					...payload,
					id: editingId.value
				})
				showSuccess('修改成功')
			} else {
				await createOperationBannerApi(payload)
				showSuccess('新增成功')
			}
			dialogVisible.value = false
			await loadData()
		} catch (error) {
			showError(error?.msg || error?.message || '保存失败')
		} finally {
			dialogLoading.value = false
		}
	}

	async function handleDelete(row) {
		await confirmAction(`确定删除轮播图「${row.name}」吗？`)
		try {
			await deleteOperationBannerApi(row.id)
			showSuccess('删除成功')
			await loadData()
		} catch (error) {
			showError(error?.msg || error?.message || '删除失败')
		}
	}

	async function handleStatusChange(row, previousValue) {
		try {
			await updateOperationBannerApi({
				id: row.id,
				name: row.name,
				imageUrl: row.imageUrl,
				jumpUrl: row.jumpUrl || '',
				sort: row.sort ?? 0,
				status: Number(row.status),
				positionCode: activePosition.value,
			})
			showSuccess('状态已更新')
		} catch (error) {
			row.status = previousValue
			showError(error?.msg || error?.message || '状态更新失败')
		}
	}

	loadData()
</script>

<template>
	<div class="hd">
		<header class="hd-hero nf-page-hero">
			<span class="hd-hero__badge">内容管理</span>
			<h1 class="hd-hero__title">首页装修</h1>
			<p class="hd-hero__desc">{{ currentTabMeta.desc }}，按排序值从小到大展示，仅「启用」状态对用户可见</p>
		</header>

		<div class="hd-filter nf-filter-card">
			<div class="nf-filter-card__head">
				<span>筛选条件</span>
			</div>
			<el-form :inline="true" class="hd-filter__form" @submit.prevent>
				<el-form-item label="名称">
					<el-input v-model="filters.name" clearable placeholder="轮播图名称" @keyup.enter="handleSearch" />
				</el-form-item>
				<el-form-item label="状态">
					<el-select v-model="filters.status" clearable placeholder="全部">
						<el-option v-for="item in statusOptions" :key="`status-${item.value}`" :label="item.label"
							:value="item.value" />
					</el-select>
				</el-form-item>
				<el-form-item class="hd-filter__actions">
					<el-space wrap>
						<el-button type="primary" @click="handleSearch">查询</el-button>
						<el-button @click="handleReset">重置</el-button>
						<el-button type="primary" :disabled="!isAuth('bannerList:add')" @click="openCreate">新增轮播图</el-button>
					</el-space>
				</el-form-item>
			</el-form>
		</div>

		<div class="hd-tabs">
			<el-tabs v-model="activePosition" @tab-change="handlePositionChange">
				<el-tab-pane v-for="item in positionTabs" :key="item.value" :label="item.label" :name="item.value" />
			</el-tabs>
		</div>

		<div class="hd-table nf-table-card">
			<el-table v-loading="loading" :data="tableData" border>
				<el-table-column prop="id" label="ID" width="80" />
				<el-table-column label="图片" width="110">
					<template #default="{ row }">
						<el-image v-if="row.imageUrl" :src="row.imageUrl" :preview-src-list="[row.imageUrl]" fit="cover"
							class="thumb-image" preview-teleported />
						<span v-else>-</span>
					</template>
				</el-table-column>
				<el-table-column prop="name" label="名称" min-width="160" />
				<el-table-column prop="jumpUrl" label="跳转链接" min-width="220" show-overflow-tooltip />
				<el-table-column prop="sort" label="排序" width="80" align="center" />
				<el-table-column label="状态" width="100" align="center">
					<template #default="{ row }">
						<el-switch v-model="row.status" :active-value="1" :inactive-value="2"
							:disabled="!isAuth('bannerList:update')"
							@change="(value) => handleStatusChange(row, value === 1 ? 2 : 1)" />
					</template>
				</el-table-column>
				<el-table-column prop="createTime" label="创建时间" width="170" />
				<el-table-column label="操作" width="160" fixed="right" align="center">
					<template #default="{ row }">
						<el-space>
							<el-button size="small" type="primary" :disabled="!isAuth('bannerList:update')"
								@click="openEdit(row)">编辑</el-button>
							<el-button size="small" type="danger" :disabled="!isAuth('bannerList:delete')"
								@click="handleDelete(row)">删除</el-button>
						</el-space>
					</template>
				</el-table-column>
			</el-table>

			<BasePagination :current-page="pageNum" :page-size="pageSize" :page-sizes="[10, 20, 50]" :total="total"
				@update:currentPage="pageNum = $event; loadData()"
				@update:pageSize="pageSize = $event; pageNum = 1; loadData()" />
		</div>

		<el-dialog v-model="dialogVisible" :title="editingId ? '编辑轮播图' : '新增轮播图'" width="640px" destroy-on-close class="nf-pro-dialog" align-center>
<div v-loading="detailLoading">
				<el-form label-width="100px">
					<el-form-item label="展示位置">
						<el-tag type="info">{{ currentTabMeta.label }}</el-tag>
					</el-form-item>
					<el-form-item label="轮播图" required>
						<div class="upload-row">
							<el-image v-if="form.imageUrl" :src="form.imageUrl" :preview-src-list="[form.imageUrl]"
								fit="cover" class="preview-image" preview-teleported />
							<el-upload :show-file-list="false" :http-request="handleImageUpload">
								<el-button :loading="uploadLoading">上传图片</el-button>
							</el-upload>
						</div>
					</el-form-item>
					<el-form-item label="名称" required>
						<el-input v-model="form.name" placeholder="如：春季萌宠市集" maxlength="64" show-word-limit />
					</el-form-item>
					<el-form-item label="副标题">
						<el-input v-model="form.subTitle" placeholder="首页主视觉副标题" maxlength="120" show-word-limit />
					</el-form-item>
					<el-form-item label="跳转链接">
						<el-input v-model="form.jumpUrl" placeholder="如：/packageA/product/detail?id=7001" />
					</el-form-item>
					<el-form-item label="排序">
						<el-input-number v-model="form.sort" :min="0" :max="9999" />
					</el-form-item>
					<el-form-item label="状态">
						<el-radio-group v-model="form.status">
							<el-radio :value="1">启用</el-radio>
							<el-radio :value="2">禁用</el-radio>
						</el-radio-group>
					</el-form-item>
				</el-form>
			</div>
			      <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dialogLoading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
		</el-dialog>
	</div>
</template>

<style scoped lang="scss">
.hd {
	display: flex;
	flex-direction: column;
	gap: 12px;
	padding: 0 0 8px;
	overflow-x: hidden;
}

.hd-hero.nf-page-hero {
	margin-bottom: 0;
	display: flex;
	flex-direction: column;
	gap: 6px;
}

.hd-hero__badge {
	display: inline-flex;
	align-self: flex-start;
	padding: 2px 10px;
	font-size: 12px;
	font-weight: 600;
	color: var(--nf-text-primary);
	background: var(--nf-brand-lite);
	border: 1px solid var(--nf-brand);
	border-radius: var(--nf-radius-sm);
}

.hd-hero__title {
	margin: 0;
	font-size: 22px;
	font-weight: 700;
	color: var(--nf-text-primary);
	line-height: 1.3;
}

.hd-hero__desc {
	margin: 0;
	font-size: 13px;
	color: var(--nf-text-secondary);
	line-height: 1.5;
}

.hd-filter.nf-filter-card {
	margin-bottom: 0;
}

.hd-filter__form {
	margin-bottom: 0;

  :deep(.el-select),
	:deep(.el-input) {
		width: 180px;
	}
}

.hd-filter__actions {
	:deep(.el-form-item__content) {
		flex-wrap: wrap;
	}
}

.hd-tabs {
	:deep(.el-tabs__header) {
		margin-bottom: 0;
		padding: 0 16px;
		background: #fff;
		border: 1px solid var(--nf-border);
		border-radius: 8px 8px 0 0;
		box-shadow: var(--nf-shadow-card);
	}

	:deep(.el-tabs__content) {
		display: none;
	}
}

.hd-table.nf-table-card {
	margin-top: 0;
	margin-bottom: 0;
	padding-bottom: 12px;
	border-radius: 0 0 8px 8px;
	border-top: none;
	box-shadow: var(--nf-shadow-card);

	:deep(.el-table) {
		--el-table-border-color: var(--nf-border);
		--el-table-header-bg-color: var(--nf-sidebar-bg-alt, #f7f8fa);
	}

	:deep(.el-table th.el-table__cell) {
		font-weight: 600;
		color: var(--nf-text-primary);
	}

	:deep(.el-table td.el-table__cell) {
		background: #fff;
	}
}

.thumb-image {
	width: 72px;
	height: 72px;
	border-radius: 8px;
}

.preview-image {
	width: 148px;
	height: 148px;
	border-radius: 10px;
}

.upload-row {
	display: flex;
	align-items: center;
	gap: 16px;
	flex-wrap: wrap;
}

@media (max-width: 768px) {
	.hd-filter__form {
		:deep(.el-select),
		:deep(.el-input) {
			width: 100%;
		}
	}
}
</style>