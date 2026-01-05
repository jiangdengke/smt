<script setup>
import { ref, onMounted } from 'vue'
import { useMessage, NCard, NDataTable } from 'naive-ui'
import { getProductionDailyRecords } from '../../api/production'
import { formatDate, formatShift } from '../../utils/format'

const message = useMessage()
const loading = ref(false)
const rows = ref([])

const columns = [
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
]

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getProductionDailyRecords()
    rows.value = res || []
  } catch (error) {
    message.error(error.message || '加载记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadRecords)
</script>

<template>
  <div class="production-records">
    <n-card title="生产记录" content-style="padding: 0;">
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        size="small"
        :single-line="false"
        :scroll-x="2200"
        style="height: calc(100vh - 180px)"
        flex-height
      />
    </n-card>
  </div>
</template>
