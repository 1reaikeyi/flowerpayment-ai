<template>
  <div class="detail-container">
    <!-- 页面标题 + 返回 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">订单详情</span>
        </template>
      </el-page-header>
    </div>

    <div v-loading="loading" class="detail-body">
      <template v-if="order.id">
        <!-- 订单基本信息 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">订单基本信息</span>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="订单号">{{ order.id }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusTagType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单用户">{{ order.userName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ order.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="配送方式">
              <el-tag :type="normalizeDeliveryType(order.deliveryType) === 1 ? 'warning' : 'info'" effect="plain">
                {{ normalizeDeliveryType(order.deliveryType) === 1 ? '立即送出' : '预约配送' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预约配送日期">
              {{ order.deliveryDate || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 收花人信息 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">收花人信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="收花人">{{ order.consignee || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ order.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="配送地址" :span="2">
              {{ order.address || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="备注/贺卡文案" :span="2">
              {{ order.remark || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 配送时间信息 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">配送时间</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="配送开始时间">
              {{ order.startDeliveryTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="预计送达时间">
              {{ order.estimatedDeliveryTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="实际送达时间" :span="2">
              {{ order.deliveryTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 订单明细列表 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-title">订单明细</span>
          </template>
          <el-table
            :data="order.flowerOrderDetailList || []"
            stripe
            class="detail-table"
            :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
          >
            <el-table-column prop="id" label="明细ID" width="80" />
            <el-table-column label="商品图片" width="100">
              <template #default="{ row }">
                <el-image
                  v-if="row.image"
                  :src="resolveImageUrl(row.image)"
                  :preview-src-list="[resolveImageUrl(row.image)]"
                  fit="contain"
                  class="product-image"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" min-width="140" />
            <el-table-column label="商品类型" min-width="100">
              <template #default="{ row }">
                <el-tag v-if="row.flowerId" type="success" effect="plain">鲜花单品</el-tag>
                <el-tag v-else-if="row.festivalId" type="warning" effect="plain">多花礼盒</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="number" label="数量" min-width="80" />
            <el-table-column label="明细金额" min-width="110">
              <template #default="{ row }">
                <span class="price">￥{{ Number(row.amount || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="包装费" min-width="100">
              <template #default="{ row }">
                {{ row.wrapFee != null ? '￥' + Number(row.wrapFee).toFixed(2) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <!-- 订单总金额 -->
          <div class="total-amount">
            <span class="label">订单总金额：</span>
            <span class="amount">￥{{ computeOrderAmount() }}</span>
          </div>
        </el-card>
      </template>

      <!-- 无数据兜底 -->
      <el-empty v-else-if="!loading" description="未获取到订单信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getOrderById } from '@/api/admin/order.js'

const route = useRoute()
const router = useRouter()

const order = ref({})
const loading = ref(false)

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

// 订单状态码 → 文案
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

// 订单状态码 → el-tag type
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
    if (statusEnumNameToCode[status] != null) return statusEnumNameToCode[status]
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

// 统一图片 URL 解析策略（本地文件优先，兼容多来源）
//   1. "/image/1.png"       → 直接走 vite /image 代理 → 后端 WebConfig 映射 file:ku/image/
//   2. "/img/flower/1.png"  → 历史遗留，前端替换前缀为 /image/
//   3. "uuid-xxx.png"       → 上传接口返回的文件名，走 /api/local?fileName=
//   4. "https://xxx.com/xxx" → 外链直接返回
const resolveImageUrl = (image) => {
  if (!image) return ''
  if (/^https?:\/\//i.test(image)) return image
  if (image.startsWith('/image/')) return image
  if (image.startsWith('/img/')) return '/image/' + image.slice(5)
  if (image.startsWith('/')) return image
  return `/api/local?fileName=${encodeURIComponent(image)}`
}

// 计算订单总金额：累加 flowerOrderDetailList 各明细的 amount
const computeOrderAmount = () => {
  const list = order.value.flowerOrderDetailList || []
  const sum = list.reduce((acc, item) => acc + Number(item.amount || 0), 0)
  return sum.toFixed(2)
}

// 获取订单详情 - GET /admin/flowerOrder?id=xxx
const fetchOrderDetail = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少订单 ID')
    return
  }
  loading.value = true
  try {
    const res = await getOrderById(id)
    if (res?.data) {
      order.value = res.data
    } else {
      order.value = {}
    }
  } catch (e) {
    console.error('获取订单详情失败:', e)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.detail-container {
  padding: 20px;
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(10, 132, 255, 0.2);

  .page-title {
    font-size: 16px;
    font-weight: 500;
    color: $sys-indigo;
  }
}

.detail-body {
  min-height: 300px;
}

.info-card {
  margin-bottom: 20px;
  border: 1px solid rgba(10, 132, 255, 0.15);

  :deep(.el-card__header) {
    background: $primary-light;
    padding: 12px 20px;
  }

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: $sys-indigo;
  }
}

.detail-table {
  width: 100%;

  .product-image {
    width: 60px;
    height: 60px;
    border-radius: 4px;
  }

  .image-placeholder {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(10, 132, 255, 0.08);
    color: rgba(94, 92, 230, 0.5);
  }

  .price {
    color: $sys-red;
    font-weight: 500;
  }
}

.total-amount {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16px;
  padding: 12px 20px;
  background: $primary-light;
  border-radius: 4px;

  .label {
    font-size: 14px;
    color: $sys-indigo;
  }

  .amount {
    font-size: 18px;
    font-weight: 600;
    color: $sys-red;
  }
}
</style>
