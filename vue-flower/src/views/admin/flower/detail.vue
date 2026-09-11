<template>
  <div class="flower-detail-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">鲜花详情</span>
        </template>
      </el-page-header>
    </div>

    <!-- 鲜花基本信息 -->
    <el-card class="info-card" shadow="never" v-if="flowerInfo.id">
      <div class="info-body">
        <el-image
          v-if="flowerInfo.image"
          :src="resolveImageUrl(flowerInfo.image)"
          :preview-src-list="[resolveImageUrl(flowerInfo.image)]"
          fit="cover"
          class="flower-image"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div v-else class="image-placeholder flower-image">
          <el-icon><Picture /></el-icon>
        </div>

        <div class="info-content">
          <div class="info-row">
            <span class="info-label">鲜花名称：</span>
            <span class="info-value">{{ flowerInfo.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">所属分类：</span>
            <span class="info-value">{{ flowerInfo.categoryName || categoryNameMap[flowerInfo.categoryId] || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">鲜花颜色：</span>
            <span class="info-value">{{ flowerInfo.color || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">鲜花价格：</span>
            <span class="info-value price">￥{{ Number(flowerInfo.price || 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">售卖状态：</span>
            <el-tag :type="flowerInfo.status === 1 ? 'success' : 'danger'">
              {{ flowerInfo.status === 1 ? '在售' : '下架' }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">花语描述：</span>
            <span class="info-value">{{ flowerInfo.description || '-' }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 鲜花规格明细管理 -->
    <el-card class="detail-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">规格明细（共 {{ detailList.length }} 条）</span>
          <el-button type="primary" size="small" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加明细
          </el-button>
        </div>
      </template>

      <el-table
        :data="detailList"
        v-loading="detailLoading"
        stripe
        class="detail-table"
        :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
        empty-text="暂无规格明细"
      >
        <el-table-column prop="id" label="明细ID" width="90" />
        <el-table-column label="送人对象" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.specObject" type="warning" size="small">{{ row.specObject }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="用途场景" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.specOption" type="success" size="small">{{ row.specOption }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-popconfirm
              title="确认删除该明细吗？"
              @confirm="handleDeleteDetail(row.id)"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑明细对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="isEditDetail ? '编辑明细' : '添加明细'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="detailFormRef"
        :model="detailForm"
        :rules="detailRules"
        label-width="100px"
      >
        <el-form-item label="送人对象">
          <el-input
            v-model="detailForm.specObject"
            placeholder="如：女友、母亲、朋友、老师"
            maxlength="20"
            clearable
          />
        </el-form-item>

        <el-form-item label="用途场景">
          <el-input
            v-model="detailForm.specOption"
            placeholder="如：表白、生日、纪念日、春节"
            maxlength="20"
            clearable
          />
        </el-form-item>

        <div class="form-tip">
          提示：送人对象与用途场景至少填写一项，便于用户按场景筛选鲜花。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="detailDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDetail">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { getFlowerById, getFlowerDetailsByFlowerId } from '@/api/admin/flower.js'
import {
  createFlowerDetail,
  updateFlowerDetail,
  deleteFlowerDetails
} from '@/api/admin/flowerDetail.js'
import { getCategoryByType } from '@/api/admin/category.js'

const router = useRouter()
const route = useRoute()

// 鲜花 ID（从路由 query 获取）
const flowerId = route.query.id

// 鲜花基本信息
const flowerInfo = ref({})
const loading = ref(false)

// 分类名称映射（type=1 鲜花商品分类）
const categoryNameMap = ref({})

// 鲜花规格明细列表
const detailList = ref([])
const detailLoading = ref(false)

// 明细表对话框
const detailDialogVisible = ref(false)
const isEditDetail = ref(false)
const detailFormRef = ref(null)
const detailForm = reactive({
  id: '',
  flowerId: '',
  specObject: '',
  specOption: ''
})
// 至少填写一项的自定义校验
const validateAtLeastOne = (rule, value, callback) => {
  if (!detailForm.specObject && !detailForm.specOption) {
    callback(new Error('送人对象与用途场景至少填写一项'))
  } else {
    callback()
  }
}
const detailRules = {
  specObject: [{ validator: validateAtLeastOne, trigger: 'blur' }],
  specOption: [{ validator: validateAtLeastOne, trigger: 'blur' }]
}

// 返回列表页
const goBack = () => {
  router.push('/admin/flower/index')
}

// 统一图片 URL 解析策略（与 index.vue 保持一致）
const resolveImageUrl = (image) => {
  if (!image) return ''
  if (/^https?:\/\//i.test(image)) return image
  if (image.startsWith('/image/')) return image
  if (image.startsWith('/img/')) return '/image/' + image.slice(5)
  if (image.startsWith('/')) return image
  return `/api/local?fileName=${encodeURIComponent(image)}`
}

// 获取分类名称映射（type=1）
const fetchCategoryMap = async () => {
  try {
    const res = await getCategoryByType(1)
    const list = res?.data || []
    const map = {}
    list.forEach(c => {
      map[c.id] = c.name
    })
    categoryNameMap.value = map
  } catch (e) {
    console.error('获取分类失败:', e)
  }
}

// 获取鲜花基本信息
const fetchFlowerInfo = async () => {
  if (!flowerId) return
  loading.value = true
  try {
    const res = await getFlowerById(flowerId)
    if (res?.data) {
      flowerInfo.value = res.data
    }
  } catch (e) {
    console.error('获取鲜花信息失败:', e)
  } finally {
    loading.value = false
  }
}

// 获取鲜花规格明细列表
const fetchDetailList = async () => {
  if (!flowerId) return
  detailLoading.value = true
  try {
    const res = await getFlowerDetailsByFlowerId(flowerId)
    detailList.value = res?.data || []
  } catch (e) {
    console.error('获取鲜花明细失败:', e)
  } finally {
    detailLoading.value = false
  }
}

// 重置明细表单
const resetDetailForm = () => {
  detailForm.id = ''
  detailForm.flowerId = flowerId
  detailForm.specObject = ''
  detailForm.specOption = ''
}

// 打开添加明细对话框
const openAddDialog = () => {
  isEditDetail.value = false
  resetDetailForm()
  detailDialogVisible.value = true
}

// 打开编辑明细对话框
const openEditDialog = (row) => {
  isEditDetail.value = true
  detailForm.id = row.id
  detailForm.flowerId = row.flowerId
  detailForm.specObject = row.specObject || ''
  detailForm.specOption = row.specOption || ''
  detailDialogVisible.value = true
}

// 提交明细（新增/编辑）
const submitDetail = async () => {
  if (!detailFormRef.value) return
  try {
    await detailFormRef.value.validate()
    const payload = {
      flowerId: detailForm.flowerId,
      specObject: detailForm.specObject,
      specOption: detailForm.specOption
    }
    if (isEditDetail.value) {
      payload.id = detailForm.id
      await updateFlowerDetail(payload)
      ElMessage.success('明细修改成功')
    } else {
      await createFlowerDetail(payload)
      ElMessage.success('明细添加成功')
    }
    detailDialogVisible.value = false
    fetchDetailList()
  } catch (e) {
    if (e !== false) {
      console.error('提交明细失败:', e)
    }
  }
}

// 删除明细
const handleDeleteDetail = async (id) => {
  try {
    await deleteFlowerDetails([id])
    ElMessage.success('删除成功')
    fetchDetailList()
  } catch (e) {
    console.error('删除明细失败:', e)
  }
}

// 初始化
onMounted(() => {
  if (!flowerId) {
    ElMessage.error('缺少鲜花ID')
    goBack()
    return
  }
  fetchCategoryMap()
  fetchFlowerInfo()
  fetchDetailList()
})
</script>

<style lang="scss" scoped>
.flower-detail-container {
  padding: 20px;
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(10, 132, 255, 0.2);
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: $sys-indigo;
}

.info-card {
  margin-bottom: 20px;

  .info-body {
    display: flex;
    gap: 24px;
  }

  .flower-image {
    width: 200px;
    height: 160px;
    border-radius: 4px;
    flex-shrink: 0;
  }

  .image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(10, 132, 255, 0.08);
    color: rgba(94, 92, 230, 0.5);
    font-size: 40px;
  }

  .info-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 12px;
  }

  .info-row {
    display: flex;
    align-items: center;

    .info-label {
      width: 90px;
      color: rgba(94, 92, 230, 0.7);
      font-weight: 500;
    }

    .info-value {
      color: #303133;
    }

    .price {
      color: $sys-red;
      font-weight: 600;
    }
  }
}

.detail-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 16px;
    font-weight: 500;
    color: $sys-indigo;
  }
}

.detail-table {
  width: 100%;
}

.text-muted {
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: rgba(94, 92, 230, 0.55);
  line-height: 1.6;
  margin-top: -10px;
}
</style>
