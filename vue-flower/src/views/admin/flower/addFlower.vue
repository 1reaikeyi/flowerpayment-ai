<template>
  <div class="add-flower-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '修改鲜花' : '添加鲜花' }}</span>
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
        class="flower-form"
      >
        <!-- 鲜花名称 -->
        <el-form-item label="鲜花名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请填写鲜花名称"
            maxlength="20"
          />
        </el-form-item>

        <!-- 颜色 -->
        <el-form-item label="鲜花颜色" prop="color">
          <el-input
            v-model="formData.color"
            placeholder="请填写鲜花颜色，如：红色、粉色、白色"
            maxlength="20"
          />
        </el-form-item>

        <!-- 分类 -->
        <el-form-item label="鲜花分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择鲜花分类">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 价格 -->
        <el-form-item label="鲜花价格" prop="price">
          <el-input
            v-model="formData.price"
            placeholder="请设置鲜花价格"
            type="number"
          >
            <template #append>元</template>
          </el-input>
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="鲜花图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-image
              v-if="formData.image"
              :src="resolveImageUrl(formData.image)"
              class="flower-image"
              fit="cover"
            />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            图片大小不超过2M<br>
            仅能上传 PNG JPEG JPG 类型图片
          </div>
        </el-form-item>

        <!-- 鲜花描述 -->
        <el-form-item label="花语描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="请输入鲜花的花语描述"
            show-word-limit
          />
        </el-form-item>

        <!-- 状态 -->
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
// API 函数名对齐新的 admin API 层（flower.js）
import { getFlowerById, createFlower, updateFlower } from '@/api/admin/flower.js'
import { getCategoryByType } from '@/api/admin/category.js'
import { uploadFile } from '@/api/file/file.js'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 分类列表（type=1 鲜花商品）
const categoryList = ref([])

// 表单数据 - 对齐后端 FlowerDTO 字段
const formData = reactive({
  id: '',
  name: '',
  color: '',
  categoryId: '',
  price: '',
  image: '',
  description: '',
  status: 1
})

// 价格验证
const validatePrice = (rule, value, callback) => {
  const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
  if (!value) {
    callback(new Error('请输入鲜花价格'))
  } else if (!reg.test(value) || Number(value) <= 0) {
    callback(new Error('请输入大于零且最多保留两位小数的金额'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入鲜花名称', trigger: 'blur' },
    { min: 2, max: 20, message: '鲜花名称长度为2-20个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择鲜花分类', trigger: 'change' }
  ],
  price: [
    { required: true, validator: validatePrice, trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传鲜花图片', trigger: 'change' }
  ]
}

// 返回列表页
const goBack = () => {
  router.push('/flower')
}

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryByType(1)
    if (res?.data) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取鲜花详情 - 编辑模式
const fetchFlowerDetail = async () => {
  try {
    const res = await getFlowerById(route.query.id)
    if (res?.data) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.color = data.color || ''
      formData.categoryId = data.categoryId
      formData.price = String(data.price)
      formData.image = data.image
      formData.description = data.description || ''
      formData.status = data.status ?? 1
    }
  } catch (error) {
    console.error('获取鲜花详情失败:', error)
    ElMessage.error('获取鲜花详情失败')
  }
}



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
  // / 开头但不是上面两种的未知静态路径，原样保留
  if (image.startsWith('/')) return image
  // 其余情况视为 uploadFile 接口返回的 UUID 文件名，走本地文件下载接口
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
// 注意：局部变量命名为 uploadFormData 避免与外层 reactive formData 冲突
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

    // 组装与后端 FlowerDTO 对齐的载荷
    const payload = {
      name: formData.name,
      color: formData.color,
      categoryId: formData.categoryId,
      price: formData.price,
      image: formData.image,
      description: formData.description,
      status: formData.status
    }

    if (isEdit.value) {
      // 编辑模式
      payload.id = formData.id
      await updateFlower(payload)
      ElMessage.success('鲜花修改成功')
      goBack()
    } else {
      // 添加模式
      await createFlower(payload)
      ElMessage.success('鲜花添加成功')

      if (continueAdd) {
        formRef.value.resetFields()
        formData.image = ''
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
    fetchFlowerDetail()
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

.add-flower-container {
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

.flower-form {
  .el-select {
    width: 300px;
  }

  .el-input {
    width: 300px;
  }
}


.flavor-box {
  width: 100%;
}

.flavor-content {
  /* 边框使用系统蓝半透明 */
  border: 1px solid rgba(10, 132, 255, 0.2);
  border-radius: 4px;
  padding: 15px;
  /* 背景使用系统蓝浅透明度 */
  background: rgba(10, 132, 255, 0.05);
}

.flavor-header {
  display: flex;
  margin-bottom: 10px;
  /* 文字使用系统靛蓝 */
  color: $sys-indigo;
  font-size: 14px;

  .flavor-title {
    width: 150px;
  }

  .flavor-tags {
    flex: 1;
  }
}

.flavor-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 15px;

  .flavor-select {
    width: 150px;

    .el-select {
      width: 100%;
    }
  }

  .flavor-tags-box {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 8px;
    /* 边框使用系统蓝半透明 */
    border: 1px solid rgba(10, 132, 255, 0.2);
    border-radius: 4px;
    /* 背景使用系统蓝极浅透明度 */
    background: rgba(10, 132, 255, 0.04);
    min-height: 40px;

    .el-tag {
      margin: 0;
    }

    .tag-input {
      width: 100px;

      :deep(.el-input__wrapper) {
        padding: 0 8px;
        box-shadow: none;
      }
    }
  }
}

.add-flavor-btn {
  margin-top: 10px;
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

  .flower-image {
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
</style>
