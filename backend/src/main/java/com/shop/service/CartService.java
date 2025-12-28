package com.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<Cart> {

    /**
     * 获取用户购物车列表
     */
    List<Cart> getCartList(Long userId);

    /**
     * 添加商品到购物车
     */
    Cart addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     */
    boolean updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     */
    boolean removeFromCart(Long userId, Long cartId);

    /**
     * 清空购物车
     */
    boolean clearCart(Long userId);

    /**
     * 更新选中状态
     */
    boolean updateChecked(Long userId, Long cartId, Integer checked);

    /**
     * 全选/取消全选
     */
    boolean checkAll(Long userId, Integer checked);

    /**
     * 获取购物车总金额
     */
    BigDecimal getCartTotal(Long userId);

    /**
     * 获取选中的购物车项
     */
    List<Cart> getCheckedCartItems(Long userId);
}
