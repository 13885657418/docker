package com.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.common.Result;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<IPage<Product>> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        logger.info("分页查询商品: pageNum={}, pageSize={}, name={}, categoryId={}, status={}",
                pageNum, pageSize, name, categoryId, status);
        IPage<Product> result = productService.getProductPage(pageNum, pageSize, name, categoryId, status);
        return Result.success(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        logger.info("获取商品详情: id={}", id);
        Product product = productService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 添加商品（管理员）
     */
    @PostMapping("/add")
    public Result<Void> addProduct(@RequestBody Product product) {
        logger.info("添加商品: {}", product.getName());
        product.setDeleted(0);
        product.setSales(0);
        productService.save(product);
        return Result.success("添加成功", null);
    }

    /**
     * 更新商品（管理员）
     */
    @PutMapping("/update")
    public Result<Void> updateProduct(@RequestBody Product product) {
        logger.info("更新商品: id={}", product.getId());
        productService.updateById(product);
        return Result.success("更新成功", null);
    }

    /**
     * 删除商品（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        logger.info("删除商品: id={}", id);
        productService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上架商品（管理员）
     */
    @PutMapping("/online/{id}")
    public Result<Void> onlineProduct(@PathVariable Long id) {
        logger.info("上架商品: id={}", id);
        Product product = new Product();
        product.setId(id);
        product.setStatus(1);
        productService.updateById(product);
        return Result.success("上架成功", null);
    }

    /**
     * 下架商品（管理员）
     */
    @PutMapping("/offline/{id}")
    public Result<Void> offlineProduct(@PathVariable Long id) {
        logger.info("下架商品: id={}", id);
        Product product = new Product();
        product.setId(id);
        product.setStatus(0);
        productService.updateById(product);
        return Result.success("下架成功", null);
    }
}
