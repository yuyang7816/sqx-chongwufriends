<script setup>
import { onMounted, reactive, ref } from 'vue'
import CoverImageUpload from '@/components/common/CoverImageUpload.vue'
import { getHomeSocialInfoApi, updateHomeSocialApi } from '@/api/modules/pet-home'
import { showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  qrUrl: '',
  wecomId: '',
  serviceLink: '',
  benefitText: '',
  xhsTitle: '',
  xhsSubtitle: '',
  xhsJumpUrl: '',
  communityTitle: '',
  communitySubtitle: '',
})

async function loadData() {
  loading.value = true
  try {
    const data = await getHomeSocialInfoApi()
    Object.assign(form, {
      qrUrl: data.qrUrl || '',
      wecomId: data.wecomId || '',
      serviceLink: data.serviceLink || '',
      benefitText: data.benefitText || '',
      xhsTitle: data.xhsTitle || '',
      xhsSubtitle: data.xhsSubtitle || '',
      xhsJumpUrl: data.xhsJumpUrl || '',
      communityTitle: data.communityTitle || '',
      communitySubtitle: data.communitySubtitle || '',
    })
  } catch (error) {
    showError(error?.msg || error?.message || '加载配置失败')
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  saving.value = true
  try {
    await updateHomeSocialApi({ ...form })
    showSuccess('保存成功')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="hsocial">
    <header class="hsocial-hero nf-page-hero">
      <span class="hsocial-hero__badge">宠物业务</span>
      <h1 class="hsocial-hero__title">首页社群入口</h1>
      <p class="hsocial-hero__desc">配置用户端首页小红书入口与社群卡片文案、二维码及客服链接</p>
    </header>

    <div v-loading="loading" class="hsocial-form nf-table-card">
      <div class="nf-filter-card__head">
        <span>小红书入口</span>
      </div>
      <el-form label-width="120px" class="hsocial-form__grid">
        <el-form-item label="标题"><el-input v-model="form.xhsTitle" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.xhsSubtitle" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.xhsJumpUrl" placeholder="可选，弹二维码时可不填" /></el-form-item>
        <el-form-item label="二维码">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="请到「系统配置 → 图片配置」中维护「首页关注小红书二维码图片」(home_xhs_qr_url)"
          />
        </el-form-item>
      </el-form>

      <div class="nf-filter-card__head hsocial-form__section-head">
        <span>社群入口</span>
      </div>
      <el-form label-width="120px" class="hsocial-form__grid">
        <el-form-item label="标题"><el-input v-model="form.communityTitle" /></el-form-item>
        <el-form-item label="副标题"><el-input v-model="form.communitySubtitle" /></el-form-item>
        <el-form-item label="企微二维码">
          <CoverImageUpload v-model="form.qrUrl" empty-text="上传二维码" />
        </el-form-item>
        <el-form-item label="企微 ID"><el-input v-model="form.wecomId" /></el-form-item>
        <el-form-item label="客服链接"><el-input v-model="form.serviceLink" placeholder="H5/外链客服地址" /></el-form-item>
        <el-form-item label="权益说明">
          <el-input
            v-model="form.benefitText"
            type="textarea"
            :rows="4"
            placeholder="每行一条，格式：标题|描述"
          />
        </el-form-item>
      </el-form>

      <div class="hsocial-form__actions">
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.hsocial {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.hsocial-hero.nf-page-hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hsocial-hero__badge {
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

.hsocial-hero__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary);
  line-height: 1.3;
}

.hsocial-hero__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary);
  line-height: 1.5;
}

.hsocial-form.nf-table-card {
  margin-bottom: 0;
}

.hsocial-form__section-head {
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--nf-border, #e8eaed);
}

.hsocial-form__grid {
  max-width: 640px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-input),
  :deep(.el-textarea) {
    max-width: 420px;
  }
}

.hsocial-form__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--nf-border, #e8eaed);
}
</style>
