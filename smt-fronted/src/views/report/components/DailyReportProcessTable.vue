<script setup>
import { computed, h } from 'vue'
import {
  NInput,
  NInputNumber,
  NSelect,
  NButton,
  NSpace,
  NCard,
  NDataTable,
  NIcon
} from 'naive-ui'
import { AddOutlined, DeleteOutlineOutlined } from '@vicons/material'

const props = defineProps({
  rows: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  isEditing: {
    type: Boolean,
    default: false
  },
  lineMachineOptions: {
    type: Array,
    default: () => []
  },
  lineId: {
    type: [Number, String],
    default: null
  },
  onAddRow: {
    type: Function,
    default: null
  },
  onRemoveRow: {
    type: Function,
    default: null
  }
})

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

function calculateGap(row) {
  if (row.targetOutput == null || row.actualOutput == null) return null
  return row.actualOutput - row.targetOutput
}

function calculateRate(row) {
  if (row.targetOutput == null || row.targetOutput === 0 || row.actualOutput == null) return null
  return (row.actualOutput / row.targetOutput) * 100
}

const getMachineNoById = (id) => {
  if (!id) return ''
  const item = props.lineMachineOptions.find((entry) => entry.id === id)
  return item ? item.machineNo : ''
}

const renderCell = (row, key, placeholder, type = 'text', numberProps = {}) => {
  if (!props.isEditing) {
    if (type === 'number') return row[key] ?? '-'
    return row[key] || '-'
  }
  if (type === 'number') {
    return h(NInputNumber, {
      value: row[key],
      onUpdateValue: (v) => row[key] = v,
      placeholder,
      showButton: false,
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
  if (!props.isEditing) {
    return row.machineNo || '-'
  }
  return h(NSelect, {
    value: row.machineId,
    options: props.lineMachineOptions,
    labelField: 'machineNo',
    valueField: 'id',
    placeholder: '机台号',
    size: 'small',
    disabled: !props.lineId,
    onUpdateValue: (v) => {
      row.machineId = v
      row.machineNo = getMachineNoById(v)
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
        fixed: 'left',
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
        render: (row) => renderCell(row, 'runMinutes', 'MIN', 'number', { min: 0 })
      },
      {
        title: '目标',
        key: 'targetOutput',
        width: 90,
        render: (row) => renderCell(row, 'targetOutput', '目标产出', 'number', { min: 0 })
      },
      {
        title: '实际',
        key: 'actualOutput',
        width: 90,
        render: (row) => renderCell(row, 'actualOutput', '实际产出', 'number', { min: 0 })
      }
    ]
  },
  {
    title: '产出分析',
    key: 'group-analysis-1',
    children: [
      {
        title: 'GAP',
        key: 'gap',
        width: 80,
        render(row) {
          const gap = calculateGap(row)
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
    key: 'group-analysis-2',
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
          return h(NInput, { value: row.ca, disabled: true, placeholder: '待维修/管理回复', size: 'small' })
        }
      }
    ]
  },
  ...(props.isEditing
    ? [{
        title: '操作',
        key: 'actions',
        width: 60,
        fixed: 'right',
        render(_, index) {
          return h(
            NButton,
            { size: 'tiny', quaternary: true, type: 'error', onClick: () => props.onRemoveRow?.(index) },
            { icon: renderIcon(DeleteOutlineOutlined) }
          )
        }
      }]
    : [])
])
</script>

<template>
  <n-card size="small" style="margin-bottom: 12px; background-color: #f9f9f9;">
    <n-space justify="space-between" align="center">
      <n-button size="small" dashed @click="onAddRow?.()">
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
</template>
