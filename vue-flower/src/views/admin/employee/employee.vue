<template>
  <div class="employee-container">
    <!-- 搜索栏 + 新增按钮 -->
    <div class="toolbar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <!-- 后端 EmployeePageDTO.name：按部门/用户名模糊查询 -->
        <!-- 后端 EmployeePageDTO.employeename：按用户名模糊搜索 -->
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.employeename"
            placeholder="请输入用户名"
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
      <!-- 新增员工：跳转 /admin/employee/add -->
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增员工
      </el-button>
    </div>

    <!-- 员工列表 -->
    <el-table
      :data="tableData"
      stripe
      v-loading="loading"
      class="employee-table"
      :header-cell-style="{ background: 'rgba(10, 132, 255, 0.1)', color: '#0A84FF', fontWeight: 'bold' }"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <!-- 后端 EmployeeVO.work：职位字段 -->
      <el-table-column prop="work" label="职位" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <!-- 状态：0 禁用 / 1 启用 -->
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <!-- 编辑：跳 /admin/employee/add?id= -->
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <!-- 启停切换：getEmployeeDetail 取详情 → 改 status → updateEmployee -->
          <el-button
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleStatusToggle(row)"
          >
            <el-icon><Switch /></el-icon>
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <!-- 删除：el-popconfirm 二次确认 + deleteEmployee([id]) -->
          <el-popconfirm
            title="确认删除该员工吗？"
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
      @size-change="fetchEmployeeList"
      @current-change="fetchEmployeeList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Switch, Delete } from '@element-plus/icons-vue'
// API 函数名对齐新的 admin API 层（admin.js）
import {
  pageEmployeeList,
  getEmployeeById,
  updateEmployee,
  deleteEmployees
} from '@/api/admin/admin.js'

const router = useRouter()

// 搜索表单 - 对齐后端 EmployeePageDTO.employeename（模糊搜索用户名）
const searchForm = reactive({
  employeename: ''
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

// 获取员工列表 - GET /admin/all，参数 { page, pageSize, name? }
// 后端返回 IPage<Employee>：{ records, total, size, current }
const fetchEmployeeList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      // employeename 为空时不传，避免发送空字符串触发模糊查询
      employeename: searchForm.employeename || undefined
    }
    const res = await pageEmployeeList(params)
    // 后端返回 Result<PageResult<EmployeeVO>>：data = { total, list, pageNum, pageSize }
    if (res?.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索 - 重置页码到第一页再查询
const handleSearch = () => {
  pagination.page = 1
  fetchEmployeeList()
}

// 跳转新增员工页
const handleAdd = () => {
  router.push('/admin/employee/add')
}

// 跳转编辑员工页 - 通过 query.id 标识编辑模式
const handleEdit = (row) => {
  router.push(`/admin/employee/add?id=${row.id}`)
}

// 启停切换：先获取详情避免遗漏字段，再修改 status 后 PUT /admin/employee
const handleStatusToggle = async (row) => {
  try {
    const detailRes = await getEmployeeById(row.id)
    const detail = detailRes?.data
    if (!detail) {
      ElMessage.error('获取员工详情失败')
      return
    }
    // 切换 status：1→0、0→1
    const newStatus = row.status === 1 ? 0 : 1
    await updateEmployee({ ...detail, status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchEmployeeList()
  } catch (error) {
    console.error('状态切换失败:', error)
  }
}

// 删除员工 - DELETE /admin?ids=1,2,3，传数组由 api 内部拼接
const handleDelete = async (row) => {
  try {
    await deleteEmployees([row.id])
    ElMessage.success('删除成功')
    // 当前页只剩一条且不是第一页时，回退到上一页避免空白
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    fetchEmployeeList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchEmployeeList()
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.employee-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: $primary-light;
  border-radius: 4px;
  flex-wrap: wrap;
  gap: 12px;

  .search-form {
    flex: 1;
  }

  .el-form-item {
    margin-bottom: 0;
  }

  :deep(.el-button--primary) {
    background-color: $primary;
    border-color: $primary;

    &:hover {
      background-color: $primary-dark;
      border-color: $primary-dark;
    }
  }
}

.employee-table {
  width: 100%;
  margin-bottom: 20px;

  :deep(.el-table__header-wrapper) {
    th {
      background-color: $primary-light !important;
      color: $primary-dark !important;
      font-weight: bold;
    }
  }

  :deep(.el-button--primary) {
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
