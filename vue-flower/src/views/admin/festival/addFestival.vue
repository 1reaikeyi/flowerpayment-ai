<template>
  <div class="add-festival-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改多花礼盒' : '添加多花礼盒' }}</span>
        </template>
      </el-page-header>
    </div>

    <!-- 表单区域 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
        class="festival-form"
      >
        <!-- 多花礼盒名称 -->
        <el-form-item label="多花礼盒名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请填写多花礼盒名称"
            maxlength="20"
          />
        </el-form-item>

        <!-- 分类 -->
        <el-form-item label="多花礼盒分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择多花礼盒分类">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 价格 -->
        <el-form-item label="多花礼盒价格" prop="price">
          <el-input
            v-model="formData.price"
            placeholder="请设置多花礼盒价格"
            type="number"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>

        <!-- 鲜花总数量 -->
        <el-form-item label="鲜花数量" prop="number">
          <el-input-number
            v-model="formData.number"
            :min="0"
            :max="999"
            placeholder="多花礼盒内鲜花总数量"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="多花礼盒图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-image
              v-if="formData.image"
              :src="resolveImageUrl(formData.image)"
              class="festival-image"
              fit="cover"
            />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            图片大小不超过2M<br>
            仅能上传 PNG JPEG JPG 类型图片
          </div>
        </el-form-item>

        <!-- 多花礼盒描述 -->
        <el-form-item label="多花礼盒描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="请输入多花礼盒描述"
            show-word-limit
          />
        </el-form-item>

        <!-- 售卖状态 -->
        <el-form-item label="售卖状态">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在售"
            inactive-text="下架"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="form-buttons">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="handleSubmit(false)">保存</el-button>
          <el-button v-if="!isEdit" type="primary" plain @click="handleSubmit(true)">
            保存并继续添加
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
import { Plus } from '@element-plus/icons-vue'
// API 函数名对齐新的 admin API 层（festival.js）
import { getFestivalById, createFestival, updateFestival } from '@/api/admin/festival.js'
import { getCategoryByType } from '@/api/admin/category.js'
import { uploadFile } from '@/api/file/file.js'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 分类列表（type=2 节日商品多只）
const categoryList = ref([])

// 表单数据 - 对齐后端 FestivalDTO 字段
const formData = reactive({
  id: '',
  name: '',
  categoryId: null,
  price: '',
  number: 0,
  image: '',
  description: '',
  status: 1
})

// 价格验证
const validatePrice = (rule, value, callback) => {
  const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
  if (!value) {
    callback(new Error('请输入多花礼盒价格'))
  } else if (!reg.test(value) || Number(value) <= 0) {
    callback(new Error('请输入大于零且最多保留两位小数的金额'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入多花礼盒名称', trigger: 'blur' },
    { min: 2, max: 20, message: '多花礼盒名称长度为2-20个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择多花礼盒分类', trigger: 'change' }
  ],
  price: [
    { required: true, validator: validatePrice, trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传多花礼盒图片', trigger: 'change' }
  ]
}

// 返回列表页
const goBack = () => {
  router.push('/admin/festival')
}

// 获取 type=2 分组的分类列表（节日多花礼盒）
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryByType(2)
    if (res?.data) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取多花礼盒详情 - 编辑模式
const fetchFestivalDetail = async () => {
  try {
    const res = await getFestivalById(route.query.id)
    if (res?.data) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.categoryId = data.categoryId ? Number(data.categoryId) : null
      formData.price = String(data.price)
      formData.number = data.number ?? 0
      formData.image = data.image
      formData.description = data.description || ''
      formData.status = data.status ?? 1
    }
  } catch (error) {
    console.error('获取多花礼盒详情失败:', error)
    ElMessage.error('获取多花礼盒详情失败')
  }
}

// 统一图片 URL 解析策略
const resolveImageUrl = (image) => {
  if (!image) return ''
  if (/^https?:\/\//i.test(image)) return image
  if (image.startsWith('/image/')) return image
  if (image.startsWith('/img/')) return '/image/' + image.slice(5)
  if (image.startsWith('/')) return image
  return `/api/local?fileName=${encodeURIComponent(image)}`
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传
// 后端返回 data = "{绝对路径}::{保存文件名}"，取 split('::')[1] 作为文件名
const handleImageUpload = async (options) => {
  try {
    const uploadFormData = new FormData()
    uploadFormData.append('file', options.file)
    const res = await uploadFile(uploadFormData)
    if (res?.data) {
      const saved = String(res.data).split('::')[1] || res.data
      formData.image = saved
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

// 提交表单
const handleSubmit = async (continueAdd) => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 组装与后端 FestivalDTO 对齐的载荷
    const payload = {
      name: formData.name,
      categoryId: formData.categoryId,
      price: formData.price,
      number: formData.number,
      image: formData.image,
      description: formData.description,
      status: formData.status
    }

    if (isEdit.value) {
      // 编辑模式
      payload.id = formData.id
      await updateFestival(payload)
      ElMessage.success('多花礼盒修改成功')
      goBack()
    } else {
      // 添加模式
      await createFestival(payload)
      ElMessage.success('多花礼盒添加成功')

      if (continueAdd) {
        formRef.value.resetFields()
        formData.image = ''
        formData.number = 0
        formData.status = 1
      } else {
        goBack()
      }
    }
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchCategoryList()
  if (isEdit.value) {
    fetchFestivalDetail()
  }
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.add-setmeal-container {
  padding: 20px;
  /* 容器背景使用系统蓝极浅透明度 */
  background: rgba(10, 132, 255, 0.04);
  border-radius: 4px;
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
  max-width: 800px;
  padding: 20px 0;
}

.setmeal-form {
  .el-select {
    width: 300px;
  }

  .el-input {
    width: 300px;
  }
}

.flower-box {
  width: 100%;
}

.flower-content {
  /* 边框使用系统蓝半透明 */
  border: 1px solid rgba(10, 132, 255, 0.2);
  border-radius: 4px;
  padding: 15px;
  /* 背景使用系统蓝浅透明度 */
  background: rgba(10, 132, 255, 0.05);
}

.add-flower-btn {
  margin-bottom: 15px;
}

.flower-table {
  width: 100%;
}

/* 图片上传样式 */
.image-uploader {
  :deep(.el-upload) {
    /* 虚线边框使用系统蓝半透明 */
    border: 1px dashed rgba(10, 132, 255, 0.3);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      /* hover 边框使用系统蓝 */
      border-color: $primary;
    }
  }

  .setmeal-image {
    width: 200px;
    height: 160px;
    display: block;
  }

  .image-uploader-icon {
    font-size: 28px;
    /* 图标颜色使用系统靛蓝半透明 */
    color: rgba(94, 92, 230, 0.5);
    width: 200px;
    height: 160px;
    line-height: 160px;
    text-align: center;
  }
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  /* 提示文字使用系统靛蓝半透明 */
  color: rgba(94, 92, 230, 0.55);
  line-height: 1.6;
}

.form-buttons {
  margin-top: 30px;
  padding-top: 20px;
  /* 顶部分隔线使用系统蓝半透明 */
  border-top: 1px solid rgba(10, 132, 255, 0.2);

  :deep(.el-button--warning) {
    /* 主按钮使用系统蓝 */
    background-color: $primary;
    border-color: $primary;
    color: $sys-yellow;

    &:hover {
      background-color: $primary-dark;
      border-color: $primary-dark;
    }

    &.is-plain {
      background-color: $primary-light;
      color: $primary;
      border-color: $primary;

      &:hover {
        background-color: $primary;
        color: $sys-yellow;
      }
    }
  }
}

.add-flower-dialog {
  .search-box {
    margin-bottom: 15px;

    .el-input {
      width: 250px;
    }

    .el-icon {
      cursor: pointer;
    }
  }

  .flower-select-content {
    display: flex;
    /* 边框使用系统蓝半透明 */
    border: 1px solid rgba(10, 132, 255, 0.2);
    border-radius: 4px;
    min-height: 400px;
  }

  .left-panel {
    flex: 1;
    /* 右分隔线使用系统蓝半透明 */
    border-right: 1px solid rgba(10, 132, 255, 0.2);
    display: flex;
    flex-direction: column;
  }

  .category-tabs {
    width: 120px;
    border-right: 1px solid rgba(10, 132, 255, 0.2);
    padding: 10px 0;

    .category-tab {
      padding: 10px 15px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        /* hover 背景使用系统蓝浅透明度 */
        background: rgba(10, 132, 255, 0.08);
      }

      &.active {
        /* 激活文字使用系统蓝 */
        color: $primary;
        /* 激活背景使用主色浅背景 */
        background: $primary-light;
        /* 右侧装饰条使用系统蓝 */
        border-right: 2px solid $primary;
      }
    }
  }

  .flower-list {
    flex: 1;
    padding: 10px;
    overflow-y: auto;
    max-height: 400px;

    .flower-item {
      padding: 10px;
      /* 底部分隔线使用系统蓝半透明 */
      border-bottom: 1px solid rgba(10, 132, 255, 0.1);

      &:last-child {
        border-bottom: none;
      }

      .flower-info {
        display: flex;
        align-items: center;
        width: 100%;

        .flower-name {
          flex: 1;
        }

        .flower-status {
          width: 60px;
          text-align: center;
          /* 状态文字使用系统靛蓝半透明 */
          color: rgba(94, 92, 230, 0.55);
        }

        .flower-price {
          width: 80px;
          text-align: right;
          /* 价格使用系统红 */
          color: $sys-red;
        }
      }
    }
  }

  .right-panel {
    width: 250px;
    padding: 15px;

    .selected-header {
      font-weight: 500;
      margin-bottom: 10px;
      padding-bottom: 10px;
      /* 底部分隔线使用系统蓝半透明 */
      border-bottom: 1px solid rgba(10, 132, 255, 0.2);
    }

    .selected-list {
      max-height: 350px;
      overflow-y: auto;

      .selected-item {
        display: flex;
        align-items: center;
        padding: 8px 10px;
        margin-bottom: 8px;
        /* 选中项背景使用系统蓝浅透明度 */
        background: rgba(10, 132, 255, 0.08);
        border-radius: 4px;

        .selected-name {
          flex: 1;
        }

        .selected-price {
          /* 选中价格使用系统红 */
          color: $sys-red;
          margin-right: 10px;
        }

        .remove-icon {
          cursor: pointer;
          /* 移除图标使用系统靛蓝半透明 */
          color: rgba(94, 92, 230, 0.55);

          &:hover {
            /* hover 移除图标使用系统红 */
            color: $sys-red;
          }
        }
      }
    }
  }
}
</style>
