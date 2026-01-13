<script setup>
import { computed, h } from 'vue'
import { NCard, NDataTable, NButton, NSpace } from 'naive-ui'
import { formatDate, formatShift } from '../../../utils/format'

const props = defineProps({
  records: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  onEdit: {
    type: Function,
    default: null
  },
  onExport: {
    type: Function,
    default: null
  }
})

const buildGroupKey = (prodDate, shift, factoryName, workshopName, lineName) =>
  [formatDate(prodDate), factoryName, workshopName, lineName].join('|')

const shiftOrder = ['DAY', 'NIGHT']

const buildShiftSummary = (shifts) => shifts.map((shift) => `${formatShift(shift)}班`).join('/')

const groupRows = computed(() => {
  const map = new Map()
  props.records.forEach((row) => {
    const key = buildGroupKey(row.prodDate, row.shift, row.factoryName, row.workshopName, row.lineName)
    if (!map.has(key)) {
      map.set(key, {
        key,
        prodDate: row.prodDate,
        factoryName: row.factoryName,
        workshopName: row.workshopName,
        lineName: row.lineName,
        processes: [],
        shifts: new Set()
      })
    }
    const group = map.get(key)
    group.processes.push(row)
    if (row.shift) {
      group.shifts.add(row.shift)
    }
  })
  return Array.from(map.values())
    .map((group) => {
      const shifts = Array.from(group.shifts).sort(
        (a, b) => shiftOrder.indexOf(a) - shiftOrder.indexOf(b)
      )
      return {
        ...group,
        shifts,
        shiftSummary: buildShiftSummary(shifts)
      }
    })
    .sort((a, b) => {
      const aKey = `${a.prodDate}|${a.factoryName}|${a.workshopName}|${a.lineName}`
      const bKey = `${b.prodDate}|${b.factoryName}|${b.workshopName}|${b.lineName}`
      return aKey.localeCompare(bKey)
    })
})

const processColumns = computed(() => [
  {
    title: '班别',
    key: 'shift',
    width: 70,
    render: (row) => `${formatShift(row.shift)}班`
  },
  { title: '机台号', key: 'machineNo', width: 120 },
  { title: '制程段', key: 'processName', width: 120 },
  { title: '生产料号', key: 'productCode', width: 140 },
  { title: '系列', key: 'seriesName', width: 100 },
  { title: 'CT(秒)', key: 'ct', width: 90 },
  { title: '设备数', key: 'equipmentCount', width: 90 },
  { title: '投产(分)', key: 'runMinutes', width: 100 },
  { title: '目标', key: 'targetOutput', width: 90 },
  { title: '实际', key: 'actualOutput', width: 90 },
  { title: 'GAP', key: 'gap', width: 90 },
  {
    title: '达成率',
    key: 'achievementRate',
    width: 100,
    render: (row) =>
      row.achievementRate === null || row.achievementRate === undefined
        ? '-'
        : `${row.achievementRate}%`
  },
  { title: 'Down机(分)', key: 'downMinutes', width: 110 },
  { title: 'FA', key: 'fa', width: 200 },
  { title: 'CA', key: 'ca', width: 200 }
])

const groupColumns = computed(() => [
  {
    type: 'expand',
    renderExpand: (group) => {
      return h('div', { style: 'padding: 12px 8px;' }, [
        h(NDataTable, {
          class: 'detail-table',
          columns: processColumns.value,
          data: group.processes,
          rowKey: (row) => row.id,
          size: 'small',
          singleLine: false,
          scrollX: 1600
        })
      ])
    }
  },
  {
    title: '日期',
    key: 'prodDate',
    width: 110,
    render: (row) => formatDate(row.prodDate)
  },
  {
    title: '厂区',
    key: 'factoryName',
    width: 120,
    render: (row) => row.factoryName || '-'
  },
  {
    title: '车间',
    key: 'workshopName',
    width: 120,
    render: (row) => row.workshopName || '-'
  },
  {
    title: '线别',
    key: 'lineName',
    width: 120,
    render: (row) => row.lineName || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    render: (row) =>
      h(NSpace, { size: 8 }, () => [
        ...(row.shifts || []).map((shift) =>
          h(
            NButton,
            { size: 'small', secondary: true, onClick: () => props.onEdit?.(row, shift) },
            { default: () => `编辑${formatShift(shift)}班` }
          )
        ),
        h(
          NButton,
          { size: 'small', onClick: () => props.onExport?.(row) },
          { default: () => '导出' }
        )
      ])
  },
  {
    title: '制程段数',
    key: 'processCount',
    width: 100,
    render: (row) => row.processes.length
  }
])
</script>

<template>
  <n-card title="每日产能记录" style="margin-top: 16px;" content-style="padding: 0;">
    <n-data-table
      class="summary-table"
      :columns="groupColumns"
      :data="groupRows"
      :loading="loading"
      :row-key="(row) => row.key"
      size="small"
      :single-line="false"
      :scroll-x="900"
      style="height: calc(100vh - 300px)"
      flex-height
    />
  </n-card>
</template>

<style scoped>
:deep(.summary-table .n-data-table-tbody .n-data-table-td) {
  color: #1f1f1f;
  background-color: #ffffff;
}
:deep(.detail-table .n-data-table-tbody .n-data-table-td) {
  color: #6b6b6b;
  background-color: #f6f7f9;
}
</style>
