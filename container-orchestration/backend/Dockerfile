# ==============================================================================
# Spring Boot Backend Multi-Stage Dockerfile (Production Optimized & Secure)
# ==============================================================================

# --- Stage 1: Build Java Application using Maven ---
FROM maven:3.8.5-openjdk-17-slim AS builder

WORKDIR /app

# Copy the pom.xml definition first to cache dependency layers
COPY pom.xml .

# Download dependencies offline to optimize build speed in subsequent runs
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Compile and package the application into a lightweight .jar file
# Skipping tests for faster build in university environment
RUN mvn clean package -DskipTests -B

# --- Stage 2: Run Application with Minimal JRE Runtime ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a system group and user 'spring' to run the application as non-root
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the generated JAR file from Stage 1 into the container
COPY --from=builder /app/target/*.jar app.jar

# Adjust ownership of the JAR file to the 'spring' user
RUN chown spring:spring app.jar

# Set active container user to 'spring' (UID 1000+) instead of root
USER spring:spring

# Expose backend API port (internally mapped in networks)
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
