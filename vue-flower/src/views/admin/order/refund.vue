<template>
  <div class="refund-container">
    <!-- 顶部工具栏：刷新按钮 -->
    <div class="toolbar">
      <span class="toolbar-title">退款订单列表（状态：已取消）</span>
      <el-button type="primary" @click="fetchList">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 退款订单列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      stripe
      class="refund-table"
      :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
    >
      <el-table-column prop="id" label="订单号" width="80" />
      <el-table-column prop="userName" label="下单用户" min-width="100" />
      <el-table-column prop="consignee" label="收花人" min-width="100" />
      <el-table-column prop="phone" label="收花人手机号" min-width="130" />
      <el-table-column label="配送地址" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.address || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="备注/贺卡文案" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.remark || '-' }}
        </template>
      </el-table-column>
      <!-- 订单金额：累加 flowerOrderDetailList.amount -->
      <el-table-column label="订单金额" min-width="110">
        <template #default="{ row }">
          <span class="price">￥{{ computeOrderAmount(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款状态" min-width="100">
        <template #default>
          <el-tag type="danger">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="fetchList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { pageOrderList } from '@/api/admin/order.js'

const router = useRouter()

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 计算订单金额：累加 flowerOrderDetailList 各明细的 amount
const computeOrderAmount = (row) => {
  const list = row.flowerOrderDetailList || []
  const sum = list.reduce((acc, item) => acc + Number(item.amount || 0), 0)
  return sum.toFixed(2)
}

// 分页查询已取消订单（status=8）
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: 8
    }
    const res = await pageOrderList(params)
    if (res?.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('获取退款订单列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  pagination.page = 1
  fetchList()
}

// 跳转订单详情
const handleDetail = (row) => {
  router.push({ path: '/admin/order/detail', query: { id: row.id } })
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.refund-container {
  padding: 20px;
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: $primary-light;
  border-radius: 4px;

  .toolbar-title {
    font-size: 15px;
    font-weight: 500;
    color: $sys-indigo;
  }
}

.refund-table {
  width: 100%;
  margin-bottom: 20px;

  .price {
    color: $sys-red;
    font-weight: 500;
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;

  :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: $primary;
  }

  :deep(.el-pagination.is-background .btn-prev:hover),
  :deep(.el-pagination.is-background .btn-next:hover) {
    color: $primary;
  }
}
</style>
