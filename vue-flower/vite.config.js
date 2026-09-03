import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },

  // 全局注入 SCSS 变量（使用 @use 替代已弃用的 @import）
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/_theme" as *;`,
        quietDeps: true
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 经此代理转发到后端 SpringBoot（localhost:8080），后端 WebConfig 把 /image/** 映射到 file:ku/image/
      '/image': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
