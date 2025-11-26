import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9898/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token 等
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 对于文本响应，直接返回字符串
    if (response.config.responseType === 'text' || typeof response.data === 'string') {
      return response.data
    }
    // 对于 JSON 响应，返回数据
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    let message = '请求失败'
    if (error.response) {
      // 服务器返回了错误响应
      if (typeof error.response.data === 'string') {
        message = error.response.data
      } else if (error.response.data?.message) {
        message = error.response.data.message
      } else {
        message = `请求失败: ${error.response.status} ${error.response.statusText}`
      }
    } else if (error.message) {
      message = error.message
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request

