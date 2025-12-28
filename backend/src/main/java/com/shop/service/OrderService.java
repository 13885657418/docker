package com.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, String receiverName, String receiverPhone, 
                      String receiverAddress, String remark);

    /**
     * 分页查询用户订单
     */
    IPage<Order> getUserOrders(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 分页查询所有订单（管理员）
     */
    IPage<Order> getAllOrders(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取订单详情
     */
    Order getOrderDetail(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long userId, Long orderId);

    /**
     * 支付订单
     */
    boolean payOrder(Long userId, Long orderId);

    /**
     * 发货（管理员）
     */
    boolean deliverOrder(Long orderId);

    /**
     * 确认收货
     */
    boolean confirmReceive(Long userId, Long orderId);

    /**
     * 更新订单状态（管理员）
     */
    boolean updateOrderStatus(Long orderId, Integer status);
}
