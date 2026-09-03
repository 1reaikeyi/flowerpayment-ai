<template>
  <el-card class="avatar-card">
    <template #header>
      <span>更换头像</span>
    </template>
    <div class="avatar-content">
      <el-avatar :src="previewUrl" :size="120" />
      <el-upload
        :show-file-list="false"
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        accept="image/*"
      >
        <el-button type="primary">选择图片</el-button>
      </el-upload>
      <div class="tip">支持 JPG/PNG，单文件不超过 2MB</div>
      <div class="actions">
        <el-button type="primary" :loading="saving" :disabled="!newAvatar" @click="handleSave">保存</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useEmployeeStore } from '@/stores/index.js'
import { uploadFile } from '@/api/file/file.js'
import { updateEmployee } from '@/api/admin/admin.js'
import defaultAvatar from '@/assets/login/avatar.png'

const employeeStore = useEmployeeStore()
const user = computed(() => employeeStore.user || {})
const newAvatar = ref('') // 新上传的保存文件名
const saving = ref(false)

// 预览：优先用新上传的，否则用当前员工的，再退回本地默认
const previewUrl = computed(() => {
  const a = newAvatar.value || user.value.avatar
  if (!a) return defaultAvatar
  return /^https?:\/\//.test(a) ? a : `/api/local?fileName=${a}`
})

const beforeUpload = (file) => {
  if (!/^image\//.test(file.type)) {
    ElMessage.error('只能上传图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片不能超过 2MB')
    return false
  }
  return true
}

const handleUpload = async (option) => {
  const fd = new FormData()
  fd.append('file', option.file)
  try {
    const res = await uploadFile(fd)
    // 后端返回 data = "{绝对路径}::{保存文件名}"，取最后一段作为 avatar
    const saved = String(res.data || '').split('::').pop()
    if (!saved) {
      ElMessage.error('上传失败')
      return
    }
    newAvatar.value = saved
    ElMessage.success('已上传，点击保存生效')
  } catch (e) {
    // 响应拦截器已提示
  }
}

const handleSave = async () => {
  if (!user.value.id) {
    ElMessage.error('缺少员工 ID，请重新登录')
    return
  }
  saving.value = true
  try {
    await updateEmployee({ id: user.value.id, avatar: newAvatar.value })
    // 同步到 store，layout 头像立即生效
    employeeStore.setUser({ ...user.value, avatar: newAvatar.value })
    ElMessage.success('头像已更新')
    newAvatar.value = ''
  } catch (e) {
    // 响应拦截器已提示
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.avatar-card {
  max-width: 600px;
  margin: 20px auto;
  .avatar-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    .tip {
      color: #999;
      font-size: 12px;
    }
    .actions {
      margin-top: 12px;
    }
  }
}
</style>
