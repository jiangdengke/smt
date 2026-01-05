<script setup>
import { ref, computed, h, watch, onMounted } from 'vue'
import { useMessage, NInput, NInputNumber, NSelect, NButton, NSpace, NCard, NDataTable } from 'naive-ui'
import { getProductionDaily, saveProductionDailyBatch } from '../../api/production'
import { useMasterDataStore } from '../../stores/masterData'
import { formatDate } from '../../utils/format'
import { SaveOutlined, AddOutlined, DeleteOutlineOutlined } from '@vicons/material'
import { NIcon } from 'naive-ui'

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

const getIdByMachineNo = (list, no) => {
  if (!no || !list) return null
  const item = list.find(i => i.machineNo === no)
  return item ? item.id : null
}

const resolveRowName = (row, list, idKey, nameKey, field = 'name') => {
  return getNameById(list, row[idKey], field) || normalizeText(row[nameKey])
}

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
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

const openForm = async () => {
  modalOpen.value = true
  isEditing.value = true
  await ensureMasterData()
  if (
    form.value.prodDate &&
    form.value.shift &&
    form.value.factoryId &&
    form.value.workshopId &&
    form.value.lineId
  ) {
    await loadData()
  }
  ensureDefaultRows()
}

const closeForm = () => {
  modalOpen.value = false
  isEditing.value = false
  rows.value = []
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
const renderCell = (row, key, placeholder, type = 'text', numberProps = {}) => {
  if (!isEditing.value) {
    // 查看模式
    if (type === 'number') return row[key] ?? '-'
    return row[key] || '-'
  }
  
  // 编辑模式
  if (type === 'number') {
    return h(NInputNumber, {
      value: row[key],
      onUpdateValue: (v) => row[key] = v,
      placeholder,
      showButton: false, // 纯数字框，更像 Excel
      size: 'small',
      ...numberProps
    })
  }
  
  return h(NInput, {
    value: row[key],
    onUpdateValue: (v) => row[key] = v,
    placeholder,
    size: 'small'
  })
}

const renderMachineSelect = (row) => {
  if (!isEditing.value) {
    return row.machineNo || '-'
  }
  return h(NSelect, {
    value: row.machineId,
    options: lineMachineOptions.value,
    labelField: 'machineNo',
    valueField: 'id',
    placeholder: '机台号',
    size: 'small',
    disabled: !form.value.lineId,
    onUpdateValue: (v) => {
      row.machineId = v
      row.machineNo = getNameById(lineMachineOptions.value, v, 'machineNo') || ''
    }
  })
}

const columns = computed(() => [
  {
    title: '基础信息',
    key: 'group-basic',
    children: [
      {
        title: '制程段',
        key: 'processName',
        width: 100,
        fixed: 'left', // 固定列
        render: (row) => renderCell(row, 'processName', '如: 印刷')
      },
      {
        title: '机台号',
        key: 'machineNo',
        width: 120,
        fixed: 'left',
        render: (row) => renderMachineSelect(row)
      },
      {
        title: '料号',
        key: 'productCode',
        width: 120,
        fixed: 'left',
        render: (row) => renderCell(row, 'productCode', '生产料号')
      },
      {
        title: '系列',
        key: 'seriesName',
        width: 80,
        render: (row) => renderCell(row, 'seriesName', '系列')
      }
    ]
  },
  {
    title: '生产设定',
    key: 'group-settings',
    children: [
      {
        title: 'CT(秒)',
        key: 'ct',
        width: 80,
        render: (row) => renderCell(row, 'ct', 'CT', 'number', { min: 0, step: 0.1 })
      },
      {
        title: '设备数',
        key: 'equipmentCount',
        width: 80,
        render: (row) => renderCell(row, 'equipmentCount', 'Qty', 'number', { min: 0 })
      },
      {
        title: '投产(分)',
        key: 'runMinutes',
        width: 90,
        render: (row) => renderCell(row, 'runMinutes', 'Min', 'number', { min: 0 })
      }
    ]
  },
  {
    title: '产出绩效',
    key: 'group-output',
    children: [
      {
        title: '目标',
        key: 'targetOutput',
        width: 90,
        render: (row) => renderCell(row, 'targetOutput', '目标', 'number', { min: 0 })
      },
      {
        title: '实际',
        key: 'actualOutput',
        width: 90,
        render: (row) => renderCell(row, 'actualOutput', '实际', 'number', { min: 0 })
      },
      {
        title: 'GAP',
        key: 'gap',
        width: 80,
        render(row) {
          const gap = calculateGap(row)
          // 实时计算，负数显红
          const color = gap !== null && gap < 0 ? '#d03050' : '#18a058'
          return h('span', { style: { color, fontWeight: 'bold' } }, gap ?? '-')
        }
      },
      {
        title: '达成率',
        key: 'achievementRate',
        width: 90,
        render(row) {
          const rate = calculateRate(row)
          return rate === null ? '-' : `${rate.toFixed(1)}%`
        }
      }
    ]
  },
  {
    title: '异常闭环 (FA/CA)',
    key: 'group-analysis',
    children: [
      {
        title: 'Down机(分)',
        key: 'downMinutes',
        width: 100,
        render: (row) => renderCell(row, 'downMinutes', 'Loss', 'number', { min: 0 })
      },
      {
        title: '异常描述 (FA)',
        key: 'fa',
        width: 200,
        render: (row) => {
          // 如果 GAP < 0，FA 应该是必填的建议（视觉提示）
          const gap = calculateGap(row)
          const isWarning = gap !== null && gap < 0 && !row.fa
          return renderCell(row, 'fa', isWarning ? '产能未达标，请填写原因' : '无异常可空')
        }
      },
      {
        title: '对策 (CA)',
        key: 'ca',
        width: 200,
        render(row) {
          // CA 始终禁用编辑，由管理端回填（此处仅展示）
          return h(NInput, { value: row.ca, disabled: true, placeholder: '待维修/管理回复', size: 'small' })
        }
      }
    ]
  },
  // 仅在编辑模式显示的“操作”列
  ...(isEditing.value ? [{
    title: '操作',
    key: 'actions',
    width: 60,
    fixed: 'right',
    render(_, index) {
      return h(
        NButton,
        { size: 'tiny', quaternary: true, type: 'error', onClick: () => removeRow(index) },
        { icon: renderIcon(DeleteOutlineOutlined) }
      )
    }
  }] : [])
])

watch(
  () => [form.value.prodDate, form.value.shift, form.value.factoryId, form.value.workshopId, form.value.lineId],
  () => {
    if (!modalOpen.value) return
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
    form.value.workshopId = null
    form.value.lineId = null
    clearRowMachines()
  }
)

watch(
  () => form.value.workshopId,
  () => {
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

function calculateGap(row) {
  if (row.targetOutput == null || row.actualOutput == null) return null
  return row.actualOutput - row.targetOutput
}

function calculateRate(row) {
  if (row.targetOutput == null || row.targetOutput === 0 || row.actualOutput == null) return null
  return (row.actualOutput / row.targetOutput) * 100
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

    <n-modal
      v-model:show="modalOpen"
      preset="card"
      style="width: 1200px;"
      @after-leave="closeForm"
    >
      <template #header>
        每日产能录入
      </template>

      <n-card size="small" style="margin-bottom: 12px;">
        <n-space align="center" wrap>
          <span style="font-size: 13px; color: gray;">日期:</span>
          <n-date-picker
            v-model:formatted-value="form.prodDate"
            value-format="yyyy-MM-dd"
            type="date"
            size="small"
            :disabled="saving"
            style="width: 130px"
          />
          <span style="font-size: 13px; color: gray;">班别:</span>
          <n-select
            v-model:value="form.shift"
            :options="shiftOptions"
            size="small"
            :disabled="saving"
            style="width: 100px"
          />
          <span style="font-size: 13px; color: gray;">厂区:</span>
          <n-select
            v-model:value="form.factoryId"
            :options="masterStore.factories"
            label-field="name"
            value-field="id"
            size="small"
            :disabled="saving"
            style="width: 140px"
            placeholder="请选择"
          />
          <span style="font-size: 13px; color: gray;">车间:</span>
          <n-select
            v-model:value="form.workshopId"
            :options="filterWorkshops"
            label-field="name"
            value-field="id"
            size="small"
            :disabled="saving"
            style="width: 140px"
            placeholder="请选择"
          />
          <span style="font-size: 13px; color: gray;">线别:</span>
          <n-select
            v-model:value="form.lineId"
            :options="filterLines"
            label-field="name"
            value-field="id"
            size="small"
            :disabled="saving"
            style="width: 140px"
            placeholder="请选择"
          />
        </n-space>
      </n-card>

      <n-card size="small" style="margin-bottom: 12px; background-color: #f9f9f9;">
        <n-space justify="space-between" align="center">
          <n-button size="small" dashed @click="addRow">
            <template #icon><n-icon :component="AddOutlined" /></template>
            新增制程段
          </n-button>
          <div style="font-size: 12px; color: #666;">
            提示：GAP 为负数时请务必填写异常描述 (FA)
          </div>
        </n-space>
      </n-card>

      <n-card content-style="padding: 0;">
        <n-data-table
          :columns="columns"
          :data="rows"
          :loading="loading"
          :single-column="true"
          :single-line="false"
          size="small"
          :scroll-x="2000"
          style="height: 520px"
          flex-height
        />
      </n-card>

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
