import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types'

// 统一 axios 实例：/api 前缀 + 响应体解包 + 错误提示。
// 开发期由 vite proxy 转发到 Spring Boot；生产由同域部署承接。
const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body && typeof body.code === 'number') {
      if (body.code !== 200) {
        ElMessage.error(body.message || '请求失败')
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return response.data
  },
  (error) => {
    const message =
      error?.response?.data?.message ?? error?.message ?? '网络异常，请稍后重试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http