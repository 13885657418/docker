package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.mapper.CartMapper;
import com.shop.service.CartService;
import com.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private ProductService productService;

    @Override
    public List<Cart> getCartList(Long userId) {
        logger.info("获取用户购物车列表: userId={}", userId);
        
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = list(wrapper);
        
        // 填充商品信息
        for (Cart cart : cartList) {
            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                cart.setProduct(product);
                cart.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
        }
        
        return cartList;
    }

    @Override
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        logger.info("添加商品到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);
        
        // 检查商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == 0) {
            throw new BusinessException("商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        
        // 检查购物车中是否已有该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        Cart existCart = getOne(wrapper);
        
        if (existCart != null) {
            // 已存在，增加数量
            int newQuantity = existCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new BusinessException("库存不足");
            }
            existCart.setQuantity(newQuantity);
            updateById(existCart);
            logger.info("更新购物车商品数量: cartId={}, newQuantity={}", existCart.getId(), newQuantity);
            return existCart;
        } else {
            // 不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setChecked(1);
            save(cart);
            logger.info("新增购物车商品: cartId={}", cart.getId());
            return cart;
        }
    }

    @Override
    public boolean updateQuantity(Long userId, Long cartId, Integer quantity) {
        logger.info("更新购物车商品数量: userId={}, cartId={}, quantity={}", userId, cartId, quantity);
        
        Cart cart = getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        
        // 检查库存
        Product product = productService.getById(cart.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (quantity > product.getStock()) {
            throw new BusinessException("库存不足");
        }
        
        cart.setQuantity(quantity);
        return updateById(cart);
    }

    @Override
    public boolean removeFromCart(Long userId, Long cartId) {
        logger.info("删除购物车商品: userId={}, cartId={}", userId, cartId);
        
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getId, cartId)
                .eq(Cart::getUserId, userId);
        return remove(wrapper);
    }

    @Override
    public boolean clearCart(Long userId) {
        logger.info("清空购物车: userId={}", userId);
        
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        return remove(wrapper);
    }

    @Override
    public boolean updateChecked(Long userId, Long cartId, Integer checked) {
        logger.info("更新选中状态: userId={}, cartId={}, checked={}", userId, cartId, checked);
        
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getId, cartId)
                .eq(Cart::getUserId, userId)
                .set(Cart::getChecked, checked);
        return update(wrapper);
    }

    @Override
    public boolean checkAll(Long userId, Integer checked) {
        logger.info("全选/取消全选: userId={}, checked={}", userId, checked);
        
        LambdaUpdateWrapper<Cart> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .set(Cart::getChecked, checked);
        return update(wrapper);
    }

    @Override
    public BigDecimal getCartTotal(Long userId) {
        List<Cart> checkedItems = getCheckedCartItems(userId);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Cart cart : checkedItems) {
            if (cart.getProduct() != null) {
                BigDecimal subtotal = cart.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cart.getQuantity()));
                total = total.add(subtotal);
            }
        }
        
        return total;
    }

    @Override
    public List<Cart> getCheckedCartItems(Long userId) {
        logger.info("获取选中的购物车项: userId={}", userId);
        
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getChecked, 1);
        List<Cart> cartList = list(wrapper);
        
        // 填充商品信息
        for (Cart cart : cartList) {
            Product product = productService.getById(cart.getProductId());
            if (product != null) {
                cart.setProduct(product);
                cart.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
        }
        
        return cartList;
    }
}
