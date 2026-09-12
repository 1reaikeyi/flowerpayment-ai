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
        empty-text="没有符合该场景的多花礼盒"
      >
        <el-table-column prop="id" label="明细ID" width="90" align="center" />
        <!-- 所在礼盒：点击跳转礼盒详情 -->
        <el-table-column label="所在礼盒" min-width="180">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goFestivalDetail(row.festivalId)">
              {{ festivalMap[row.festivalId]?.name || `礼盒#${row.festivalId}` }}
            </el-button>
          </template>
        </el-table-column>
        <!-- 礼盒中的鲜花 -->
        <el-table-column label="包含鲜花" min-width="160">
          <template #default="{ row }">
            {{ flowerMap[row.flowerId]?.name || `鲜花#${row.flowerId}` }}
          </template>
        </el-table-column>
        <el-table-column label="送人对象" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.specObject" type="warning" size="small">{{ row.specObject }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="用途场景" min-width="120" align="center">
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
// 2.8 / 2.9：按送人对象 / 用途场景模糊查询多花礼盒明细；getFestivalById 用于补全礼盒名称
import {
  getFestivalDetailsByObject,
  getFestivalDetailsByOption,
  getFestivalById
} from '@/api/admin/festival.js'
// 结果补全鲜花名称
import { getFlowerById } from '@/api/admin/flower.js'

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

// 名称映射：补全 FestivalDetailVO 中只有 ID 的鲜花与礼盒
const flowerMap = ref({})
const festivalMap = ref({})

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
  festivalMap.value = {}
}

// 按维度调用对应接口
const fetchResult = async (type, keyword) => {
  loading.value = true
  try {
    const res = type === 'object'
      ? await getFestivalDetailsByObject(keyword)
      : await getFestivalDetailsByOption(keyword)
    const list = res?.data || []
    resultList.value = list
    hasQueried.value = true
    // 批量补全鲜花/礼盒名称
    if (list.length) {
      await fetchNameMaps(list)
    } else {
      flowerMap.value = {}
      festivalMap.value = {}
    }
  } catch (e) {
    console.error('查询多花礼盒明细失败:', e)
    ElMessage.error('查询失败，请稍后重试')
    resultList.value = []
  } finally {
    loading.value = false
  }
}

// 批量查询鲜花与礼盒名称（按 ID 去重后逐个查询）
const fetchNameMaps = async (list) => {
  const flowerIds = [...new Set(list.map(r => r.flowerId).filter(Boolean))]
  const festivalIds = [...new Set(list.map(r => r.festivalId).filter(Boolean))]

  const [flowers, festivals] = await Promise.all([
    Promise.all(flowerIds.map(async (fid) => {
      try {
        const res = await getFlowerById(fid)
        return res?.data ? [fid, res.data] : null
      } catch (e) {
        console.error(`获取鲜花#${fid}失败:`, e)
        return null
      }
    })),
    Promise.all(festivalIds.map(async (sid) => {
      try {
        const res = await getFestivalById(sid)
        return res?.data ? [sid, res.data] : null
      } catch (e) {
        console.error(`获取礼盒#${sid}失败:`, e)
        return null
      }
    }))
  ])

  flowerMap.value = Object.fromEntries(flowers.filter(Boolean))
  festivalMap.value = Object.fromEntries(festivals.filter(Boolean))
}

// 跳转礼盒详情
const goFestivalDetail = (festivalId) => {
  if (!festivalId) return
  router.push({ path: '/admin/festival/detail', query: { id: festivalId } })
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
