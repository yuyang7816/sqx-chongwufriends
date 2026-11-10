<script setup>
import { computed, ref } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import { toPublicFileUrl } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

const props = defineProps({
  modelValue: { type: String, default: '' },
  accept: { type: String, default: 'image/*' },
  /** 空态文案；传空字符串则只显示 + */
  emptyText: { type: String, default: '' },
  tip: { type: String, default: '' },
  size: { type: Number, default: 100 },
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const previewUrl = computed(() => toPublicFileUrl(props.modelValue))
const boxStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
}))

function resolveUploadUrl(data) {
  return toPublicFileUrl(data)
}

async function handleUpload(option) {
  loading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = resolveUploadUrl(data)
    if (!url) throw new Error('未获取到图片地址')
    emit('update:modelValue', url)
    showSuccess('上传成功')
    option.onSuccess?.(data)
  } catch (error) {
    showError(error?.msg || error?.message || '图片上传失败')
    option.onError?.(error)
  } finally {
    loading.value = false
  }
}

function clearImage() {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="nf-img-upload nf-img-upload--single">
    <div class="nf-img-upload__row">
      <!-- 已上传：仅展示缩略图（单图不再出现 +） -->
      <div v-if="previewUrl" class="nf-img-upload__item" :style="boxStyle">
        <el-image
          :src="previewUrl"
          :preview-src-list="[previewUrl]"
          class="nf-img-upload__preview"
          fit="cover"
          preview-teleported
        />
        <div class="nf-img-upload__mask">
          <el-upload
            class="nf-img-upload__mask-upload"
            :show-file-list="false"
            :http-request="handleUpload"
            :accept="accept"
            :disabled="loading"
          >
            <span class="nf-img-upload__mask-btn">更换</span>
          </el-upload>
          <button type="button" class="nf-img-upload__mask-btn is-danger" @click.stop="clearImage">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 空态：虚线 + -->
      <el-upload
        v-else
        class="nf-img-upload__adder-wrap"
        :show-file-list="false"
        :http-request="handleUpload"
        :accept="accept"
        :disabled="loading"
      >
        <div class="nf-img-upload__adder" :class="{ 'is-loading': loading }" :style="boxStyle">
          <el-icon v-if="!loading" class="nf-img-upload__plus"><Plus /></el-icon>
          <span v-if="loading || emptyText" class="nf-img-upload__adder-text">
            {{ loading ? '上传中' : emptyText }}
          </span>
        </div>
      </el-upload>

      <div v-if="$slots.extra" class="nf-img-upload__extra">
        <slot name="extra" />
      </div>
    </div>
    <p v-if="tip" class="nf-img-upload__tip">{{ tip }}</p>
  </div>
</template>

<style scoped>
.nf-img-upload {
  width: 100%;
}

.nf-img-upload__row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.nf-img-upload__item {
  position: relative;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  background: #fafafa;
  overflow: hidden;
  flex-shrink: 0;
}

.nf-img-upload__preview {
  width: 100%;
  height: 100%;
  display: block;
}

.nf-img-upload__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.nf-img-upload__item:hover .nf-img-upload__mask {
  opacity: 1;
}

.nf-img-upload__mask-upload {
  display: inline-flex;
}

.nf-img-upload__mask-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  border: 0;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.92);
  color: #303133;
  font-size: 12px;
  cursor: pointer;
  line-height: 1;
}

.nf-img-upload__mask-btn.is-danger {
  color: #f56c6c;
  padding: 0;
  width: 28px;
}

.nf-img-upload__adder-wrap {
  display: inline-flex;
}

.nf-img-upload__adder {
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
  background: #fafafa;
  color: #8c8c8c;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
  user-select: none;
  box-sizing: border-box;
}

.nf-img-upload__adder:hover,
.nf-img-upload__adder.is-loading {
  border-color: var(--nf-menu-active-soft, #bc926d);
  color: var(--nf-menu-active-soft, #bc926d);
  background: #fffaf4;
}

.nf-img-upload__plus {
  font-size: 22px;
}

.nf-img-upload__adder-text {
  font-size: 12px;
  line-height: 1.2;
}

.nf-img-upload__extra {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.nf-img-upload__tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
