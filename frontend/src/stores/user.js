import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../utils/api'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const userId = computed(() => user.value?.id)

  const login = async (username, password) => {
    const res = await api.post('/user/login', { username, password })
    user.value = res.data.user
    token.value = res.data.token
    localStorage.setItem('user', JSON.stringify(res.data.user))
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  const register = async (username, password, nickname) => {
    const res = await api.post('/user/register', { username, password, nickname })
    return res.data
  }

  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    userId,
    login,
    register,
    logout
  }
})
