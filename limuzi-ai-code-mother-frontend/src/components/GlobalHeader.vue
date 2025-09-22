<template>
  <div class="global-header">
    <div class="header-left">
      <!-- Logo 和网站标题 -->
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="Logo" class="logo"/>
        <span class="site-title">Limuzi AI</span>
      </div>

      <!-- 菜单项 -->
      <a-menu v-model:selectedKeys="selectedKeys" mode="horizontal" class="header-menu" :items="menuItems"
              @click="handleMenuClick"/>
    </div>

    <div class="header-right">
      <!-- 登录按钮（暂时替代用户头像和昵称） -->
      <a-button type="primary" @click="handleLogin">
        登录
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import type {MenuProps} from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

// 菜单配置
const menuItems = ref([
  {
    key: 'home',
    label: '首页',
    icon: 'HomeOutlined'
  },
  {
    key: 'about',
    label: '关于',
    icon: 'InfoCircleOutlined'
  },
  {
    key: 'projects',
    label: '项目',
    icon: 'ProjectOutlined'
  },
  {
    key: 'contact',
    label: '联系',
    icon: 'ContactsOutlined'
  }
])

// 当前选中的菜单项
const selectedKeys = computed(() => {
  const path = route.path
  if (path === '/') return ['home']
  if (path === '/about') return ['about']
  if (path === '/projects') return ['projects']
  if (path === '/contact') return ['contact']
  return []
})

// 菜单点击处理
const handleMenuClick: MenuProps['onClick'] = ({key}) => {
  switch (key) {
    case 'home':
      router.push('/')
      break
    case 'about':
      router.push('/about')
      break
    case 'projects':
      router.push('/projects')
      break
    case 'contact':
      router.push('/contact')
      break
  }
}

// 登录处理
const handleLogin = () => {
  console.log('登录按钮被点击')
  // TODO: 实现登录逻辑
}
</script>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.site-title {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}

.header-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent;
}

.header-menu :deep(.ant-menu-item) {
  height: 64px;
  line-height: 64px;
  border-bottom: none !important;
  position: relative;
}

.header-menu :deep(.ant-menu-item::after) {
  display: none !important;
}

.header-menu :deep(.ant-menu-item-selected) {
  border-bottom: 2px solid #1890ff !important;
  color: #1890ff;
}

.header-menu :deep(.ant-menu-item-selected::after) {
  display: none !important;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
    height: 56px;
  }

  .logo-section {
    margin-right: 20px;
  }

  .site-title {
    font-size: 18px;
  }

  .logo {
    width: 28px;
    height: 28px;
    margin-right: 8px;
  }

  .header-menu {
    display: none;
    /* 移动端隐藏菜单，可以后续添加汉堡菜单 */
  }
}

@media (max-width: 480px) {
  .global-header {
    padding: 0 12px;
  }

  .site-title {
    font-size: 16px;
  }
}
</style>
