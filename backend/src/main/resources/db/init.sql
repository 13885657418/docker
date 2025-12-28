-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shop_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 购物车表
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `checked` TINYINT DEFAULT 1 COMMENT '是否选中: 0-未选中, 1-选中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态: 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
    `pay_time` DATETIME DEFAULT NULL COMMENT '付款时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单项表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 插入初始数据

-- 插入管理员用户 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', 'admin123', '管理员', 1, 1),
('user1', '123456', '测试用户1', 0, 1),
('user2', '123456', '测试用户2', 0, 1);

-- 插入商品分类
INSERT INTO `category` (`name`, `description`, `sort`, `status`) VALUES
('电子产品', '手机、电脑、平板等电子设备', 1, 1),
('服装鞋帽', '男装、女装、鞋子、帽子等', 2, 1),
('食品饮料', '零食、饮料、生鲜等', 3, 1),
('家居用品', '家具、家纺、厨具等', 4, 1),
('图书文具', '图书、办公用品、文具等', 5, 1);

-- 插入商品
INSERT INTO `product` (`name`, `description`, `price`, `stock`, `category_id`, `image`, `status`, `sales`) VALUES
('iPhone 15 Pro', '苹果最新款手机，A17 Pro芯片，钛金属设计', 8999.00, 100, 1, 'https://picsum.photos/400/400?random=1', 1, 50),
('MacBook Pro 14', '搭载M3 Pro芯片，14英寸Liquid视网膜XDR显示屏', 16999.00, 50, 1, 'https://picsum.photos/400/400?random=2', 1, 30),
('AirPods Pro 2', '主动降噪，自适应音频，MagSafe充电盒', 1899.00, 200, 1, 'https://picsum.photos/400/400?random=3', 1, 100),
('男士休闲夹克', '时尚休闲，舒适透气，春秋必备', 299.00, 500, 2, 'https://picsum.photos/400/400?random=4', 1, 200),
('女士连衣裙', '优雅气质，修身显瘦，多色可选', 199.00, 300, 2, 'https://picsum.photos/400/400?random=5', 1, 150),
('运动跑鞋', '轻便透气，缓震舒适，适合跑步健身', 399.00, 400, 2, 'https://picsum.photos/400/400?random=6', 1, 180),
('坚果礼盒', '精选多种坚果，营养健康，送礼佳品', 128.00, 1000, 3, 'https://picsum.photos/400/400?random=7', 1, 500),
('进口牛奶', '新西兰进口纯牛奶，高钙营养', 69.00, 800, 3, 'https://picsum.photos/400/400?random=8', 1, 300),
('现磨咖啡豆', '哥伦比亚进口咖啡豆，香醇浓郁', 89.00, 600, 3, 'https://picsum.photos/400/400?random=9', 1, 250),
('简约书桌', '北欧风格，简约时尚，实木材质', 599.00, 100, 4, 'https://picsum.photos/400/400?random=10', 1, 80),
('四件套床品', '纯棉材质，柔软亲肤，多款花色', 299.00, 200, 4, 'https://picsum.photos/400/400?random=11', 1, 120),
('不锈钢锅具套装', '食品级不锈钢，耐用易清洗', 399.00, 150, 4, 'https://picsum.photos/400/400?random=12', 1, 90),
('编程入门书籍', 'Python编程从入门到精通，适合初学者', 59.00, 500, 5, 'https://picsum.photos/400/400?random=13', 1, 200),
('高档钢笔套装', '德国进口，书写流畅，商务礼品', 199.00, 300, 5, 'https://picsum.photos/400/400?random=14', 1, 100),
('文具收纳盒', '多功能设计，桌面整洁，办公必备', 39.00, 800, 5, 'https://picsum.photos/400/400?random=15', 1, 400);

