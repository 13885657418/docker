package com.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.Result;
import com.shop.entity.Category;
import com.shop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有启用的分类
     */
    @GetMapping("/list")
    public Result<List<Category>> getEnabledCategories() {
        logger.info("获取所有启用的分类");
        List<Category> categories = categoryService.getEnabledCategories();
        return Result.success(categories);
    }

    /**
     * 分页查询所有分类（管理员）
     */
    @GetMapping("/page")
    public Result<IPage<Category>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        logger.info("分页查询分类: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<Category> page = new Page<>(pageNum, pageSize);
        IPage<Category> result = categoryService.page(page);
        return Result.success(result);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        logger.info("获取分类详情: id={}", id);
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 添加分类（管理员）
     */
    @PostMapping("/add")
    public Result<Void> addCategory(@RequestBody Category category) {
        logger.info("添加分类: {}", category.getName());
        category.setDeleted(0);
        categoryService.save(category);
        return Result.success("添加成功", null);
    }

    /**
     * 更新分类（管理员）
     */
    @PutMapping("/update")
    public Result<Void> updateCategory(@RequestBody Category category) {
        logger.info("更新分类: id={}", category.getId());
        categoryService.updateById(category);
        return Result.success("更新成功", null);
    }

    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        logger.info("删除分类: id={}", id);
        categoryService.removeById(id);
        return Result.success("删除成功", null);
    }
}
