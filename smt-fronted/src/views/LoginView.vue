<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMessage } from 'naive-ui'

const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()

const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    message.warning('请输入账号和密码')
    return
  }
  
  loading.value = true
  try {
    await authStore.login(form.value)
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f5f5f5;">
    <n-card title="SMT生产维修系统" style="width: 400px;" size="huge">
      <n-form>
        <n-form-item label="账号">
          <n-input v-model:value="form.username" placeholder="请输入用户名" @keydown.enter="handleLogin" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" show-password-on="click" @keydown.enter="handleLogin" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="handleLogin">登录</n-button>
      </n-form>
    </n-card>
  </div>
</template>
