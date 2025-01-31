import axios from 'axios'
import router from '@/router'
import { Message } from 'element-ui'

// 创建axios实例
const request = axios.create({
    baseURL: process.env.VUE_APP_BASE_API || 'http://localhost:9090',
    timeout: 30000,
    withCredentials: true
})

// request 拦截器
request.interceptors.request.use(
    config => {
        // 获取用户信息
        let user = JSON.parse(localStorage.getItem("honey-user") || '{}')
        
        // 检查登录时间
        if (user.token) {
            const loginTime = user.loginTime // 登录时间戳
            const currentTime = new Date().getTime()
            const timeLimit = 2 * 60 * 60 * 1000 // 2小时过期
            
            // 如果登录时间超过2小时，清除登录信息
            if (!loginTime || currentTime - loginTime > timeLimit) {
                localStorage.removeItem('honey-user')
                location.href = '/login'
                return Promise.reject(new Error('登录已过期，请重新登录'))
            }
            
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
        // 预期内的业务错误，不需要抛出异常
        const businessErrors = ['400', '403', '1001'] // 1001 假设是注册相关的错误码
        
        if (res.code === '401') {
            Message.error('请重新登录')
            localStorage.removeItem('honey-user')
            router.push("/login")
            return Promise.reject(new Error('未登录或登录过期'))
        } else if (businessErrors.includes(res.code)) {
            // 显示错误消息但不抛出异常
            Message.error(res.msg || '操作失败')
            return {
                code: res.code,
                msg: res.msg,
                success: false,
                data: res.data
            }
        } else if (res.code !== '200') {
            Message.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || '系统错误'))
        }
        return {
            code: res.code,
            msg: res.msg,
            success: true,
            data: res.data
        }
    },
    error => {
        // 网络错误等非业务错误才记录到控制台
        if (process.env.NODE_ENV === 'development') {
            console.error('网络请求错误：', error)
        }
        
        if (error.response) {
            switch (error.response.status) {
                case 404:
                    Message.error('请求的接口不存在，请检查API路径')
                    break
                case 403:
                    Message.error('没有权限访问该资源')
                    break
                case 401:
                    Message.error('请重新登录')
                    localStorage.removeItem('honey-user')
                    router.push("/login")
                    break
                case 500:
                    Message.error('服务器内部错误')
                    break
                default:
                    Message.error(`服务器错误: ${error.response.status}`)
            }
        } else if (error.request) {
            Message.error('无法连接到服务器，请检查网络和服务器状态')
        } else {
            Message.error('请求失败：' + error.message)
        }
        
        return Promise.reject(error)
    }
)

export default request
