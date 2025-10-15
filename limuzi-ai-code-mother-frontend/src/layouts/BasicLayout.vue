<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader v-if="!$route.meta?.hideChrome" />
    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <a-layout-content class="main-content">
        <router-view />
      </a-layout-content>
      <!-- 底部版权信息 -->
      <GlobalFooter v-if="!$route.meta?.hideChrome" />
    </div>
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: transparent;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 64px; /* 与导航栏高度相同 */
}

.main-content {
  width: 100%;
  padding: 0;
  background: transparent;
  flex: 1;
}
</style>
