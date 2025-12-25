<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getRepairRecords } from '../../api/repair'
import { toDateTime, formatDate, formatShift } from '../../utils/format'
import { useMessage } from 'naive-ui'

const message = useMessage()

const reportDate = ref(new Date().toISOString().slice(0, 10))
const records = ref([])
const total = ref(0)
const loading = ref(false)

const reportSummary = computed(() => {
  const t = records.value.length
  const fixed = records.value.filter((item) => item.isFixed).length
  const pending = t - fixed
  return { total: t, fixed, pending }
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '时间', key: 'occurAt', render: (r) => `${formatDate(r.occurAt)} · ${formatShift(r.shift)}班` },
  { title: '位置', key: 'loc', render: (r) => `${r.factoryName || '-'} / ${r.workshopName || '-'} / ${r.lineName || '-'}` },
  { title: '机台', key: 'mac', render: (r) => `${r.modelName || '-'} ${r.machineNo || '-'}` },
  { title: '异常', key: 'abn', render: (r) => `${r.abnormalCategoryName || '-'}-${r.abnormalTypeName || '-'}: ${r.abnormalDesc || '-'}` },
  { title: '状态', key: 'st', render: (r) => r.isFixed ? '已修复' : '待处理' }
]

const loadReportRecords = async () => {
  loading.value = true
  try {
    const query = {
      occurFrom: toDateTime(reportDate.value),
      occurTo: toDateTime(reportDate.value, true),
      page: 1,
      size: 100 // Report usually shows all or many
    }
    const res = await getRepairRecords(query)
    records.value = res?.data || []
    total.value = res?.total || 0
  } catch (err) {
    message.error(err.message || '加载日报失败')
  } finally {
    loading.value = false
  }
}

watch(reportDate, loadReportRecords)
onMounted(loadReportRecords)
</script>

<template>
  <div>
    <n-page-header title="每日报表" subtitle="产线异常与维修闭环日报" style="margin-bottom: 24px">
      <template #extra>
        <n-date-picker v-model:formatted-value="reportDate" value-format="yyyy-MM-dd" type="date" />
      </template>
    </n-page-header>

    <n-grid x-gap="12" :cols="3" style="margin-bottom: 24px">
      <n-grid-item>
        <n-card>
          <n-statistic label="当日异常" :value="reportSummary.total" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="已修复" :value="reportSummary.fixed" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="待处理" :value="reportSummary.pending">
            <template #suffix>
              <span style="font-size: 12px; color: orange" v-if="reportSummary.pending > 0">需关注</span>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="日报明细">
      <n-data-table
        :columns="columns"
        :data="records"
        :loading="loading"
      />
    </n-card>
  </div>
</template>
