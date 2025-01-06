import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/views/Layout'
import MemberLayout from '@/views/member/MemberLayout'
import AdminLayout from '@/views/admin/AdminLayout'

Vue.use(VueRouter)

// 公共路由
const publicRoutes = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/public/Home')
      },
      {
        path: 'home',
        redirect: '/'
      },
      {
        path: 'scripts',
        name: 'Scripts',
        component: () => import('@/views/public/Scripts')
      },
      {
        path: 'contact',
        name: 'Contact',
        component: () => import('@/views/public/Contact')
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/Login')
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/Register')
      }
    ]
  }
]

// 会员路由
const memberRoutes = [
  {
    path: '/member',
    component: MemberLayout,
    meta: { requiresAuth: true, role: 'member' },
    children: [
      {
        path: 'home',
        name: 'MemberHome',
        component: () => import('@/views/member/Home')
      },
      {
        path: 'balance',
        name: 'Balance',
        component: () => import('@/views/member/Balance')
      },
      {
        path: 'history',
        name: 'PlayHistory',
        component: () => import('@/views/member/PlayHistory')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/member/Profile')
      }
    ]
  }
]

// 管理员路由
const adminRoutes = [
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/admin/Home')
      },
      {
        path: 'members',
        name: 'MemberManagement',
        component: () => import('@/views/admin/MemberManagement')
      },
      {
        path: 'scripts',
        name: 'ScriptManagement',
        component: () => import('@/views/admin/ScriptManagement')
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/admin/AnnouncementManagement')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/member/Profile'),
        meta: { title: '个人设置' }
      }
    ]
  }
]

const router = new VueRouter({
  routes: [...publicRoutes, ...memberRoutes, ...adminRoutes]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('honey-user') || '{}')
  
  // 如果已登录且访问登录页，重定向到对应的首页
  if (to.path === '/login' && user.token) {
    next(user.role === 'admin' ? '/admin/home' : '/member/home')
    return
  }
  
  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!user.token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      const hasPermission = to.matched.every(record => {
        return !record.meta.role || record.meta.role === user.role
      })
      
      if (hasPermission) {
        next()
      } else {
        next('/')
      }
    }
  } else {
    next()
  }
})

export default router