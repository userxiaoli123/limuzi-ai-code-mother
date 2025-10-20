<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <!-- <GlobalHeader v-if="!$route.meta?.hideChrome" /> -->
     <GlobalHeader v-if="showChrome" />
    <!-- 主要内容区域 -->
    <div class="content-wrapper" :class="{ 'with-header': showChrome }">
      <a-layout-content class="main-content">
        <router-view />
      </a-layout-content>
      <GlobalFooter v-if="showChrome" />
    </div>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
const route = useRoute()

// 只要任何一个匹配到的记录标了 hideChrome，就隐藏
const leaf = computed(() => route.matched[route.matched.length - 1])
const showChrome = computed(() => !leaf.value?.meta?.hideChrome)
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
}

/* 只有显示 Header 时才预留头部高度 */
.content-wrapper.with-header {
  margin-top: 64px; /* 与导航栏高度相同 */
}

.main-content {
  width: 100%;
  padding: 0;
  background: transparent;
  flex: 1;
}
</style>

