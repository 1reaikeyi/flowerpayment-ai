<template>
  <div class="add-category-container">
    <!-- 页面标题与返回 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改分类' : '新增分类' }}</span>
        </template>
      </el-page-header>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="category-form"
      >
        <!-- 分类名称 -->
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            maxlength="20"
          />
        </el-form-item>

        <!-- 分类类型 -->
        <el-form-item label="分类类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择分类类型">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 排序：数字越小越靠前 -->
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :max="9999"
            placeholder="数字越小越靠前"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 状态：启用 1 / 禁用 0 -->
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="form-buttons">
          <el-button @click="goBack">取消</el-button>
          <el-button type="warning" :loading="submitting" @click="handleSubmit">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
// API 函数名对齐新的 admin API 层（category.js）
import { createCategory, updateCategory, pageCategoryList } from '@/api/admin/category.js'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)

// 分类类型常量：后端 FlowerCategoryDTO.type
// 1=鲜花商品单只，2=节日商品多只，3=礼品
const typeOptions = [
  { value: 1, label: '鲜花商品' },
  { value: 2, label: '节日多花礼盒' },
  { value: 3, label: '礼品' }
]

// 是否为编辑模式：query.id 存在即编辑
const isEdit = computed(() => !!route.query.id)

// 表单数据：字段与后端 RestaurantCategoryDTO 对齐（id/type/name/sort/status）
const formData = reactive({
  id: null,
  type: 1,
  name: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '分类名称长度为1-20个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择分类类型', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 编辑模式回显：
// 优先从 query.row 还原整行（列表页跳转时携带）；
// 缺失时拉全量分页后按 id 过滤兜底（后端无「按 id 查分类」接口）
const loadEditData = async () => {
  const id = route.query.id
  let record = null

  // 1) 优先解析 query.row
  if (route.query.row) {
    try {
      record = JSON.parse(decodeURIComponent(route.query.row))
    } catch (e) {
      console.error('解析 row 参数失败:', e)
    }
  }

  // 2) 兜底：全量分页查询后按 id 过滤
  if (!record) {
    try {
      const res = await pageCategoryList({ page: 1, pageSize: 9999 })
      const records = res?.data?.records || []
      record = records.find(r => String(r.id) === String(id)) || null
    } catch (e) {
      console.error('获取分类列表失败:', e)
    }
  }

  // 3) 回填表单
  if (record) {
    formData.id = record.id
    formData.type = record.type ?? 1
    formData.name = record.name ?? ''
    formData.sort = record.sort ?? 0
    formData.status = record.status ?? 1
  } else {
    ElMessage.error('未找到该分类信息')
    goBack()
  }
}

// 提交表单：按 isEdit 决定 addCategory 或 updateCategory
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true

    // 组装与后端 DTO 完全对齐的载荷（type/name/sort/status）
    const payload = {
      type: formData.type,
      name: formData.name,
      sort: formData.sort,
      status: formData.status
    }

    if (isEdit.value) {
      // 编辑：updateCategory 必含 id
      payload.id = formData.id
      await updateCategory(payload)
      ElMessage.success('分类修改成功')
    } else {
      // 新增
      await createCategory(payload)
      ElMessage.success('分类添加成功')
    }
    router.back()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 初始化：编辑模式才回显
onMounted(() => {
  if (isEdit.value) {
    loadEditData()
  }
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

.add-category-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  /* 分隔线使用系统蓝半透明 */
  border-bottom: 1px solid rgba(10, 132, 255, 0.2);
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  /* 标题文字使用系统靛蓝 */
  color: $sys-indigo;
}

.form-container {
  max-width: 700px;
  padding: 20px 0;
}

.category-form {
  .el-select {
    width: 300px;
  }

  .el-input {
    width: 300px;
  }

  .el-switch {
    /* 开关激活态使用系统绿 */
    :deep(.el-switch.is-checked .el-switch__core) {
      background-color: $sys-green;
      border-color: $sys-green;
    }
  }
}

.form-buttons {
  margin-top: 30px;
  padding-top: 20px;
  /* 顶部分隔线使用系统蓝半透明 */
  border-top: 1px solid rgba(10, 132, 255, 0.2);

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
</style>
