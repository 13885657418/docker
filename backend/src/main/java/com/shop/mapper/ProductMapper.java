package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询商品（带分类名称）
     */
    @Select("<script>" +
            "SELECT p.*, c.name as category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.deleted = 0 " +
            "<if test='name != null and name != \"\"'> AND p.name LIKE CONCAT('%', #{name}, '%') </if>" +
            "<if test='categoryId != null'> AND p.category_id = #{categoryId} </if>" +
            "<if test='status != null'> AND p.status = #{status} </if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Product> selectProductPage(Page<Product> page,
                                     @Param("name") String name,
                                     @Param("categoryId") Long categoryId,
                                     @Param("status") Integer status);
}
