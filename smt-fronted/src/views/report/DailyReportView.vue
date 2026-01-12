<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useMessage, NButton, NCard, NModal, NSpace, NPageHeader, NIcon } from 'naive-ui'
import { getProductionDaily, saveProductionDailyBatch, getProductionDailyRecords, exportProductionDailyByDate } from '../../api/production'
import { useMasterDataStore } from '../../stores/masterData'
import { formatDate } from '../../utils/format'
import { SaveOutlined } from '@vicons/material'
import DailyReportHeaderForm from './components/DailyReportHeaderForm.vue'
import DailyReportProcessTable from './components/DailyReportProcessTable.vue'
import DailyReportRecordList from './components/DailyReportRecordList.vue'

const message = useMessage()
const masterStore = useMasterDataStore()
const DEFAULT_ROW_COUNT = 3

const shiftOptions = [
  { label: '白班', value: 'DAY' },
  { label: '夜班', value: 'NIGHT' }
]

const form = ref({
  prodDate: formatDate(new Date().toISOString()),
  shift: 'DAY',
  factoryId: null,
  workshopId: null,
  lineId: null
})

const filterWorkshops = computed(() =>
  form.value.factoryId
    ? masterStore.workshops.filter((item) => item.factoryId === form.value.factoryId)
    : masterStore.workshops
)

const filterLines = computed(() =>
  form.value.workshopId
    ? masterStore.lines.filter((item) => item.workshopId === form.value.workshopId)
    : masterStore.lines
)

const lineMachineOptions = computed(() =>
  form.value.lineId
    ? masterStore.machines.filter((item) => item.lineId === form.value.lineId)
    : []
)

const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false) // 控制查看/编辑模式
const modalOpen = ref(false)
const rows = ref([]) // 统一的数据源
const masterLoaded = ref(false)
const formMode = ref('create')
const recordLoading = ref(false)
const recordRows = ref([])
const headerLocked = computed(() => saving.value || formMode.value === 'edit')

const ensureMasterData = async () => {
  if (masterLoaded.value) return
  await masterStore.loadAll()
  masterLoaded.value = true
}

const getNameById = (list, id, field = 'name') => {
  if (!id) return null
  const item = list.find((entry) => entry.id === id)
  return item ? item[field] : null
}

const getIdByName = (list, name) => {
  if (!name || !list) return null
  const item = list.find((entry) => entry.name === name)
  return item ? item.id : null
}

const getIdByMachineNo = (list, no) => {
  if (!no || !list) return null
  const item = list.find(i => i.machineNo === no)
  return item ? item.id : null
}

const resolveRowName = (row, list, idKey, nameKey, field = 'name') => {
  return getNameById(list, row[idKey], field) || normalizeText(row[nameKey])
}

const ensureDefaultRows = () => {
  if (rows.value.length > 0) return
  rows.value = Array.from({ length: DEFAULT_ROW_COUNT }, () => createRow())
}

// 核心加载逻辑
const loadData = async () => {
  if (!form.value.prodDate || !form.value.shift || !form.value.factoryId || !form.value.workshopId || !form.value.lineId) {
    return
  }
  loading.value = true
  try {
    const factoryName = getNameById(masterStore.factories, form.value.factoryId)
    const workshopName = getNameById(masterStore.workshops, form.value.workshopId)
    const lineName = getNameById(masterStore.lines, form.value.lineId)
    const res = await getProductionDaily(
      form.value.prodDate,
      form.value.shift,
      factoryName,
      workshopName,
      lineName
    )
    const processes = res?.processes || []
    
    // 映射数据
    rows.value = processes.map(mapProcessToRow)
    
    if (rows.value.length === 0 && modalOpen.value) {
      ensureDefaultRows()
    }
  } catch (error) {
    message.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = {
    prodDate: formatDate(new Date().toISOString()),
    shift: 'DAY',
    factoryId: null,
    workshopId: null,
    lineId: null
  }
}

const openForm = async () => {
  formMode.value = 'create'
  resetForm()
  rows.value = []
  modalOpen.value = true
  isEditing.value = true
  await ensureMasterData()
  ensureDefaultRows()
}

const closeForm = () => {
  modalOpen.value = false
  isEditing.value = false
  rows.value = []
  formMode.value = 'create'
}

const openEditGroup = async (group) => {
  formMode.value = 'edit'
  await ensureMasterData()
  const factoryId = getIdByName(masterStore.factories, group.factoryName)
  const workshopId = getIdByName(
    masterStore.workshops.filter((item) => item.factoryId === factoryId),
    group.workshopName
  )
  const lineId = getIdByName(
    masterStore.lines.filter((item) => item.workshopId === workshopId),
    group.lineName
  )
  form.value = {
    prodDate: formatDate(group.prodDate),
    shift: group.shift,
    factoryId,
    workshopId,
    lineId
  }
  modalOpen.value = true
  isEditing.value = true
  await loadData()
}

// 保存逻辑
const handleSave = async () => {
  if (!form.value.prodDate || !form.value.shift || !form.value.factoryId || !form.value.workshopId || !form.value.lineId) {
    message.warning('请完善表头信息（日期、班别、厂区、车间、线别）')
    return
  }
  if (!validateRows(rows.value)) return
  
  saving.value = true
  try {
    const factoryName = getNameById(masterStore.factories, form.value.factoryId)
    const workshopName = getNameById(masterStore.workshops, form.value.workshopId)
    const lineName = getNameById(masterStore.lines, form.value.lineId)
    if (formMode.value === 'create') {
      const key = buildGroupKey(form.value.prodDate, form.value.shift, factoryName, workshopName, lineName)
      if (groupKeySet.value.has(key)) {
        message.warning('该日期/班别/厂区/车间/线别已存在，请从列表编辑')
        return
      }
    }
    const payload = {
      prodDate: form.value.prodDate,
      shift: form.value.shift,
      factoryName,
      workshopName,
      lineName,
      processes: rows.value
        .filter((row) => !isRowEmpty(row))
        .map((row) => {
          const machineNo = resolveRowName(row, masterStore.machines, 'machineId', 'machineNo', 'machineNo')
          return {
            id: row.id || null,
            machineNo,
            processName: normalizeText(row.processName),
            productCode: normalizeText(row.productCode),
            seriesName: normalizeText(row.seriesName),
            ct: row.ct,
            equipmentCount: row.equipmentCount,
            runMinutes: row.runMinutes,
            targetOutput: row.targetOutput,
            actualOutput: row.actualOutput,
            downMinutes: row.downMinutes,
            fa: normalizeText(row.fa),
            ca: normalizeText(row.ca)
          }
        })
    }
    const res = await saveProductionDailyBatch(payload)
    rows.value = (res?.processes || []).map(mapProcessToRow)
    message.success('保存成功')
    closeForm()
    await loadRecordList()
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 行操作
const addRow = () => rows.value.push(createRow())
const removeRow = (index) => rows.value.splice(index, 1)

// --- 列定义 (动态切换 编辑/查看 渲染) ---

// 辅助渲染：根据 isEditing 决定显示 Input 还是 Text
watch(
  () => [form.value.prodDate, form.value.shift, form.value.factoryId, form.value.workshopId, form.value.lineId],
  () => {
    if (!modalOpen.value || formMode.value !== 'edit') return
    loadData()
  }
)

const clearRowMachines = () => {
  rows.value.forEach((row) => {
    row.machineId = null
    row.machineNo = ''
  })
}

watch(
  () => form.value.factoryId,
  () => {
    if (formMode.value === 'edit') return
    form.value.workshopId = null
    form.value.lineId = null
    clearRowMachines()
  }
)

watch(
  () => form.value.workshopId,
  () => {
    if (formMode.value === 'edit') return
    form.value.lineId = null
    clearRowMachines()
  }
)

watch(
  () => form.value.lineId,
  () => {
    clearRowMachines()
  }
)

onMounted(async () => {
  await masterStore.loadAll()
  masterLoaded.value = true
  loadRecordList()
})

// --- Helpers ---

function createRow() {
  return {
    id: null,
    machineId: null,
    machineNo: '',
    processName: '',
    productCode: '',
    seriesName: '',
    ct: null,
    equipmentCount: null,
    runMinutes: 600, // 默认 10小时
    targetOutput: null,
    actualOutput: null,
    downMinutes: 0,
    fa: '',
    ca: ''
  }
}

const buildGroupKey = (prodDate, shift, factoryName, workshopName, lineName) =>
  [formatDate(prodDate), shift, factoryName, workshopName, lineName].join('|')

const groupKeySet = computed(() => {
  const set = new Set()
  recordRows.value.forEach((row) => {
    const key = buildGroupKey(row.prodDate, row.shift, row.factoryName, row.workshopName, row.lineName)
    set.add(key)
  })
  return set
})

const handleExportGroup = async (group) => {
  try {
    const prodDate = formatDate(group.prodDate)
    if (!prodDate) {
      message.warning('导出日期缺失')
      return
    }
    await exportProductionDailyByDate(prodDate)
    message.success('导出成功')
  } catch (error) {
    message.error(error.message || '导出失败')
  }
}

const loadRecordList = async () => {
  recordLoading.value = true
  try {
    const res = await getProductionDailyRecords()
    recordRows.value = res || []
  } catch (error) {
    message.error(error.message || '加载记录失败')
  } finally {
    recordLoading.value = false
  }
}

function mapProcessToRow(item) {
  return {
    id: item.id || null,
    machineId: getIdByMachineNo(masterStore.machines, item.machineNo),
    machineNo: item.machineNo || '',
    processName: item.processName || '',
    productCode: item.productCode || '',
    seriesName: item.seriesName || '',
    ct: item.ct ?? null,
    equipmentCount: item.equipmentCount ?? null,
    runMinutes: item.runMinutes ?? null,
    targetOutput: item.targetOutput ?? null,
    actualOutput: item.actualOutput ?? null,
    downMinutes: item.downMinutes ?? null,
    fa: item.fa || '',
    ca: item.ca || ''
  }
}

function normalizeText(value) {
  if (value === null || value === undefined) return null
  const text = String(value).trim()
  return text.length ? text : null
}

function isRowEmpty(row) {
  return !normalizeText(row.processName)
    && !normalizeText(row.productCode)
    && !normalizeText(row.seriesName)
    && !row.machineId
    && !normalizeText(row.machineNo)
}

function validateRows(rowList) {
  const targetRows = rowList.filter((row) => !isRowEmpty(row))
  if (!targetRows.length) {
    message.warning('请至少填写一条数据')
    return false
  }
  // 简单校验
  for (let i = 0; i < targetRows.length; i++) {
    const row = targetRows[i]
    if (!resolveRowName(row, masterStore.machines, 'machineId', 'machineNo', 'machineNo')) {
      message.warning(`第 ${i + 1} 行缺少机台号`)
      return false
    }
    if (!normalizeText(row.processName)) {
      message.warning(`第 ${i + 1} 行缺少制程段名称`)
      return false
    }
    if (!normalizeText(row.productCode)) {
      message.warning(`第 ${i + 1} 行缺少生产料号`)
      return false
    }
    if (!normalizeText(row.seriesName)) {
      message.warning(`第 ${i + 1} 行缺少系列`)
      return false
    }
  }
  return true
}
</script>

<template>
  <div class="daily-report-page">
    <n-page-header title="每日产能录入" subtitle="点击新增后填写制程段明细" style="margin-bottom: 16px">
      <template #extra>
        <n-button type="primary" @click="openForm">
          新增每日产能
        </n-button>
      </template>
    </n-page-header>

    <n-card>
      请在弹出的表单中选择日期、班别、厂区、车间、线别，并填写各制程段的产能数据。
    </n-card>

    <DailyReportRecordList
      :records="recordRows"
      :loading="recordLoading"
      :on-edit="openEditGroup"
      :on-export="handleExportGroup"
    />

    <n-modal
      v-model:show="modalOpen"
      preset="card"
      style="width: 1200px;"
      @after-leave="closeForm"
    >
      <template #header>
        每日产能录入
      </template>

      <DailyReportHeaderForm
        :form="form"
        :shift-options="shiftOptions"
        :factories="masterStore.factories"
        :workshops="filterWorkshops"
        :lines="filterLines"
        :header-locked="headerLocked"
      />

      <DailyReportProcessTable
        :rows="rows"
        :loading="loading"
        :is-editing="isEditing"
        :line-machine-options="lineMachineOptions"
        :line-id="form.lineId"
        :on-add-row="addRow"
        :on-remove-row="removeRow"
      />

      <template #footer>
        <n-space justify="end">
          <n-button @click="closeForm">取消</n-button>
          <n-button type="primary" color="#18a058" @click="handleSave" :loading="saving">
            <template #icon><n-icon :component="SaveOutlined" /></template>
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
/* 稍微调整一下表格样式，使其更像 Excel */
:deep(.n-data-table .n-data-table-td) {
  padding: 4px 8px;
}
:deep(.n-input .n-input__input-el) {
  height: 28px;
}
</style>
