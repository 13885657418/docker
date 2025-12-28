<template>
  <div class="product-detail" v-loading="loading">
    <el-card v-if="product">
      <div class="detail-content">
        <div class="product-image">
          <el-image :src="product.image" fit="cover" />
        </div>
        
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-category">分类：{{ product.categoryName }}</p>
          
          <div class="price-section">
            <span class="label">价格：</span>
            <span class="price">¥{{ product.price?.toFixed(2) }}</span>
          </div>
          
          <div class="stock-section">
            <span class="label">库存：</span>
            <span :class="product.stock > 0 ? 'in-stock' : 'out-of-stock'">
              {{ product.stock > 0 ? `${product.stock}件` : '暂无库存' }}
            </span>
          </div>
          
          <div class="sales-section">
            <span class="label">销量：</span>
            <span>{{ product.sales }}件</span>
          </div>
          
          <el-divider />
          
          <div class="quantity-section">
            <span class="label">数量：</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          </div>
          
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :icon="ShoppingCart"
              :disabled="product.stock <= 0"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
            <el-button size="large" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="description-section">
        <h3>商品描述</h3>
        <p>{{ product.description || '暂无描述' }}</p>
      </div>
    </el-card>
    
    <el-empty v-if="!loading && !product" description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const product = ref(null)
const quantity = ref(1)

const fetchProduct = async () => {
  loading.value = true
  try {
    const res = await api.get(`/product/${route.params.id}`)
    product.value = res.data
  } catch (error) {
    console.error('获取商品详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await cartStore.addToCart(product.value.id, quantity.value)
    ElMessage.success('添加成功')
  } catch (error) {
    console.error('添加购物车失败:', error)
  }
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.product-detail {
  padding: 20px 0;
}

.detail-content {
  display: flex;
  gap: 40px;
}

.product-image {
  flex: 0 0 400px;
}

.product-image .el-image {
  width: 400px;
  height: 400px;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  margin: 0 0 10px;
}

.product-category {
  color: #999;
  margin-bottom: 20px;
}

.price-section,
.stock-section,
.sales-section,
.quantity-section {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.label {
  color: #666;
  width: 60px;
}

.price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
}

.in-stock {
  color: #67c23a;
}

.out-of-stock {
  color: #f56c6c;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 15px;
}

.description-section {
  margin-top: 20px;
}

.description-section h3 {
  margin-bottom: 15px;
}

.description-section p {
  color: #666;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }
  
  .product-image {
    flex: none;
  }
  
  .product-image .el-image {
    width: 100%;
    height: auto;
  }
}
</style>
