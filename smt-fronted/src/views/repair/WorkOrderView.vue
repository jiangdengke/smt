<script setup>
import { ref, onMounted, h, watch } from 'vue'
import { useMessage, NButton, NTag, NSelect, NCard, NDataTable, NModal, NForm, NFormItem, NInput } from 'naive-ui'
import { getWorkOrders, completeWorkOrder } from '../../api/workOrder'
import { formatDate, formatShift } from '../../utils/format'

const message = useMessage()

// State
const workOrders = ref([])
const loading = ref(false)
const statusFilter = ref('OPEN') // Default to OPEN for actionable items
const modalOpen = ref(false)
const saving = ref(false)
const form = ref({
  id: null,
  ca: ''
})

const columns = [
  {
    title: '日期/班别',
    key: 'prodDate',
    render(row) {
      return `${formatDate(row.prodDate)} · ${formatShift(row.shift)}班`
    }
  },
  {
    title: '制程段',
    key: 'process',
    render(row) {
      return `${row.processName || '-'}`
    }
  },
  {
    title: '料号/系列',
    key: 'product',
    render(row) {
      return `${row.productCode || '-'} / ${row.seriesName || '-'}`
    }
  },
  {
    title: '异常描述 (FA)',
    key: 'fa',
    render(row) {
      return row.fa || '-'
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row) {
      const status = row.status || ''
      const type = status === 'DONE' ? 'success' : status === 'IN_PROGRESS' ? 'warning' : 'error'
      const label = status === 'DONE' ? '已完成' : status === 'IN_PROGRESS' ? '处理中' : '待处理'
      return h(NTag, { type, bordered: false }, { default: () => label })
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row) {
      if (row.status === 'DONE') return null
      return h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          onClick: () => openModal(row)
        },
        { default: () => '回填对策' }
      )
    }
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWorkOrders({ status: statusFilter.value })
    workOrders.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (err) {
    message.error(err.message || '加载工单失败')
  } finally {
    loading.value = false
  }
}

const openModal = (row) => {
  form.value = {
    id: row.id,
    ca: ''
  }
  modalOpen.value = true
}

const handleSubmit = async () => {
  if (!form.value.ca || !form.value.ca.trim()) {
    message.warning('请输入解决对策')
    return
  }
  saving.value = true
  try {
    await completeWorkOrder(form.value.id, { ca: form.value.ca.trim() })
    message.success('回填成功')
    modalOpen.value = false
    loadData()
  } catch (err) {
    message.error(err.message || '回填失败')
  } finally {
    saving.value = false
  }
}

watch(statusFilter, loadData)

onMounted(loadData)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <h2 style="margin: 0; font-size: 18px; font-weight: 500;">维修工单处理</h2>
      <div style="display: flex; gap: 12px;">
        <n-select
          v-model:value="statusFilter"
          placeholder="状态筛选"
          clearable
          style="width: 150px"
          :options="[
            { label: '待处理', value: 'OPEN' },
            { label: '处理中', value: 'IN_PROGRESS' },
            { label: '已完成', value: 'DONE' }
          ]"
        />
        <n-button @click="loadData" :loading="loading">刷新</n-button>
      </div>
    </div>

    <n-card content-style="padding: 0;">
      <n-data-table
        :columns="columns"
        :data="workOrders"
        :loading="loading"
        :pagination="{ pageSize: 15 }"
        :bordered="false"
      />
    </n-card>

    <n-modal v-model:show="modalOpen" preset="card" title="回填解决对策" style="width: 520px">
      <n-form label-placement="top">
        <n-form-item label="解决对策 (CA)">
          <n-input
            v-model:value="form.ca"
            type="textarea"
            placeholder="请详细描述维修过程和解决措施..."
            :autosize="{ minRows: 4, maxRows: 8 }"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="modalOpen = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSubmit">确认回填并结单</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
