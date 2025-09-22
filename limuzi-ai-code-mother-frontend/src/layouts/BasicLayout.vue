<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="layout-header">
      <GlobalHeader />
    </a-layout-header>

    <!-- 主要内容区域 -->
    <a-layout-content class="layout-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>

    <!-- 底部版权信息 -->
    <a-layout-footer class="layout-footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.layout-content {
  flex: 1;
  margin-top: 64px;
  /* 导航栏高度 */
  margin-bottom: 40px;
  /* 底部高度 */
  background: #f5f5f5;
}

.content-wrapper {
  min-height: calc(100vh - 104px);
  /* 总高度 - 导航栏 - 底部 */
  padding: 24px;
}

.layout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #001529;
  color: #fff;
  text-align: center;
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 16px;
  }

  .layout-content {
    margin-top: 56px;
    /* 移动端导航栏高度 */
    margin-bottom: 35px;
    /* 移动端底部高度 */
  }

  .content-wrapper {
    min-height: calc(100vh - 91px);
  }
}
</style>
