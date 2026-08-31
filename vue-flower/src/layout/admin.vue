<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <!-- Logo区域 -->
      <div class="el-aside__logo1"></div>
      <div class="el-aside__logo2"></div>
      <!-- 侧边栏菜单 -->
      <el-menu
          active-text-color="#ffd04b"
          background-color="#232323"
          :default-active="$route.path"
          text-color="#fff"
          router
      >
        <el-menu-item index="/admin/category">
          <el-icon><Menu /></el-icon>
          <span>菜单分类</span>
        </el-menu-item>

        <el-sub-menu index="/admin/flower">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>鲜花销售</span>
          </template>
          <el-menu-item index="/admin/flower/index">
            <el-icon><Goods /></el-icon>
            <span>单只鲜花</span>
          </el-menu-item>
          <el-menu-item index="/admin/flower/detail">
            <el-icon><Goods /></el-icon>
            <span>送人</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/admin/festival">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>多花礼盒</span>
          </template>
          <el-menu-item index="/admin/festival/index">
            <el-icon><Present /></el-icon>
            <span>多花</span>
          </el-menu-item>
          <el-menu-item index="/admin/festival/detail">
            <el-icon><Present /></el-icon>
            <span>用途</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/admin/order">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/order/pay">
            <el-icon><ShoppingCart /></el-icon>
            <span>主要情况</span>
          </el-menu-item>
          <el-menu-item index="/admin/order/refund">
            <el-icon><ShoppingCart /></el-icon>
            <span>退款情况</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/shop">
          <el-icon><Shop /></el-icon>
          <span>店铺管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/employee">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>

        <el-sub-menu index="/admin/statistics">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>今日数据</span>
          </template>
          <el-menu-item index="/admin/statistics/line">
            <el-icon><TrendCharts /></el-icon>
            <span>鲜花销量</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics/bar">
            <el-icon><Histogram /></el-icon>
            <span>多花礼盒销量</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics/treemap">
            <el-icon><PieChart /></el-icon>
            <span>订单状态分布</span>
          </el-menu-item>
        </el-sub-menu>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <!-- header外层flex，实现左右分离 -->
        <div class="header-container">
          <div class="header-left"></div>
          <!-- 右侧：头像下拉 + 横向菜单 -->
          <div class="header-right">
            <el-dropdown placement="bottom-end" @command="handleCommand">
              <span class="avatar-wrap">
                <!-- 头像 src：后端返回的是文件名，需拼接 /api/local?fileName= 完整 URL；为空时用本地默认头像兜底 -->
                <el-avatar :src="avatarUrl" :size="50" />
                <el-icon><CaretBottom /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="avatar">更换头像</el-dropdown-item>
                  <el-dropdown-item command="password">重置密码</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-menu
                mode="horizontal"
                background-color="#545c64"
                text-color="#fff"
                active-text-color="#ffd04b"
                :default-active="activeIndex"
                @select="handleSelect"
            >
              <el-menu-item index="1">首页</el-menu-item>
              <el-menu-item index="2">用户信息</el-menu-item>
              <el-menu-item index="3">退出登录</el-menu-item>
            </el-menu>
          </div>
        </div>
      </el-header>

      <el-main>
        <router-view></router-view>
      </el-main>
      <el-footer>鲜小花</el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
// 导入 Element Plus 图标组件
import {
  Menu,
  Goods,
  Present,
  ShoppingCart,
  Shop,
  User,
  DataAnalysis,
  TrendCharts,
  Histogram,
  PieChart
} from '@element-plus/icons-vue'
// 导入默认头像图片（用作兜底）
import avatar from '@/assets/login/avatar.png'
// 导入员工状态管理
import { useEmployeeStore } from '@/stores/index.js'
// 导入路由
import { useRouter } from 'vue-router'

const activeIndex = ref('1')
// 初始化员工状态和路由实例
const employeeStore = useEmployeeStore()
// 头像 URL：后端返回的是文件名，需要拼接 /api/local?fileName= 完整 URL；为空时用本地默认头像兜底
const avatarUrl = computed(() => {
  const a = employeeStore.user?.avatar
  if (!a) return avatar
  // 如果已经是完整 URL（http/https 开头）直接用，否则当作后端文件名拼接
  return /^https?:\/\//.test(a) ? a : `/api/local?fileName=${a}`
})
// 组件挂载时获取员工信息
onMounted(() => {
  employeeStore.getUser()
})
const router = useRouter()

// 横向菜单选中事件
const handleSelect = (key) => {
  if (key === '1') {
    // 首页：跳转到 admin 默认页（今日数据）
    router.push('/admin/employee/avatar')
  } else if (key === '2') {
    // 当前员工信息
    router.push('/admin/employee/profile')
  } else if (key === '3') {
    // 退出登录：清登录态后回到 admin 登录页
    employeeStore.logout()
    router.push('/admin/login')
  }
}

// 处理头像下拉菜单命令
const handleCommand = async (key) => {
  router.push(`/admin/employee/${key}`)
}
</script>

<style lang="scss" scoped>

.layout-container {
  height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  position: absolute;
  left: 0;
  top: 0;

  .el-aside {
    background-color: #232323;
    .el-dropdown__box {
      display: flex;
      justify-content: center;
      padding: 10px 0;
    }
    &__logo1 {
      height: 120px;
      background: url('@/assets/login/layout1.png') no-repeat center / 190px auto;
    }
    &__logo2 {
      height: 120px;
      background: url('../assets/login/layout2.png') no-repeat center / 190px auto;
    }
    .el-menu {
      border-right: none;
    }
  }

  .el-header {
    padding: 0 !important;
    background-color: #545c64;

    .header-container {
      display: flex;
      width: 100%;
      height: 100%;
      align-items: center;

      .header-left {
        flex: 1;
      }
      .header-right {
        display: flex;
        align-items: center;

        .avatar-wrap {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #fff;
          cursor: pointer;
          margin-right: 12px;
        }

        :deep(.el-menu--horizontal) {
          border-bottom: none;
        }
      }
    }
  }

  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}
</style>