import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'
const baseURL = '/api'

const instance = axios.create({
    baseURL,
    timeout: 10000
})

instance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('flower:admin') ? JSON.parse(localStorage.getItem('flower:admin')).token : ''
        if (token) {
            // 后端 EmployeeRefreshRequestFilter 要求 Authorization 头以 "Bearer " 前缀开头
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (err) => Promise.reject(err)
)

// 响应拦截器
// 后端统一返回 Result { code, msg, data }：成功 code=200，失败 code=500 且 msg 为错误描述
instance.interceptors.response.use(
    (res) => {
        const response = res.data
        if (response.code === 200) {
            return response
        } else {
            // 失败时取后端的 msg 字段（文档为 msg，不是 message）
            ElMessage.error(response.msg || '操作失败')
            return Promise.reject(response)
        }
    },
    (err) => {
        // 401 未登录：跳转到管理员登录页
        if (err.response?.status === 401) {
            router.push('/admin/login')
        }
        // 优先取后端 Result.msg，再退回 axios 默认 message
        ElMessage.error(err.response?.data?.msg || '服务异常')
        return Promise.reject(err)
    }
)

export default instance
export { baseURL }
