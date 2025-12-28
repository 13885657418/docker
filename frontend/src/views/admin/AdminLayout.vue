<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="aside">
        <div class="logo">
          <el-icon size="20"><Setting /></el-icon>
          <span>管理后台</span>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/admin">
            <el-icon><DataAnalysis /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-button link @click="$router.push('/')">
              <el-icon><House /></el-icon> 返回前台
            </el-button>
          </div>
          <div class="header-right">
            <span class="admin-name">{{ userStore.user?.nickname }}</span>
            <el-button link @click="logout">退出登录</el-button>
          </div>
        </el-header>
        
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Setting, DataAnalysis, Goods, Grid, List, House } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const logout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  if (!userStore.isLoggedIn || !userStore.isAdmin) {
    ElMessage.warning('请先使用管理员账号登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-layout .el-container {
  height: 100%;
}

.aside {
  background: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3f5369;
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-name {
  color: #666;
}

.main {
  background: #f5f5f5;
}
</style>
