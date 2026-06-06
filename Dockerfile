# ---- Stage 1: Build (JDK 21 LTS) ----
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1. Copy Maven wrapper and Project file first
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# 2. Grant execution rights
RUN chmod +x mvnw

# 3. Download dependencies
# CRITICAL CHANGE: Removed '--mount=type=cache'.
# This allows GHA to cache this layer (vs. the temporary mount which gets lost).
# We use 'dependency:resolve' which is often more reliable than 'go-offline' for plugins.
RUN ./mvnw dependency:resolve dependency:resolve-plugins

# 4. Copy source code and build
COPY src ./src

# CRITICAL CHANGE: Removed 'clean' (unnecessary in fresh container) and removed mount.
RUN ./mvnw package -DskipTests

# ---- Stage 2: Runtime optimized ----
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create uploads folder
RUN mkdir -p /app/uploads

# Copy the jar
COPY --from=builder /app/target/*.jar app.jar

# Optimized JVM arguments
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]