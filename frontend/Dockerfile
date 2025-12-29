# ==========================================
# 多阶段构建 - 前端Vue3应用
# ==========================================

# 阶段1: 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件并安装依赖(利用Docker层缓存)
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY src ./src
COPY index.html ./
COPY vite.config.js ./
RUN npm run build

# 阶段2: 运行阶段（使用Nginx Alpine精简版）
FROM nginx:alpine

LABEL maintainer="shop-system"
LABEL description="电商系统前端服务"

# 删除默认配置
RUN rm -rf /etc/nginx/conf.d/default.conf

# 复制自定义nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

# 设置文件权限
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
