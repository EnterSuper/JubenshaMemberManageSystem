import axios from 'axios'
import router from '@/router'
import { Message } from 'element-ui'

// 创建axios实例
const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 30000,
    withCredentials: true
})

// request 拦截器
request.interceptors.request.use(
    config => {
        // 设置token
        let user = JSON.parse(localStorage.getItem("honey-user") || '{}')
        if (user.token) {
            config.headers['token'] = user.token
        }
        return config
    },
    error => {
        console.error('请求错误：', error)
        Message.error('请求配置错误')
        return Promise.reject(error)
    }
)

// response 拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === '401') {
            Message.error('请重新登录')
            router.push("/login")
            return Promise.reject(new Error('未登录或登录过期'))
        } else if (res.code !== '200') {
            Message.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || '系统错误'))
        }
        return res
    },
    error => {
        console.error('响应错误：', error)
        if (error.response) {
            if (error.response.status === 404) {
                Message.error('请求的资源不存在')
            } else if (error.response.status === 403) {
                Message.error('没有权限访问该资源')
            } else if (error.response.status === 401) {
                Message.error('请重新登录')
                router.push("/login")
            } else {
                Message.error(`服务器错误: ${error.response.status}`)
            }
        } else if (error.request) {
            Message.error('无法连接到服务器，请检查网络')
        } else {
            Message.error('请求失败：' + error.message)
        }
        return Promise.reject(error)
    }
)

export default request
