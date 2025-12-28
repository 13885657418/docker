import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../utils/api'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])
  const userStore = useUserStore()

  const cartCount = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const cartTotal = computed(() => {
    return cartList.value
      .filter(item => item.checked === 1)
      .reduce((sum, item) => sum + item.subtotal, 0)
  })

  const checkedItems = computed(() => {
    return cartList.value.filter(item => item.checked === 1)
  })

  const fetchCartList = async () => {
    if (!userStore.userId) return
    const res = await api.get(`/cart/list/${userStore.userId}`)
    cartList.value = res.data || []
  }

  const addToCart = async (productId, quantity = 1) => {
    await api.post('/cart/add', {
      userId: userStore.userId,
      productId,
      quantity
    })
    await fetchCartList()
  }

  const updateQuantity = async (cartId, quantity) => {
    await api.put('/cart/update', {
      userId: userStore.userId,
      cartId,
      quantity
    })
    await fetchCartList()
  }

  const removeFromCart = async (cartId) => {
    await api.delete(`/cart/remove/${userStore.userId}/${cartId}`)
    await fetchCartList()
  }

  const updateChecked = async (cartId, checked) => {
    await api.put('/cart/check', {
      userId: userStore.userId,
      cartId,
      checked
    })
    await fetchCartList()
  }

  const checkAll = async (checked) => {
    await api.put('/cart/checkAll', {
      userId: userStore.userId,
      checked
    })
    await fetchCartList()
  }

  const clearCart = async () => {
    await api.delete(`/cart/clear/${userStore.userId}`)
    cartList.value = []
  }

  return {
    cartList,
    cartCount,
    cartTotal,
    checkedItems,
    fetchCartList,
    addToCart,
    updateQuantity,
    removeFromCart,
    updateChecked,
    checkAll,
    clearCart
  }
})
