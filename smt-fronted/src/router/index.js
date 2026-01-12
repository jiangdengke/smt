import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('../views/layout/MainLayout.vue'),
    // redirect: { name: 'Workbench' }, // Removed: Let navigation guard handle root redirect
    children: [
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('../views/repair/WorkbenchView.vue'),
        meta: { permission: 'repair:read' }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('../views/repair/AnalyticsView.vue'),
        meta: { permission: 'repair:read' }
      },
      {
        path: 'spare-parts',
        name: 'SpareParts',
        component: () => import('../views/NotImplementedView.vue'),
        meta: { permission: 'repair:read' }
      },
      {
        path: 'repair-attendance',
        name: 'RepairAttendance',
        component: () => import('../views/NotImplementedView.vue'),
        meta: { permission: 'repair:read' }
      },
      {
        path: 'ai-module',
        name: 'AiModule',
        component: () => import('../views/NotImplementedView.vue'),
        meta: { permission: 'repair:read' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/report/DailyReportView.vue'),
        meta: { role: 'PRODUCTION' } // Or permission report:read
      },
      {
        path: 'sys',
        name: 'SystemFields',
        component: () => import('../views/sys/FieldManagementView.vue'),
        meta: { role: 'ADMIN' }
      },
      {
        path: 'users',
        name: 'UserAdmin',
        component: () => import('../views/admin/UserManagementView.vue'),
        meta: { role: 'ADMIN' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // Ensure user is loaded
  if (!authStore.authReady) {
    await authStore.loadUser()
  }

  const isAuthenticated = authStore.isAuthenticated

  if (to.name === 'Login') {
    if (isAuthenticated) {
      next('/')
    } else {
      next()
    }
    return
  }

  if (!isAuthenticated) {
    next('/login')
    return
  }

  // Permission Check
  if (to.meta.role) {
    if (!authStore.hasRole(to.meta.role) && !authStore.isAdmin) {
      // Fallback logic could be better, but for now redirect or stay
      next(false) 
      return 
    }
  }
  
  if (to.meta.permission) {
     // Admin usually has all permissions, but explicit check:
     if (!authStore.hasPermission(to.meta.permission) && !authStore.isAdmin) {
       next(false)
       return
     }
  }

  // If root, redirect based on role
  if (to.path === '/') {
    if (authStore.isAdmin) {
      next({ name: 'UserAdmin' }) // Admin default
      return
    } else if (authStore.isProduction) {
      next({ name: 'Report' })
      return
    } else {
      next({ name: 'Workbench' })
      return
    }
  }

  next()
})

export default router
