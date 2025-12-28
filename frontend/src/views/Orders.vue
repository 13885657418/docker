<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    
    <!-- 订单状态筛选 -->
    <el-radio-group v-model="selectedStatus" @change="fetchOrders" class="status-filter">
      <el-radio-button :label="-1">全部</el-radio-button>
      <el-radio-button :label="0">待付款</el-radio-button>
      <el-radio-button :label="1">已付款</el-radio-button>
      <el-radio-button :label="2">已发货</el-radio-button>
      <el-radio-button :label="3">已完成</el-radio-button>
      <el-radio-button :label="4">已取消</el-radio-button>
    </el-radio-group>
    
    <div v-loading="loading">
      <template v-if="orders.length > 0">
        <el-card v-for="order in orders" :key="order.id" class="order-card">
          <template #header>
            <div class="order-header">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </template>
          
          <div class="order-items">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item">
              <el-image :src="item.productImage" fit="cover" class="item-image" />
              <div class="item-info">
                <p class="item-name">{{ item.productName }}</p>
                <p class="item-price">¥{{ item.productPrice?.toFixed(2) }} × {{ item.quantity }}</p>
              </div>
              <span class="item-subtotal">¥{{ item.subtotal?.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              订单金额：<span class="total-price">¥{{ order.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <el-button v-if="order.status === 0" type="primary" @click="payOrder(order)">
                立即支付
              </el-button>
              <el-button v-if="order.status === 0" @click="cancelOrder(order)">
                取消订单
              </el-button>
              <el-button v-if="order.status === 2" type="success" @click="confirmReceive(order)">
                确认收货
              </el-button>
              <el-button link @click="viewDetail(order)">查看详情</el-button>
            </div>
          </div>
        </el-card>
      </template>
      
      <el-empty v-else description="暂无订单" />
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
    
    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedStatus = ref(-1)
const detailVisible = ref(false)
const currentOrder = ref(null)

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'primary' },
  2: { text: '已发货', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (selectedStatus.value >= 0) {
      params.status = selectedStatus.value
    }
    const res = await api.get(`/order/user/${userStore.userId}`, { params })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const payOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要支付该订单吗？', '提示')
    await api.put('/order/pay', {
      userId: userStore.userId,
      orderId: order.id
    })
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await api.put('/order/cancel', {
      userId: userStore.userId,
      orderId: order.id
    })
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
    }
  }
}

const confirmReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示')
    await api.put('/order/confirm', {
      userId: userStore.userId,
      orderId: order.id
    })
    ElMessage.success('确认收货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
    }
  }
}

const viewDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 20px 0;
}

.orders-page h2 {
  margin-bottom: 20px;
}

.status-filter {
  margin-bottom: 20px;
}

.order-card {
  margin-bottom: 15px;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-no {
  font-weight: bold;
}

.order-time {
  color: #999;
  font-size: 14px;
}

.order-items {
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.item-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 5px;
}

.item-price {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.item-subtotal {
  color: #f56c6c;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
}

.order-total {
  font-size: 14px;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
