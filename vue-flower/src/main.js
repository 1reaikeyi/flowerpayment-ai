import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

import {createPinia} from 'pinia'
import persist from 'pinia-plugin-persistedstate'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCN from 'element-plus/dist/locale/zh-cn.js'

import router from './router'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia.use(persist))
app.use(ElementPlus,{locale:zhCN})
app.use(router)
app.mount('#app')

export default pinia
