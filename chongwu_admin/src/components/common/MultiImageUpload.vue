<script setup>
import { computed, ref } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { uploadMerchantImageApi } from '@/api/modules/merchant-center'
import { toPublicFileUrl } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

const props = defineProps({
  /** string[] 图片地址列表 */
  modelValue: { type: Array, default: () => [] },
  accept: { type: String, default: 'image/*' },
  tip: { type: String, default: '' },
  limit: { type: Number, default: 9 },
  size: { type: Number, default: 100 },
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)

const list = computed(() =>
  (Array.isArray(props.modelValue) ? props.modelValue : [])
    .map((url) => toPublicFileUrl(url))
    .filter(Boolean)
)

const canAdd = computed(() => list.value.length < props.limit)
const boxStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
}))
const previewList = computed(() => list.value)

function resolveUploadUrl(data) {
  return toPublicFileUrl(data)
}

async function handleUpload(option) {
  if (!canAdd.value) {
    showError(`最多上传 ${props.limit} 张`)
    return
  }
  loading.value = true
  try {
    const data = await uploadMerchantImageApi(option.file)
    const url = resolveUploadUrl(data)
    if (!url) throw new Error('未获取到图片地址')
    emit('update:modelValue', [...(props.modelValue || []), url])
    showSuccess('上传成功')
    option.onSuccess?.(data)
  } catch (error) {
    showError(error?.msg || error?.message || '图片上传失败')
    option.onError?.(error)
  } finally {
    loading.value = false
  }
}

function removeAt(index) {
  const next = [...(props.modelValue || [])]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<template>
  <div class="nf-img-upload nf-img-upload--multi">
    <div class="nf-img-upload__row">
      <div
        v-for="(url, index) in list"
        :key="`${url}-${index}`"
        class="nf-img-upload__item"
        :style="boxStyle"
      >
        <el-image
          :src="url"
          :preview-src-list="previewList"
          :initial-index="index"
          class="nf-img-upload__preview"
          fit="cover"
          preview-teleported
        />
        <div class="nf-img-upload__mask">
          <button type="button" class="nf-img-upload__mask-btn is-danger" @click.stop="removeAt(index)">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>

      <!-- 多图：始终保留虚线 +（未达上限时） -->
      <el-upload
        v-if="canAdd"
        class="nf-img-upload__adder-wrap"
        :show-file-list="false"
        :http-request="handleUpload"
        :accept="accept"
        :disabled="loading"
        multiple
      >
        <div class="nf-img-upload__adder" :class="{ 'is-loading': loading }" :style="boxStyle">
          <el-icon v-if="!loading" class="nf-img-upload__plus"><Plus /></el-icon>
          <span v-else class="nf-img-upload__adder-text">上传中</span>
        </div>
      </el-upload>
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
  gap: 12px;
  align-items: flex-start;
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
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.nf-img-upload__item:hover .nf-img-upload__mask {
  opacity: 1;
}

.nf-img-upload__mask-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.92);
  color: #f56c6c;
  cursor: pointer;
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
}

.nf-img-upload__tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
