<template>
  <a-modal
    v-model:open="open"
    title="编辑资料"
    :confirm-loading="submitting"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form :model="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
      <a-form-item label="昵称">
        <a-input v-model:value="form.userName" placeholder="输入昵称" />
      </a-form-item>
      <a-form-item label="头像URL">
        <a-input v-model:value="form.userAvatar" placeholder="输入头像图片地址" />
      </a-form-item>
      <a-form-item label="简介">
        <a-textarea v-model:value="form.userProfile" :rows="3" placeholder="一句话介绍你自己" />
      </a-form-item>
    </a-form>
  </a-modal>
  
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'

interface Props {
  open: boolean
}

const props = defineProps<Props>()
const emit = defineEmits(['update:open'])

const loginUserStore = useLoginUserStore()

const open = ref(props.open)
watch(
  () => props.open,
  (v) => {
    open.value = v
    if (v) {
      resetForm()
    }
  }
)
watch(open, (v) => emit('update:open', v))

const submitting = ref(false)

const form = reactive<API.UserUpdateRequest>({
  id: loginUserStore.loginUser?.id,
  userName: loginUserStore.loginUser?.userName,
  userAvatar: loginUserStore.loginUser?.userAvatar,
  userProfile: loginUserStore.loginUser?.userProfile,
})

const resetForm = () => {
  form.id = loginUserStore.loginUser?.id
  form.userName = loginUserStore.loginUser?.userName
  form.userAvatar = loginUserStore.loginUser?.userAvatar
  form.userProfile = loginUserStore.loginUser?.userProfile
}

const handleOk = async () => {
  submitting.value = true
  try {
    const res = await updateUser({
      id: form.id,
      userName: form.userName,
      userAvatar: form.userAvatar,
      userProfile: form.userProfile,
    })
    if (res.data.code === 0) {
      message.success('更新成功')
      await loginUserStore.fetchLoginUser()
      open.value = false
    } else {
      message.error('更新失败，' + res.data.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  open.value = false
}
</script>

<style scoped>
</style>


