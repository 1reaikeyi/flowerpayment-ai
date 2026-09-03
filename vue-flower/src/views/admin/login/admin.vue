<template>
  <div class="login">
    <div class="login-box">
      <!-- 修正路径：admin.vue 在 src/views/admin/login/，需要向上三级到 src/ -->
      <img src="../../../assets/login/login.png" style="width: 500px; height: 500px" alt="饿了吗" />
      <div class="login-form">
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <!-- 修正路径：admin.vue 在 src/views/admin/login/，需要向上三级到 src/ -->
            <img
                src="../../../assets/login/login-ico.png"
                style="width: 250px; height: 100px"
                alt=""
            />
          </div>
          <!-- 字段名与后端 LoginDTO.username 对齐 -->
          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                type="text"
                auto-complete="off"
                placeholder="账号"
                prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="el-icon-lock"
                @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button
                :loading="loadLogIn"
                class="login-btn"
                size="default"
                type="primary"
                style="width: 100%"
                @click="handleLogin"
            >
              <span v-if="!loadLogIn">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginEmployee } from '@/api/admin/admin.js'
import { useEmployeeStore } from '@/stores/index.js'

// 加载状态
const loadLogIn = ref(false)

// 登录表单 - 字段名与后端 LoginDTO (username, password) 对齐
const loginForm = reactive({
  username: '',
  password: '',
})

const isRegister = ref(false)
// 修复：reactive 不能用 .value 赋值
watch(isRegister, () => {
  loginForm.username = ''
  loginForm.password = ''
})

// 表单实例
const loginFormRef = ref()

// 表单校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码必须在6位以上', trigger: 'blur' }
  ]
}

const router = useRouter()
const jwt = useEmployeeStore()

// 登录方法 - 核心修复：去掉 loginForm.value
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loadLogIn.value = true

    // 重点：直接传 loginForm，不要 .value
    const res = await loginEmployee(loginForm)
    jwt.setToken(res.data)
    ElMessage.success('登录成功')
    // 登录后进入管理后台首页（会被路由 redirect 到 /admin/statistics）
    router.push('/admin')

  } catch (error) {
    console.error('登录失败', error)
    ElMessage.error('登录失败，请重试')
  } finally {
    loadLogIn.value = false
  }
}
</script>

<style lang="scss" scoped>
/* 系统色板变量已全局注入，可直接使用 $sys-blue、$primary 等 */

.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 页面深背景使用系统靛蓝 */
  background-color: $sys-purple;
}

.login-box {
  width: 1000px;
  height: 500px;
  border-radius: 8px;
  display: flex;
  img {
    width: 60%;
    height: auto;
  }
}

.title {
  margin: 0px auto 10px auto;
  text-align: left;
  /* 标题文字使用系统蓝半透明 */
  color: rgba(10, 132, 255, 0.5);
}

.login-form {
  /* 表单背景使用系统蓝极浅透明度 */
  background: rgba($sys-orange, 1.0);
  width: 50%;
  border-radius: 0px 8px 8px 0px;
  display: flex;
  justify-content: center;
  align-items: center;
  .el-form {
    width: 214px;
    height: 307px;
  }
  .el-form-item {
    margin-bottom: 30px;
  }
  .el-form-item.is-error .el-input__inner {
    border: 0 !important;
    /* 错误边框使用系统红 */
    border-bottom: 1px solid $sys-red !important;
    background: rgba(10, 132, 255, 0.05) !important;
  }
  .input-icon {
    height: 32px;
    width: 18px;
    margin-left: -2px;
  }
  .el-input__inner {
    border: 0;
    /* 输入框下边框使用系统蓝半透明 */
    border-bottom: 1px solid rgba(10, 132, 255, 0.2);
    border-radius: 0;
    font-size: 12px;
    font-weight: 400;
    /* 输入文字使用系统靛蓝 */
    color: $sys-indigo;
    height: 32px;
    line-height: 32px;
  }
  .el-input__prefix {
    left: 0;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 26px;
  }
  .el-input__inner::placeholder {
    /* 占位符使用系统靛蓝低透明度 */
    color: rgba(94, 92, 230, 0.4);
  }
  .el-form-item--medium .el-form-item__content {
    line-height: 32px;
  }
  .el-input--medium .el-input__icon {
    line-height: 32px;
  }
}

.login-btn {
  border-radius: 17px;
  padding: 11px 20px !important;
  margin-top: 10px;
  font-weight: 500;
  font-size: 12px;
  border: 0;
  font-weight: 500;
  /* 按钮文字使用系统靛蓝 */
  color: $sys-indigo;
  /* 登录按钮使用系统黄 */
  background-color: $sys-yellow;
  &:hover,
  &:focus {
    background-color: $sys-yellow;
    /* hover 时文字使用系统蓝 */
    color: $sys-blue;
  }
}
.login-form-title {
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
  .title-label {
    font-weight: 500;
    font-size: 20px;
    /* 标题文字使用系统靛蓝 */
    color: $sys-indigo;
    margin-left: 10px;
  }
}
</style>