# 注意，本文件仅为示例！由于本项目涉及的依赖较多，不推荐使用 Docker 部署
# 使用轻量级 JDK21 运行环境
FROM openjdk:21-slim

# 工作目录
WORKDIR /app

# 复制已经打包好的 JAR 文件（假设已放在当前目录）
COPY ./limuzi-ai-code-mother-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 使用生产环境配置启动应用
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
