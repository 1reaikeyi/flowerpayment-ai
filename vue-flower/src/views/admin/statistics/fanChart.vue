<template>
  <!-- 必须给容器设置宽高 -->
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

const chartRef = ref(null);
let chartInstance = null;

// 你的配置项（直接粘贴进来即可）
const option = {
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: '5%',
    left: 'center'
  },
  series: [
    {
      name: 'Access From',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      padAngle: 5,
      itemStyle: {
        borderRadius: 10
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 40,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 1048, name: 'Search Engine' },
        { value: 735, name: 'Direct' },
        { value: 580, name: 'Email' },
        { value: 484, name: 'Union Ads' },
        { value: 300, name: 'Video Ads' }
      ]
    }
  ]
};

onMounted(() => {
  if (chartRef.value) {
    // 初始化图表
    chartInstance = echarts.init(chartRef.value);
    // 设置配置项
    chartInstance.setOption(option);

    // 监听窗口大小变化，自适应
    window.addEventListener('resize', handleResize);
  }
});

const handleResize = () => {
  chartInstance?.resize();
};

onUnmounted(() => {
  if (chartInstance) {
    window.removeEventListener('resize', handleResize);
    // 销毁实例，防止内存泄漏
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px; /* 可以根据需要调整 */
}
</style>