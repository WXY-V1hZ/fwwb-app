import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [
    vue(),
  ],
  server: {
    host: '127.0.0.1',
    port: 3001,
    hmr: true,
    proxy: {
      "/api": {
        target: "http://localhost:7001",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/'),
      }
    }
  }
})
