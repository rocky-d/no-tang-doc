# 多阶段构建：JDK 24（若拉取失败改 ARG JDK_VERSION=23）
# Maven版本和JDK对照表: https://hub.docker.com/_/maven
ARG JDK_VERSION=24
ARG MVN_VERSION=3.9.11

# ========== 构建阶段 ==========
FROM maven:${MVN_VERSION}-eclipse-temurin-${JDK_VERSION} AS build
WORKDIR /app
# 复制当前单模块 pom 以利用缓存
COPY pom.xml .
RUN mvn -q -e dependency:go-offline || true
# 复制源码
COPY src src
# 编译打包（单模块，无需 -pl）
RUN mvn -DskipTests clean package

# ========== 运行阶段 ==========
FROM eclipse-temurin:${JDK_VERSION}
WORKDIR /app
# 通配 jar（target 下可能含 original- 前缀）
COPY --from=build /app/target/*SNAPSHOT.jar app.jar
# 若名称不含 SNAPSHOT，可改为 *.jar

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

EXPOSE 8070

# 仅保留可覆盖的 JVM_OPTS，其他运行期配置统一在 docker-compose.yml 注入
ARG JVM_OPTS="-Xms256m -Xmx512m"
ENV JVM_OPTS=${JVM_OPTS}

ENTRYPOINT ["sh","-c","exec java $JVM_OPTS -jar /app/app.jar"]
