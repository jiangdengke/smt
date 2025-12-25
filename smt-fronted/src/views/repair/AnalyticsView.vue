<script setup>
import { ref, computed, onMounted, watch, provide } from 'vue'
import { useMessage } from 'naive-ui'
import { getRepairRecords } from '../../api/repair'
import { toDateTime, formatDate } from '../../utils/format'

// ECharts imports
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart, { THEME_KEY } from 'vue-echarts'

use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

provide(THEME_KEY, 'light')

const message = useMessage()

const dateRange = ref(getDefaultRange())
const loading = ref(false)
const records = ref([])

// --- Chart Options ---

const trendOption = computed(() => {
  const [start, end] = dateRange.value || []
  const dates = buildDateRange(start, end)
  const counts = new Map(dates.map((item) => [item, 0]))
  for (const record of records.value) {
    const key = formatDate(record.occurAt)
    if (counts.has(key)) counts.set(key, counts.get(key) + 1)
  }
  const data = dates.map((date) => counts.get(date) || 0)
  const labels = dates.map(date => date.slice(5)) // MM-DD

  return {
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    grid: { top: 30, right: 30, bottom: 20, left: 40, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: labels,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666' }
    },
    yAxis: { 
      type: 'value', 
      minInterval: 1,
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [
      {
        name: '维修单量',
        data: data,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#18a058', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24, 160, 88, 0.4)' },
              { offset: 1, color: 'rgba(24, 160, 88, 0.05)' }
            ]
          }
        }
      }
    ]
  }
})

const abnormalTypeOption = computed(() => {
  const counter = new Map()
  for (const record of records.value) {
    const key = record.abnormalTypeName || '未分类'
    counter.set(key, (counter.get(key) || 0) + 1)
  }
  const data = Array.from(counter.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)
    .slice(0, 8)

  return {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', type: 'scroll' },
    series: [
      {
        name: '异常类型',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['55%', '50%'],
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          scale: true,
          scaleSize: 10,
          label: { show: true, fontSize: 14, fontWeight: 'bold', color: '#333' }
        },
        data: data
      }
    ]
  }
})

const lineTopOption = computed(() => {
  const counter = new Map()
  for (const record of records.value) {
    const key = record.lineName || '未知线别'
    counter.set(key, (counter.get(key) || 0) + 1)
  }
  const sorted = Array.from(counter.entries())
    .map(([label, value]) => ({ label, value }))
    .sort((a, b) => b.value - a.value) // Descending for vertical bar
    .slice(0, 10)

  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 30, right: 20, bottom: 20, left: 20, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: sorted.map(i => i.label),
      axisLabel: { interval: 0, rotate: 30, color: '#666' },
      axisTick: { alignWithLabel: true }
    },
    yAxis: { 
      type: 'value', 
      minInterval: 1,
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [
      {
        name: '维修次数',
        type: 'bar',
        barWidth: '40%',
        data: sorted.map(i => i.value),
        itemStyle: { 
          borderRadius: [4, 4, 0, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#f5a524' },
              { offset: 1, color: '#f9cb89' }
            ]
          }
        }
      }
    ]
  }
})

const personAvgOption = computed(() => {
  const totals = new Map()
  for (const record of records.value) {
    if (!record.isFixed || record.repairMinutes == null) continue
    const names = record.repairPersonNames || []
    for (const name of names) {
      if (!name) continue
      const current = totals.get(name) || { total: 0, count: 0 }
      current.total += Number(record.repairMinutes) || 0
      current.count += 1
      totals.set(name, current)
    }
  }
  const sorted = Array.from(totals.entries())
    .map(([label, value]) => ({
      label,
      avgMinutes: value.count > 0 ? Math.round(value.total / value.count) : 0
    }))
    .sort((a, b) => b.avgMinutes - a.avgMinutes) // Descending
    .slice(0, 10)

  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 30, right: 20, bottom: 20, left: 20, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: sorted.map(i => i.label),
      axisLabel: { interval: 0, rotate: 0, color: '#666' }
    },
    yAxis: { 
      type: 'value', 
      name: '分钟',
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [
      {
        name: '平均耗时',
        type: 'bar',
        barWidth: '40%',
        data: sorted.map(i => i.avgMinutes),
        itemStyle: { 
          borderRadius: [4, 4, 0, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#7033ff' },
              { offset: 1, color: '#b48aff' }
            ]
          }
        }
      }
    ]
  }
})

// --- Data Loading ---

const loadRecords = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  const [start, end] = dateRange.value
  if (!start || !end) return
  loading.value = true
  try {
    const all = []
    let page = 1
    const size = 200
    while (true) {
      const res = await getRepairRecords({
        page,
        size,
        occurFrom: toDateTime(start),
        occurTo: toDateTime(end, true)
      })
      const data = res?.data || []
      all.push(...data)
      if (data.length < size) break
      page += 1
      if (page > 50) break
    }
    records.value = all
  } catch (err) {
    message.error(err.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

watch(dateRange, loadRecords, { deep: true })
onMounted(loadRecords)

function getDefaultRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 14) // 2 weeks default
  return [formatDateKey(start), formatDateKey(end)]
}

function formatDateKey(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseDate(text) {
  if (!text) return null
  const [year, month, day] = text.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function buildDateRange(start, end) {
  if (!start || !end) return []
  const startDate = parseDate(start)
  const endDate = parseDate(end)
  if (!startDate || !endDate || startDate > endDate) return []
  const dates = []
  const cursor = new Date(startDate)
  while (cursor <= endDate) {
    dates.push(formatDateKey(cursor))
    cursor.setDate(cursor.getDate() + 1)
  }
  return dates
}
</script>

<template>
  <div class="analytics">
    <n-page-header title="数据统计" subtitle="维修趋势与异常分析" style="margin-bottom: 16px">
      <template #extra>
        <n-date-picker
          v-model:formatted-value="dateRange"
          value-format="yyyy-MM-dd"
          type="daterange"
          clearable
        />
      </template>
    </n-page-header>

    <n-spin :show="loading">
      <n-grid x-gap="12" y-gap="12" :cols="2">
        <!-- Row 1: Trend & Pie -->
        <n-grid-item>
          <n-card title="维修单量趋势" content-style="padding: 10px;">
            <div style="height: 300px">
              <v-chart class="chart" :option="trendOption" autoresize />
            </div>
          </n-card>
        </n-grid-item>
        
        <n-grid-item>
          <n-card title="异常类型分布" content-style="padding: 10px;">
             <div style="height: 300px">
              <v-chart class="chart" :option="abnormalTypeOption" autoresize />
            </div>
          </n-card>
        </n-grid-item>

        <!-- Row 2: Bars -->
        <n-grid-item>
          <n-card title="产线故障排行" content-style="padding: 10px;">
             <div style="height: 300px">
              <v-chart class="chart" :option="lineTopOption" autoresize />
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card title="维修人平均耗时排行" content-style="padding: 10px;">
             <div style="height: 300px">
              <v-chart class="chart" :option="personAvgOption" autoresize />
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-spin>
  </div>
</template>

<style scoped>
.chart {
  height: 100%;
  width: 100%;
}
</style>
