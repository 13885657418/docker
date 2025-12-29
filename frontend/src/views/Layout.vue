<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon size="24"><ShoppingCart /></el-icon>
          <span>电商 统</span>
        </div>
        
        <div class="nav-menu">
          <el-menu mode="horizontal" :ellipsis="false" router>
            <el-menu-item index="/">首页</el-menu-item>
          </el-menu>
        </div>
        
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-badge :value="cartStore.cartCount" :hidden="cartStore.cartCount === 0">
              <el-button :icon="ShoppingCart" circle @click="$router.push('/cart')" />
            </el-badge>
            
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32">{{ userStore.user?.nickname?.charAt(0) || 'U' }}</el-avatar>
                <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
            <el-button @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    
    <!-- 主要内容区域 -->
    <el-main class="main">
      <router-view />
    </el-main>
    
    <!-- 底部 -->
    <el-footer class="footer">
      <p>© 2024 电商系统 - Java Web课程作业</p>
    </el-footer>
  </div>
</template>

<script setup>
import { ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.fetchCartList()
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'orders':
      router.push('/orders')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  margin-left: 40px;
}

.nav-menu .el-menu {
  border: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  color: #333;
}

.main {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
}

.footer {
  background: #333;
  color: #fff;
  text-align: center;
  padding: 20px;
}
</style>
