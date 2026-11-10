<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: '' },
})

const modules = import.meta.glob('/src/icons/svg/*.svg', { eager: true, import: 'default' })

const iconUrl = computed(() => {
  if (!props.name) {
    return ''
  }
  return modules[`/src/icons/svg/icon-${props.name}.svg`] || ''
})
</script>

<template>
  <span
    v-if="iconUrl"
    class="icon-svg"
    :style="{
      maskImage: `url(${iconUrl})`,
      WebkitMaskImage: `url(${iconUrl})`,
    }"
  />
  <span v-else class="icon-svg icon-svg--empty" />
</template>
