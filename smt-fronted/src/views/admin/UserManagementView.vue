<script setup>
import { ref, onMounted, h } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getUsers, createUser, updateUser, deleteUser } from '../../api/user'
import { useMessage, NButton, NPopconfirm } from 'naive-ui'

const authStore = useAuthStore()
const message = useMessage()

const users = ref([])
const loading = ref(false)

const newUserOpen = ref(false)
const newUserForm = ref({ username: '', password: '', roleCode: 'USER' })
const editUserOpen = ref(false)
const editUserForm = ref({ id: null, username: '', roleCode: 'USER', password: '' })

const roleOptions = [
  { code: 'ADMIN', label: '管理员' },
  { code: 'USER', label: '维修端' },
  { code: 'PRODUCTION', label: '生产端' }
]

const columns = [
  { title: '用户名', key: 'username' },
  { 
    title: '角色', 
    key: 'roleCode',
    render: (row) => roleOptions.find(r => r.code === row.roleCode)?.label || row.roleCode 
  },
  {
    title: '操作',
    key: 'actions',
    render(row) {
      return h(
        'div',
        { style: 'display: flex; gap: 8px;' },
        [
          h(NButton, { size: 'small', onClick: () => openEditUser(row) }, { default: () => '编辑' }),
          h(
            NPopconfirm,
            { onPositiveClick: () => handleDelete(row) },
            { 
              trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
              default: () => '确认删除该账号吗？'
            }
          )
        ]
      )
    }
  }
]

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res || []
  } catch (err) {
    message.error('加载用户失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await deleteUser(row.id)
    message.success('删除成功')
    loadUsers()
  } catch (err) {
    message.error(err.message || '删除失败')
  }
}

const openNewUser = () => {
  newUserForm.value = { username: '', password: '', roleCode: 'USER' }
  newUserOpen.value = true
}

const submitNewUser = async () => {
  try {
    await createUser(newUserForm.value)
    message.success('创建成功')
    newUserOpen.value = false
    loadUsers()
  } catch (err) {
    message.error(err.message || '创建失败')
  }
}

const openEditUser = (user) => {
  editUserForm.value = {
    id: user.id,
    username: user.username,
    roleCode: user.roleCode || 'USER',
    password: ''
  }
  editUserOpen.value = true
}

const submitEditUser = async () => {
  try {
    const payload = {
      username: editUserForm.value.username,
      roleCode: editUserForm.value.roleCode,
      password: editUserForm.value.password || null
    }
    await updateUser(editUserForm.value.id, payload)
    message.success('更新成功')
    editUserOpen.value = false
    loadUsers()
  } catch (err) {
    message.error(err.message || '更新失败')
  }
}

onMounted(loadUsers)
</script>

<template>
  <div>
    <n-page-header title="账户管理" subtitle="账号信息与权限查看" style="margin-bottom: 24px">
      <template #extra>
        <n-button type="primary" @click="openNewUser">新增账号</n-button>
      </template>
    </n-page-header>

    <n-card title="账号列表">
      <n-data-table
        :columns="columns"
        :data="users"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
      />
    </n-card>

    <!-- New User Modal -->
    <n-modal v-model:show="newUserOpen" preset="card" title="新增账号" style="width: 500px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="账号" required><n-input v-model:value="newUserForm.username" /></n-form-item>
        <n-form-item label="密码" required><n-input v-model:value="newUserForm.password" /></n-form-item>
        <n-form-item label="权限" required><n-select v-model:value="newUserForm.roleCode" :options="roleOptions" label-field="label" value-field="code" /></n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="newUserOpen = false">取消</n-button>
          <n-button type="primary" @click="submitNewUser">保存</n-button>
        </div>
      </template>
    </n-modal>

    <!-- Edit User Modal -->
    <n-modal v-model:show="editUserOpen" preset="card" title="编辑账号" style="width: 500px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="账号"><n-input v-model:value="editUserForm.username" /></n-form-item>
        <n-form-item label="新密码"><n-input v-model:value="editUserForm.password" placeholder="留空不修改" /></n-form-item>
        <n-form-item label="权限"><n-select v-model:value="editUserForm.roleCode" :options="roleOptions" label-field="label" value-field="code" /></n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="editUserOpen = false">取消</n-button>
          <n-button type="primary" @click="submitEditUser">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
