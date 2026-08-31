<template>
  <div class="festival-container">
    <!-- 顶部搜索栏 + 新增按钮 -->
    <div class="toolbar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="多花多花礼盒名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入多花礼盒名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增多花礼盒
      </el-button>
    </div>

    <!-- 多花礼盒列表 -->
    <el-table
      :data="tableData"
      v-loading="loading"
      stripe
      class="festival-table"
      :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <!-- 多花礼盒图 -->
      <el-table-column label="多花礼盒图" width="110">
        <template #default="{ row }">
          <el-image
            v-if="row.image"
            :src="resolveImageUrl(row.image)"
            :preview-src-list="[resolveImageUrl(row.image)]"
            fit="cover"
            class="festival-image"
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
      <el-table-column prop="name" label="多花礼盒名称" min-width="140" />
      <!-- 分类名反查（多花礼盒分类用 type=2） -->
      <el-table-column label="分类" min-width="120">
        <template #default="{ row }">
          {{ categoryNameMap[row.categoryId] || '-' }}
        </template>
      </el-table-column>
      <!-- 价格 -->
      <el-table-column label="价格" min-width="100">
        <template #default="{ row }">
          <span class="price">￥{{ Number(row.price || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <!-- 状态：1 在售 / 0 下架 -->
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <!-- 操作列 -->
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            :loading="statusLoadingId === row.id"
            @click="handleStatusToggle(row)"
          >
            <el-icon><Switch /></el-icon>
            {{ row.status === 1 ? '下架' : '在售' }}
          </el-button>
          <el-popconfirm
            title="确认删除该多花礼盒吗？"
            @confirm="handleDelete(row.id)"
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
import { Search, Plus, Edit, Delete, Switch, Picture } from '@element-plus/icons-vue'
import {
  pageFestivalList,
  getFestivalById,
  updateFestival,
  deleteFestivals
} from '@/api/admin/festival.js'
import { getCategoryByType } from '@/api/admin/category.js'

const router = useRouter()

// 搜索表单（接口仅支持 name 模糊查询）
const searchForm = reactive({
  name: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 列表数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const categoryNameMap = ref({})

// 启停切换中按钮 loading 的行 id
const statusLoadingId = ref(null)

// 统一图片 URL 解析策略（本地文件优先，兼容多来源）
// 数据库 image 字段可能有多种形态：
//   1. "/image/1.png"         → 直接走 vite /image 代理 → 后端 WebConfig 映射 file:ku/image/
//   2. "/img/flower/1.png"      → 历史遗留，前端替换前缀为 /image/
//   3. "uuid-xxx.png"         → 上传接口返回的文件名，需要走 /api/local?fileName=
//   4. "https://xxx.com/xxx"  → 外链直接返回
const resolveImageUrl = (image) => {
  if (!image) return ''
  // 外链（OSS/CDN）直接放行
  if (/^https?:\/\//i.test(image)) return image
  // /image/ 前缀：新格式，直接用，命中 vite /image 代理 → 后端 ku/image/
  if (image.startsWith('/image/')) return image
  // /img/ 前缀：历史遗留（数据库旧格式 /img/flower/1.png），替换成 /image/
  if (image.startsWith('/img/')) return '/image/' + image.slice(5)
  // / 开头但不是上面两种的未知静态路径，原样保留（防止破坏其他路径）
  if (image.startsWith('/')) return image
  // 其余情况视为 uploadFile 接口返回的 UUID 文件名，走本地文件下载接口
  return `/api/local?fileName=${encodeURIComponent(image)}`
}

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

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    // name 为空时不传，避免后端按空字符串过滤
    if (searchForm.name) {
      params.name = searchForm.name
    }
    // 后端返回 Result<List<FestivalVO>>，直接取数组
    const res = await pageFestivalList(params)
    if (res?.data) {
      tableData.value = res.data
      total.value = res.data.length
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (e) {
    console.error('获取套餐列表失败:', e)
  } finally {
    loading.value = false
  }
}

// 搜索：重置页码到第一页
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

// pageSize 变化：回到第一页
const handleSizeChange = () => {
  pagination.page = 1
  fetchList()
}

const handleAdd = () => {
  router.push('/admin/festival/add')
}

const handleEdit = (row) => {
  router.push({ path: '/admin/festival/add', query: { id: row.id } })
}

// 启停切换：直接复制列表行数据改 status 后 updateFestival
// 后端 FestivalDTO 直接接受完整对象，无需额外拆分
const handleStatusToggle = async (row) => {
  statusLoadingId.value = row.id
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '在售' : '下架'
  try {
    const payload = {
      ...row,
      status: newStatus
    }
    await updateFestival(payload)
    ElMessage.success(`已${actionText}`)
    fetchList()
  } catch (e) {
    console.error('切换状态失败:', e)
  } finally {
    statusLoadingId.value = null
  }
}

// 删除多花礼盒：deleteFestivals 接受 id 数组
const handleDelete = async (id) => {
  try {
    await deleteFestivals([id])
    ElMessage.success('删除成功')
    // 删除后若当前页只剩这一条且不是第一页，回退一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    fetchList()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

// 初始化：先拉分类反查表（不阻塞列表），再拉列表
onMounted(() => {
  fetchCategoryMap()
  fetchList()
})
</script>

<style lang="scss" scoped>
/* 系统色板 - 严格只使用以下 9 种颜色及其透明度变体（参考 admin.vue） */
$sys-blue: #0A84FF;      // 系统蓝 - 主题主色
$sys-red: #FF453A;       // 系统红 - 价格
$sys-orange: #FF9F0A;    // 系统橙
$sys-yellow: #FFD60A;    // 系统黄
$sys-green: #30D158;     // 系统绿
$sys-cyan: #40C8E0;      // 系统青
$sys-indigo: #5E5CE6;    // 系统靛蓝 - 深色/文字
$sys-purple: #BF5AF2;    // 系统紫
$sys-pink: #FF375F;      // 系统粉

/* 主题色变量（基于系统蓝） */
$primary: $sys-blue;
$primary-light: rgba(10, 132, 255, 0.1);   // 主色浅背景
$primary-dark: $sys-indigo;                  // 主色深色

.festival-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

/* 顶部工具栏：搜索 + 新增 */
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

  :deep(.el-button--warning) {
    /* 主按钮使用系统蓝 */
    background-color: $primary;
    border-color: $primary;
    color: $sys-yellow;

    &:hover {
      background-color: $primary-dark;
      border-color: $primary-dark;
    }
  }
}

.festival-table {
  width: 100%;
  margin-bottom: 20px;

  .festival-image {
    width: 80px;
    height: 50px;
    border-radius: 4px;
  }

  .image-placeholder {
    width: 80px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    /* 占位背景使用系统蓝浅透明度 */
    background: rgba(10, 132, 255, 0.08);
    /* 占位图标使用系统靛蓝半透明 */
    color: rgba(94, 92, 230, 0.5);
  }

  .price {
    /* 价格使用系统红 */
    color: $sys-red;
    font-weight: 500;
  }
}

/* 分页 */
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
