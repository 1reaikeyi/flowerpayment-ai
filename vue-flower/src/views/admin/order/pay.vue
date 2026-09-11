<template>
  <div class="order-container">
    <!-- 顶部工具栏：按订单状态筛选 -->
    <div class="toolbar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      stripe
      class="order-table"
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
          <!-- deliveryType 也是枚举（DeliveryStatusEnum: NOW=1, BOOK_TIME=0），同样需归一化 -->
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
      <el-table-column label="订单状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <!-- 状态流转按钮：仅对当前状态显示对应的下一步流转操作 -->
          <!-- row.status 可能是枚举名字符串/数字/对象，统一用 normalizeStatus 归一化后再比较 -->
          <el-button
            v-if="normalizeStatus(row.status) === 3"
            type="primary"
            link
            size="small"
            :loading="actionLoadingId === row.id"
            @click="handleWorkflow(row, 'go')"
          >
            取货
          </el-button>
          <el-button
            v-if="normalizeStatus(row.status) === 4"
            type="primary"
            link
            size="small"
            :loading="actionLoadingId === row.id"
            @click="handleWorkflow(row, 'delivering')"
          >
            开始配送
          </el-button>
          <el-button
            v-if="normalizeStatus(row.status) === 5"
            type="primary"
            link
            size="small"
            :loading="actionLoadingId === row.id"
            @click="handleWorkflow(row, 'arrived')"
          >
            确认到达
          </el-button>
          <el-button
            v-if="normalizeStatus(row.status) === 6"
            type="success"
            link
            size="small"
            :loading="actionLoadingId === row.id"
            @click="handleWorkflow(row, 'complete')"
          >
            确认完成
          </el-button>
          <!-- 查看详情：所有状态均可见 -->
          <el-button type="primary" link size="small" @click="handleDetail(row)">
            详情
          </el-button>
          <!-- 取消订单：仅未完成（status<7）且未取消（status!=8）的订单可取消，会触发支付宝退款 -->
          <el-popconfirm
            v-if="normalizeStatus(row.status) < 7 && normalizeStatus(row.status) !== 8"
            title="确认取消该订单吗？将触发支付宝退款"
            width="240"
            @confirm="handleCancel(row)"
          >
            <template #reference>
              <el-button
                type="danger"
                link
                size="small"
                :loading="cancelLoadingId === row.id"
              >
                取消
              </el-button>
            </template>
          </el-popconfirm>
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
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  pageOrderList,
  updateOrderToGo,
  updateOrderToDelivering,
  updateOrderToArrived,
  updateOrderToComplete,
  cancelOrder
} from '@/api/admin/order.js'

const router = useRouter()

// 订单状态枚举：1-用户下单, 2-已支付, 3-商家制作, 4-工作人员取货, 5-配送中, 6-已到达, 7-已完成, 8-已取消
const statusOptions = [
  { value: 1, label: '用户下单' },
  { value: 2, label: '已支付' },
  { value: 3, label: '商家制作' },
  { value: 4, label: '工作人员取货' },
  { value: 5, label: '配送中' },
  { value: 6, label: '已到达' },
  { value: 7, label: '已完成' },
  { value: 8, label: '已取消' }
]

// 后端 status 字段是 OrderStatusEnum 枚举，JacksonConfig 未做自定义序列化，
// 默认序列化为枚举名（字符串 "ORDER"/"PAYMENT"...），不是数字 code。
// 这里同时兼容三种可能格式：字符串枚举名 / 数字 code / 对象 {code, text}
const statusEnumNameToCode = {
  ORDER: 1,
  PAYMENT: 2,
  COOKING: 3,
  GO: 4,
  DELIVERING: 5,
  ARRIVED: 6,
  COMPLETED: 7,
  CANCELLED: 8
}

// 状态码 → 文案
const statusTextMap = {
  1: '用户下单',
  2: '已支付',
  3: '商家制作',
  4: '工作人员取货',
  5: '配送中',
  6: '已到达',
  7: '已完成',
  8: '已取消'
}

// 状态码 → el-tag type
const statusTagTypeMap = {
  1: 'info',
  2: 'warning',
  3: 'primary',
  4: 'primary',
  5: 'primary',
  6: 'success',
  7: 'success',
  8: 'danger'
}

// 将任意格式的 status 归一化为数字 code（1~8）
const normalizeStatus = (status) => {
  if (status == null) return null
  if (typeof status === 'number') return status
  if (typeof status === 'string') {
    // 枚举名 → code
    if (statusEnumNameToCode[status] != null) return statusEnumNameToCode[status]
    // 数字字符串 → number
    const num = Number(status)
    return Number.isNaN(num) ? null : num
  }
  if (typeof status === 'object') {
    return status.code != null ? Number(status.code) : null
  }
  return null
}

const getStatusText = (status) => {
  const code = normalizeStatus(status)
  return (code != null && statusTextMap[code]) || '未知'
}

const getStatusTagType = (status) => {
  const code = normalizeStatus(status)
  return (code != null && statusTagTypeMap[code]) || 'info'
}

// 后端 deliveryType 字段是 DeliveryStatusEnum 枚举（NOW=1 立即送出, BOOK_TIME=0 预约配送），
// 同样默认序列化为枚举名，需归一化为数字 code
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

// 搜索表单 - 后端 FlowerOrderPageDTO 支持 status 筛选
// status 为空时不传该参数，查看所有状态的订单（主要情况）
const searchForm = reactive({
  status: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 行级按钮 loading：状态流转 / 取消
const actionLoadingId = ref(null)
const cancelLoadingId = ref(null)

// 分页查询订单列表
// 后端返回 Result<PageResult<FlowerOrderVO>>：{ code, data: { total, list, pageNum, pageSize } }
const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    // status 为空时不传，走后端默认值（3 商家制作）
    if (searchForm.status != null && searchForm.status !== '') {
      params.status = searchForm.status
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
    console.error('获取订单列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

const handleSizeChange = () => {
  pagination.page = 1
  fetchList()
}

// 跳转订单详情
const handleDetail = (row) => {
  router.push({ path: '/admin/order/detail', query: { id: row.id } })
}

// 状态流转：根据 action 调用对应接口
// action: 'go' | 'delivering' | 'arrived' | 'complete'
const handleWorkflow = async (row, action) => {
  actionLoadingId.value = row.id
  const actionTextMap = {
    go: '取货',
    delivering: '开始配送',
    arrived: '确认到达',
    complete: '确认完成'
  }
  try {
    const actionFn = {
      go: updateOrderToGo,
      delivering: updateOrderToDelivering,
      arrived: updateOrderToArrived,
      complete: updateOrderToComplete
    }[action]
    await actionFn(row.id)
    ElMessage.success(`${actionTextMap[action]}操作成功`)
    fetchList()
  } catch (e) {
    console.error('状态流转失败:', e)
  } finally {
    actionLoadingId.value = null
  }
}

// 取消订单（含支付宝退款）
const handleCancel = async (row) => {
  cancelLoadingId.value = row.id
  try {
    await cancelOrder(row.id)
    ElMessage.success('订单已取消并触发退款')
    fetchList()
  } catch (e) {
    console.error('取消订单失败:', e)
  } finally {
    cancelLoadingId.value = null
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.order-container {
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

  .el-form-item {
    margin-bottom: 0;
  }
}

.order-table {
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
