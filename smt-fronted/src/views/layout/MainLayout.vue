<script setup>
import { ref, computed, h } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useMessage, NIcon } from 'naive-ui'
import { updatePassword } from '../../api/auth'
import {
  BuildOutlined,
  ComputerOutlined,
  BarChartOutlined,
  AssessmentOutlined,
  ManageAccountsOutlined,
  SettingsOutlined
} from '@vicons/material'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()

const sidebarCollapsed = ref(false)
const passwordOpen = ref(false)
const passwordForm = ref({ newPassword: '' })

// 系统标题
const systemTitle = computed(() => {
  if (authStore.isAdmin) return 'SMT生产维修系统 - 管理员端'
  if (authStore.isProduction) return 'SMT生产维修系统 - 生产端'
  return 'SMT生产维修系统 - 维修端'
})

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 构建菜单
const menuOptions = computed(() => {
  const options = []
  
  // 1. 如果是管理员，只展示管理菜单 (优先级最高)
  if (authStore.isAdmin) {
    options.push({ 
      label: '账户管理', 
      key: 'UserAdmin',
      icon: renderIcon(ManageAccountsOutlined)
    })
    options.push({ 
      label: '字段管理', 
      key: 'SystemFields',
      icon: renderIcon(SettingsOutlined)
    })
    return options
  }

  // 2. 如果是生产端，只展示报表 (优先级次之)
  if (authStore.isProduction) {
    options.push({ 
      label: '每日报表', 
      key: 'Report',
      icon: renderIcon(AssessmentOutlined)
    })
    return options
  }

  // 3. 默认/维修端，展示维修管理
  if (authStore.hasPermission('repair:read') || authStore.hasPermission('repair:write')) {
    options.push({
      label: '维修管理',
      key: 'maintenance',
      icon: renderIcon(BuildOutlined),
      children: [
        { 
          label: '维修工作台', 
          key: 'Workbench',
          icon: renderIcon(ComputerOutlined)
        },
        { 
          label: '数据统计', 
          key: 'Analytics',
          icon: renderIcon(BarChartOutlined)
        }
      ]
    })
  }

  return options
})

// 当前选中的菜单 Key，需要跟 Route Name 对应
const activeKey = computed(() => route.name)

const handleMenuUpdate = (key) => {
  router.push({ name: key })
}

const handleLogout = async () => {
  await authStore.logout()
  message.info('已退出登录')
  router.push('/login')
}

const openPassword = () => {
  passwordForm.value.newPassword = ''
  passwordOpen.value = true
}

const submitPassword = async () => {
  if (!passwordForm.value.newPassword) {
    message.warning('请输入新密码')
    return
  }
  try {
    await updatePassword(passwordForm.value)
    message.success('密码已更新')
    passwordOpen.value = false
  } catch (error) {
    message.error(error.message || '密码修改失败')
  }
}
</script>

<template>
  <n-layout style="height: 100vh;">
    <n-layout-header bordered style="height: 64px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
      <div style="font-size: 18px; font-weight: bold; display: flex; align-items: center; gap: 8px;">
        <span style="background: #18a058; color: white; padding: 2px 8px; border-radius: 4px; font-size: 14px;">SMT</span>
        <span>{{ systemTitle }}</span>
      </div>
      <div style="display: flex; gap: 12px; align-items: center;">
         <span>{{ authStore.currentUser.name }}</span>
         <n-button size="small" @click="openPassword">修改密码</n-button>
         <n-button size="small" type="error" ghost @click="handleLogout">退出</n-button>
      </div>
    </n-layout-header>

    <n-layout has-sider position="absolute" style="top: 64px; bottom: 0;">
      <n-layout-sider
        collapse-mode="width"
        :collapsed-width="64"
        :width="240"
        show-trigger
        bordered
        :collapsed="sidebarCollapsed"
        @collapse="sidebarCollapsed = true"
        @expand="sidebarCollapsed = false"
      >
        <n-menu
          :collapsed="sidebarCollapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
          :options="menuOptions"
          :value="activeKey"
          @update:value="handleMenuUpdate"
        />
      </n-layout-sider>

      <n-layout-content content-style="padding: 24px; background-color: #f0f2f5;">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>

  <!-- 修改密码模态框 -->
  <n-modal v-model:show="passwordOpen" preset="card" title="修改密码" style="width: 400px">
    <n-form>
      <n-form-item label="新密码" required>
        <n-input v-model:value="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password-on="click" />
      </n-form-item>
    </n-form>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 12px">
        <n-button @click="passwordOpen = false">取消</n-button>
        <n-button type="primary" @click="submitPassword">保存</n-button>
      </div>
    </template>
  </n-modal>
</template>
