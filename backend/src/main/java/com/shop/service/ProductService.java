package com.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.entity.Product;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品
     */
    IPage<Product> getProductPage(Integer pageNum, Integer pageSize, 
                                   String name, Long categoryId, Integer status);

    /**
     * 获取商品详情
     */
    Product getProductDetail(Long id);

    /**
     * 扣减库存
     */
    boolean reduceStock(Long productId, Integer quantity);

    /**
     * 恢复库存
     */
    boolean restoreStock(Long productId, Integer quantity);
}
