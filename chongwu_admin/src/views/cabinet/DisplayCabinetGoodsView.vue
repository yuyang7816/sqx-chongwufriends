<script setup>
import { computed, ref, reactive } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import { getVenueSimplePageApi } from '@/api/modules/display-cabinet'
import {
  createDisplayCabinetGoodsApi,
  createDisplayCabinetGoodsCategoryApi,
  deleteDisplayCabinetGoodsApi,
  deleteDisplayCabinetGoodsCategoryApi,
  getDisplayCabinetGoodsCategoryPageApi,
  getDisplayCabinetGoodsPageApi,
  toggleDisplayCabinetGoodsCategoryApi,
  updateDisplayCabinetGoodsApi,
  updateDisplayCabinetGoodsCategoryApi,
} from '@/api/modules/display-cabinet-goods'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const activeTab = ref('goods')

const goodsLoading = ref(false)
const goodsPage = ref(1)
const goodsSize = ref(10)
const goodsTotal = ref(0)
const goodsList = ref([])

const goodsFilters = reactive({
  tearoomId: '',
  bannerId: '',
})

const categoryLoading = ref(false)
const categoryPage = ref(1)
const categorySize = ref(10)
const categoryTotal = ref(0)
const categoryList = ref([])

const categoryFilters = reactive({
  tearoomId: '',
})

const venueOptions = ref([])

const goodsDialogVisible = ref(false)
const goodsDialogLoading = ref(false)
const goodsDialogTitle = computed(() => (goodsForm.id ? '修改' : '添加'))
const goodsUploadLoading = ref(false)
const goodsForm = reactive({
  id: '',
  title: '',
  money: '',
  goodsNum: '',
  image: '',
  goodsDesc: '',
  tearoomId: '',
  tearoomName: '',
  bannerId: '',
})

const venueDialogVisible = ref(false)
const venueLoading = ref(false)
const venuePage = ref(1)
const venueSize = ref(10)
const venueTotal = ref(0)
const venueList = ref([])
const venueKeyword = ref('')
const venueSelectTarget = ref('goods')

const categoryDialogVisible = ref(false)
const categoryDialogLoading = ref(false)
const categoryDialogTitle = computed(() => (categoryForm.id ? '修改分类' : '添加分类'))
const categoryForm = reactive({
  id: '',
  tearoomId: '',
  name: '',
})

function splitImages(value) {
  return String(value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function buildAddress(row) {
  return [row.tearoomProvince, row.tearoomCity, row.tearoomDistrict, row.tearoomAddress].filter(Boolean).join('')
}

async function loadVenueOptions() {
  const data = await getVenueSimplePageApi({ page: 1, limit: 1000, tearoomName: '', city: '', status: '' })
  venueOptions.value = data.list || []
}

async function loadGoodsList() {
  goodsLoading.value = true
  try {
    const data = await getDisplayCabinetGoodsPageApi({
      page: goodsPage.value,
      limit: goodsSize.value,
      tearoomId: goodsFilters.tearoomId,
      bannerId: goodsFilters.bannerId,
    })
    goodsList.value = data.list || []
    goodsTotal.value = data.total || 0
  } finally {
    goodsLoading.value = false
  }
}

async function loadCategoryList() {
  categoryLoading.value = true
  try {
    const data = await getDisplayCabinetGoodsCategoryPageApi({
      page: categoryPage.value,
      limit: categorySize.value,
      state: -1,
      classify: 100,
      tearoomId: categoryFilters.tearoomId,
    })
    categoryList.value = data.list || []
    categoryTotal.value = data.total || 0
  } finally {
    categoryLoading.value = false
  }
}

function handleGoodsSearch() {
  goodsPage.value = 1
  loadGoodsList()
}

function handleGoodsReset() {
  goodsFilters.tearoomId = ''
  goodsFilters.bannerId = ''
  goodsPage.value = 1
  loadGoodsList()
}

function handleCategorySearch() {
  categoryPage.value = 1
  loadCategoryList()
}

function handleCategoryReset() {
  categoryFilters.tearoomId = ''
  categoryPage.value = 1
  loadCategoryList()
}

function openCreateGoodsDialog() {
  Object.assign(goodsForm, {
    id: '',
    title: '',
    money: '',
    goodsNum: '',
    image: '',
    goodsDesc: '',
    tearoomId: '',
    tearoomName: '',
    bannerId: '',
  })
  goodsDialogVisible.value = true
}

function openEditGoodsDialog(row) {
  Object.assign(goodsForm, {
    id: row.id,
    title: row.title || '',
    money: row.money || '',
    goodsNum: row.goodsNum || '',
    image: row.image || '',
    goodsDesc: row.goodsDesc || '',
    tearoomId: row.tearoomId || '',
    tearoomName: row.tearoom?.tearoomName || row.tearoomName || '',
    bannerId: row.bannerId || '',
  })
  goodsDialogVisible.value = true
}

async function handleGoodsImageUpload(option) {
  goodsUploadLoading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    goodsForm.image = String(data?.url || data?.data || data?.src || data?.path || data || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    goodsUploadLoading.value = false
  }
}

async function submitGoods() {
  if (!goodsForm.tearoomId) return showError('请选择羽毛球馆')
  if (!goodsForm.title.trim()) return showError('请输入商品名称')
  if (goodsForm.money === '') return showError('请输入金额')
  if (goodsForm.goodsNum === '') return showError('请输入商品库存')
  if (!goodsForm.image) return showError('请上传图片')
  if (!goodsForm.goodsDesc.trim()) return showError('请输入商品介绍')

  goodsDialogLoading.value = true
  try {
    const payload = {
      id: goodsForm.id || undefined,
      title: goodsForm.title.trim(),
      money: goodsForm.money,
      goodsNum: goodsForm.goodsNum,
      image: goodsForm.image,
      goodsDesc: goodsForm.goodsDesc.trim(),
      tearoomId: goodsForm.tearoomId,
      bannerId: goodsForm.bannerId,
    }
    if (goodsForm.id) {
      await updateDisplayCabinetGoodsApi(payload)
    } else {
      delete payload.id
      await createDisplayCabinetGoodsApi(payload)
    }
    showSuccess('操作成功')
    goodsDialogVisible.value = false
    await loadGoodsList()
  } finally {
    goodsDialogLoading.value = false
  }
}

async function removeGoods(row) {
  await confirmAction('确定删除此商品?')
  await deleteDisplayCabinetGoodsApi(row.id)
  showSuccess('删除成功')
  await loadGoodsList()
}

function openVenueDialog(target) {
  venueSelectTarget.value = target
  venuePage.value = 1
  venueKeyword.value = ''
  venueDialogVisible.value = true
  loadVenueList()
}

async function loadVenueList() {
  venueLoading.value = true
  try {
    const data = await getVenueSimplePageApi({
      page: venuePage.value,
      limit: venueSize.value,
      tearoomName: venueKeyword.value.trim(),
      city: '',
      status: '',
    })
    venueList.value = data.list || []
    venueTotal.value = data.total || 0
  } finally {
    venueLoading.value = false
  }
}

function chooseVenue(row) {
  if (venueSelectTarget.value === 'goods') {
    goodsForm.tearoomId = row.tearoomId
    goodsForm.tearoomName = row.tearoomName || ''
  } else {
    categoryForm.tearoomId = row.tearoomId
  }
  venueDialogVisible.value = false
}

function openCreateCategoryDialog() {
  Object.assign(categoryForm, {
    id: '',
    tearoomId: '',
    name: '',
  })
  categoryDialogVisible.value = true
}

function openEditCategoryDialog(row) {
  Object.assign(categoryForm, {
    id: row.id,
    tearoomId: row.tearoomId || '',
    name: row.name || '',
  })
  categoryDialogVisible.value = true
}

async function submitCategory() {
  if (!categoryForm.tearoomId) return showError('请选择羽毛球馆')
  if (!categoryForm.name.trim()) return showError('请输入名称')

  categoryDialogLoading.value = true
  try {
    const payload = {
      classify: 100,
      imageUrl: '',
      state: '',
      url: '',
      name: categoryForm.name.trim(),
      describes: '',
      id: categoryForm.id || undefined,
      tearoomId: categoryForm.tearoomId,
    }
    if (categoryForm.id) {
      await updateDisplayCabinetGoodsCategoryApi(payload)
    } else {
      delete payload.id
      await createDisplayCabinetGoodsCategoryApi(payload)
    }
    showSuccess('操作成功')
    categoryDialogVisible.value = false
    await loadCategoryList()
    await loadVenueOptions()
  } finally {
    categoryDialogLoading.value = false
  }
}

async function removeCategory(row) {
  await confirmAction('确定删除此条信息?')
  await deleteDisplayCabinetGoodsCategoryApi(row.id)
  showSuccess('删除成功')
  await loadCategoryList()
}

async function toggleCategory(row) {
  await toggleDisplayCabinetGoodsCategoryApi(row.id)
  showSuccess('操作成功')
  await loadCategoryList()
}

loadVenueOptions()
loadGoodsList()
loadCategoryList()
</script>

<template>
  <div class="cab-goods">
    <header class="cab-goods-hero nf-page-hero">
      <span class="cab-goods-hero__badge">展示柜</span>
      <h1 class="cab-goods-hero__title">展示柜商品管理</h1>
      <p class="cab-goods-hero__desc">管理展示柜商品与商品分类，支持筛选查询、添加修改与分类启用切换</p>
    </header>

    <div class="cab-goods-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品列表" name="goods">
          <div class="cab-goods-filter nf-filter-card">
            <div class="nf-filter-card__head">
              <span>筛选条件</span>
            </div>
            <el-form :inline="true" class="cab-goods-filter__form" @submit.prevent>
            <el-form-item label="商品分类">
              <el-select v-model="goodsFilters.bannerId" placeholder="请选择商品分类" clearable @change="handleGoodsSearch">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="羽毛球馆">
              <el-select v-model="goodsFilters.tearoomId" placeholder="请选择羽毛球馆" clearable @change="handleGoodsSearch">
                <el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" />
              </el-select>
            </el-form-item>
            <el-form-item class="cab-goods-filter__actions">
              <el-space wrap>
                <el-button type="primary" @click="handleGoodsSearch">查询</el-button>
                <el-button @click="handleGoodsReset">重置</el-button>
                <el-button type="primary" :disabled="!isAuth('commodity:add')" @click="openCreateGoodsDialog">添加</el-button>
              </el-space>
            </el-form-item>
          </el-form>
          </div>

          <div class="cab-goods-table nf-table-card">
          <el-table v-loading="goodsLoading" :data="goodsList" border>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="tearoom.tearoomName" label="羽毛球馆名称" min-width="160" />
            <el-table-column prop="banner.name" label="商品分类" min-width="140" />
            <el-table-column prop="title" label="商品名称" min-width="160" />
            <el-table-column label="商品图片" width="100">
              <template #default="{ row }"><el-image :src="row.image" class="thumb-image" :preview-src-list="[row.image]" preview-teleported /></template>
            </el-table-column>
            <el-table-column prop="money" label="金额(元)" width="100" />
            <el-table-column prop="goodsNum" label="商品库存" width="100" />
            <el-table-column prop="goodsDesc" label="商品介绍" min-width="240" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-space wrap alignment="start">
                  <el-button size="small" type="primary" :disabled="!isAuth('commodity:update')" @click="openEditGoodsDialog(row)">修改</el-button>
                  <el-button size="small" type="danger" :disabled="!isAuth('commodity:delete')" @click="removeGoods(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>

          <BasePagination :current-page="goodsPage" :page-size="goodsSize" :page-sizes="[10, 20, 30, 50, 100]" :total="goodsTotal" @update:currentPage="goodsPage = $event; loadGoodsList()" @update:pageSize="goodsSize = $event; goodsPage = 1; loadGoodsList()" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="商品分类" name="categories">
          <div class="cab-goods-filter nf-filter-card">
            <div class="nf-filter-card__head">
              <span>筛选条件</span>
            </div>
            <el-form :inline="true" class="cab-goods-filter__form" @submit.prevent>
            <el-form-item label="羽毛球馆">
              <el-select v-model="categoryFilters.tearoomId" placeholder="请选择羽毛球馆" clearable @change="handleCategorySearch">
                <el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" />
              </el-select>
            </el-form-item>
            <el-form-item class="cab-goods-filter__actions">
              <el-space wrap>
                <el-button type="primary" @click="handleCategorySearch">查询</el-button>
                <el-button @click="handleCategoryReset">重置</el-button>
                <el-button type="primary" :disabled="!isAuth('bannerList:add')" @click="openCreateCategoryDialog">添加分类</el-button>
              </el-space>
            </el-form-item>
          </el-form>
          </div>

          <div class="cab-goods-table nf-table-card">
          <el-table v-loading="categoryLoading" :data="categoryList" border>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="tearoom.tearoomName" label="羽毛球馆" min-width="160" />
            <el-table-column prop="name" label="分类名称" min-width="160" />
            <el-table-column label="是否启用" width="120">
              <template #default="{ row }"><el-switch v-model="row.state" :active-value="1" :inactive-value="2" @change="toggleCategory(row)" /></template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-space wrap alignment="start">
                  <el-button size="small" type="primary" :disabled="!isAuth('bannerList:update')" @click="openEditCategoryDialog(row)">修改</el-button>
                  <el-button size="small" type="danger" :disabled="!isAuth('bannerList:delete')" @click="removeCategory(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>

          <BasePagination :current-page="categoryPage" :page-size="categorySize" :page-sizes="[10, 20, 30, 50, 100]" :total="categoryTotal" @update:currentPage="categoryPage = $event; loadCategoryList()" @update:pageSize="categorySize = $event; categoryPage = 1; loadCategoryList()" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="goodsDialogVisible" :title="goodsDialogTitle" width="720px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">羽毛球馆</span><el-input v-model="goodsForm.tearoomName" class="dialog-field" placeholder="请选择羽毛球馆" readonly @click="openVenueDialog('goods')" /></div>
        <div class="form-row"><span class="form-label">商品分类</span><el-select v-model="goodsForm.bannerId" class="dialog-field"><el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" /></el-select></div>
        <div class="form-row"><span class="form-label">商品名称</span><el-input v-model="goodsForm.title" class="dialog-field" placeholder="请输入商品名称" /></div>
        <div class="form-row"><span class="form-label">金额(元)</span><el-input v-model="goodsForm.money" type="number" class="dialog-field" placeholder="请输入金额(元)" /></div>
        <div class="form-row"><span class="form-label">商品库存</span><el-input v-model="goodsForm.goodsNum" type="number" class="dialog-field" placeholder="请输入商品库存" /></div>
        <div class="form-row"><span class="form-label">商品图片</span><div class="upload-row"><el-image v-if="goodsForm.image" :src="goodsForm.image" class="large-thumb" :preview-src-list="[goodsForm.image]" preview-teleported /><el-upload :show-file-list="false" :http-request="handleGoodsImageUpload"><el-button :loading="goodsUploadLoading">上传</el-button></el-upload></div></div>
        <div class="form-row"><span class="form-label">商品介绍</span><el-input v-model="goodsForm.goodsDesc" class="dialog-field" placeholder="请输入商品介绍" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="goodsDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="goodsDialogLoading" @click="submitGoods">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" :title="categoryDialogTitle" width="560px" destroy-on-close class="nf-pro-dialog" align-center>
<div class="form-stack">
        <div class="form-row"><span class="form-label">羽毛球馆</span><el-select v-model="categoryForm.tearoomId" class="dialog-field"><el-option v-for="item in venueOptions" :key="item.tearoomId" :label="item.tearoomName" :value="item.tearoomId" /></el-select></div>
        <div class="form-row"><span class="form-label">分类名称</span><el-input v-model="categoryForm.name" class="dialog-field" placeholder="请输入名称" /></div>
      </div>
            <template #footer>
        <div class="nf-pro-dialog__footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="categoryDialogLoading" @click="submitCategory">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="venueDialogVisible" title="羽毛球馆列表" width="80%" destroy-on-close class="nf-pro-dialog" align-center>
<el-form :inline="true" class="search-grid" @submit.prevent>
        <el-form-item label="羽毛球馆名称">
          <el-input v-model="venueKeyword" clearable placeholder="请输入羽毛球馆名称" @keyup.enter="venuePage = 1; loadVenueList()" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="venuePage = 1; loadVenueList()">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="venueLoading" :data="venueList" border>
        <el-table-column prop="tearoomId" label="编号" width="80" fixed="left" />
        <el-table-column prop="tearoomName" label="羽毛球馆名称" min-width="140" fixed="left" />
        <el-table-column label="羽毛球馆logo" width="100"><template #default="{ row }"><el-image v-if="row.tearoomTitleImage" :src="row.tearoomTitleImage" class="thumb-image" :preview-src-list="splitImages(row.tearoomTitleImage)" preview-teleported /><span v-else>暂无图片</span></template></el-table-column>
        <el-table-column label="羽毛球馆轮播图" width="180"><template #default="{ row }"><div v-if="splitImages(row.tearoomCoverImage).length" class="image-list-cell"><el-image v-for="item in splitImages(row.tearoomCoverImage)" :key="item" :src="item" :preview-src-list="splitImages(row.tearoomCoverImage)" fit="cover" class="thumb-image" preview-teleported /></div></template></el-table-column>
        <el-table-column label="羽毛球馆地址" min-width="240"><template #default="{ row }"><div>{{ buildAddress(row) }}</div><div>经度：{{ row.tearoomLongitude || '未绑定' }}</div><div>纬度：{{ row.tearoomLatitude || '未绑定' }}</div></template></el-table-column>
        <el-table-column label="营业时间" width="120"><template #default="{ row }">{{ row.startTime }}--{{ row.endTime }}</template></el-table-column>
        <el-table-column prop="score" label="羽毛球馆评分" width="120" />
        <el-table-column label="操作" width="100" fixed="right"><template #default="{ row }"><el-button size="small" type="primary" @click="chooseVenue(row)">选择</el-button></template></el-table-column>
      </el-table>
      <BasePagination :current-page="venuePage" :page-size="venueSize" :page-sizes="[10, 20, 30, 50, 100]" :total="venueTotal" @update:currentPage="venuePage = $event; loadVenueList()" @update:pageSize="venueSize = $event; venuePage = 1; loadVenueList()" />
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.cab-goods {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.cab-goods-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cab-goods-hero__badge {
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

.cab-goods-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.cab-goods-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.cab-goods-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    background: #fff;
    border-radius: 8px 8px 0 0;
    padding: 0 16px;
    border: 1px solid var(--nf-border);
    border-bottom: none;
    box-shadow: var(--nf-shadow-card);
  }

  :deep(.el-tabs__content) {
    overflow: visible;
  }
}

.cab-goods-filter.nf-filter-card {
  margin-bottom: 0;
  border-radius: 0;
  border-top: none;
}

.cab-goods-filter__form {
  margin-bottom: 0;

  :deep(.el-select),
  :deep(.el-input) {
    width: 180px;
  }
}

.cab-goods-filter__actions {
  :deep(.el-form-item__content) {
    flex-wrap: wrap;
  }
}

.cab-goods-table.nf-table-card {
  margin-bottom: 0;
  padding-bottom: 12px;
  border-radius: 0 0 8px 8px;
  border-top: none;

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

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 100px;
  line-height: 32px;
  text-align: right;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.dialog-field {
  flex: 1;
}

.thumb-image {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: block;
}

.large-thumb {
  width: 148px;
  height: 148px;
  border-radius: 10px;
}

.upload-row,
.image-list-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .cab-goods-filter__form {
    :deep(.el-select),
    :deep(.el-input) {
      width: 100%;
    }
  }

  .form-row {
    flex-direction: column;
  }

  .form-label {
    width: auto;
    text-align: left;
  }
}
</style>
