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
      <a-form-item label="头像">
        <div class="avatar-upload-container">
          <a-upload :file-list="fileList" :before-upload="beforeUpload" :custom-request="customUpload"
            list-type="picture-card" :show-upload-list="false" accept="image/*">
            <div class="avatar-upload-area">
              <img v-if="formState.userAvatar" :src="formState.userAvatar" alt="头像预览" class="avatar-preview" />
              <div v-else class="avatar-placeholder">
                <plus-outlined />
                <div class="upload-text">上传头像</div>
              </div>
            </div>
          </a-upload>
          <div class="avatar-actions">
            <a-button @click="() => setVisible(true)" :disabled="!formState.userAvatar">头像预览</a-button>
          </div>
        </div>
        <a-image :width="200" :style="{ display: 'none' }" :preview="{
          visible,
          onVisibleChange: setVisible,
        }" :src="formState.userAvatar" />
      </a-form-item>
      <a-form-item label="个人简介" name="userProfile">
        <a-textarea v-model:value="formState.userProfile" :rows="3" placeholder="一句话介绍自己" allow-clear />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { LoginOutlined, LogoutOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { userLogout, updateUser, updateAvatar } from '@/api/userController'
import type { FormInstance, UploadFile } from 'ant-design-vue'

const visible = ref<boolean>(false);
const setVisible = (value:any): void => {
  visible.value = value;
};


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
    key: '/admin/chatManage',
    label: '对话管理',
    title: '对话管理'
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
const fileList = ref<UploadFile[]>([])

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

// 图片上传前验证
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义上传
const customUpload = async (options: any) => {
  const { file } = options

  try {
    // 使用 userController.ts 中的 updateAvatar 函数，但覆盖 headers 设置
    const res = await updateAvatar({},file, {
      headers: {
        // 明确删除 Content-Type，让浏览器自动设置为 multipart/form-data
        'Content-Type': undefined
      }
    })

    if (res.data.code === 0) {
      formState.value.userAvatar = res.data.data
      // 更新登录用户信息，使头像立即在界面上显示
      await loginUserStore.fetchLoginUser()
      message.success('头像上传成功')
    } else {
      message.error('头像上传失败: ' + res.data.message)
    }
  } catch (error) {
    message.error('头像上传失败')
  }
}

// 移除头像
const removeAvatar = () => {
  formState.value.userAvatar = ''
  message.success('头像已移除')
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
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

/* 头像上传样式 */
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-upload-area {
  width: 104px;
  height: 104px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.avatar-upload-area:hover {
  border-color: #1890ff;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  font-size: 14px;
}

.avatar-placeholder .anticon {
  font-size: 24px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 12px;
}

.avatar-actions {
  display: flex;
  gap: 8px;
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
