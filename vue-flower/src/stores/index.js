import { createPinia } from 'pinia'

const pinia = createPinia()

export { pinia }

// 导出所有 stores
export { useEmployeeStore } from './modules/admin.js'
