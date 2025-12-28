# ==========================================
# 多阶段构建 - 后端Spring Boot应用
# ==========================================

# 阶段1: 构建阶段
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker层缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 阶段2: 运行阶段（使用精简JRE镜像）
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="shop-system"
LABEL description="电商系统后端API服务"

WORKDIR /app

# 创建非root用户运行应用
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 从构建阶段复制JAR包
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appgroup /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 环境变量配置
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE="prod" \
    TZ="Asia/Shanghai"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/product/page || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
