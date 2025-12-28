<template>
  <div class="home">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input
        v-model="searchName"
        placeholder="搜索商品..."
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <el-radio-group v-model="selectedCategory" @change="handleSearch">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button v-for="cat in categories" :key="cat.id" :label="cat.id">
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品列表 -->
    <div class="product-grid" v-loading="loading">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        shadow="hover"
        @click="goToDetail(product.id)"
      >
        <img :src="product.image" class="product-image" :alt="product.name" />
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-category">{{ product.categoryName }}</p>
          <div class="product-footer">
            <span class="product-price">¥{{ product.price.toFixed(2) }}</span>
            <span class="product-sales">销量: {{ product.sales }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchProducts"
        @current-change="fetchProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import api from '../utils/api'

const router = useRouter()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(8)
const searchName = ref('')
const selectedCategory = ref(null)

const fetchCategories = async () => {
  try {
    const res = await api.get('/category/list')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: 1 // 只查询上架商品
    }
    if (searchName.value) {
      params.name = searchName.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    const res = await api.get('/product/page', { params })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchProducts()
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.search-area {
  max-width: 500px;
  margin: 0 auto 20px;
}

.search-input {
  width: 100%;
}

.category-filter {
  margin-bottom: 20px;
  text-align: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 300px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  padding: 10px 0;
}

.product-name {
  font-size: 16px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  color: #999;
  font-size: 12px;
  margin: 0 0 8px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.product-sales {
  color: #999;
  font-size: 12px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
