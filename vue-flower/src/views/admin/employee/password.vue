<template>
  <el-card class="pwd-card">
    <template #header>
      <span>重置密码</span>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="pwd-form">
      <el-form-item label="新密码" prop="password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirm">
        <el-input v-model="form.confirm" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSubmit">提交</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useEmployeeStore } from '@/stores/index.js'
// 改用专用修改密码接口：PUT /admin/employee/password (Query 参数)
import { updateEmployeePassword } from '@/api/admin/admin.js'

const employeeStore = useEmployeeStore()
const user = computed(() => employeeStore.user || {})
const formRef = ref()
const saving = ref(false)
const form = reactive({ password: '', confirm: '' })

const rules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== form.password) cb(new Error('两次输入不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!user.value.id) {
    ElMessage.error('缺少员工 ID，请重新登录')
    return
  }
  saving.value = true
  try {
    // DELETE /admin/employee/password + RequestBody PasswordDTO
    // 后端从 token 取当前员工 id，前端只需传 { newPassword, confirmPassword }
    await updateEmployeePassword({
      newPassword: form.password,
      confirmPassword: form.confirm
    })
    ElMessage.success('密码已更新，请重新登录')
    // 改完密码让旧 token 失效，清登录态跳登录
    await employeeStore.logout()
    const { default: router } = await import('@/router')
    router.push('/admin/login')
  } catch (e) {
    // 响应拦截器已提示
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.pwd-card {
  max-width: 500px;
  margin: 20px auto;
  .pwd-form {
    padding: 20px 20px 0 0;
  }
}
</style>
