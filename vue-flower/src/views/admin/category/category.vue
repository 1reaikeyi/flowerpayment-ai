<template>
  <div class="category-container">
    <!-- 顶部搜索栏：按名称搜索 + 按分类类型筛选 + 新增按钮 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入分类名称"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择分类类型"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="add-btn-item">
          <!-- 新增分类：跳转到新增页 /admin/category/add -->
          <el-button type="warning" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 分类列表：id / 类型 / 名称 / 排序 / 状态 / 操作 -->
    <el-table
      :data="tableData"
      stripe
      v-loading="loading"
      class="category-table"
      :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
    >
      <el-table-column prop="id" label="ID" width="100" align="center" />
      <!-- 分类类型：el-tag 展示「菜品分类/套餐分类」 -->
      <el-table-column label="分类类型" width="140" align="center">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type).type">
            {{ getTypeTag(row.type).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="分类名称" min-width="150" />
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <!-- 状态：el-tag 展示启用/禁用 -->
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <!-- 操作：编辑 / 启停切换 / 删除 -->
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleStatusChange(row)"
          >
            <el-icon><Switch /></el-icon>
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <!-- 删除：el-popconfirm 二次确认 -->
          <el-popconfirm
            title="确认删除该分类吗？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDelete(row)"
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

    <!-- 空状态 -->
    <el-empty v-if="!loading && tableData.length === 0" description="暂无分类数据">
      <el-button type="warning" @click="handleAdd">添加第一个分类</el-button>
    </el-empty>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :page-sizes="[10, 20, 30, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="fetchCategoryList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Switch } from '@element-plus/icons-vue'
// 仅引入列表页需要的接口：add 由 addCategory 页面负责
// API 函数名对齐新的 admin API 层（category.js）
import { pageCategoryList, updateCategory, deleteCategories } from '@/api/admin/category.js'

const router = useRouter()

// 分类类型常量：后端 FlowerCategoryDTO.type
// 1=鲜花商品单只，2=节日商品多只，3=礼品
const typeOptions = [
  { value: 1, label: '鲜花商品' },
  { value: 2, label: '节日多花礼盒' },
  { value: 3, label: '礼品' }
]

// 根据后端 type 返回标签文案与 el-tag 类型
const getTypeTag = (type) => {
  switch (Number(type)) {
    case 1:
      return { text: '鲜花商品', type: 'success' }
    case 2:
      return { text: '节日多花礼盒', type: 'primary' }
    case 3:
      return { text: '礼品', type: 'warning' }
    default:
      return { text: '其他', type: 'info' }
  }
}

// 搜索表单：后端 FlowerCategoryPageDTO 支持 type 筛选；name 在前端暂不支持（后端无此字段）
const searchForm = reactive({
  name: '',
  type: null
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 获取分类列表（后端仅返回 status=1 的启用分类）
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      type: searchForm.type ?? undefined
    }
    const res = await pageCategoryList(params)
    // 后端返回 Result<List<FlowerCategoryVO>>，直接取数组
    if (res?.data) {
      tableData.value = res.data
      total.value = res.data.length
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 每页条数变化：重置到第一页
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchCategoryList()
}

// 搜索：重置到第一页后查询
const handleSearch = () => {
  pagination.page = 1
  fetchCategoryList()
}

// 新增分类：跳转到新增页
const handleAdd = () => {
  router.push('/admin/category/add')
}

// 编辑分类：跳转并携带 id 与整行数据
// 因后端无「按 id 查分类」接口，故通过 query 携带整行，避免再发一次全量请求
const handleEdit = (row) => {
  router.push({
    path: '/admin/category/add',
    query: {
      id: row.id,
      row: encodeURIComponent(JSON.stringify(row))
    }
  })
}

// 启停切换：以当前行数据为基础翻转 status，传完整 RestaurantCategoryDTO
// 注意：后端分页仅返回 status=1，禁用后刷新列表该行将不再显示
const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  try {
    await updateCategory({
      id: row.id,
      type: row.type,
      name: row.name,
      sort: row.sort,
      status: newStatus
    })
    ElMessage.success(`分类${actionText}成功`)
    fetchCategoryList()
  } catch (error) {
    console.error('修改状态失败:', error)
  }
}

// 删除分类：deleteCategory 接收 id 数组，后端按 ids 逗号拼接删除
const handleDelete = async (row) => {
  try {
    await deleteCategories([row.id])
    ElMessage.success('删除成功')
    fetchCategoryList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 初始化：挂载即拉取首页数据
onMounted(() => {
  fetchCategoryList()
})
</script>

<style lang="scss" scoped>
/* 系统色板 - 严格只使用以下 9 种颜色及其透明度变体 */
$sys-blue: #0A84FF;      // 系统蓝 - 主题主色
$sys-red: #FF453A;       // 系统红
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

.category-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  /* 搜索栏背景使用主色浅背景 */
  background: $primary-light;
  border-radius: 4px;

  .el-form-item {
    margin-bottom: 0;
  }

  /* 新增按钮靠右 */
  .add-btn-item {
    margin-left: auto;
  }

  :deep(.el-button--warning) {
    /* 主按钮使用系统蓝，文字使用系统黄 */
    background-color: $primary;
    border-color: $primary;
    color: $sys-yellow;

    &:hover {
      background-color: $primary-dark;
      border-color: $primary-dark;
    }
  }
}

.category-table {
  width: 100%;
  margin-bottom: 20px;

  :deep(.el-table__header-wrapper) {
    th {
      background-color: $primary-light !important;
      color: $primary-dark !important;
      font-weight: bold;
    }
  }

  :deep(.el-tag--warning) {
    background-color: $primary-light;
    color: $primary-dark;
    border-color: $primary;
  }

  :deep(.el-button--warning) {
    color: $primary;

    &:hover {
      color: $primary-dark;
    }
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
