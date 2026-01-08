<script setup>
import { ref, onMounted, computed } from 'vue'
import { useMessage, NCard, NDataTable, NButton, NSpace } from 'naive-ui'
import { getProductionDailyRecords, exportProductionDailyRecords } from '../../api/production'
import { formatDate, formatShift } from '../../utils/format'

const message = useMessage()
const loading = ref(false)
const rows = ref([])
const checkedRowKeys = ref([])
const selectionBase = ref(null)

const rowMap = computed(() => {
  const map = new Map()
  rows.value.forEach((row) => map.set(row.id, row))
  return map
})

const columns = computed(() => [
  { type: 'selection', multiple: true },
  {
    title: '日期',
    key: 'prodDate',
    width: 110,
    render: (row) => formatDate(row.prodDate)
  },
  {
    title: '班别',
    key: 'shift',
    width: 70,
    render: (row) => `${formatShift(row.shift)}班`
  },
  { title: '厂区', key: 'factoryName', width: 120 },
  { title: '车间', key: 'workshopName', width: 120 },
  { title: '线别', key: 'lineName', width: 120 },
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

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getProductionDailyRecords()
    rows.value = res || []
    checkedRowKeys.value = []
    selectionBase.value = null
  } catch (error) {
    message.error(error.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

const buildBaseKey = (row) => {
  if (!row) return null
  return {
    prodDate: formatDate(row.prodDate),
    factoryName: row.factoryName || '',
    workshopName: row.workshopName || '',
    lineName: row.lineName || ''
  }
}

const isSameBase = (row, base) => {
  if (!row || !base) return false
  return (
    formatDate(row.prodDate) === base.prodDate &&
    (row.factoryName || '') === base.factoryName &&
    (row.workshopName || '') === base.workshopName &&
    (row.lineName || '') === base.lineName
  )
}

const handleSelectionChange = (keys) => {
  if (!keys.length) {
    checkedRowKeys.value = []
    selectionBase.value = null
    return
  }
  const currentMap = rowMap.value
  const base = selectionBase.value || buildBaseKey(currentMap.get(keys[0]))
  const filtered = keys.filter((key) => isSameBase(currentMap.get(key), base))
  if (filtered.length !== keys.length) {
    message.warning('只能选择同一天、同厂区、同车间、同线别的记录导出')
  }
  checkedRowKeys.value = filtered
  selectionBase.value = filtered.length ? buildBaseKey(currentMap.get(filtered[0])) : null
}

const handleExportSelected = async () => {
  if (!checkedRowKeys.value.length) {
    message.warning('请先选择要导出的记录')
    return
  }
  try {
    await exportProductionDailyRecords(checkedRowKeys.value)
    message.success('导出成功')
  } catch (error) {
    message.error(error.message || '导出失败')
  }
}

onMounted(loadRecords)
</script>

<template>
  <div class="production-records">
    <n-card title="生产记录">
      <template #header-extra>
        <n-space>
          <n-button secondary @click="handleExportSelected">导出所选</n-button>
        </n-space>
      </template>
      <n-card content-style="padding: 0;">
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :row-key="(row) => row.id"
        :checked-row-keys="checkedRowKeys"
        @update:checked-row-keys="handleSelectionChange"
        size="small"
        :single-line="false"
        :scroll-x="2200"
        style="height: calc(100vh - 180px)"
        flex-height
      />
      </n-card>
    </n-card>
  </div>
</template>
