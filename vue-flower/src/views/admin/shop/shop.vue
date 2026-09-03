<template>
  <div class="shop-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">店铺营业状态</h2>
      <p class="page-subtitle">管理店铺的营业/打烊状态，并导出用户数据报表</p>
    </div>

    <!-- 状态展示卡片 -->
    <div class="status-card" :class="{ open: isOpen, closed: !isOpen }">
      <div class="status-icon">
        <el-icon :size="64">
          <Shop v-if="isOpen" />
          <CircleClose v-else />
        </el-icon>
      </div>
      <div class="status-text">
        <h3>{{ statusText }}</h3>
        <p>{{ statusDesc }}</p>
      </div>
      <!-- 大字号状态标签：营业中绿 / 已打烊红 -->
      <el-tag
        :type="isOpen ? 'success' : 'danger'"
        size="large"
        class="status-tag"
        effect="dark"
      >
        {{ isOpen ? '营业中' : '已打烊' }}
      </el-tag>
    </div>

    <!-- 营业状态切换 -->
    <div class="action-area">
      <div class="action-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>{{ actionTip }}</span>
      </div>
      <!-- 使用 el-switch 切换营业 / 打烊，调 setShopStatus(1) 或 setShopStatus(0) -->
      <div class="switch-wrapper">
        <span class="switch-label" :class="{ active: !isOpen }">已打烊</span>
        <el-switch
          v-model="isOpen"
          :loading="loading"
          :before-change="handleBeforeToggle"
          active-color="#30D158"
          inactive-color="#FF453A"
          inline-prompt
          active-text="营业"
          inactive-text="打烊"
          style="--el-switch-on-color: #30D158; --el-switch-off-color: #FF453A"
        />
        <span class="switch-label" :class="{ active: isOpen }">营业中</span>
      </div>
    </div>

    <!-- 报表导出 -->
    <div class="export-section">
      <el-card class="export-card">
        <template #header>
          <div class="card-header">
            <el-icon><Download /></el-icon>
            <span>报表导出</span>
          </div>
        </template>
        <div class="export-content">
          <div class="export-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>导出用户数据为 Excel 文件，先在服务端生成文件，再触发浏览器下载</span>
          </div>
          <!-- 导出 Excel：先 writeExcel() 拿文件路径提示，再 downloadExcel() 触发 blob 下载 -->
          <el-button
            type="success"
            size="large"
            class="export-btn"
            :loading="exportLoading"
            @click="handleExportExcel"
          >
            <el-icon><Download /></el-icon>
            导出 Excel 报表
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 营业提示信息 -->
    <div class="info-section">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <el-icon><HelpFilled /></el-icon>
            <span>营业提示</span>
          </div>
        </template>
        <ul class="tip-list">
          <li>
            <el-icon><Check /></el-icon>
            <span>营业状态下，用户正常下单</span>
          </li>
          <li>
            <el-icon><Check /></el-icon>
            <span>打烊状态下，用户将无法下单</span>
          </li>
        </ul>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  CircleClose,
  InfoFilled,
  Check,
  HelpFilled,
  Download
} from '@element-plus/icons-vue'
// 按任务要求使用的 API 函数（禁止改名）
import { getShopStatus, setShopStatus } from '@/api/admin/shop.js'
import { writeExcel, downloadExcel } from '@/api/file/excel.js'

// 切换状态加载中
const loading = ref(false)
// 导出 Excel 加载中
const exportLoading = ref(false)
// 店铺状态：true 营业中，false 已打烊
const isOpen = ref(false)

// 状态文本
const statusText = computed(() => {
  return isOpen.value ? '店铺正在营业中' : '店铺当前已打烊'
})

// 状态描述
const statusDesc = computed(() => {
  return isOpen.value
    ? '顾客可以在小程序正常浏览和下单'
    : '顾客无法在小程序下单，请先开始营业'
})

// 操作提示
const actionTip = computed(() => {
  return isOpen.value
    ? '切换为打烊后顾客将无法下单，请确认是否继续？'
    : '切换为营业后顾客将可以正常下单'
})

// 获取店铺营业状态
// 后端 GET /admin/shop 返回 ShopVO { status: "营业中"/"已打烊" }
const fetchShopStatus = async () => {
  try {
    const res = await getShopStatus()
    if (res?.data) {
      // 以后端返回的字符串为准
      isOpen.value = res.data.status === '营业中'
    }
  } catch (error) {
    console.error('获取店铺状态失败:', error)
    ElMessage.error('获取店铺状态失败')
  }
}

// el-switch 切换前钩子：返回 Promise<boolean>，false 中止切换
// 调用 setShopStatus(1) 营业 / setShopStatus(0) 打烊
const handleBeforeToggle = async () => {
  const action = isOpen.value ? '打烊' : '营业'
  const newStatus = isOpen.value ? 0 : 1
  try {
    // 二次确认
    await ElMessageBox.confirm(
      `确定要${action}吗？${isOpen.value ? '打烊后顾客将无法下单。' : '营业后顾客将可以正常下单。'}`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    loading.value = true
    // 调 setShopStatus：1=营业中，其它=打烊
    await setShopStatus(newStatus)
    ElMessage.success(`${action}成功`)
    return true
  } catch (error) {
    // 用户取消或接口失败：阻止 switch 切换
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
    }
    return false
  } finally {
    loading.value = false
  }
}

// 导出 Excel
// 流程：先 writeExcel() 在服务端生成文件并拿到文件路径，再 downloadExcel() 触发浏览器 blob 下载
const handleExportExcel = async () => {
  exportLoading.value = true
  try {
    // 1. 服务端写入文件，返回文件绝对路径（JSON Result 包装）
    const writeRes = await writeExcel()
    if (writeRes?.data) {
      // 提示文件已生成（路径不暴露给用户，仅 console 用于排查）
      ElMessage.success('Excel 文件已生成，开始下载...')
      console.log('Excel 文件路径:', writeRes.data)
    }

    // 2. 触发浏览器下载（blob 方式）
    // 注意：downloadExcel 为 blob 响应，request.js 拦截器按 code===200 判断，
    //       blob 无 code 字段会被视为"非 200"并 reject，rejected value 即 Blob 本身
    try {
      await downloadExcel()
    } catch (blobOrErr) {
      if (blobOrErr instanceof Blob) {
        // 清理拦截器误弹的"操作失败"提示
        ElMessage.closeAll()
        const blob = new Blob([blobOrErr], { type: 'application/vnd.ms-excel' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        // 后端固定文件名"导出用户数据.xlsx"
        link.download = '导出用户数据.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success('Excel 报表下载完成')
      } else {
        // 非 Blob：HTTP 错误或解析异常，拦截器已提示错误
        console.error('下载失败:', blobOrErr)
      }
    }
  } catch (error) {
    // writeExcel 失败或其他未预期异常
    console.error('导出 Excel 失败:', error)
  } finally {
    exportLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchShopStatus()
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.shop-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 30px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    /* 标题文字使用系统靛蓝 */
    color: $sys-indigo;
    margin: 0 0 8px 0;
  }

  .page-subtitle {
    font-size: 14px;
    /* 副标题使用系统靛蓝半透明 */
    color: rgba(94, 92, 230, 0.55);
    margin: 0;
  }
}

/* 状态展示卡片 */
.status-card {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 50px 30px;
  border-radius: 12px;
  margin-bottom: 30px;
  transition: all 0.3s ease;

  &.open {
    /* 营业中渐变背景使用系统绿透明度 */
    background: linear-gradient(135deg, rgba(48, 209, 88, 0.15) 0%, rgba(48, 209, 88, 0.25) 100%);
    /* 边框使用系统绿 */
    border: 2px solid $sys-green;

    .status-icon {
      color: $sys-green;
    }

    .status-text h3 {
      color: $sys-green;
    }
  }

  &.closed {
    /* 打烊渐变背景使用系统红透明度 */
    background: linear-gradient(135deg, rgba(255, 69, 58, 0.12) 0%, rgba(255, 69, 58, 0.22) 100%);
    /* 边框使用系统红 */
    border: 2px solid $sys-red;

    .status-icon {
      color: $sys-red;
    }

    .status-text h3 {
      color: $sys-red;
    }
  }

  .status-icon {
    margin-bottom: 16px;
    transition: all 0.3s ease;
  }

  .status-text {
    text-align: center;
    margin-bottom: 16px;

    h3 {
      font-size: 24px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }

    p {
      font-size: 14px;
      /* 描述文字使用系统靛蓝半透明 */
      color: rgba(94, 92, 230, 0.7);
      margin: 0;
    }
  }

  .status-tag {
    /* 大字号状态标签 */
    font-size: 16px;
    padding: 12px 28px;
    height: auto;
  }
}

/* 营业状态切换区域 */
.action-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;

  .action-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 20px;
    font-size: 14px;
    /* 提示文字使用系统靛蓝半透明 */
    color: rgba(94, 92, 230, 0.7);

    .el-icon {
      /* 图标使用系统蓝 */
      color: $primary;
    }
  }

  .switch-wrapper {
    display: flex;
    align-items: center;
    gap: 16px;

    .switch-label {
      font-size: 15px;
      /* 默认文字使用系统靛蓝半透明 */
      color: rgba(94, 92, 230, 0.45);
      transition: color 0.3s;

      &.active {
        font-weight: 600;
      }

      &:nth-child(1).active {
        /* 打烊激活使用系统红 */
        color: $sys-red;
      }

      &:nth-child(3).active {
        /* 营业激活使用系统绿 */
        color: $sys-green;
      }
    }

    :deep(.el-switch) {
      /* 放大 switch 尺寸 */
      transform: scale(1.2);
    }
  }
}

/* 报表导出区域 */
.export-section {
  margin-bottom: 30px;

  .export-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      /* 卡片头部背景使用主色浅背景 */
      background: $primary-light;
      /* 底部分隔线使用系统蓝半透明 */
      border-bottom: 1px solid rgba(10, 132, 255, 0.2);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      /* 标题使用系统靛蓝 */
      color: $primary-dark;

      .el-icon {
        color: $primary;
      }
    }
  }

  .export-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    gap: 15px;

    .export-tip {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: rgba(94, 92, 230, 0.7);

      .el-icon {
        color: $primary;
      }
    }

    .export-btn {
      width: 220px;
      height: 48px;
      font-size: 15px;
      border-radius: 8px;

      &.el-button--success {
        /* 成功按钮使用系统绿 */
        background-color: $sys-green;
        border-color: $sys-green;

        &:hover {
          /* hover 使用系统绿半透明加深 */
          background-color: rgba(48, 209, 88, 0.8);
          border-color: rgba(48, 209, 88, 0.8);
        }
      }

      .el-icon {
        margin-right: 8px;
      }
    }
  }
}

/* 信息提示区域 */
.info-section {
  max-width: 600px;
  margin: 0 auto;

  .info-card {
    :deep(.el-card__header) {
      padding: 15px 20px;
      background: $primary-light;
      border-bottom: 1px solid rgba(10, 132, 255, 0.2);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 500;
      color: $primary-dark;

      .el-icon {
        color: $primary;
      }
    }
  }

  .tip-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 0;
      /* 底部分隔线使用系统蓝半透明 */
      border-bottom: 1px solid rgba(10, 132, 255, 0.1);
      font-size: 14px;
      /* 列表文字使用系统靛蓝半透明 */
      color: rgba(94, 92, 230, 0.7);

      &:last-child {
        border-bottom: none;
      }

      .el-icon {
        color: $primary;
        font-size: 16px;
      }
    }
  }
}
</style>
