import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: string
  username: string
  nickname: string
  phone?: string
  email?: string
  avatar?: string
  role: number | string
  creditScore: number
  completionRate: number
}

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  
  // 当前身份角色: publisher(发布者) | runner(跑腿者)
  const currentRole = ref<'publisher' | 'runner'>(
    (localStorage.getItem('currentRole') as 'publisher' | 'runner') || 'publisher'
  )

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const initUserInfo = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
    }
  }
  
  // 切换身份角色
  const switchRole = (role: 'publisher' | 'runner') => {
    currentRole.value = role
    localStorage.setItem('currentRole', role)
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    currentRole,
    setToken,
    setUserInfo,
    logout,
    initUserInfo,
    switchRole
  }
})
