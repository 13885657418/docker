<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    
    <el-card v-loading="loading">
      <template v-if="cartStore.cartList.length > 0">
        <el-table :data="cartStore.cartList" style="width: 100%">
          <el-table-column width="50">
            <template #header>
              <el-checkbox
                v-model="allChecked"
                @change="handleCheckAll"
              />
            </template>
            <template #default="{ row }">
              <el-checkbox
                :model-value="row.checked === 1"
                @change="(val) => handleCheck(row.id, val)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="product-cell">
                <el-image
                  :src="row.product?.image"
                  fit="cover"
                  class="product-image"
                />
                <div class="product-info">
                  <p class="product-name">{{ row.product?.name }}</p>
                  <p class="product-price">¥{{ row.product?.price?.toFixed(2) }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="180">
            <template #default="{ row }">
              <el-input-number
                :model-value="row.quantity"
                :min="1"
                :max="row.product?.stock"
                @change="(val) => handleQuantityChange(row.id, val)"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="subtotal">¥{{ row.subtotal?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleRemove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="cart-footer">
          <div class="cart-summary">
            <span>已选 {{ cartStore.checkedItems.length }} 件商品</span>
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ cartStore.cartTotal.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            :disabled="cartStore.checkedItems.length === 0"
            @click="goToCheckout"
          >
            去结算
          </el-button>
        </div>
      </template>
      
      <el-empty v-else description="购物车是空的">
        <el-button type="primary" @click="$router.push('/')">去购物</el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)

const allChecked = computed({
  get: () => {
    return cartStore.cartList.length > 0 && 
           cartStore.cartList.every(item => item.checked === 1)
  },
  set: () => {}
})

const handleCheckAll = async (checked) => {
  await cartStore.checkAll(checked ? 1 : 0)
}

const handleCheck = async (cartId, checked) => {
  await cartStore.updateChecked(cartId, checked ? 1 : 0)
}

const handleQuantityChange = async (cartId, quantity) => {
  if (quantity < 1) return
  await cartStore.updateQuantity(cartId, quantity)
}

const handleRemove = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    await cartStore.removeFromCart(cartId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const goToCheckout = () => {
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('请选择要购买的商品')
    return
  }
  router.push('/checkout')
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loading.value = true
  await cartStore.fetchCartList()
  loading.value = false
})
</script>

<style scoped>
.cart-page {
  padding: 20px 0;
}

.cart-page h2 {
  margin-bottom: 20px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  margin: 0 0 5px;
  font-size: 14px;
}

.product-price {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.cart-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.total-label {
  color: #666;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
