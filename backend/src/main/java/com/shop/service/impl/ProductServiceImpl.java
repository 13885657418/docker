package com.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.exception.BusinessException;
import com.shop.mapper.ProductMapper;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private CategoryService categoryService;

    @Override
    public IPage<Product> getProductPage(Integer pageNum, Integer pageSize, 
                                          String name, Long categoryId, Integer status) {
        logger.info("分页查询商品: pageNum={}, pageSize={}, name={}, categoryId={}, status={}", 
                    pageNum, pageSize, name, categoryId, status);
        
        Page<Product> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectProductPage(page, name, categoryId, status);
    }

    @Override
    public Product getProductDetail(Long id) {
        logger.info("获取商品详情: id={}", id);
        
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 获取分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }
        
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceStock(Long productId, Integer quantity) {
        logger.info("扣减库存: productId={}, quantity={}", productId, quantity);
        
        Product product = getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, productId)
                .ge(Product::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .setSql("sales = sales + " + quantity);
        
        boolean result = update(wrapper);
        if (!result) {
            throw new BusinessException("扣减库存失败，库存不足");
        }
        
        logger.info("扣减库存成功: productId={}, quantity={}", productId, quantity);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(Long productId, Integer quantity) {
        logger.info("恢复库存: productId={}, quantity={}", productId, quantity);
        
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Product::getId, productId)
                .setSql("stock = stock + " + quantity)
                .setSql("sales = sales - " + quantity);
        
        boolean result = update(wrapper);
        logger.info("恢复库存结果: productId={}, quantity={}, result={}", productId, quantity, result);
        return result;
    }
}
