import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginEmployee, logoutEmployee, registerEmployee, getEmployeeById } from '@/api/admin/employee.js'
import { getUserIdFromToken } from '@/stores/modules/jwt.js'

export const useEmployeeStore
    = defineStore('flower:admin', () => {
        const token = ref('')

        const setToken = (newToken) => {
            token.value = newToken
        }
        const removeToken = () => {
            token.value = ''
        }

        const userId = computed(() => getUserIdFromToken(token.value))

        const user = ref({})

        const login = async (data) => {
            const res = await loginEmployee(data)
            return res
        }

        const register = async (data) => {
            const res = await registerEmployee(data)
            return res
        }

        const getUser = async () => {
            if (!userId.value) {
                console.error('无法获取用户ID，token可能无效')
                return
            }
            try {
                const res = await getEmployeeById(userId.value)
                user.value = res.data
            } catch (e) {
                // token 失效或后端拒绝时，清掉本地登录态，避免持续弹"服务异常"
                // 这里不再 ElMessage.error（响应拦截器已经提示过），只负责清理状态
                removeToken()
                user.value = {}
                // 跳转登录页（动态 import router 避免循环依赖）
                const { default: router } = await import('@/router')
                if (router.currentRoute.value.path !== '/admin/login') {
                    router.push('/admin/login')
                }
            }
        }

        const setUser = (obj) => {
            user.value = obj
        }

        // 退出登录：调用后端登出接口清 Redis，再清本地登录态
        // 路由跳转由调用方处理（layout 已跳 /admin/login），这里只负责状态
        const logout = async () => {
            try {
                await logoutEmployee()
            } catch (e) {
                // 后端登出失败也清本地状态，避免卡住
            }
            removeToken()
            user.value = {}
        }

        return {
            token, setToken, removeToken,
            user, getUser, setUser, userId,
            login, register, logout
        }
    },
    {
        persist: true
    }
)
