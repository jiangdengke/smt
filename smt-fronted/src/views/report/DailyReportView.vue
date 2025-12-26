<script setup>
import { ref, computed, h, watch, onMounted } from 'vue'
import { useMessage, NInput, NInputNumber, NSelect, NButton, NTag, NSpace, NCard, NDataTable, NSwitch } from 'naive-ui'
import { getProductionDaily, saveProductionDailyBatch } from '../../api/production'
import { formatDate } from '../../utils/format'
import { SaveOutlined, AddOutlined, DeleteOutlineOutlined, EditOutlined, DownloadOutlined, CloseOutlined, AutoAwesomeOutlined } from '@vicons/material'
import { NIcon } from 'naive-ui'

const message = useMessage()
const apiBase = import.meta.env.VITE_API_BASE || '/api'

const shiftOptions = [
  { label: '白班', value: 'DAY' },
  { label: '夜班', value: 'NIGHT' }
]

// 预设的标准制程段，用于快速填充
const STANDARD_PROCESSES = ['印刷', 'SPI', '贴片', '回流焊', 'AOI']

const form = ref({
  prodDate: formatDate(new Date().toISOString()),
  shift: 'DAY'
})

const loading = ref(false)
const saving = ref(false)
const isEditing = ref(false) // 控制查看/编辑模式
const rows = ref([]) // 统一的数据源

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 核心加载逻辑
const loadData = async () => {
  if (!form.value.prodDate || !form.value.shift) return
  loading.value = true
  try {
    const res = await getProductionDaily(form.value.prodDate, form.value.shift)
    const processes = res?.processes || []
    
    // 映射数据
    rows.value = processes.map(mapProcessToRow)
    
    // 如果没有数据，且处于编辑模式，或者刚切换日期，自动退出编辑模式
    if (rows.value.length === 0 && !isEditing.value) {
      // 可以在这里提示暂无数据
    }
  } catch (error) {
    message.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 切换到编辑模式
const enterEditMode = () => {
  if (rows.value.length === 0) {
    // 如果是空的，自动加一行
    addRow()
  }
  isEditing.value = true
}

// 取消编辑（重新加载数据）
const cancelEdit = () => {
  isEditing.value = false
  loadData()
}

// 保存逻辑
const handleSave = async () => {
  if (!form.value.prodDate || !form.value.shift) {
    message.warning('请选择日期和班别')
    return
  }
  if (!validateRows(rows.value)) return
  
  saving.value = true
  try {
    const payload = {
      prodDate: form.value.prodDate,
      shift: form.value.shift,
      processes: rows.value
        .filter((row) => !isRowEmpty(row))
        .map((row) => ({
          id: row.id || null,
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
        }))
    }
    const res = await saveProductionDailyBatch(payload)
    rows.value = (res?.processes || []).map(mapProcessToRow)
    message.success('保存成功')
    isEditing.value = false
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleExport = () => {
  if (!form.value.prodDate) {
    message.warning('请选择日期')
    return
  }
  const params = new URLSearchParams({
    from: form.value.prodDate,
    to: form.value.prodDate,
    shift: form.value.shift
  }).toString()
  window.open(`${apiBase}/production-daily/export?${params}`, '_blank')
}

// 快捷操作：一键填充标准制程
const fillStandardProcesses = () => {
  // 检查是否已经是标准顺序，避免重复添加
  const existingNames = new Set(rows.value.map(r => r.processName))
  const newRows = []
  
  STANDARD_PROCESSES.forEach(name => {
    if (!existingNames.has(name)) {
      const row = createRow()
      row.processName = name
      newRows.push(row)
    }
  })
  
  if (newRows.length === 0) {
    message.info('标准制程已存在，无需填充')
    return
  }
  
  rows.value = [...rows.value, ...newRows]
  message.success(`已添加 ${newRows.length} 个标准制程`)
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

const columns = computed(() => [
  {
    title: '基础信息',
    key: 'group-basic',
    children: [
      {
        title: '制程段',
        key: 'processName',
        width: 120,
        fixed: 'left', // 固定列
        render: (row) => renderCell(row, 'processName', '如: 印刷')
      },
      {
        title: '料号',
        key: 'productCode',
        width: 140,
        fixed: 'left',
        render: (row) => renderCell(row, 'productCode', '生产料号')
      },
      {
        title: '系列',
        key: 'seriesName',
        width: 100,
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
  () => [form.value.prodDate, form.value.shift],
  () => {
    isEditing.value = false // 切换日期自动退出编辑
    loadData()
  }
)

onMounted(loadData)

// --- Helpers ---

function createRow() {
  return {
    id: null,
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
  return !normalizeText(row.processName) && !normalizeText(row.productCode)
}

function validateRows(rowList) {
  const targetRows = rowList.filter((row) => !isRowEmpty(row))
  if (!targetRows.length) {
    message.warning('请至少填写一条数据')
    return false
  }
  // 简单校验
  for (let i = 0; i < targetRows.length; i++) {
    if (!targetRows[i].processName) {
      message.warning(`第 ${i+1} 行缺少制程段名称`)
      return false
    }
  }
  return true
}
</script>

<template>
  <div class="daily-report-page">
    <n-page-header title="每日产能录入" subtitle="生产绩效与异常闭环" style="margin-bottom: 16px">
      <template #extra>
        <div style="display: flex; gap: 12px; align-items: center;">
          <!-- 筛选区 -->
          <n-card size="small" style="padding: 4px 12px; border-radius: 4px;">
            <n-space align="center">
              <span style="font-size: 13px; color: gray;">日期:</span>
              <n-date-picker
                v-model:formatted-value="form.prodDate"
                value-format="yyyy-MM-dd"
                type="date"
                size="small"
                :disabled="isEditing"
                style="width: 140px"
              />
              <span style="font-size: 13px; color: gray; margin-left: 8px;">班别:</span>
              <n-select
                v-model:value="form.shift"
                :options="shiftOptions"
                size="small"
                :disabled="isEditing"
                style="width: 80px"
              />
            </n-space>
          </n-card>

          <!-- 操作区 -->
          <template v-if="!isEditing">
            <n-button type="primary" @click="enterEditMode">
              <template #icon><n-icon :component="EditOutlined" /></template>
              进入编辑
            </n-button>
            <n-button secondary @click="handleExport">
              <template #icon><n-icon :component="DownloadOutlined" /></template>
              导出Excel
            </n-button>
          </template>

          <template v-else>
            <n-button type="primary" color="#18a058" @click="handleSave" :loading="saving">
              <template #icon><n-icon :component="SaveOutlined" /></template>
              保存修改
            </n-button>
            <n-button @click="cancelEdit">
              <template #icon><n-icon :component="CloseOutlined" /></template>
              取消
            </n-button>
          </template>
        </div>
      </template>
    </n-page-header>

    <!-- 编辑工具栏 (仅编辑模式显示) -->
    <n-card v-if="isEditing" size="small" style="margin-bottom: 12px; background-color: #f9f9f9;">
      <n-space justify="space-between" align="center">
        <div style="display: flex; gap: 8px; align-items: center;">
          <n-button size="small" dashed @click="addRow">
            <template #icon><n-icon :component="AddOutlined" /></template>
            添加一行
          </n-button>
          <n-button size="small" dashed @click="fillStandardProcesses">
            <template #icon><n-icon :component="AutoAwesomeOutlined" /></template>
            一键填充标准制程
          </n-button>
        </div>
        <div style="font-size: 12px; color: #666;">
          <n-icon :component="AutoAwesomeOutlined" style="margin-right: 4px; vertical-align: bottom;" />
          提示：GAP 为负数时请务必填写异常描述 (FA)
        </div>
      </n-space>
    </n-card>

    <!-- 主表格 -->
    <n-card content-style="padding: 0;">
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :single-column="true"
        :single-line="false"
        size="small"
        :scroll-x="1500"
        style="height: calc(100vh - 220px)"
        flex-height
      />
    </n-card>
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