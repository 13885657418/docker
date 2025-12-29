# 电商数据管理系统

## 项目简介

基于 Docker 容器化部署的全栈电商管理系统，采用前后端分离架构，实现商品管理、用户管理、订单管理、购物车等核心电商功能。

## 技术栈

| 层级 | 技术选型 | 版本 |
|------|----------|------|
| 前端 | Vue 3 + Vite + Element Plus | Vue 3.x |
| 后端 | Spring Boot + MyBatis-Plus | 3.2.0 |
| 数据库 | MySQL | 8.0 |
| 容器化 | Docker + Docker Compose | 24.x |
| 反向代理 | Nginx | Alpine |
| CI/CD | GitHub Actions | - |

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户访问层                                │
│                     http://localhost:3000                       │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    前端服务 (Nginx + Vue3)                       │
│                    Container: shop-frontend                      │
│                    Port: 3000:80                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  静态资源服务 + API反向代理 (/api → backend:8080)         │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ /api/*
┌─────────────────────────────────────────────────────────────────┐
│                   后端服务 (Spring Boot)                         │
│                   Container: shop-backend                        │
│                   Port: 8088:8080                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  RESTful API: 用户/商品/订单/购物车/分类                   │    │
│  │  技术: Spring Boot 3.2 + MyBatis-Plus 3.5.5              │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼ JDBC
┌─────────────────────────────────────────────────────────────────┐
│                     数据库服务 (MySQL 8.0)                       │
│                     Container: shop-mysql                        │
│                     Port: 3306:3306                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  数据库: shop_db                                          │    │
│  │  表: user, product, category, cart, orders, order_item   │    │
│  │  数据持久化: mysql_data volume                            │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Docker Network: shop-network                 │
│                     Subnet: 172.28.0.0/16                        │
└─────────────────────────────────────────────────────────────────┘
```

## 快速启动

### 前置条件

- Docker 20.10+
- Docker Compose 2.0+
- Git

### 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/your-repo/shop-system.git
cd shop-system

# 2. 启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:3000 | 用户界面 |
| 后端API | http://localhost:8088/api | RESTful接口 |
| 数据库 | localhost:3306 | MySQL数据库 |

### 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | 123456 |

## 项目结构

```
shop-system/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/com/shop/
│   │   ├── controller/         # REST控制器
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── config/             # 配置类
│   │   ├── common/             # 通用类
│   │   └── exception/          # 异常处理
│   ├── src/main/resources/
│   │   ├── db/init.sql         # 数据库初始化脚本
│   │   └── application.yml     # 应用配置
│   ├── Dockerfile              # 后端Docker构建文件
│   └── pom.xml                 # Maven配置
├── frontend/                   # 前端Vue3项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   └── utils/              # 工具函数
│   ├── Dockerfile              # 前端Docker构建文件
│   ├── nginx.conf              # Nginx主配置
│   └── default.conf            # Nginx站点配置
├── mysql/
│   └── my.cnf                  # MySQL配置
├── k8s/                        # Kubernetes配置
│   ├── namespace.yaml
│   ├── mysql-deployment.yaml
│   ├── backend-deployment.yaml
│   └── frontend-deployment.yaml
├── .github/workflows/          # CI/CD配置
│   └── ci-cd.yml
├── docker-compose.yml          # Docker Compose编排
├── docker-compose.monitor.yml  # 监控服务编排
├── .env                        # 环境变量
└── README.md                   # 项目文档
```

## Dockerfile 构建说明

### 后端 Dockerfile（多阶段构建）

```dockerfile
# 阶段1: 构建阶段 - 使用Maven镜像编译项目
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
# 阶段2: 运行阶段 - 使用精简JRE镜像
FROM eclipse-temurin:17-jre-alpine
```

**设计要点：**

1. **多阶段构建**：分离构建环境和运行环境，最终镜像不包含Maven和源代码
2. **层缓存优化**：先复制pom.xml下载依赖，再复制源代码，减少重复构建时间
3. **安全性**：创建非root用户(appuser)运行应用
4. **健康检查**：90秒启动期后，每30秒检查API可用性

### 前端 Dockerfile（多阶段构建）

```dockerfile
# 阶段1: 构建阶段 - Node环境编译Vue项目
FROM node:18-alpine AS builder
# 阶段2: 运行阶段 - Nginx托管静态资源
FROM nginx:alpine
```

**设计要点：**

1. **精简镜像**：使用Alpine版本，体积小于100MB
2. **API代理**：Nginx配置反向代理到后端服务
3. **健康检查**：检查/health端点确保服务可用

## 健康检查机制

### 健康检查配置详解

```yaml
# docker-compose.yml 中的健康检查配置
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/api/product/page"]
  interval: 30s      # 检查间隔
  timeout: 10s       # 超时时间
  start_period: 90s  # 启动等待期
  retries: 5         # 重试次数
```

### 服务启动顺序

```
MySQL (60s健康检查) 
    ↓ healthy
Backend (90s启动期 + 健康检查)
    ↓ healthy  
Frontend (10s启动期 + 健康检查)
    ↓ ready
用户可访问
```

### 健康检查状态

| 状态 | 说明 |
|------|------|
| starting | 服务启动中，在start_period内 |
| healthy | 健康检查通过 |
| unhealthy | 连续retries次检查失败 |

## API 接口文档

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /register | 用户注册 |
| POST | /login | 用户登录 |
| GET | /info/{id} | 获取用户信息 |
| PUT | /update | 更新用户信息 |

### 商品模块 `/api/product`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页查询商品 |
| GET | /{id} | 获取商品详情 |
| POST | /add | 添加商品 |
| PUT | /update | 更新商品 |
| DELETE | /{id} | 删除商品 |
| PUT | /online/{id} | 上架商品 |
| PUT | /offline/{id} | 下架商品 |

### 订单模块 `/api/order`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /create | 创建订单 |
| GET | /user/{userId} | 查询用户订单 |
| GET | /list | 查询所有订单 |
| PUT | /pay | 支付订单 |
| PUT | /cancel | 取消订单 |
| PUT | /deliver/{orderId} | 发货 |
| PUT | /confirm | 确认收货 |

### 购物车模块 `/api/cart`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list/{userId} | 获取购物车 |
| POST | /add | 添加到购物车 |
| PUT | /update | 更新数量 |
| DELETE | /remove/{userId}/{cartId} | 删除商品 |
| DELETE | /clear/{userId} | 清空购物车 |

### 分类模块 `/api/category`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 获取分类列表 |
| GET | /page | 分页查询 |
| POST | /add | 添加分类 |
| PUT | /update | 更新分类 |
| DELETE | /{id} | 删除分类 |

## 数据库设计

### E-R 关系图

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│   User   │     │  Orders  │     │OrderItem │
│──────────│     │──────────│     │──────────│
│ id (PK)  │←────│ user_id  │     │ order_id │───→Orders.id
│ username │     │ order_no │←────│product_id│───→Product.id
│ password │     │ status   │     │ quantity │
│ role     │     │ total    │     │ subtotal │
└──────────┘     └──────────┘     └──────────┘
                       ↑
┌──────────┐     ┌─────┴────┐
│ Category │     │   Cart   │
│──────────│     │──────────│
│ id (PK)  │←────│ user_id  │───→User.id
│ name     │     │product_id│───→Product.id
└──────────┘     │ quantity │
       ↑         └──────────┘
┌──────┴───┐
│ Product  │
│──────────│
│ id (PK)  │
│category_id
│name,price│
│stock,sales
└──────────┘
```

### 表结构说明

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | id, username, password, role, status |
| category | 分类表 | id, name, description, sort |
| product | 商品表 | id, name, price, stock, category_id |
| cart | 购物车 | id, user_id, product_id, quantity |
| orders | 订单表 | id, order_no, user_id, total_amount, status |
| order_item | 订单项 | id, order_id, product_id, quantity |

## 故障排查

### 常见问题

#### 1. 后端无法连接数据库

```bash
# 检查MySQL容器状态
docker-compose ps mysql

# 查看MySQL日志
docker-compose logs mysql

# 手动测试连接
docker exec -it shop-mysql mysql -uroot -proot123
```

#### 2. 前端无法访问后端API

```bash
# 检查后端健康状态
curl http://localhost:8088/api/product/page

# 检查Nginx配置
docker exec -it shop-frontend cat /etc/nginx/conf.d/default.conf

# 查看Nginx日志
docker-compose logs frontend
```

#### 3. 服务启动顺序问题

```bash
# 查看健康检查状态
docker inspect shop-backend --format='{{.State.Health.Status}}'

# 重新启动服务
docker-compose down
docker-compose up -d
```

### 日志查看

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend

# 查看最近100行日志
docker-compose logs --tail=100 backend
```

### 性能调优

```bash
# 查看资源使用
docker stats

# 调整资源限制 (docker-compose.yml)
deploy:
  resources:
    limits:
      cpus: '1'
      memory: 768M
```

## Git 协作规范

### 分支策略

```
main          # 生产分支，保护分支
  └── develop # 开发分支
       ├── feature/xxx  # 功能开发
       ├── bugfix/xxx   # Bug修复
       └── hotfix/xxx   # 紧急修复
```

### Commit 规范

```
feat: 新功能
fix: Bug修复
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 示例

```bash
git checkout -b feature/user-login
# 开发完成后
git commit -m "feat: 实现用户登录功能"
git push origin feature/user-login
# 创建Pull Request合并到develop
```

## 团队分工

| 成员 | 职责 | 交付物 |
|------|------|--------|
| 胡佳烙 | 前端开发 + 容器化 | Vue页面、前端Dockerfile、Nginx配置 |
| 禄万智 | 后端开发 + 数据库 | Spring Boot API、数据库设计、后端Dockerfile |
| 彭涛 | CI/CD + 系统集成 | Docker Compose、GitHub Actions、文档 |

## License

MIT License
