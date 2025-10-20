<template>
  <div id="userForgotPasswordPage">
    <h2 class="title">Limuzi AI 应用生成 - 找回密码</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-steps :current="currentStep" size="small" class="steps">
      <a-step title="填写新密码" />
      <a-step title="验证邮箱" />
    </a-steps>

    <!-- 第一步：设置新密码 -->
    <div v-if="currentStep === 0">
      <a-form :model="formState" name="basic" autocomplete="off" @finish="goToNextStep">
        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认新密码' },
            { min: 8, message: '密码不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请确认新密码" />
        </a-form-item>
        <div class="tips">
          想起密码了？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">下一步</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 第二步：邮箱验证 -->
    <div v-if="currentStep === 1">
      <a-form :model="formState" name="email-verify" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="email" :rules="[
          { required: true, message: '请输入邮箱' },
          { type: 'email', message: '请输入有效的邮箱地址' }
        ]">
          <a-input v-model:value="formState.email" placeholder="请输入邮箱">
            <template #addonAfter>
              <a-button
                type="link"
                size="small"
                :loading="gettingCode"
                :disabled="codeButtonDisabled"
                @click="getVerificationCode"
              >
                {{ codeButtonText }}
              </a-button>
            </template>
          </a-input>
        </a-form-item>
        <a-form-item name="verificationCode" :rules="[{ required: true, message: '请输入验证码' }]">
          <a-input v-model:value="formState.verificationCode" placeholder="请输入验证码" />
        </a-form-item>
        <div class="action-buttons">
          <a-button style="margin-right: 10px" @click="currentStep = 0">上一步</a-button>
          <a-button type="primary" html-type="submit" :loading="submitting">提交</a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { findPassword, getCodeForFindPassword } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive, ref } from 'vue'

const router = useRouter()
const currentStep = ref(0)
const submitting = ref(false)
const codeButtonDisabled = ref(false)
const codeButtonText = ref('获取验证码')
const gettingCode = ref<boolean>(false);
let countdownTimer: number | null = null

interface FormState {
  userPassword: string
  checkPassword: string
  email: string
  verificationCode: string
}

const formState = reactive<FormState>({
  userPassword: '',
  checkPassword: '',
  email: '',
  verificationCode: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 进入下一步
 */
const goToNextStep = () => {
  currentStep.value = 1
}

/**
 * 获取验证码
 */
const getVerificationCode = async () => {
  if (!formState.email) {
    message.error('请先输入邮箱')
    return
  }

  try {
    gettingCode.value = true;
    const res = await getCodeForFindPassword({ email: formState.email })
    if (res.data.code === 0) {
      message.success('验证码已发送到您的邮箱')
      startCountdown()
    } else {
      message.error('验证码发送失败：' + res.data.message)
    }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (error) {
    message.error('验证码发送失败，请稍后重试')
  } finally{
    gettingCode.value = false;
  }
}

/**
 * 开始倒计时
 */
const startCountdown = () => {
  let countdown = 300
  codeButtonDisabled.value = true
  codeButtonText.value = `${countdown}秒后重试`

  countdownTimer = window.setInterval(() => {
    countdown--
    codeButtonText.value = `${countdown}秒后重试`

    if (countdown <= 0) {
      clearInterval(countdownTimer!)
      codeButtonDisabled.value = false
      codeButtonText.value = '获取验证码'
    }
  }, 1000)
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formState.email || !formState.verificationCode) {
    message.error('请填写邮箱和验证码')
    return
  }

  submitting.value = true
  try {
    const res = await findPassword({
      email: formState.email,
      code: formState.verificationCode,
      password: formState.userPassword
    })

    if (res.data.code === 0) {
      message.success('密码重置成功')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('密码重置失败，' + res.data.message)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userForgotPasswordPage {
  background: white;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

.steps {
  margin-bottom: 24px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
