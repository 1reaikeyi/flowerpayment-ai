import { createRouter, createWebHistory } from 'vue-router'
import { useEmployeeStore } from '@/stores'
import { parseJWT } from '@/stores/modules/jwt.js'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 根路径：重定向到 admin 默认页
        {
            path: '/',
            redirect: '/admin'
        },
        // Level 1:
        {
            path: '/admin/login',
            component: () => import('@/views/admin/login/admin.vue'),
            meta: { requiresAuth: false }
        },
        // Level 2:
        {
            path: '/user/login',
            component: () => import('@/views/user/login/user.vue'),
            meta: { requiresAuth: false }
        },

        // Level 1 - 分支 A: 管理员体系
        {
            path: '/admin',
            component: () => import('@/layout/admin.vue'), // 管理员专用布局
            redirect: 'employee/profile',
            children: [

                { path: 'category', component: () => import('@/views/admin/category/category.vue') },
                { path: 'category/add', component: () => import('@/views/admin/category/addCategory.vue') },

                { path: 'flower/index', component: () => import('@/views/admin/flower/index.vue') },
                { path: 'flower/add', component: () => import('@/views/admin/flower/addFlower.vue') },
                { path: 'flower/detail', component: () => import('@/views/admin/flower/detail.vue') },

                { path: 'festival/index', component: () => import('@/views/admin/festival/index.vue') },
                { path: 'festival/add', component: () => import('@/views/admin/festival/addFestival.vue') },
                { path: 'festival/detail', component: () => import('@/views/admin/festival/detail.vue') },

                { path: 'statistics/line', component: () => import('@/views/admin/statistics/lineChart.vue') },
                { path: 'statistics/bar', component: () => import('@/views/admin/statistics/barChart.vue') },
                { path: 'statistics/treemap', component: () => import('@/views/admin/statistics/treemapChart.vue') },

                { path: 'shop', component: () => import('@/views/admin/shop/shop.vue') },
                { path: 'order/pay', component: () => import('@/views/admin/order/pay.vue') },
                { path: 'order/refund', component: () => import('@/views/admin/order/refund.vue') },
                { path: 'order/detail', component: () => import('@/views/admin/order/detail.vue') },

                { path: 'employee', component: () => import('@/views/admin/employee/employee.vue') },
                { path: 'employee/add', component: () => import('@/views/admin/employee/addEmployee.vue') },
                { path: 'employee/profile', component: () => import('@/views/admin/employee/profile.vue') },   // 当前员工信息
                { path: 'employee/avatar', component: () => import('@/views/admin/employee/avatar.vue') },    // 更换头像
                { path: 'employee/password', component: () => import('@/views/admin/employee/password.vue') } // 重置密码

            ]
        },

        // Level 2 - 分支 B: user体系
        {
            path: '/user', // 员工体系的根路径
            component: () => import('@/layout/user.vue'), // 员工专用布局
            redirect: '/user/order',
            children: [
                { path: 'category', component: () => import('@/views/user/category/category.vue') },
                { path: 'flower', component: () => import('@/views/user/flower/index.vue') },
                { path: 'festival', component: () => import('@/views/user/festival/index.vue') },
                { path: 'shop', component: () => import('@/views/user/shop/shop.vue') },
                { path: 'shoppingCart', component: () => import('@/views/user/shop/shop.vue') },
                { path: 'order', component: () => import('@/views/user/order/order.vue') }
            ]
        }
    ]
})

// 登录访问拦截 => 默认是直接放行的
// 如果没有token, 且访问的是非登录页，拦截到登录，其他情况正常放行
router.beforeEach((to) => {
    // 直接从localStorage中获取token，避免初始化顺序问题
    const token = localStorage.getItem('flower:admin') ? JSON.parse(localStorage.getItem('flower:admin')).token : ''
    // 校验 token 是否已过期：解析 JWT payload 的 exp（秒级时间戳）与当前时间比较
    let isExpired = false
    if (token) {
        const payload = parseJWT(token)
        if (!payload || (payload.exp && payload.exp * 1000 < Date.now())) {
            isExpired = true
        }
    }
    // token 不存在或已过期：清掉本地登录态
    if (!token || isExpired) {
        localStorage.removeItem('flower:admin')
        if (to.path !== '/admin/login') return '/admin/login'
    }
})

export default router
