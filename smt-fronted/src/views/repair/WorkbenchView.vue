<script setup>
import { ref, computed, onMounted, h, watch } from 'vue'
import { useMessage, NButton, NTag } from 'naive-ui'
import { useMasterDataStore } from '../../stores/masterData'
import { useAuthStore } from '../../stores/auth'
import { getRepairRecords, createRepairRecord } from '../../api/repair'
import { formatDate, formatShift, toDateTime, normalizeText } from '../../utils/format'

const message = useMessage()
const masterStore = useMasterDataStore()
const authStore = useAuthStore()

const getNameById = (list, id, field = 'name') => {
  if (!id) return null
  const item = list.find((entry) => entry.id === id)
  return item ? item[field] : null
}

const getNamesByIds = (list, ids, field = 'name') => {
  if (!ids || ids.length === 0) return []
  return ids
    .map((id) => getNameById(list, id, field))
    .filter((name) => name)
}

// State
const records = ref([])
const total = ref(0)
const loading = ref(false)
const filterForm = ref({
  occurDate: null,
  fixedDate: null,
  shift: null,
  factoryId: null,
  workshopId: null,
  lineId: null,
  modelId: null,
  machineId: null,
  abnormalCategoryId: null,
  abnormalTypeId: null,
  responsiblePersonId: null,
  repairPersonId: null
})

// New Record Modal State
const newRecordOpen = ref(false)
const newRecordForm = ref(createEmptyRecord())

// Computed Options (Cascading)
const filterWorkshops = computed(() => 
  filterForm.value.factoryId 
    ? masterStore.workshops.filter(i => i.factoryId === filterForm.value.factoryId) 
    : masterStore.workshops
)
const filterLines = computed(() => 
  filterForm.value.workshopId 
    ? masterStore.lines.filter(i => i.workshopId === filterForm.value.workshopId) 
    : masterStore.lines
)
const filterMachines = computed(() => 
  filterForm.value.modelId 
    ? masterStore.machines.filter(i => i.modelId === filterForm.value.modelId) 
    : masterStore.machines
)
const filterAbnormalTypes = computed(() => 
  filterForm.value.abnormalCategoryId 
    ? masterStore.abnormalTypes.filter(i => i.abnormalCategoryId === filterForm.value.abnormalCategoryId) 
    : masterStore.abnormalTypes
)

// Computed for New Record (Cascading)
const newAvailableWorkshops = computed(() => 
  newRecordForm.value.factoryId 
    ? masterStore.workshops.filter(i => i.factoryId === newRecordForm.value.factoryId) 
    : []
)
const newAvailableLines = computed(() => 
  newRecordForm.value.workshopId 
    ? masterStore.lines.filter(i => i.workshopId === newRecordForm.value.workshopId) 
    : []
)
const newAvailableMachines = computed(() => 
  newRecordForm.value.modelId 
    ? masterStore.machines.filter(i => i.modelId === newRecordForm.value.modelId) 
    : []
)
const newAvailableAbnormalTypes = computed(() => 
  newRecordForm.value.abnormalCategoryId 
    ? masterStore.abnormalTypes.filter(i => i.abnormalCategoryId === newRecordForm.value.abnormalCategoryId) 
    : []
)
const newAvailablePeople = computed(() => 
  newRecordForm.value.teamId 
    ? masterStore.people.filter(i => i.teamId === newRecordForm.value.teamId) 
    : []
)

function createEmptyRecord() {
  return {
    occurAt: null,
    shift: 'DAY',
    factoryId: null,
    workshopId: null,
    lineId: null,
    modelId: null,
    machineId: null,
    abnormalCategoryId: null,
    abnormalTypeId: null,
    abnormalDesc: '',
    solution: '',
    isFixed: false,
    fixedAt: null,
    repairMinutes: 0,
    teamId: null,
    responsiblePersonId: null,
    repairPersonIds: []
  }
}

// Table Columns
const columns = [
  { title: 'ID', key: 'id', width: 60 },
  {
    title: '时间/班次',
    key: 'occurAt',
    render(row) {
      return `${formatDate(row.occurAt)} · ${formatShift(row.shift)}班`
    }
  },
  {
    title: '位置',
    key: 'location',
    render(row) {
      return `${row.factoryName || '-'} / ${row.workshopName || '-'} / ${row.lineName || '-'}`
    }
  },
  {
    title: '机台',
    key: 'machine',
    render(row) {
      return `${row.modelName || '-'} - ${row.machineNo || '-'}`
    }
  },
  {
    title: '异常',
    key: 'abnormal',
    render(row) {
      return h('div', [
        h('div', `${row.abnormalCategoryName || ''} · ${row.abnormalTypeName || ''}`),
        h('div', { style: 'font-size: 12px; color: gray' }, row.abnormalDesc)
      ])
    }
  },
  {
    title: '人员',
    key: 'people',
    render(row) {
      const repairNames = row.repairPersonNames?.join('、') || '-'
      return h('div', [
        h('div', `责任: ${row.responsiblePersonName || '-'}`),
        h('div', `维修: ${repairNames}`)
      ])
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row) {
      return h(
        NTag,
        { type: row.isFixed ? 'success' : 'warning', bordered: false },
        { default: () => (row.isFixed ? `已修复 (${row.repairMinutes}m)` : '待处理') }
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row) {
      return h(
        NButton,
        { size: 'small', onClick: () => message.info('详情功能暂未实现') },
        { default: () => '详情' }
      )
    }
  }
]

// Actions
const loadRecords = async (page = 1) => {
  loading.value = true
  try {
    const query = {
      page,
      size: 20,
      occurFrom: toDateTime(filterForm.value.occurDate),
      occurTo: toDateTime(filterForm.value.occurDate, true),
      fixedFrom: toDateTime(filterForm.value.fixedDate),
      fixedTo: toDateTime(filterForm.value.fixedDate, true),
      shift: filterForm.value.shift,
      factoryName: getNameById(masterStore.factories, filterForm.value.factoryId),
      workshopName: getNameById(masterStore.workshops, filterForm.value.workshopId),
      lineName: getNameById(masterStore.lines, filterForm.value.lineId),
      modelName: getNameById(masterStore.models, filterForm.value.modelId),
      machineNo: getNameById(masterStore.machines, filterForm.value.machineId, 'machineNo'),
      abnormalCategoryName: getNameById(masterStore.abnormalCategories, filterForm.value.abnormalCategoryId),
      abnormalTypeName: getNameById(masterStore.abnormalTypes, filterForm.value.abnormalTypeId),
      responsiblePersonName: getNameById(masterStore.people, filterForm.value.responsiblePersonId),
      repairPersonName: getNameById(masterStore.people, filterForm.value.repairPersonId)
    }
    const res = await getRepairRecords(query)
    records.value = res?.data || []
    total.value = res?.total || 0
  } catch (err) {
    message.error(err.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

const openNewRecord = () => {
  newRecordForm.value = createEmptyRecord()
  newRecordOpen.value = true
}

const submitNewRecord = async () => {
  try {
    const f = newRecordForm.value
    const payload = {
      occurAt: toDateTime(f.occurAt),
      shift: f.shift,
      factoryName: getNameById(masterStore.factories, f.factoryId),
      workshopName: getNameById(masterStore.workshops, f.workshopId),
      lineName: getNameById(masterStore.lines, f.lineId),
      modelName: getNameById(masterStore.models, f.modelId),
      machineNo: getNameById(masterStore.machines, f.machineId, 'machineNo'),
      abnormalCategoryName: getNameById(masterStore.abnormalCategories, f.abnormalCategoryId),
      abnormalTypeName: getNameById(masterStore.abnormalTypes, f.abnormalTypeId),
      teamName: getNameById(masterStore.teams, f.teamId),
      responsiblePersonName: getNameById(masterStore.people, f.responsiblePersonId),
      repairPersonNames: getNamesByIds(masterStore.people, f.repairPersonIds),
      fixedAt: f.isFixed ? toDateTime(f.fixedAt) : null,
      abnormalDesc: normalizeText(f.abnormalDesc),
      solution: normalizeText(f.solution),
      isFixed: f.isFixed,
      repairMinutes: f.isFixed ? f.repairMinutes : null
    }
    await createRepairRecord(payload)
    message.success('保存成功')
    newRecordOpen.value = false
    loadRecords()
  } catch (err) {
    message.error(err.message || '保存失败')
  }
}

// Watchers for cascading dropdown resets (simplified)
watch(() => newRecordForm.value.factoryId, () => newRecordForm.value.workshopId = null)
watch(() => newRecordForm.value.workshopId, () => newRecordForm.value.lineId = null)
watch(() => newRecordForm.value.modelId, () => newRecordForm.value.machineId = null)
watch(() => newRecordForm.value.abnormalCategoryId, () => newRecordForm.value.abnormalTypeId = null)
watch(() => newRecordForm.value.teamId, () => {
  newRecordForm.value.responsiblePersonId = null
  newRecordForm.value.repairPersonIds = []
})
// Filter cascading
watch(() => filterForm.value.factoryId, () => filterForm.value.workshopId = null)
watch(() => filterForm.value.workshopId, () => filterForm.value.lineId = null)
watch(() => filterForm.value.modelId, () => filterForm.value.machineId = null)
watch(() => filterForm.value.abnormalCategoryId, () => filterForm.value.abnormalTypeId = null)
watch(filterForm, () => loadRecords(1), { deep: true })

onMounted(async () => {
  await masterStore.loadAll()
  loadRecords()
})
</script>

<template>
  <div>
    <n-card style="margin-bottom: 16px;">
      <n-grid x-gap="12" y-gap="12" :cols="6">
        <n-grid-item>
          <n-date-picker v-model:formatted-value="filterForm.occurDate" value-format="yyyy-MM-dd" type="date" clearable placeholder="发生时间" />
        </n-grid-item>
        <n-grid-item>
          <n-date-picker v-model:formatted-value="filterForm.fixedDate" value-format="yyyy-MM-dd" type="date" clearable placeholder="修复日期" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.shift" clearable placeholder="班次" :options="[{label:'白班', value:'DAY'}, {label:'夜班', value:'NIGHT'}]" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.factoryId" clearable placeholder="厂区" label-field="name" value-field="id" :options="masterStore.factories" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.workshopId" clearable placeholder="车间" label-field="name" value-field="id" :options="filterWorkshops" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.lineId" clearable placeholder="线别" label-field="name" value-field="id" :options="filterLines" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.modelId" clearable placeholder="机型" label-field="name" value-field="id" :options="masterStore.models" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.machineId" clearable placeholder="机台" label-field="machineNo" value-field="id" :options="filterMachines" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.abnormalCategoryId" clearable placeholder="异常类别" label-field="name" value-field="id" :options="masterStore.abnormalCategories" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.abnormalTypeId" clearable placeholder="异常分类" label-field="name" value-field="id" :options="filterAbnormalTypes" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.responsiblePersonId" clearable placeholder="责任人" label-field="name" value-field="id" :options="masterStore.people" />
        </n-grid-item>
        <n-grid-item>
          <n-select v-model:value="filterForm.repairPersonId" clearable placeholder="维修人" label-field="name" value-field="id" :options="masterStore.people" />
        </n-grid-item>
      </n-grid>
      <div style="margin-top: 12px; display: flex; justify-content: flex-end;">
        <n-button type="primary" @click="openNewRecord">新增记录</n-button>
      </div>
    </n-card>

    <n-data-table
      remote
      :columns="columns"
      :data="records"
      :loading="loading"
      :pagination="{ pageSize: 20, itemCount: total }"
    />

    <!-- New Record Modal -->
    <n-modal v-model:show="newRecordOpen" preset="card" title="新增维修记录" style="width: 800px">
      <n-form label-placement="left" label-width="100">
        <n-grid x-gap="24" :cols="2">
          <n-grid-item><n-form-item label="发生时间" required><n-date-picker v-model:formatted-value="newRecordForm.occurAt" value-format="yyyy-MM-dd" type="date" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="班次" required><n-select v-model:value="newRecordForm.shift" :options="[{label:'白班', value:'DAY'}, {label:'夜班', value:'NIGHT'}]" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="厂区" required><n-select v-model:value="newRecordForm.factoryId" label-field="name" value-field="id" :options="masterStore.factories" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="车间" required><n-select v-model:value="newRecordForm.workshopId" label-field="name" value-field="id" :options="newAvailableWorkshops" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="线别" required><n-select v-model:value="newRecordForm.lineId" label-field="name" value-field="id" :options="newAvailableLines" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="机型" required><n-select v-model:value="newRecordForm.modelId" label-field="name" value-field="id" :options="masterStore.models" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="机台号" required><n-select v-model:value="newRecordForm.machineId" label-field="machineNo" value-field="id" :options="newAvailableMachines" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="异常类别" required><n-select v-model:value="newRecordForm.abnormalCategoryId" label-field="name" value-field="id" :options="masterStore.abnormalCategories" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="异常分类" required><n-select v-model:value="newRecordForm.abnormalTypeId" label-field="name" value-field="id" :options="newAvailableAbnormalTypes" /></n-form-item></n-grid-item>
          <n-grid-item span="2"><n-form-item label="异常描述" required><n-input v-model:value="newRecordForm.abnormalDesc" /></n-form-item></n-grid-item>
          <n-grid-item span="2"><n-form-item label="解决对策"><n-input v-model:value="newRecordForm.solution" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="是否已修复" required><n-switch v-model:value="newRecordForm.isFixed" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="修复日期"><n-date-picker v-model:formatted-value="newRecordForm.fixedAt" value-format="yyyy-MM-dd" type="date" :disabled="!newRecordForm.isFixed" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="维修耗时"><n-input-number v-model:value="newRecordForm.repairMinutes" :min="0" :disabled="!newRecordForm.isFixed" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="组别" required><n-select v-model:value="newRecordForm.teamId" label-field="name" value-field="id" :options="masterStore.teams" /></n-form-item></n-grid-item>
          <n-grid-item><n-form-item label="责任人" required><n-select v-model:value="newRecordForm.responsiblePersonId" label-field="name" value-field="id" :options="newAvailablePeople" /></n-form-item></n-grid-item>
          <n-grid-item span="2"><n-form-item label="维修人"><n-select multiple v-model:value="newRecordForm.repairPersonIds" label-field="name" value-field="id" :options="newAvailablePeople" /></n-form-item></n-grid-item>
        </n-grid>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="newRecordOpen = false">取消</n-button>
          <n-button type="primary" @click="submitNewRecord">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
