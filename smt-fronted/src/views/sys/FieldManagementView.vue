<script setup>
import { ref, computed, onMounted, h, watch } from 'vue'
import { useMasterDataStore } from '../../stores/masterData'
import { NTag, NButton, useMessage, NSelect, NSpace, NInput, NIcon, NPopconfirm, NModal, NForm, NFormItem, NInputNumber } from 'naive-ui'
import {
  DomainOutlined,
  StoreOutlined,
  LinearScaleOutlined,
  DeviceHubOutlined,
  PrintOutlined,
  CategoryOutlined,
  ClassOutlined,
  GroupsOutlined,
  PersonOutlined,
  PublicOutlined,
  PrecisionManufacturingOutlined,
  ErrorOutlineOutlined,
  BadgeOutlined
} from '@vicons/material'

const masterStore = useMasterDataStore()
const message = useMessage()
const activeModuleId = ref('factory')

// 筛选状态
const filters = ref({
  factoryId: null,
  workshopId: null,
  modelId: null,
  abnormalCategoryId: null,
  teamId: null,
  keyword: ''
})

// 表单状态
const modalVisible = ref(false)
const modalType = ref('create') // 'create' | 'edit'
const formRef = ref(null)
const formData = ref({})
const submitting = ref(false)

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 监听模块切换，重置筛选和表单
watch(activeModuleId, () => {
  filters.value = {
    factoryId: null,
    workshopId: null,
    modelId: null,
    abnormalCategoryId: null,
    teamId: null,
    keyword: ''
  }
})

// 级联数据计算
const availableWorkshops = computed(() => {
  if (!filters.value.factoryId) return masterStore.workshops
  return masterStore.workshops.filter(w => w.factoryId === filters.value.factoryId)
})

const formWorkshops = computed(() => {
  if (!formData.value.factoryId) return masterStore.workshops
  return masterStore.workshops.filter(w => w.factoryId === formData.value.factoryId)
})

// 左侧菜单配置（分组）
const menuOptions = [
  {
    label: '区域架构',
    key: 'group-location',
    type: 'group',
    children: [
      { label: '厂区维护', key: 'factory' },
      { label: '车间维护', key: 'workshop' },
      { label: '线别维护', key: 'line' }
    ]
  },
  {
    label: '设备管理',
    key: 'group-equipment',
    type: 'group',
    children: [
      { label: '机型维护', key: 'model' },
      { label: '机台维护', key: 'machine' }
    ]
  },
  {
    label: '异常体系',
    key: 'group-abnormal',
    type: 'group',
    children: [
      { label: '异常类别', key: 'abnormal-category' },
      { label: '异常分类', key: 'abnormal-type' }
    ]
  },
  {
    label: '人员组织',
    key: 'group-org',
    type: 'group',
    children: [
      { label: '组别维护', key: 'team' },
      { label: '人员维护', key: 'people' }
    ]
  }
]

// 模块详细配置
const moduleConfig = computed(() => ({
  factory: {
    label: '厂区维护',
    data: masterStore.factories,
    columns: [
      { title: '厂区名称', key: 'name' },
      { title: '排序', key: 'sortOrder', width: 80 },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  workshop: {
    label: '车间维护',
    filterType: 'workshop', // 标识需要哪些筛选器
    data: masterStore.workshops,
    columns: [
      { title: '车间名称', key: 'name' },
      { title: '所属厂区', key: 'factoryId', render: (row) => masterStore.factories.find(f => f.id === row.factoryId)?.name || row.factoryId },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  line: {
    label: '线别维护',
    filterType: 'line',
    data: masterStore.lines,
    columns: [
      { title: '线别名称', key: 'name' },
      { title: '所属车间', key: 'workshopId', render: (row) => masterStore.workshops.find(w => w.id === row.workshopId)?.name || row.workshopId },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  model: {
    label: '机型维护',
    data: masterStore.models,
    columns: [
      { title: '机型名称', key: 'name' },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  machine: {
    label: '机台维护',
    filterType: 'machine',
    data: masterStore.machines,
    columns: [
      { title: '机台号', key: 'machineNo' },
      { title: '所属机型', key: 'modelId', render: (row) => masterStore.models.find(m => m.id === row.modelId)?.name || row.modelId },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  'abnormal-category': {
    label: '异常类别',
    data: masterStore.abnormalCategories,
    columns: [
      { title: '类别名称', key: 'name' },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  'abnormal-type': {
    label: '异常分类',
    filterType: 'abnormal',
    data: masterStore.abnormalTypes,
    columns: [
      { title: '分类名称', key: 'name' },
      { title: '所属类别', key: 'abnormalCategoryId', render: (row) => masterStore.abnormalCategories.find(c => c.id === row.abnormalCategoryId)?.name || row.abnormalCategoryId },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  team: {
    label: '组别维护',
    data: masterStore.teams,
    columns: [
      { title: '组别名称', key: 'name' },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  },
  people: {
    label: '人员维护',
    filterType: 'people',
    data: masterStore.people,
    columns: [
      { title: '姓名', key: 'name' },
      { title: '所属组别', key: 'teamId', render: (row) => masterStore.teams.find(t => t.id === row.teamId)?.name || row.teamId },
      { title: '操作', key: 'actions', width: 150, render: renderActions }
    ]
  }
}))

const currentModule = computed(() => moduleConfig.value[activeModuleId.value])

// 过滤后的表格数据
const filteredData = computed(() => {
  let data = currentModule.value.data || []
  const f = filters.value

  if (f.factoryId) {
    if (activeModuleId.value === 'workshop') {
      data = data.filter(d => d.factoryId === f.factoryId)
    } else if (activeModuleId.value === 'line') {
      const workshopIds = masterStore.workshops.filter(w => w.factoryId === f.factoryId).map(w => w.id)
      data = data.filter(d => workshopIds.includes(d.workshopId))
    }
  }
  if (f.workshopId && activeModuleId.value === 'line') data = data.filter(d => d.workshopId === f.workshopId)
  if (f.modelId && activeModuleId.value === 'machine') data = data.filter(d => d.modelId === f.modelId)
  if (f.abnormalCategoryId && activeModuleId.value === 'abnormal-type') data = data.filter(d => d.abnormalCategoryId === f.abnormalCategoryId)
  if (f.teamId && activeModuleId.value === 'people') data = data.filter(d => d.teamId === f.teamId)
  if (f.keyword) {
    const k = f.keyword.toLowerCase()
    data = data.filter(d => 
      (d.name && d.name.toLowerCase().includes(k)) || 
      (d.machineNo && d.machineNo.toLowerCase().includes(k))
    )
  }
  return data
})

function renderActions(row) {
  return h(
    'div',
    { style: 'display: flex; gap: 8px;' },
    [
      h(NButton, { size: 'small', onClick: () => handleEdit(row) }, { default: () => '编辑' }),
      h(
        NPopconfirm,
        { onPositiveClick: () => handleDelete(row) },
        { 
          trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
          default: () => '确认删除该条数据吗？'
        }
      )
    ]
  )
}

// 交互逻辑
const handleCreate = () => {
  formData.value = { sortOrder: 0 }
  modalType.value = 'create'
  modalVisible.value = true
}

const handleEdit = (row) => {
  formData.value = { ...row }
  modalType.value = 'edit'
  modalVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await masterStore.deleteItem(activeModuleId.value, row.id)
    message.success('删除成功')
  } catch (e) {
    message.error(e.message || '删除失败')
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (modalType.value === 'create') {
      await masterStore.createItem(activeModuleId.value, formData.value)
      message.success('创建成功')
    } else {
      await masterStore.updateItem(activeModuleId.value, formData.value.id, formData.value)
      message.success('更新成功')
    }
    modalVisible.value = false
  } catch (e) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  masterStore.loadAll()
})
</script>

<template>
  <div style="height: calc(100vh - 120px); display: flex; flex-direction: column;">
    <n-page-header title="字段管理" subtitle="基础数据字典维护" style="margin-bottom: 16px; flex-shrink: 0;" />

    <n-layout has-sider style="flex: 1; background-color: white; border-radius: 3px; border: 1px solid #efeff5;">
      <n-layout-sider bordered width="200" content-style="padding: 0;">
        <n-menu
          v-model:value="activeModuleId"
          :options="menuOptions"
          style="width: 100%"
        />
      </n-layout-sider>
      
      <n-layout-content content-style="padding: 24px;">
        <div style="display: flex; flex-direction: column; gap: 16px;">
          <!-- 工具栏 -->
          <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
              <h2 style="margin: 0 0 12px 0; font-size: 18px; font-weight: 500;">{{ currentModule.label }}列表</h2>
              <n-space>
                <n-select 
                  v-if="['workshop', 'line'].includes(activeModuleId)"
                  v-model:value="filters.factoryId" 
                  placeholder="所属厂区" 
                  :options="masterStore.factories" 
                  label-field="name" value-field="id" clearable style="width: 150px"
                />
                <n-select 
                  v-if="activeModuleId === 'line'"
                  v-model:value="filters.workshopId" 
                  placeholder="所属车间" 
                  :options="availableWorkshops" 
                  label-field="name" value-field="id" clearable style="width: 150px"
                />
                <n-select 
                  v-if="activeModuleId === 'machine'"
                  v-model:value="filters.modelId" 
                  placeholder="所属机型" 
                  :options="masterStore.models" 
                  label-field="name" value-field="id" clearable style="width: 150px"
                />
                <n-select 
                  v-if="activeModuleId === 'abnormal-type'"
                  v-model:value="filters.abnormalCategoryId" 
                  placeholder="所属类别" 
                  :options="masterStore.abnormalCategories" 
                  label-field="name" value-field="id" clearable style="width: 150px"
                />
                <n-select 
                  v-if="activeModuleId === 'people'"
                  v-model:value="filters.teamId" 
                  placeholder="所属组别" 
                  :options="masterStore.teams" 
                  label-field="name" value-field="id" clearable style="width: 150px"
                />
                <n-input v-model:value="filters.keyword" placeholder="搜索名称/编号" clearable style="width: 200px" />
              </n-space>
            </div>
            <n-button type="primary" @click="handleCreate">
              新增{{ currentModule.label.replace('维护', '') }}
            </n-button>
          </div>
          
          <n-data-table
            :columns="currentModule.columns"
            :data="filteredData"
            :pagination="{ pageSize: 10 }"
            :bordered="false"
          />
        </div>
      </n-layout-content>
    </n-layout>

    <!-- 通用表单模态框 -->
    <n-modal v-model:show="modalVisible" preset="card" :title="`${modalType === 'create' ? '新增' : '编辑'}${currentModule.label.replace('维护', '')}`" style="width: 500px">
      <n-form ref="formRef" label-placement="left" label-width="80">
        <!-- 通用名称字段 -->
        <n-form-item v-if="activeModuleId !== 'machine'" label="名称" required>
          <n-input v-model:value="formData.name" placeholder="请输入名称" />
        </n-form-item>
        
        <!-- 机台特有字段 -->
        <n-form-item v-if="activeModuleId === 'machine'" label="机台号" required>
          <n-input v-model:value="formData.machineNo" placeholder="请输入机台号" />
        </n-form-item>

        <!-- 关联字段：车间所属厂区 -->
        <n-form-item v-if="activeModuleId === 'workshop'" label="所属厂区" required>
          <n-select v-model:value="formData.factoryId" :options="masterStore.factories" label-field="name" value-field="id" />
        </n-form-item>

        <!-- 关联字段：线别所属厂区和车间 -->
        <n-form-item v-if="activeModuleId === 'line'" label="所属厂区">
           <n-select v-model:value="formData.factoryId" :options="masterStore.factories" label-field="name" value-field="id" clearable placeholder="辅助筛选车间" />
        </n-form-item>
        <n-form-item v-if="activeModuleId === 'line'" label="所属车间" required>
          <n-select v-model:value="formData.workshopId" :options="formWorkshops" label-field="name" value-field="id" />
        </n-form-item>

        <!-- 关联字段：机台所属机型 -->
        <n-form-item v-if="activeModuleId === 'machine'" label="所属机型" required>
          <n-select v-model:value="formData.modelId" :options="masterStore.models" label-field="name" value-field="id" />
        </n-form-item>

        <!-- 关联字段：异常分类所属类别 -->
        <n-form-item v-if="activeModuleId === 'abnormal-type'" label="所属类别" required>
          <n-select v-model:value="formData.abnormalCategoryId" :options="masterStore.abnormalCategories" label-field="name" value-field="id" />
        </n-form-item>

        <!-- 关联字段：人员所属组别 -->
        <n-form-item v-if="activeModuleId === 'people'" label="所属组别" required>
          <n-select v-model:value="formData.teamId" :options="masterStore.teams" label-field="name" value-field="id" />
        </n-form-item>

      </n-form>
      
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
