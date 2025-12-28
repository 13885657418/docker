package com.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.common.Result;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String receiverName = (String) params.get("receiverName");
        String receiverPhone = (String) params.get("receiverPhone");
        String receiverAddress = (String) params.get("receiverAddress");
        String remark = (String) params.getOrDefault("remark", "");

        logger.info("创建订单: userId={}", userId);
        Order order = orderService.createOrder(userId, receiverName, receiverPhone, receiverAddress, remark);
        return Result.success("订单创建成功", order);
    }

    /**
     * 查询用户订单
     */
    @GetMapping("/user/{userId}")
    public Result<IPage<Order>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("查询用户订单: userId={}, status={}", userId, status);
        IPage<Order> orders = orderService.getUserOrders(userId, status, pageNum, pageSize);
        return Result.success(orders);
    }

    /**
     * 查询所有订单（管理员）
     */
    @GetMapping("/list")
    public Result<IPage<Order>> getAllOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("查询所有订单: status={}", status);
        IPage<Order> orders = orderService.getAllOrders(status, pageNum, pageSize);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        logger.info("获取订单详情: orderId={}", orderId);
        Order order = orderService.getOrderDetail(orderId);
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result<Void> cancelOrder(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long orderId = Long.valueOf(params.get("orderId").toString());
        
        logger.info("取消订单: userId={}, orderId={}", userId, orderId);
        orderService.cancelOrder(userId, orderId);
        return Result.success("订单已取消", null);
    }

    /**
     * 支付订单
     */
    @PutMapping("/pay")
    public Result<Void> payOrder(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long orderId = Long.valueOf(params.get("orderId").toString());
        
        logger.info("支付订单: userId={}, orderId={}", userId, orderId);
        orderService.payOrder(userId, orderId);
        return Result.success("支付成功", null);
    }

    /**
     * 发货（管理员）
     */
    @PutMapping("/deliver/{orderId}")
    public Result<Void> deliverOrder(@PathVariable Long orderId) {
        logger.info("订单发货: orderId={}", orderId);
        orderService.deliverOrder(orderId);
        return Result.success("发货成功", null);
    }

    /**
     * 确认收货
     */
    @PutMapping("/confirm")
    public Result<Void> confirmReceive(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long orderId = Long.valueOf(params.get("orderId").toString());
        
        logger.info("确认收货: userId={}, orderId={}", userId, orderId);
        orderService.confirmReceive(userId, orderId);
        return Result.success("确认收货成功", null);
    }

    /**
     * 更新订单状态（管理员）
     */
    @PutMapping("/status")
    public Result<Void> updateOrderStatus(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        logger.info("更新订单状态: orderId={}, status={}", orderId, status);
        orderService.updateOrderStatus(orderId, status);
        return Result.success("状态更新成功", null);
    }
}
