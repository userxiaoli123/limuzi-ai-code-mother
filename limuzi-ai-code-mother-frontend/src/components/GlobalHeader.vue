<template>
  <div class="global-header">
    <div class="header-left">
      <!-- Logo 和网站标题 -->
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <span class="site-title">Limuzi AI</span>
      </div>

      <!-- 菜单项 -->
      <a-menu v-model:selectedKeys="selectedKeys" mode="horizontal" class="header-menu" :items="menuItems"
        @click="handleMenuClick" />
    </div>

    <div class="header-right">
      <!-- 登录按钮（暂时替代用户头像和昵称） -->
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item disabled>
                <a-space>
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                  <div>
                    <div>{{ loginUserStore.loginUser.userName ?? '无名' }}</div>
                    <div
                      style="font-size: 12px; color: #999; max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                      {{ loginUserStore.loginUser.userProfile || '这个人很懒，什么都没有写～' }}
                    </div>
                  </div>
                </a-space>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item @click="openEditModal">
                编辑资料
              </a-menu-item>
              <a-menu-item @click="doLoginOut">
                <LogoutOutlined></LogoutOutlined>
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <a-button v-else type="primary" @click="handleLogin">
        登录
      </a-button>
    </div>
  </div>

  <!-- 编辑资料弹窗 -->
  <a-modal v-model:open="editVisible" title="编辑个人资料" :confirm-loading="saving" @ok="handleEditOk"
    @cancel="handleEditCancel" ok-text="保存" cancel-text="取消">
    <a-form ref="formRef" :model="formState" :label-col="{ style: { width: '88px' } }" :wrapper-col="{ span: 18 }">
      <a-form-item label="昵称" name="userName" :rules="[{ required: true, message: '请输入昵称' }]">
        <a-input v-model:value="formState.userName" placeholder="请输入昵称" allow-clear />
      </a-form-item>
      <a-form-item label="头像地址" name="userAvatar" :rules="[{ type: 'url', message: '请输入合法的图片地址', trigger: 'blur' }]">
        <a-input v-model:value="formState.userAvatar" placeholder="请输入头像图片 URL" allow-clear />
      </a-form-item>
      <a-form-item label="个人简介" name="userProfile">
        <a-textarea v-model:value="formState.userProfile" :rows="3" placeholder="一句话介绍自己" allow-clear />
      </a-form-item>
      <a-form-item :wrapper-col="{ offset: 0 }">
        <a-space>
          <a-avatar :src="formState.userAvatar || loginUserStore.loginUser.userAvatar" />
          <span style="color:#999;">头像预览</span>
        </a-space>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { LoginOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout, updateUser } from '@/api/userController'
import type { FormInstance } from 'ant-design-vue'

// 用户登录信息
const loginUserStore = useLoginUserStore()

const router = useRouter()
const route = useRoute()

// 菜单配置
const originItems = [
  {
    key: '/',
    label: '首页',
    title: '首页'
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理'
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理'
  },
  {
    key: '/about',
    label: '关于',
    title: '关于'
  }
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 当前选中的菜单项（通用：依据路由自动匹配）
const selectedKeys = computed(() => {
  const path = route.path
  const items = (menuItems.value || []) as any[]
  const exact = items.find((i) => i?.key === path)
  if (exact?.key) return [exact.key as string]
  const prefix = items
    .filter((i) => typeof i?.key === 'string' && i.key !== '/')
    .find((i) => path.startsWith(i.key))
  if (prefix?.key) return [prefix.key as string]
  return path === '/' ? ['/'] : []
})

// 菜单点击处理（通用：key 即为路由路径）
const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  if (typeof key === 'string') {
    router.push(key)
  }
}

// 登录处理
const handleLogin = () => {
  router.push('/user/login')
  console.log('登录按钮被点击')
  // TODO: 实现登录逻辑
}

const doLoginOut = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录'
    })
    message.success("退出登录成功")
    await router.push("/user/login")
  }
}

// 编辑资料
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const formState = ref<Partial<API.UserUpdateRequest>>({})

const openEditModal = () => {
  formState.value = {
    id: loginUserStore.loginUser.id,
    userName: loginUserStore.loginUser.userName,
    userAvatar: loginUserStore.loginUser.userAvatar,
    userProfile: loginUserStore.loginUser.userProfile,
  }
  editVisible.value = true
}

const handleEditCancel = () => {
  editVisible.value = false
}

const handleEditOk = async () => {
  try {
    saving.value = true
    // 校验
    await formRef.value?.validate()
    const res = await updateUser(formState.value as API.UserUpdateRequest)
    if (res.data.code === 0) {
      message.success('资料已更新')
      // 刷新登录用户信息
      await loginUserStore.fetchLoginUser()
      editVisible.value = false
    }
  } finally {
    saving.value = false
  }
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  transition: color 0.25s ease;
  overflow: hidden;
}

.header-menu :deep(.ant-menu-item::after) {
  display: none !important;
}

/* 动画下划线（默认收起） */
.header-menu :deep(.ant-menu-item::before) {
  content: '';
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 0;
  height: 2px;
  background-color: #1677ff;
  transform: scaleX(0);
  transform-origin: center;
  transition: transform 0.25s ease;
}

.header-menu :deep(.ant-menu-item:hover) {
  color: #1677ff;
}

.header-menu :deep(.ant-menu-item-selected) {
  color: #1677ff !important;
  background: transparent !important;
}

/* 悬停或选中时展开下划线 */
.header-menu :deep(.ant-menu-item:hover::before),
.header-menu :deep(.ant-menu-item-selected::before) {
  transform: scaleX(1);
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
    /* 移动端保留菜单，允许横向滚动，避免被隐藏 */
    display: block;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    /* 隐藏滚动条 */
    scrollbar-width: none;
    /* Firefox */
  }

  .header-menu::-webkit-scrollbar {
    display: none;
    /* Chrome/Safari */
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
