<template>
  <div class="orders-admin">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>
    
    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="订单状态">
          <el-select v-model="selectedStatus" placeholder="全部状态" clearable @change="fetchOrders">
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card>
      <el-table :data="orders" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column prop="receiverPhone" label="电话" width="120" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.totalAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="deliverOrder(row)">发货</el-button>
            <el-button link @click="changeStatus(row)">改状态</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchOrders"
        @current-change="fetchOrders"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ currentOrder.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        
        <h4 style="margin: 20px 0 10px;">商品明细</h4>
        <el-table :data="currentOrder.orderItems" size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-image :src="row.productImage" style="width: 40px; height: 40px;" fit="cover" />
                <span>{{ row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ row.productPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥{{ row.subtotal?.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
    
    <!-- 修改状态弹窗 -->
    <el-dialog v-model="statusVisible" title="修改订单状态" width="400px">
      <el-form label-width="80px">
        <el-form-item label="订单号">{{ currentOrder?.orderNo }}</el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width: 100%;">
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="updateStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const selectedStatus = ref(null)

const detailVisible = ref(false)
const statusVisible = ref(false)
const currentOrder = ref(null)
const newStatus = ref(0)

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
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (selectedStatus.value !== null) params.status = selectedStatus.value
    
    const res = await api.get('/order/list', { params })
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取订单失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await api.get(`/order/${row.id}`)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

const deliverOrder = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示')
    await api.put(`/order/deliver/${row.id}`)
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') console.error('发货失败:', error)
  }
}

const changeStatus = (row) => {
  currentOrder.value = row
  newStatus.value = row.status
  statusVisible.value = true
}

const updateStatus = async () => {
  try {
    await api.put('/order/status', { orderId: currentOrder.value.id, status: newStatus.value })
    ElMessage.success('状态更新成功')
    statusVisible.value = false
    fetchOrders()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-admin h2 { margin: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
</style>
