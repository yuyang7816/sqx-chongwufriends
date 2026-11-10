<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    align-center
    destroy-on-close
    class="nf-pro-dialog"
    :close-on-click-modal="false"
    @close="$emit('update:modelValue', false)"
  >
    <slot />
    <template #footer>
      <div class="nf-pro-dialog__footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" class="nf-pro-dialog__confirm" :loading="loading" @click="$emit('confirm')">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '720px' },
  loading: { type: Boolean, default: false },
})

defineEmits(['update:modelValue', 'confirm'])
</script>
