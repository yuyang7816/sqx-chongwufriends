<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageTopbar from '@/components/common/PageTopbar.vue'
import {
  createMerchantGoodsApi,
  getMerchantGoodsDetailApi,
  uploadMerchantImageApi,
  updateMerchantGoodsApi,
} from '@/api/modules/merchant-center'
import { showError, showSuccess } from '@/utils/modal'

const route = useRoute()
const router = useRouter()

const merchantId = computed(() => Number(route.params.id || 0))
const goodsId = computed(() => Number(route.params.goodsId || 0))
const isEdit = computed(() => Boolean(goodsId.value))

const loading = ref(false)
const saving = ref(false)
const uploadingField = ref('')

const form = reactive({
  goodsName: '',
  goodsType: '',
  hourPrice: '',
  minutePrice: '',
  yyToken: '',
  yyId: '',
  sort: '',
  sales: '',
  goodsTips: '',
  cameraId: '',
  isOpen: 1,
  isVip: 2,
  goodsCoverImage: '',
  goodsTitleImage: '',
  iconImg: '',
  goodsCategory: 1,
  subscription: '',
})

const title = computed(() => (isEdit.value ? '羽毛球场修改' : '添加羽毛球场'))

const heroDesc = computed(() =>
  isEdit.value
    ? '修改羽毛球场基础信息、价格与图片资源，保存后立即生效'
    : '填写羽毛球场基础信息、价格与图片资源，完成后发布至门店商品列表',
)

function goBack() {
  router.push({ path: `/merchants/${merchantId.value}`, query: { tab: 'goods' } })
}

async function handleUpload(field, option) {
  uploadingField.value = field
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = typeof data === 'string' ? data : data?.url || data?.data || data?.src || data?.path || ''
    form[field] = String(url || '')
    option.onSuccess?.(data)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadingField.value = ''
  }
}

function validateForm() {
  if (!form.goodsName.trim()) return '请输入羽毛球场名称'
  if (!form.hourPrice && form.hourPrice !== 0) return '请输入半小时价'
  if (!form.minutePrice && form.minutePrice !== 0) return '请输入分钟价'
  if (!form.goodsCoverImage) return '请上传开台前图片'
  if (!form.goodsTitleImage) return '请上传开台后图片'
  if (!form.iconImg) return '请上传羽毛球场图标'
  if (!form.goodsType.trim()) return '请输入羽毛球场信息'
  return ''
}

async function loadDetail() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await getMerchantGoodsDetailApi(goodsId.value)
    Object.assign(form, {
      goodsName: data.goodsName || '',
      goodsType: data.goodsType || '',
      hourPrice: data.hourPrice ?? '',
      minutePrice: data.minutePrice ?? '',
      yyToken: data.yyToken || '',
      yyId: data.yyId || '',
      sort: data.sort ?? '',
      sales: data.sales ?? '',
      goodsTips: data.goodsTips || '',
      cameraId: data.cameraId || '',
      isOpen: Number(data.isOpen || 1),
      isVip: Number(data.isVip || 2),
      goodsCoverImage: data.goodsCoverImage || '',
      goodsTitleImage: data.goodsTitleImage || '',
      iconImg: data.iconImg || '',
      goodsCategory: Number(data.goodsCategory || 1),
      subscription: data.subscription ?? '',
    })
  } finally {
    loading.value = false
  }
}

async function submit() {
  const message = validateForm()
  if (message) {
    showError(message)
    return
  }
  saving.value = true
  try {
    const payload = {
      goodsId: isEdit.value ? goodsId.value : undefined,
      tearoomId: merchantId.value,
      goodsName: form.goodsName.trim(),
      goodsType: form.goodsType.trim(),
      goodsCoverImage: form.goodsCoverImage,
      goodsTitleImage: form.goodsTitleImage,
      iconImg: form.iconImg,
      goodsDetails: '',
      goodsTips: form.goodsTips.trim(),
      cameraId: form.cameraId.trim(),
      sales: form.sales,
      goodsCategory: Number(form.goodsCategory || 1),
      hourPrice: form.hourPrice,
      minutePrice: form.minutePrice,
      subscription: form.subscription,
      yyToken: form.yyToken.trim(),
      yyId: form.yyId.trim(),
      sort: form.sort,
      isOpen: Number(form.isOpen || 1),
      isVip: Number(form.isVip || 2),
    }
    if (isEdit.value) {
      await updateMerchantGoodsApi(payload)
      showSuccess('操作成功')
    } else {
      delete payload.goodsId
      await createMerchantGoodsApi(payload)
      showSuccess('操作成功')
    }
    goBack()
  } finally {
    saving.value = false
  }
}

watch(
  () => route.fullPath,
  () => {
    loadDetail()
  },
  { immediate: true },
)
</script>

<template>
  <div class="mgf">
    <PageTopbar :title="title" @back="goBack" />

    <header class="mgf-hero nf-page-hero">
      <span class="mgf-hero__badge">羽毛球馆</span>
      <h1 class="mgf-hero__title">{{ title }}</h1>
      <p class="mgf-hero__desc">{{ heroDesc }}</p>
    </header>

    <div v-loading="loading" class="mgf-form nf-table-card">
      <div class="nf-filter-card__head">
        <span>场地信息</span>
      </div>
      <el-form label-width="130px" class="mgf-form__grid">
        <el-form-item label="羽毛球场名称">
          <el-input v-model="form.goodsName" placeholder="请输入羽毛球场名称" />
        </el-form-item>
        <el-form-item label="羽毛球场信息">
          <el-input v-model="form.goodsType" placeholder="请输入羽毛球场信息" />
        </el-form-item>
        <el-form-item label="半小时价">
          <el-input v-model="form.hourPrice" type="number" placeholder="请输入半小时价" />
        </el-form-item>
        <el-form-item label="分钟价">
          <el-input v-model="form.minutePrice" type="number" placeholder="请输入分钟价" />
        </el-form-item>
        <el-form-item label="语音播报token">
          <el-input v-model="form.yyToken" placeholder="请输入语音播报token" />
        </el-form-item>
        <el-form-item label="语音播报ID">
          <el-input v-model="form.yyId" placeholder="请输入语音播报ID" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model="form.sort" type="number" placeholder="请输入排序" />
        </el-form-item>
        <el-form-item label="羽毛球场销量">
          <el-input v-model="form.sales" type="number" placeholder="请输入羽毛球场销量" />
        </el-form-item>
        <el-form-item label="推流链接">
          <el-input v-model="form.goodsTips" placeholder="请输入推流链接" />
        </el-form-item>
        <el-form-item label="摄像头配置">
          <el-input v-model="form.cameraId" placeholder="请输入摄像头配置" />
        </el-form-item>
        <el-form-item label="是否线下开台">
          <el-radio-group v-model="form.isOpen">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="2">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否是vip区域">
          <el-radio-group v-model="form.isVip">
            <el-radio :value="1">是</el-radio>
            <el-radio :value="2">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开台前图片">
          <div class="upload-row">
            <img v-if="form.goodsCoverImage" :src="form.goodsCoverImage" class="image-preview" />
            <el-upload :show-file-list="false" :http-request="(option) => handleUpload('goodsCoverImage', option)">
              <el-button :loading="uploadingField === 'goodsCoverImage'">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="开台后图片">
          <div class="upload-row">
            <img v-if="form.goodsTitleImage" :src="form.goodsTitleImage" class="image-preview" />
            <el-upload :show-file-list="false" :http-request="(option) => handleUpload('goodsTitleImage', option)">
              <el-button :loading="uploadingField === 'goodsTitleImage'">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="羽毛球场图标">
          <div class="upload-row">
            <img v-if="form.iconImg" :src="form.iconImg" class="image-preview" />
            <el-upload :show-file-list="false" :http-request="(option) => handleUpload('iconImg', option)">
              <el-button :loading="uploadingField === 'iconImg'">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <div class="mgf-form__actions">
        <el-button @click="goBack">返回上一页</el-button>
        <el-button type="primary" :loading="saving" @click="submit">{{ isEdit ? '保存' : '确认发布' }}</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.mgf {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.mgf-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mgf-hero__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary);
  background: var(--nf-brand-lite);
  border: 1px solid var(--nf-brand);
  border-radius: var(--nf-radius-sm, 6px);
}

.mgf-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.mgf-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.mgf-form.nf-table-card {
  margin-bottom: 0;
}

.mgf-form__grid {
  max-width: 900px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-input) {
    max-width: 420px;
  }
}

.upload-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.image-preview {
  width: 148px;
  height: 148px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--nf-border, #e8eaed);
  background: #fff;
}

.mgf-form__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--nf-border, #e8eaed);
}
</style>
