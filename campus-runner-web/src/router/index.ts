import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/tasks'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/tasks',
      name: 'tasks',
      component: () => import('@/views/TasksView.vue'),
      meta: { requiresAuth: true, title: '任务大厅' }
    },
    {
      path: '/task/:id',
      name: 'task-detail',
      component: () => import('@/views/TaskDetailView.vue'),
      meta: { requiresAuth: true, title: '任务详情' }
    },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('@/views/PublishView.vue'),
      meta: { requiresAuth: true, title: '发布任务' }
    },
    {
      path: '/my-tasks',
      name: 'my-tasks',
      component: () => import('@/views/MyTasksView.vue'),
      meta: { requiresAuth: true, title: '我的任务' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true, title: '个人中心' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '管理员后台' }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '用户管理' }
    },
    {
      path: '/admin/tasks',
      name: 'admin-tasks',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '任务管理' }
    },
    {
      path: '/admin/admins',
      name: 'admin-admins',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: '管理员管理' }
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/FavoritesView.vue'),
      meta: { requiresAuth: true, title: '我的收藏' }
    },
    {
      path: '/payments',
      name: 'payments',
      component: () => import('@/views/PaymentsView.vue'),
      meta: { requiresAuth: true, title: '支付记录' }
    },
  ],
})

// 路由守卫
router.beforeEach((to, from) => {
  const userStore = useUserStore()

  // 确保从 localStorage 初始化用户信息
  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }

  // 需要登录但未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }

  // 需要管理员权限但不是管理员
  const userRole = userStore.userInfo?.role
  if (to.meta.requiresAdmin && userRole !== 'admin') {
    return '/'
  }

  // 防止跑腿者访问发布任务页面
  if (to.path === '/publish' && userStore.currentRole === 'runner') {
    return '/tasks'
  }

  // 游客页面但已登录
  if (to.meta.guest && userStore.isLoggedIn) {
    // 管理员登录后跳转到管理员后台，普通用户跳转到首页
    if (userRole === 'admin') {
      return '/admin'
    }
    return '/'
  }
})

export default router
