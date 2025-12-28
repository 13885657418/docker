<template>
  <div class="checkout-page">
    <h2>确认订单</h2>
    
    <el-card class="order-card">
      <h3>收货信息</h3>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="收货地址" prop="receiverAddress">
          <el-input
            v-model="form.receiverAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细收货地址"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="选填，可填写特殊要求"
          />
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="order-card">
      <h3>商品清单</h3>
      <el-table :data="cartStore.checkedItems" style="width: 100%">
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.product?.image" fit="cover" class="product-image" />
              <span>{{ row.product?.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            ¥{{ row.product?.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="数量" width="100" prop="quantity" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">¥{{ row.subtotal?.toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="order-footer">
        <div class="order-total">
          <span>共 {{ cartStore.checkedItems.length }} 件商品</span>
          <span class="total-label">应付金额：</span>
          <span class="total-price">¥{{ cartStore.cartTotal.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">
          提交订单
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/api'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: ''
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const submitOrder = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('请选择要购买的商品')
    return
  }
  
  submitting.value = true
  try {
    await api.post('/order/create', {
      userId: userStore.userId,
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      receiverAddress: form.receiverAddress,
      remark: form.remark
    })
    ElMessage.success('订单提交成功')
    await cartStore.fetchCartList()
    router.push('/orders')
  } catch (error) {
    console.error('提交订单失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('请先选择商品')
    router.push('/cart')
  }
})
</script>

<style scoped>
.checkout-page {
  padding: 20px 0;
}

.checkout-page h2 {
  margin-bottom: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-card h3 {
  margin: 0 0 20px;
  font-size: 16px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.subtotal {
  color: #f56c6c;
}

.order-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
}

.order-total {
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
