<template>
  <div class="give-container">
    <!-- 送人对象：预设多个按钮，点击作为 object 参数查询 -->
    <el-card shadow="never" class="filter-card">
      <template #header>
        <span class="card-title">按送人对象选择</span>
      </template>
      <div class="btn-group">
        <el-button
          v-for="item in objectOptions"
          :key="'obj-' + item"
          :type="activeType === 'object' && activeKeyword === item ? 'primary' : 'default'"
          round
          @click="handleSelect('object', item)"
        >
          {{ item }}
        </el-button>
      </div>
    </el-card>

    <!-- 用途场景：预设多个按钮，点击作为 option 参数查询 -->
    <el-card shadow="never" class="filter-card">
      <template #header>
        <span class="card-title">按用途场景选择</span>
      </template>
      <div class="btn-group">
        <el-button
          v-for="item in optionOptions"
          :key="'opt-' + item"
          :type="activeType === 'option' && activeKeyword === item ? 'primary' : 'default'"
          round
          @click="handleSelect('option', item)"
        >
          {{ item }}
        </el-button>
      </div>
    </el-card>

    <!-- 查询结果 -->
    <el-card shadow="never" class="result-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            查询结果
            <template v-if="activeKeyword">
              （{{ activeType === 'object' ? '送人对象' : '用途场景' }} = {{ activeKeyword }}，共 {{ resultList.length }} 条）
            </template>
          </span>
          <el-button v-if="activeKeyword" type="primary" link size="small" @click="clearSelection">
            清除选择
          </el-button>
        </div>
      </template>

      <el-table
        v-if="hasQueried"
        :data="resultList"
        stripe
        class="result-table"
        :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
        empty-text="没有符合该场景的鲜花"
      >
        <el-table-column prop="id" label="明细ID" width="100" align="center" />
        <!-- 鲜花：点击跳转鲜花详情 -->
        <el-table-column label="鲜花" min-width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goFlowerDetail(row.flowerId)">
              {{ flowerMap[row.flowerId]?.name || `鲜花#${row.flowerId}` }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="送人对象" min-width="140" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.specObject" type="warning" size="small">{{ row.specObject }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="用途场景" min-width="140" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.specOption" type="success" size="small">{{ row.specOption }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 未选择任何按钮时的引导提示 -->
      <el-empty v-else description="请选择上方的送人对象或用途场景" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
// 5.7 / 5.8：按送人对象 / 用途场景模糊查询鲜花明细；getFlowerById 用于补全鲜花名称
import {
  getFlowerDetailsByObject,
  getFlowerDetailsByOption,
  getFlowerById
} from '@/api/admin/flower.js'

const router = useRouter()

// 送人对象预设关键词（spec_object LIKE %object%，与明细录入占位口径一致）
const objectOptions = ['女友', '男友', '母亲', '父亲', '朋友', '老师', '长辈', '客户', '爱人']

// 用途场景预设关键词（spec_option LIKE %option%）
const optionOptions = ['生日', '表白', '纪念日', '求婚', '道歉', '婚礼', '春节', '探望', '毕业', '感谢']

// 当前选中的维度与关键词
const activeType = ref(null) // 'object' | 'option'
const activeKeyword = ref('')

// 查询结果
const hasQueried = ref(false)
const loading = ref(false)
const resultList = ref([])

// 鲜花名称映射：补全 FlowerDetailVO 中只有 ID 的鲜花
const flowerMap = ref({})

// 点击场景按钮：再次点击已选中按钮则取消选择
const handleSelect = (type, keyword) => {
  if (activeType.value === type && activeKeyword.value === keyword) {
    clearSelection()
    return
  }
  activeType.value = type
  activeKeyword.value = keyword
  fetchResult(type, keyword)
}

// 清除选择
const clearSelection = () => {
  activeType.value = null
  activeKeyword.value = ''
  resultList.value = []
  hasQueried.value = false
  flowerMap.value = {}
}

// 按维度调用对应接口
const fetchResult = async (type, keyword) => {
  loading.value = true
  try {
    const res = type === 'object'
      ? await getFlowerDetailsByObject(keyword)
      : await getFlowerDetailsByOption(keyword)
    const list = res?.data || []
    resultList.value = list
    hasQueried.value = true
    // 批量补全鲜花名称
    if (list.length) {
      await fetchFlowerMap(list)
    } else {
      flowerMap.value = {}
    }
  } catch (e) {
    console.error('查询鲜花明细失败:', e)
    ElMessage.error('查询失败，请稍后重试')
    resultList.value = []
  } finally {
    loading.value = false
  }
}

// 批量查询鲜花名称（按 ID 去重后逐个查询）
const fetchFlowerMap = async (list) => {
  const flowerIds = [...new Set(list.map(r => r.flowerId).filter(Boolean))]
  const flowers = await Promise.all(flowerIds.map(async (fid) => {
    try {
      const res = await getFlowerById(fid)
      return res?.data ? [fid, res.data] : null
    } catch (e) {
      console.error(`获取鲜花#${fid}失败:`, e)
      return null
    }
  }))
  flowerMap.value = Object.fromEntries(flowers.filter(Boolean))
}

// 跳转鲜花详情
const goFlowerDetail = (flowerId) => {
  if (!flowerId) return
  router.push({ path: '/admin/flower/detail', query: { id: flowerId } })
}
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.give-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

/* 场景选择卡片 */
.filter-card {
  margin-bottom: 20px;
  border: 1px solid rgba(10, 132, 255, 0.15);

  :deep(.el-card__header) {
    background: $primary-light;
    padding: 12px 20px;
  }

  .card-title {
    font-size: 14px;
    font-weight: 600;
    /* 卡片标题使用系统靛蓝 */
    color: $sys-indigo;
  }
}

/* 按钮分组：自动换行排列 */
.btn-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

/* 查询结果卡片 */
.result-card {
  border: 1px solid rgba(10, 132, 255, 0.15);

  :deep(.el-card__header) {
    background: $primary-light;
    padding: 12px 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: $sys-indigo;
  }
}

.result-table {
  width: 100%;
}

.text-muted {
  color: #909399;
}
</style>
