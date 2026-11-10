<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  placeholder: { type: String, default: '输入后回车添加' },
  max: { type: Number, default: 0 },
})

const emit = defineEmits(['update:modelValue'])

const inputValue = ref('')

watch(
  () => props.modelValue,
  () => {
    inputValue.value = ''
  },
)

function addTag() {
  const value = inputValue.value.trim()
  if (!value) return
  if (props.max > 0 && props.modelValue.length >= props.max) return
  if (props.modelValue.includes(value)) {
    inputValue.value = ''
    return
  }
  emit('update:modelValue', [...props.modelValue, value])
  inputValue.value = ''
}

function removeTag(index) {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<template>
  <div class="tag-input">
    <el-tag
      v-for="(tag, index) in modelValue"
      :key="`${tag}-${index}`"
      closable
      class="tag-item"
      @close="removeTag(index)"
    >
      {{ tag }}
    </el-tag>
    <el-input
      v-model="inputValue"
      size="small"
      class="tag-input-field"
      :placeholder="placeholder"
      @keyup.enter="addTag"
      @blur="addTag"
    />
  </div>
</template>

<style scoped>
.tag-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  width: 100%;
  padding: 6px 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  min-height: 36px;
}
.tag-item {
  margin: 0;
}
.tag-input-field {
  flex: 1;
  min-width: 120px;
}
.tag-input-field :deep(.el-input__wrapper) {
  box-shadow: none;
  padding-left: 0;
}
</style>
