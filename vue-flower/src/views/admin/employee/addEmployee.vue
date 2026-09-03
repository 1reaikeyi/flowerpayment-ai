<template>
  <div class="add-employee-container">
    <!-- 页面头部 - 返回 + 标题 -->
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">{{ isEdit ? '编辑员工' : '新增员工' }}</span>
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
        class="employee-form"
      >
        <!-- 用户名 - 编辑模式禁止修改 -->
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            maxlength="20"
            :disabled="isEdit"
          />
        </el-form-item>

        <!-- 在职部门/职位：后端 EmployeeDTO.work 字段 -->
        <el-form-item label="在职职位" prop="work">
          <el-input
            v-model="formData.work"
            placeholder="请输入在职职位"
            maxlength="32"
          />
        </el-form-item>

        <!-- 密码：新增必填，编辑留空表示不修改 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            :placeholder="isEdit ? '留空表示不修改密码' : '请输入密码'"
            maxlength="20"
            show-password
          />
        </el-form-item>

        <!-- 头像：el-upload 自定义 http-request 调 uploadFile -->
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleAvatarUpload"
            accept="image/*"
          >
            <img
              v-if="formData.avatar"
              :src="avatarPreviewUrl"
              class="avatar-preview"
              alt="头像"
            />
            <div v-else class="avatar-placeholder">
              <el-icon><Plus /></el-icon>
              <span>上传头像</span>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            maxlength="64"
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>

        <!-- 性别：el-radio 男/女 -->
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 状态：el-switch 0/1 -->
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
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
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
import { Plus } from '@element-plus/icons-vue'
import {
  registerEmployee,
  getEmployeeById,
  updateEmployee
} from '@/api/admin/admin.js'
import { uploadFile } from '@/api/file/file.js'
import { baseURL } from '@/utils/admin/request.js'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)

// 是否编辑模式：query.id 存在即为编辑
const isEdit = computed(() => !!route.query.id)

// 表单数据 - 字段对齐后端 EmployeeDTO（work 字段表示职位）
const formData = reactive({
  id: null,
  username: '',
  work: '',
  password: '',
  avatar: '',
  email: '',
  phone: '',
  sex: '男',
  status: 1
})

// 头像回显 URL - 通过 /api/local?fileName=xxx 拉取二进制流
const avatarPreviewUrl = computed(() => {
  if (!formData.avatar) return ''
  return `${baseURL}/local?fileName=${encodeURIComponent(formData.avatar)}`
})

// 密码校验：新增必填，编辑可选（留空表示不修改）
const validatePassword = (rule, value, callback) => {
  if (!isEdit.value && !value) {
    callback(new Error('请输入密码'))
  } else if (value && value.length < 6) {
    callback(new Error('密码长度至少 6 位'))
  } else {
    callback()
  }
}

// 手机号校验
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 邮箱校验（可空）
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字、下划线', trigger: 'blur' }
  ],
  work: [
    { required: true, message: '请输入在职职位', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 头像上传 - 自定义 http-request 调 uploadFile
// 后端返回 data = "{绝对路径}::{保存文件名}"，取 split('::')[1] 作为头像字段值
const handleAvatarUpload = async (options) => {
  const { file } = options
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await uploadFile(fd)
    // data 格式 "{绝对路径}::{保存文件名}"，取后半段
    const saved = String(res.data || '').split('::')[1] || ''
    if (!saved) {
      ElMessage.error('头像上传失败')
      return
    }
    formData.avatar = saved
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 加载员工详情 - 编辑模式 onMounted 调用
const fetchEmployeeDetail = async () => {
  try {
    const res = await getEmployeeById(route.query.id)
    const data = res?.data
    if (data) {
      // 注意：password 字段 @JsonIgnore 不返回，编辑时留空表示不修改
      Object.assign(formData, {
        id: data.id,
        username: data.username || '',
        work: data.work || '',
        password: '',
        avatar: data.avatar || '',
        email: data.email || '',
        phone: data.phone || '',
        sex: data.sex || '男',
        status: data.status ?? 1
      })
    }
  } catch (error) {
    console.error('获取员工详情失败:', error)
    ElMessage.error('获取员工详情失败')
  }
}

// 提交表单 - query.id 决定 registerEmployee（新增）或 updateEmployee（编辑）
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true

    // 组装请求数据 - 后端 EmployeeDTO 字段
    const payload = {
      username: formData.username,
      work: formData.work,
      avatar: formData.avatar,
      email: formData.email,
      phone: formData.phone,
      sex: formData.sex,
      status: formData.status
    }

    if (isEdit.value) {
      // 编辑模式：必传 id；password 留空不传表示不修改
      payload.id = formData.id
      if (formData.password) {
        payload.password = formData.password
      }
      await updateEmployee(payload)
      ElMessage.success('员工信息更新成功')
    } else {
      // 新增模式：后端无专用 add 接口，注册即新增
      payload.password = formData.password
      await registerEmployee(payload)
      ElMessage.success('员工新增成功')
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

// 初始化：编辑模式拉取详情
onMounted(() => {
  if (isEdit.value) {
    fetchEmployeeDetail()
  }
})
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.add-employee-container {
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
  color: $primary-dark;
}

.form-container {
  max-width: 640px;
  padding: 20px 0;
}

.employee-form {
  .el-input {
    width: 300px;
  }
}

/* 头像上传组件样式 */
.avatar-uploader {
  :deep(.el-upload) {
    width: 120px;
    height: 120px;
    border: 1px dashed rgba(10, 132, 255, 0.4);
    border-radius: 8px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.2s;

    &:hover {
      border-color: $primary;
    }
  }
}

.avatar-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(94, 92, 230, 0.5);
  font-size: 12px;
  gap: 6px;

  .el-icon {
    font-size: 24px;
  }
}

.form-buttons {
  margin-top: 30px;
  padding-top: 20px;
  /* 顶部分隔线使用系统蓝半透明 */
  border-top: 1px solid rgba(10, 132, 255, 0.2);

  :deep(.el-button--primary) {
    background-color: $primary;
    border-color: $primary;

    &:hover {
      background-color: $primary-dark;
      border-color: $primary-dark;
    }
  }
}
</style>
