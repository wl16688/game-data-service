<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-blue-900">
    <div class="max-w-md w-full bg-white rounded-xl shadow-2xl overflow-hidden p-8 transform transition-all duration-500 hover:scale-[1.01]">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">后台管理系统</h1>
        <p class="text-gray-500 mt-2 text-sm">请输入管理员账号登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        class="space-y-6"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号 (如 admin)"
            :prefix-icon="User"
            class="h-12"
          />
        </el-form-item>

        <el-button
          type="primary"
          :loading="loading"
          class="w-full h-12 text-lg font-semibold tracking-wide rounded-lg shadow-md hover:shadow-lg transition-all"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
      
      <div class="mt-6 text-center text-xs text-gray-400">
        <p>测试账号: admin</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loginForm = reactive({
  username: 'admin'
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ]
}

const loading = ref(false)

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await login(loginForm.username)
        userStore.setToken(res.token, res.role)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error: any) {
        // Error handled in request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
