package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.entity.Cart;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.mapper.OrderItemMapper;
import com.shop.mapper.OrderMapper;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, String receiverName, String receiverPhone,
                             String receiverAddress, String remark) {
        logger.info("创建订单: userId={}", userId);

        // 获取选中的购物车项
        List<Cart> cartItems = cartService.getCheckedCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        // 校验库存并计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            Product product = cart.getProduct();
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (product.getStatus() == 0) {
                throw new BusinessException("商品【" + product.getName() + "】已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品【" + product.getName() + "】库存不足");
            }
            totalAmount = totalAmount.add(cart.getSubtotal());
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        order.setDeleted(0);
        save(order);

        // 创建订单项并扣减库存
        for (Cart cart : cartItems) {
            Product product = cart.getProduct();

            // 扣减库存
            productService.reduceStock(product.getId(), cart.getQuantity());

            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setSubtotal(cart.getSubtotal());
            orderItemMapper.insert(orderItem);
        }

        // 清空选中的购物车项
        for (Cart cart : cartItems) {
            cartService.removeById(cart.getId());
        }

        logger.info("订单创建成功: orderNo={}", orderNo);
        return order;
    }

    @Override
    public IPage<Order> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        logger.info("查询用户订单: userId={}, status={}", userId, status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null && status >= 0) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = page(page, wrapper);

        // 填充订单项
        for (Order order : orderPage.getRecords()) {
            fillOrderItems(order);
        }

        return orderPage;
    }

    @Override
    public IPage<Order> getAllOrders(Integer status, Integer pageNum, Integer pageSize) {
        logger.info("查询所有订单: status={}", status);

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null && status >= 0) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = page(page, wrapper);

        // 填充订单项
        for (Order order : orderPage.getRecords()) {
            fillOrderItems(order);
        }

        return orderPage;
    }

    @Override
    public Order getOrderDetail(Long orderId) {
        logger.info("获取订单详情: orderId={}", orderId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        fillOrderItems(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long userId, Long orderId) {
        logger.info("取消订单: userId={}, orderId={}", userId, orderId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款的订单");
        }

        // 恢复库存
        fillOrderItems(order);
        for (OrderItem item : order.getOrderItems()) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long userId, Long orderId) {
        logger.info("支付订单: userId={}, orderId={}", userId, orderId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }

        order.setStatus(1); // 已付款
        order.setPayTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliverOrder(Long orderId) {
        logger.info("订单发货: orderId={}", orderId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只能发货已付款的订单");
        }

        order.setStatus(2); // 已发货
        order.setDeliveryTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Long userId, Long orderId) {
        logger.info("确认收货: userId={}, orderId={}", userId, orderId);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("只能确认收货已发货的订单");
        }

        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        logger.info("更新订单状态: orderId={}, status={}", orderId, status);

        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setStatus(status);
        if (status == 1) {
            order.setPayTime(LocalDateTime.now());
        } else if (status == 2) {
            order.setDeliveryTime(LocalDateTime.now());
        } else if (status == 3) {
            order.setFinishTime(LocalDateTime.now());
        }

        return updateById(order);
    }

    /**
     * 填充订单项
     */
    private void fillOrderItems(Order order) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        order.setOrderItems(items);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return timestamp + random;
    }
}
