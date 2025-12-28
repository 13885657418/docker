package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.Cart;
import com.shop.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list/{userId}")
    public Result<List<Cart>> getCartList(@PathVariable Long userId) {
        logger.info("获取购物车列表: userId={}", userId);
        List<Cart> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Cart> addToCart(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        logger.info("添加商品到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);
        Cart cart = cartService.addToCart(userId, productId, quantity);
        return Result.success("添加成功", cart);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public Result<Void> updateQuantity(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long cartId = Long.valueOf(params.get("cartId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        logger.info("更新购物车商品数量: userId={}, cartId={}, quantity={}", userId, cartId, quantity);
        cartService.updateQuantity(userId, cartId, quantity);
        return Result.success("更新成功", null);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/remove/{userId}/{cartId}")
    public Result<Void> removeFromCart(@PathVariable Long userId, @PathVariable Long cartId) {
        logger.info("删除购物车商品: userId={}, cartId={}", userId, cartId);
        cartService.removeFromCart(userId, cartId);
        return Result.success("删除成功", null);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear/{userId}")
    public Result<Void> clearCart(@PathVariable Long userId) {
        logger.info("清空购物车: userId={}", userId);
        cartService.clearCart(userId);
        return Result.success("清空成功", null);
    }

    /**
     * 更新选中状态
     */
    @PutMapping("/check")
    public Result<Void> updateChecked(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long cartId = Long.valueOf(params.get("cartId").toString());
        Integer checked = Integer.valueOf(params.get("checked").toString());
        
        logger.info("更新选中状态: userId={}, cartId={}, checked={}", userId, cartId, checked);
        cartService.updateChecked(userId, cartId, checked);
        return Result.success();
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/checkAll")
    public Result<Void> checkAll(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer checked = Integer.valueOf(params.get("checked").toString());
        
        logger.info("全选/取消全选: userId={}, checked={}", userId, checked);
        cartService.checkAll(userId, checked);
        return Result.success();
    }

    /**
     * 获取购物车总金额
     */
    @GetMapping("/total/{userId}")
    public Result<BigDecimal> getCartTotal(@PathVariable Long userId) {
        logger.info("获取购物车总金额: userId={}", userId);
        BigDecimal total = cartService.getCartTotal(userId);
        return Result.success(total);
    }
}
