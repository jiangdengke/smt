<script setup>
import { computed } from 'vue'

const weeklyTrend = [
  { label: '一', value: 12 },
  { label: '二', value: 18 },
  { label: '三', value: 9 },
  { label: '四', value: 22 },
  { label: '五', value: 15 },
  { label: '六', value: 6 },
  { label: '日', value: 10 }
]

const typeBreakdown = [
  { label: '送料异常', value: 32 },
  { label: '漏贴', value: 24 },
  { label: '抛料', value: 18 },
  { label: '润滑不足', value: 12 }
]

const weeklyMax = computed(() => Math.max(...weeklyTrend.map((item) => item.value), 1))
const typeMax = computed(() => Math.max(...typeBreakdown.map((item) => item.value), 1))
</script>

<template>
  <div class="analytics">
    <n-grid x-gap="12" :cols="4" style="margin-bottom: 24px">
      <n-grid-item>
        <n-card>
          <n-statistic label="本周异常" value="92">
            <template #suffix><span style="font-size: 12px; color: green">↑ 12%</span></template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="平均修复时长" value="46">
            <template #suffix>分</template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="未修复数" value="6">
              <template #suffix><span style="font-size: 12px; color: red">需提升</span></template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card>
          <n-statistic label="责任人覆盖" value="98">
            <template #suffix>%</template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-grid x-gap="12" :cols="2">
      <n-grid-item>
        <n-card title="周趋势 (异常件数)">
          <div class="bar-chart" style="height: 200px; display: flex; align-items: flex-end; justify-content: space-around;">
            <div v-for="item in weeklyTrend" :key="item.label" style="display: flex; flex-direction: column; align-items: center; width: 100%;">
              <div
                style="background-color: #18a058; width: 20px; border-radius: 4px 4px 0 0; transition: height 0.3s;"
                :style="{ height: `${(item.value / weeklyMax) * 100}%` }"
              />
              <span style="margin-top: 8px; font-size: 12px; color: gray;">{{ item.label }}</span>
            </div>
          </div>
        </n-card>
      </n-grid-item>

      <n-grid-item>
        <n-card title="异常分类分布 (Top 4)">
          <div class="list-chart">
            <div v-for="item in typeBreakdown" :key="item.label" style="margin-bottom: 12px;">
              <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px;">
                <span>{{ item.label }}</span>
                <span>{{ item.value }} 次</span>
              </div>
              <div style="background-color: #f0f0f0; height: 8px; border-radius: 4px; overflow: hidden;">
                <div
                  style="background-color: #2080f0; height: 100%;"
                  :style="{ width: `${(item.value / typeMax) * 100}%` }"
                />
              </div>
            </div>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>
