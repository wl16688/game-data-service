import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import Dashboard from '@/views/Dashboard.vue'
import Games from '@/views/Games.vue'
import Users from '@/views/Users.vue'
import Records from '@/views/Records.vue'
import Leaderboards from '@/views/Leaderboards.vue'
import Login from '@/views/Login.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '控制台', icon: 'Odometer' }
      },
      {
        path: 'games',
        name: 'Games',
        component: Games,
        meta: { title: '游戏管理', icon: 'Trophy' }
      },
      {
        path: 'users',
        name: 'Users',
        component: Users,
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'records',
        name: 'Records',
        component: Records,
        meta: { title: '记录管理', icon: 'List' }
      },
      {
        path: 'leaderboards',
        name: 'Leaderboards',
        component: Leaderboards,
        meta: { title: '排行榜维护', icon: 'DataLine' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else if (token && to.path === '/login') {
    next('/')
  } else {
    next()
  }
})

export default router
