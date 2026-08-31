<template>
  <div class="dashboard-container">
    <!-- ECharts 柱状图卡片：套量排行 -->
    <div class="chart-card">
      <header class="chart-header">
        <div class="header-title">
          <h3>销量排行</h3>
          <p>Axios 动态数据加载</p>
        </div>
        <div class="data-status">{{ dataStatus }}</div>
      </header>
      <section class="chart-container">
        <div ref="chartRef" class="chart-dom"></div>
      </section>
      <footer class="chart-footer">
        <button class="btn" @click="refresh">获取最新数据</button>
        <button class="btn btn-primary" @click="toggleAutoUpdate">
          <span>{{ autoUpdate ? '停止自动更新' : '开启自动更新' }}</span>
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
// 引入 ECharts 核心
import * as echarts from 'echarts'
// 按任务要求使用的 API 函数（禁止改名）
import { getSetmealScore } from '@/api/admin/statistics.js'

const setmealScore = ref([])

// ===== 图表 DOM ref 与实例 =====
const chartRef = ref(null)
let chart = null

// ===== 自动更新相关 =====
let autoUpdateTimer = null
const autoUpdate = ref(false)
const updateInterval = 5000
const dataStatus = ref('等待数据...')

// ===== 数据获取 =====
const fetchData = async () => {
  dataStatus.value = '正在加载数据...'
  try {
    const res = await getSetmealScore()
    if (Array.isArray(res?.data)) setmealScore.value = res.data
    dataStatus.value = `数据更新于: ${new Date().toLocaleTimeString()}`
  } catch (e) {
    console.error('获取套餐销量统计失败:', e)
    dataStatus.value = '数据加载失败，请稍后重试'
  }
}

// 一次性刷新（按钮触发）
const refresh = fetchData

// 自动更新切换
const toggleAutoUpdate = () => {
  if (autoUpdateTimer) {
    clearInterval(autoUpdateTimer)
    autoUpdateTimer = null
    autoUpdate.value = false
    dataStatus.value = '自动更新已停止'
  } else {
    autoUpdateTimer = setInterval(refresh, updateInterval)
    autoUpdate.value = true
    refresh()
  }
}

// ===== ECharts 渲染 =====
const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const names = setmealScore.value.map(p => p.name || '未知套餐')
  const numbers = setmealScore.value.map(p => Number(p.number) || 0)

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names,
      axisTick: { alignWithLabel: true },
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: { color: '#666', rotate: names.length > 6 ? 30 : 0, interval: 0 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [{
      name: '销量',
      type: 'bar',
      barWidth: '60%',
      data: numbers,
      // 圆角柱 + 蓝色 - 对齐柱形图.html 视觉
      itemStyle: { color: '#5470c6', borderRadius: [4, 4, 0, 0] },
      showBackground: true,
      backgroundStyle: { color: 'rgba(180, 180, 180, 0.1)' }
    }],
    animationDuration: 1000,
    animationEasing: 'cubicOut'
  }, true)
}

// 监听数据变化，自动渲染
watch(setmealScore, () => nextTick(renderChart), { deep: true, flush: 'post' })

// ===== 窗口自适应 =====
const handleResize = () => chart?.resize()

// ===== 生命周期 =====
onMounted(async () => {
  await nextTick()
  if (chartRef.value) chart = echarts.init(chartRef.value)
  window.addEventListener('resize', handleResize)
  await fetchData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (autoUpdateTimer) clearInterval(autoUpdateTimer)
  chart?.dispose()
})
</script>

<style lang="scss" scoped>
/* 系统色板 */
$sys-blue: #0A84FF;
$sys-indigo: #5E5CE6;
$primary: $sys-blue;

.dashboard-container {
  margin: 20px;
  background: linear-gradient(135deg, rgba(10, 132, 255, 0.08) 0%, rgba(10, 132, 255, 0.15) 100%);
  min-height: calc(100vh - 100px);
  padding: 20px;
  border-radius: 12px;
}

/* ECharts 图表卡片 - 对齐柱形图.html 的卡片样式 */
.chart-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  overflow: hidden;
  border-top: 4px solid #5470c6; /* 柱状图卡片：蓝色系主题 */
}

.chart-header {
  padding: 24px 32px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;

  @media (max-width: 600px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 16px;
  }

  .header-title {
    h3 {
      font-size: 1.25rem;
      font-weight: 700;
      color: $sys-indigo;
      margin: 0 0 4px 0;
    }

    p {
      color: rgba(94, 92, 230, 0.55);
      font-size: 0.875rem;
      margin: 0;
    }
  }

  .data-status {
    font-size: 0.875rem;
    color: #999;
  }
}

.chart-container {
  position: relative;
  height: 400px;
  width: 100%;
  padding: 20px;
  background-color: #fff;

  @media (max-width: 600px) {
    height: 300px;
    padding: 10px;
  }
}

.chart-dom {
  width: 100%;
  height: 100%;
}

.chart-footer {
  padding: 16px 32px;
  background-color: #f9fafb;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  @media (max-width: 600px) {
    flex-direction: column;
    padding: 16px;
  }
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: white;
  color: $sys-indigo;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: #f3f4f6;
    border-color: #9ca3af;
  }

  &.btn-primary {
    background: $primary;
    color: white;
    border: none;

    &:hover {
      background: #5a67d8;
      transform: translateY(-1px);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }
  }

  @media (max-width: 600px) {
    width: 100%;
  }
}
</style>
