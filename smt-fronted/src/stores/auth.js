import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMe, signIn as apiSignIn, signOut as apiSignOut } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref({
    id: null,
    name: '',
    roles: [],
    permissions: []
  })
  
  const authReady = ref(false)

  const isAuthenticated = computed(() => Boolean(currentUser.value?.id))
  const isAdmin = computed(() => hasRole('ADMIN') || hasPermission('sys:write'))
  const isProduction = computed(() => hasRole('PRODUCTION') || hasPermission('report:read'))
  
  const hasRole = (role) => currentUser.value?.roles?.includes(role)
  const hasPermission = (permission) => currentUser.value?.permissions?.includes(permission)

  const loadUser = async () => {
    try {
      const user = await getMe({ allowUnauthorized: true })
      if (user && user.username) {
        currentUser.value = {
          id: user.id,
          name: user.username,
          roles: user.roles || [],
          permissions: user.permissions || []
        }
      } else {
        resetUser()
      }
    } catch (error) {
      resetUser()
    } finally {
      authReady.value = true
    }
  }

  const resetUser = () => {
    currentUser.value = { id: null, name: '', roles: [], permissions: [] }
  }

  const login = async (credentials) => {
    await apiSignIn(credentials)
    await loadUser()
    if (!isAuthenticated.value) {
      throw new Error('登录验证失败')
    }
  }

  const logout = async () => {
    try {
      await apiSignOut()
    } finally {
      resetUser()
    }
  }

  return {
    currentUser,
    authReady,
    isAuthenticated,
    isAdmin,
    isProduction,
    hasRole,
    hasPermission,
    loadUser,
    login,
    logout
  }
})
