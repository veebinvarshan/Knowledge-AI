# Stage 1: Build stage using Java 21 JDK Alpine
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven Wrapper and configuration files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Grant execution permission to Maven Wrapper
RUN chmod +x ./mvnw

# Copy source code and build production executable JAR
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Stage 2: Production runtime stage using minimal Java 21 JRE Alpine
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root system user for production container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership for non-root execution
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
