<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, HomeFilled, WarningFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => route.query.title || '页面不存在')
const menuUrl = computed(() => route.query.menuUrl || route.path)

function goDashboard() {
  router.push('/dashboard')
}
</script>

<template>
  <div class="nf-404">
    <header class="nf-page-hero nf-404__hero">
      <span class="nf-404__badge">404</span>
      <h1 class="nf-404__title">{{ pageTitle }}</h1>
      <p class="nf-404__desc">当前菜单已返回，但前端暂未实现对应页面。</p>
    </header>

    <div class="nf-table-card nf-404__card">
      <div class="nf-404__visual">
        <span class="nf-404__code" aria-hidden="true">404</span>
        <span class="nf-404__icon">
          <el-icon :size="40"><WarningFilled /></el-icon>
        </span>
      </div>

      <div class="nf-404__meta">
        <span class="nf-404__meta-label">菜单地址</span>
        <code class="nf-404__meta-value">{{ menuUrl }}</code>
      </div>

      <div class="nf-404__actions">
        <el-button type="primary" @click="goDashboard">
          <el-icon><HomeFilled /></el-icon>
          返回工作台
        </el-button>
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.nf-404 {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 8px;
  overflow-x: hidden;
}

.nf-404__hero {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nf-404__badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--nf-text-primary, #1a2433);
  background: var(--nf-brand-lite, #fefdfc);
  border: 1px solid var(--nf-brand, #f8efe2);
  border-radius: 6px;
}

.nf-404__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--nf-text-primary, #1a2433);
  line-height: 1.3;
}

.nf-404__desc {
  margin: 0;
  font-size: 13px;
  color: var(--nf-text-secondary, #909399);
  line-height: 1.5;
}

.nf-404__card {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 32px 24px 28px;
  text-align: center;
}

.nf-404__visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 320px;
  min-height: 120px;
}

.nf-404__code {
  font-size: 96px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.04em;
  color: transparent;
  background: linear-gradient(
    160deg,
    var(--nf-brand-deep, #eed8b9) 0%,
    var(--nf-brand, #f8efe2) 55%,
    rgba(238, 216, 185, 0.35) 100%
  );
  -webkit-background-clip: text;
  background-clip: text;
  user-select: none;
}

.nf-404__icon {
  position: absolute;
  right: 0;
  bottom: 8px;
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  color: var(--nf-brand-deep, #eed8b9);
  background: var(--nf-brand-lite, #fefdfc);
  border: 1px solid var(--nf-border, #e8eaed);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.nf-404__meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 480px;
  padding: 14px 16px;
  border-radius: 8px;
  background: var(--nf-sidebar-bg-alt, #f7f8fa);
  border: 1px solid var(--nf-border, #e8eaed);
}

.nf-404__meta-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--nf-text-secondary, #909399);
}

.nf-404__meta-value {
  display: block;
  max-width: 100%;
  padding: 6px 10px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: var(--nf-text-primary, #1a2433);
  word-break: break-all;
  background: #fff;
  border: 1px solid var(--nf-border, #e8eaed);
  border-radius: 6px;
}

.nf-404__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.nf-404__actions .el-button .el-icon {
  margin-right: 4px;
}
</style>
