<template>
  <div class="dashboard-container">
    <!-- ECharts 矩阵树图卡片：订单状态分布 -->
    <div class="chart-card">
      <header class="chart-header">
        <div class="header-title">
          <h3>订单状态分布</h3>
          <p>矩形树图展示动态数据加载</p>
        </div>
        <div class="controls">
          <button class="btn" @click="refresh">获取最新数据</button>
          <button class="btn btn-primary" @click="resetZoom">重置视图</button>
        </div>
      </header>
      <section class="chart-container">
        <div ref="chartRef" class="chart-dom"></div>
      </section>
      <footer class="chart-footer info-panel">
        <div class="info-item">数据来源: <span>后端 API</span></div>
        <div class="info-item">订单总数: <span>{{ totalOrders }}</span></div>
        <div class="info-item">状态种类: <span>{{ orderList.length }}</span></div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
// 引入 ECharts 核心
import * as echarts from 'echarts'
// 按任务要求使用的 API 函数（禁止改名）
import { getOrderOverview } from '@/api/admin/statistics.js'

// orderList: 保留 getOrderOverview 原始 List<OrderStatisticsVO { status, name, count }> - 矩阵树图数据源
const orderList = ref([])

// 订单总数：reduce 出所有状态 count 之和
const totalOrders = computed(() =>
  orderList.value.reduce((sum, o) => sum + (Number(o.count) || 0), 0)
)

// ===== 图表 DOM ref 与实例 =====
const chartRef = ref(null)
let chart = null

// ===== 数据获取 =====
// 拉取订单概览：保留原始 list 给矩阵树图
const fetchData = async () => {
  try {
    const res = await getOrderOverview()
    if (Array.isArray(res?.data)) orderList.value = res.data
  } catch (e) {
    console.error('获取订单概览统计失败:', e)
  }
}

// 一次性刷新（按钮触发）
const refresh = fetchData

// 重置视图（对齐矩阵.html 的"重置视图"按钮）
const resetZoom = () => {
  chart?.dispatchAction({ type: 'dataZoom', start: 0, end: 100 })
}

// ===== ECharts 渲染 =====
// 矩阵树图：订单状态分布（参考矩阵.html 风格 - treemap 多色块）
const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  // 把原始 orderList 转成 treemap 需要的 [{name, value}] 格式
  const data = orderList.value.map(o => ({
    name: o.name || `状态${o.status}`,
    value: Number(o.count) || 0
  }))

  // 空数据兜底，避免 treemap 空白
  if (data.length === 0) {
    chart.setOption({
      title: {
        text: '等待数据加载...',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 18 }
      }
    }, true)
    return
  }

  chart.setOption({
    tooltip: {
      formatter: (info) => {
        return `<div style="font-weight:bold;margin-bottom:4px">${info.name}</div>
                <div>订单数: <b>${info.value}</b></div>`
      }
    },
    series: [{
      name: '订单状态',
      type: 'treemap',
      top: 10,
      roam: false,
      nodeClick: 'link',
      breadcrumb: { show: true },
      label: {
        position: 'insideTopLeft',
        formatter: (params) => [
          `{name|${params.name}}`,
          `{hr|}`,
          `{budget|${params.value}} {label|订单}`
        ].join('\n'),
        rich: {
          budget: { fontSize: 22, lineHeight: 30, color: '#fff' },
          label: {
            fontSize: 9,
            backgroundColor: 'rgba(0,0,0,0.3)',
            color: '#fff',
            borderRadius: 2,
            padding: [2, 4],
            lineHeight: 25,
            align: 'right'
          },
          name: { fontSize: 14, color: '#fff' },
          hr: {
            width: '100%',
            borderColor: 'rgba(255,255,255,0.2)',
            borderWidth: 0.5,
            height: 0,
            lineHeight: 10
          }
        }
      },
      upperLabel: { show: true, height: 30, color: '#fff' },
      itemStyle: { borderColor: '#000', borderWidth: 1, gapWidth: 1 },
      levels: [
        {
          // 多色调色板 - 对齐矩阵.html 视觉
          color: ['#c23531', '#314656', '#61a0a8', '#dd8668', '#91c7ae', '#6e7074', '#bda29a', '#44525d', '#c4ccd3'],
          colorMappingBy: 'id',
          itemStyle: { borderWidth: 3, gapWidth: 3 }
        },
        {
          colorAlpha: [0.5, 1],
          itemStyle: { gapWidth: 1 }
        }
      ],
      data
    }]
  }, true)
}

// 监听数据变化，自动渲染
watch(orderList, () => nextTick(renderChart), { deep: true, flush: 'post' })

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
  chart?.dispose()
})
</script>

<style lang="scss" scoped>
/* 系统色板 */
$sys-blue: #0A84FF;
$sys-orange: #FF9F0A;
$sys-indigo: #5E5CE6;
$primary: $sys-blue;

.dashboard-container {
  margin: 20px;
  background: linear-gradient(135deg, rgba(10, 132, 255, 0.08) 0%, rgba(10, 132, 255, 0.15) 100%);
  min-height: calc(100vh - 100px);
  padding: 20px;
  border-radius: 12px;
}

/* ECharts 图表卡片 - 对齐矩阵.html 的卡片样式 */
.chart-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(10, 132, 255, 0.15);
  overflow: hidden;
  border-top: 4px solid $sys-orange; /* 矩阵树图卡片：多彩主题 */
}

.chart-header {
  padding: 24px 32px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }

  .header-title {
    h3 {
      font-size: 1.5rem;
      font-weight: 700;
      color: $sys-indigo;
      margin: 0 0 8px 0;
    }

    p {
      color: rgba(94, 92, 230, 0.55);
      font-size: 0.9rem;
      margin: 0;
    }
  }

  .controls {
    display: flex;
    gap: 12px;
  }
}

.chart-container {
  position: relative;
  height: 600px;
  width: 100%;
  background: #fff;

  @media (max-width: 768px) {
    height: 450px;
  }
}

.chart-dom {
  width: 100%;
  height: 100%;
}

.chart-footer {
  padding: 16px 32px;
  background: #fafafa;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 24px;

  /* 矩阵树图底部信息面板 - 对齐矩阵.html 风格 */
  &.info-panel {
    justify-content: flex-start;

    .info-item {
      font-size: 0.875rem;
      color: rgba(94, 92, 230, 0.55);

      span {
        font-weight: 600;
        color: $sys-indigo;
        margin-left: 4px;
      }
    }
  }

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 12px;
    padding: 16px 20px;
  }
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: white;
  color: $sys-indigo;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f5f7fa;
    border-color: #c0c4cc;
  }

  &.btn-primary {
    background: $primary;
    color: white;
    border-color: $primary;

    &:hover {
      background: #66b1ff;
      border-color: #66b1ff;
    }
  }
}
</style>
