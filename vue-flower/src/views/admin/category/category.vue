<template>
  <div class="category-container">
    <!-- 顶部工具栏：分类类型筛选 + 查询，右侧新增按钮 -->
    <div class="toolbar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <!-- 分类列表：id / 类型 / 名称 / 排序 / 状态 / 更新时间 / 操作 -->
    <el-table
      :data="tableData"
      stripe
      v-loading="loading"
      class="category-table"
      :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
    >
      <el-table-column prop="id" label="ID" width="100" align="center" />
      <el-table-column label="分类类型" width="160" align="center">
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
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            :loading="statusLoadingId === row.id"
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
// API 函数名对齐 admin 接口文档第 4 节（category.js）
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

// 筛选表单：后端 FlowerCategoryPageDTO 仅支持 type 筛选（无 name 字段，见文档 4.3）
const searchForm = reactive({
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

// 启停切换中按钮 loading 的行 id
const statusLoadingId = ref(null)

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    // type 为空时不传，避免后端按空值过滤
    if (searchForm.type) {
      params.type = searchForm.type
    }
    // 后端返回 Result<PageResult<FlowerCategoryVO>>：data = { total, list, pageNum, pageSize }
    const res = await pageCategoryList(params)
    if (res?.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索：重置页码到第一页
const handleSearch = () => {
  pagination.page = 1
  fetchCategoryList()
}

// 每页条数变化：重置到第一页
const handleSizeChange = () => {
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

// 启停切换：复制列表行数据改 status 后 updateCategory
// 注意：后端分页可能仅返回启用分类，禁用后刷新该行将不再显示
const handleStatusChange = async (row) => {
  statusLoadingId.value = row.id
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  try {
    await updateCategory({
      ...row,
      status: newStatus
    })
    ElMessage.success(`分类${actionText}成功`)
    // 禁用后若当前页只剩这一条且不是第一页，回退一页
    if (newStatus === 0 && tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    fetchCategoryList()
  } catch (error) {
    console.error('修改状态失败:', error)
  } finally {
    statusLoadingId.value = null
  }
}

// 删除分类：deleteCategories 接收 id 数组
const handleDelete = async (row) => {
  try {
    await deleteCategories([row.id])
    ElMessage.success('删除成功')
    // 删除后若当前页只剩这一条且不是第一页，回退一页
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
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
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.category-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

/* 顶部工具栏：筛选 + 新增 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  /* 工具栏背景使用主色浅背景 */
  background: $primary-light;
  border-radius: 4px;

  .el-form-item {
    margin-bottom: 0;
  }
}

.category-table {
  width: 100%;
  margin-bottom: 20px;
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
