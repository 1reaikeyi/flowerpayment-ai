<template>
  <el-card class="profile-card">
    <template #header>
      <span>当前员工信息</span>
    </template>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="ID">{{ user.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
      <el-descriptions-item label="在职职位">{{ user.work }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ user.sex }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ user.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user.status === 1 ? 'success' : 'danger'">
          {{ user.status === 1 ? '启用' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="头像">
        <el-avatar :src="avatarUrl" :size="80" />
      </el-descriptions-item>
    </el-descriptions>
    <div class="actions">
      <el-button @click="refresh">刷新</el-button>
      <el-button type="primary" @click="$router.push('/admin/employee/avatar')">更换头像</el-button>
      <el-button type="warning" @click="$router.push('/admin/employee/password')">重置密码</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useEmployeeStore } from '@/stores/index.js'
import defaultAvatar from '@/assets/login/avatar.png'

const employeeStore = useEmployeeStore()
const user = computed(() => employeeStore.user || {})

// 头像 URL：后端返回的是文件名，需要拼接 /api/local?fileName=
const avatarUrl = computed(() => {
  const a = user.value.avatar
  if (!a) return defaultAvatar
  return /^https?:\/\//.test(a) ? a : `/api/local?fileName=${a}`
})

const refresh = () => employeeStore.getUser()
onMounted(() => {
  // 进入页面若 store 为空（如刷新），主动拉一次
  if (!user.value.id) employeeStore.getUser()
})
</script>

<style lang="scss" scoped>
.profile-card {
  max-width: 800px;
  margin: 20px auto;
  .actions {
    margin-top: 20px;
    text-align: center;
  }
}
</style>
