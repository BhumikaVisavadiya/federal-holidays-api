# Builder stage
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/federal-holidays-api-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
