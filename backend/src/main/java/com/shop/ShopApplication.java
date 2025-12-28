package com.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 电商系统启动类
 */
@SpringBootApplication
@MapperScan("com.shop.mapper")
@EnableTransactionManagement
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
        System.out.println("========================================");
        System.out.println("     电商系统启动成功！");
        System.out.println("     访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
