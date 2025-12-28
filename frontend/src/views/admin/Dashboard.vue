<template>
  <div class="dashboard">
    <h2>控制台</h2>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon size="30"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.productCount }}</p>
            <p class="stat-label">商品总数</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon size="30"><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.categoryCount }}</p>
            <p class="stat-label">分类总数</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon size="30"><List /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.orderCount }}</p>
            <p class="stat-label">订单总数</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon size="30"><User /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-value">{{ stats.userCount }}</p>
            <p class="stat-label">用户总数</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="welcome-card">
      <h3>欢迎使用电商后台管理系统</h3>
      <p>您可以在此管理商品、分类和订单。</p>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/admin/products')">管理商品</el-button>
        <el-button @click="$router.push('/admin/orders')">查看订单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Goods, Grid, List, User } from '@element-plus/icons-vue'
import api from '../../utils/api'

const stats = ref({
  productCount: 0,
  categoryCount: 0,
  orderCount: 0,
  userCount: 0
})

const fetchStats = async () => {
  try {
    // 获取商品数量
    const productRes = await api.get('/product/page', { params: { pageSize: 1 } })
    stats.value.productCount = productRes.data?.total || 0
    
    // 获取分类数量
    const categoryRes = await api.get('/category/page', { params: { pageSize: 1 } })
    stats.value.categoryCount = categoryRes.data?.total || 0
    
    // 获取订单数量
    const orderRes = await api.get('/order/list', { params: { pageSize: 1 } })
    stats.value.orderCount = orderRes.data?.total || 0
    
    // 用户数量暂时写死
    stats.value.userCount = 3
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-card .el-card__body {
  display: flex;
  align-items: center;
  width: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.stat-label {
  margin: 5px 0 0;
  color: #999;
  font-size: 14px;
}

.welcome-card {
  margin-top: 20px;
}

.welcome-card h3 {
  margin: 0 0 10px;
}

.welcome-card p {
  color: #666;
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  gap: 10px;
}
</style>
