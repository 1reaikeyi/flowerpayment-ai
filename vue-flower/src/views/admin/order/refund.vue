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
      <el-table-column label="配送方式" min-width="100">
        <template #default="{ row }">
          <!-- deliveryType 后端序列化为枚举名（NOW/BOOK_TIME），归一化后展示 -->
          <el-tag :type="normalizeDeliveryType(row.deliveryType) === 1 ? 'warning' : 'info'" effect="plain">
            {{ normalizeDeliveryType(row.deliveryType) === 1 ? '立即送出' : '预约配送' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注/贺卡文案" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.remark || '-' }}
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
      :page-sizes="[10, 20]"
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

// 后端 deliveryType 字段是 DeliveryStatusEnum 枚举（NOW=1 立即送出, BOOK_TIME=0 预约配送），
// Jackson 默认序列化为枚举名，需归一化为数字 code
const deliveryTypeEnumNameToCode = {
  NOW: 1,
  BOOK_TIME: 0
}
const normalizeDeliveryType = (val) => {
  if (val == null) return null
  if (typeof val === 'number') return val
  if (typeof val === 'string') {
    if (deliveryTypeEnumNameToCode[val] != null) return deliveryTypeEnumNameToCode[val]
    const num = Number(val)
    return Number.isNaN(num) ? null : num
  }
  if (typeof val === 'object') {
    // DeliveryStatusEnum 的 @EnumValue 字段名是 value
    return val.value != null ? Number(val.value) : (val.code != null ? Number(val.code) : null)
  }
  return null
}

// 分页查询已取消订单（status=8）
// 注意：分页接口仅返回订单主表字段，不含 flowerOrderDetailList，金额请进入详情查看
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
