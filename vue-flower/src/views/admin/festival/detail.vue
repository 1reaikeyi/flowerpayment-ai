<template>
  <div class="festival-detail-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">多花礼盒详情</span>
        </template>
      </el-page-header>
    </div>

    <!-- 礼盒基本信息 -->
    <el-card class="info-card" shadow="never" v-if="festivalInfo.id">
      <div class="info-body">
        <el-image
          v-if="festivalInfo.image"
          :src="resolveImageUrl(festivalInfo.image)"
          :preview-src-list="[resolveImageUrl(festivalInfo.image)]"
          fit="cover"
          class="festival-image"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div v-else class="image-placeholder festival-image">
          <el-icon><Picture /></el-icon>
        </div>

        <div class="info-content">
          <div class="info-row">
            <span class="info-label">礼盒名称：</span>
            <span class="info-value">{{ festivalInfo.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">所属分类：</span>
            <span class="info-value">{{ categoryNameMap[festivalInfo.categoryId] || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">礼盒价格：</span>
            <span class="info-value price">￥{{ Number(festivalInfo.price || 0).toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">售卖状态：</span>
            <el-tag :type="festivalInfo.status === 1 ? 'success' : 'danger'">
              {{ festivalInfo.status === 1 ? '在售' : '下架' }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">礼盒描述：</span>
            <span class="info-value">{{ festivalInfo.description || '-' }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 礼盒明细管理 -->
    <el-card class="detail-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">礼盒明细（共 {{ detailList.length }} 条）</span>
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
        empty-text="暂无明细数据"
      >
        <el-table-column prop="id" label="明细ID" width="90" />
        <el-table-column label="鲜花名称" min-width="160">
          <template #default="{ row }">
            {{ flowerMap[row.flowerId]?.name || `鲜花#${row.flowerId}` }}
          </template>
        </el-table-column>
        <el-table-column label="鲜花数量" min-width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.specNumber != null">× {{ row.specNumber }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="送人对象" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.specObject" type="warning" size="small">{{ row.specObject }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="用途场景" min-width="120">
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
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="detailFormRef"
        :model="detailForm"
        :rules="detailRules"
        label-width="100px"
      >
        <el-form-item label="选择鲜花" prop="flowerId" v-if="!isEditDetail">
          <el-select
            v-model="detailForm.flowerId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入鲜花名称搜索"
            :remote-method="searchFlowers"
            :loading="flowerSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in flowerOptions"
              :key="item.id"
              :label="`${item.name}（${item.categoryName || ''}）￥${item.price}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="鲜花名称" v-else>
          <span>{{ flowerMap[detailForm.flowerId]?.name || `鲜花#${detailForm.flowerId}` }}</span>
        </el-form-item>

        <el-form-item label="鲜花数量" prop="specNumber">
          <el-input-number
            v-model="detailForm.specNumber"
            :min="1"
            :max="999"
            placeholder="该鲜花在礼盒中的数量"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="送人对象" prop="specObject">
          <el-input
            v-model="detailForm.specObject"
            placeholder="如：女友、母亲、朋友"
            maxlength="20"
            clearable
          />
        </el-form-item>

        <el-form-item label="用途场景" prop="specOption">
          <el-input
            v-model="detailForm.specOption"
            placeholder="如：表白、生日、纪念日"
            maxlength="20"
            clearable
          />
        </el-form-item>
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
import {
  getFestivalById,
  getFestivalDetailsByFestivalId
} from '@/api/admin/festival.js'
import {
  createFestivalDetail,
  updateFestivalDetail,
  deleteFestivalDetails
} from '@/api/admin/festivalDetail.js'
import { getCategoryByType } from '@/api/admin/category.js'
import { pageFlowerList, getFlowerById } from '@/api/admin/flower.js'

const router = useRouter()
const route = useRoute()

// 礼盒 ID（从路由 query 获取）
const festivalId = route.query.id

// 礼盒基本信息
const festivalInfo = ref({})
const loading = ref(false)

// 分类名称映射（type=2 节日多花礼盒分类）
const categoryNameMap = ref({})

// 礼盒明细列表
const detailList = ref([])
const detailLoading = ref(false)

// 鲜花信息映射：flowerId -> FlowerVO（用于显示鲜花名称等）
const flowerMap = ref({})

// 明细表对话框
const detailDialogVisible = ref(false)
const isEditDetail = ref(false)
const detailFormRef = ref(null)
const detailForm = reactive({
  id: '',
  festivalId: '',
  flowerId: null,
  specNumber: 1,
  specObject: '',
  specOption: ''
})
const detailRules = {
  flowerId: [
    { required: true, message: '请选择鲜花', trigger: 'change' }
  ]
}

// 鲜花选择器相关
const flowerOptions = ref([])
const flowerSearchLoading = ref(false)

// 返回列表页
const goBack = () => {
  router.push('/admin/festival/index')
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

// 获取分类名称映射（type=2）
const fetchCategoryMap = async () => {
  try {
    const res = await getCategoryByType(2)
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

// 获取礼盒基本信息
const fetchFestivalInfo = async () => {
  if (!festivalId) return
  loading.value = true
  try {
    const res = await getFestivalById(festivalId)
    if (res?.data) {
      festivalInfo.value = res.data
    }
  } catch (e) {
    console.error('获取礼盒信息失败:', e)
  } finally {
    loading.value = false
  }
}

// 批量获取鲜花信息（根据 detailList 中的 flowerId 去重后逐个查询）
const fetchFlowerMap = async (detailItems) => {
  const uniqueFlowerIds = [...new Set(detailItems.map(d => d.flowerId))]
  const map = {}
  await Promise.all(
    uniqueFlowerIds.map(async (fid) => {
      try {
        const res = await getFlowerById(fid)
        if (res?.data) {
          map[fid] = res.data
        }
      } catch (e) {
        console.error(`获取鲜花#${fid}失败:`, e)
      }
    })
  )
  flowerMap.value = map
}

// 获取礼盒明细列表
const fetchDetailList = async () => {
  if (!festivalId) return
  detailLoading.value = true
  try {
    const res = await getFestivalDetailsByFestivalId(festivalId)
    const list = res?.data || []
    detailList.value = list
    // 批量获取鲜花信息用于展示
    if (list.length) {
      await fetchFlowerMap(list)
    } else {
      flowerMap.value = {}
    }
  } catch (e) {
    console.error('获取礼盒明细失败:', e)
  } finally {
    detailLoading.value = false
  }
}

// 远程搜索鲜花（用于添加明细时选择鲜花）
const searchFlowers = async (query) => {
  flowerSearchLoading.value = true
  try {
    const params = { page: 1, pageSize: 20 }
    if (query) {
      params.name = query
    }
    const res = await pageFlowerList(params)
    flowerOptions.value = res?.data?.list || []
  } catch (e) {
    console.error('搜索鲜花失败:', e)
    flowerOptions.value = []
  } finally {
    flowerSearchLoading.value = false
  }
}

// 重置明细表单
const resetDetailForm = () => {
  detailForm.id = ''
  detailForm.festivalId = festivalId
  detailForm.flowerId = null
  detailForm.specNumber = 1
  detailForm.specObject = ''
  detailForm.specOption = ''
}

// 打开添加明细对话框
const openAddDialog = () => {
  isEditDetail.value = false
  resetDetailForm()
  detailDialogVisible.value = true
  // 默认加载一次鲜花列表
  searchFlowers('')
}

// 打开编辑明细对话框
const openEditDialog = (row) => {
  isEditDetail.value = true
  detailForm.id = row.id
  detailForm.festivalId = row.festivalId
  detailForm.flowerId = row.flowerId
  detailForm.specNumber = row.specNumber ?? 1
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
      festivalId: detailForm.festivalId,
      flowerId: detailForm.flowerId,
      specNumber: detailForm.specNumber,
      specObject: detailForm.specObject,
      specOption: detailForm.specOption
    }
    if (isEditDetail.value) {
      payload.id = detailForm.id
      await updateFestivalDetail(payload)
      ElMessage.success('明细修改成功')
    } else {
      await createFestivalDetail(payload)
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
    await deleteFestivalDetails([id])
    ElMessage.success('删除成功')
    fetchDetailList()
  } catch (e) {
    console.error('删除明细失败:', e)
  }
}

// 初始化
onMounted(() => {
  if (!festivalId) {
    ElMessage.error('缺少礼盒ID')
    goBack()
    return
  }
  fetchCategoryMap()
  fetchFestivalInfo()
  fetchDetailList()
})
</script>

<style lang="scss" scoped>
.festival-detail-container {
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

  .festival-image {
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
</style>
