<template>
  <div class="user-filter-fields">
    <el-form-item class="user-filter-fields__item">
      <el-input
        :model-value="phone"
        placeholder="手机号"
        clearable
        class="user-filter-fields__input"
        @update:model-value="$emit('update:phone', $event)"
        @keyup.enter="$emit('search')"
      />
    </el-form-item>
    <el-form-item class="user-filter-fields__item">
      <el-input
        :model-value="username"
        placeholder="用户名/昵称"
        clearable
        class="user-filter-fields__input"
        @update:model-value="$emit('update:username', $event)"
        @keyup.enter="$emit('search')"
      />
    </el-form-item>
  </div>
</template>

<script setup>
defineProps({
  phone: { type: String, default: '' },
  username: { type: String, default: '' },
})

defineEmits(['update:phone', 'update:username', 'search'])
</script>

<style scoped>
.user-filter-fields {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.user-filter-fields__item {
  margin: 0 !important;
}

.user-filter-fields__input {
  width: 160px;
}
</style>
