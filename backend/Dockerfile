FROM maven:3.9.6-eclipse-temurin-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/backend-1.0.jar app.jar
EXPOSE 3001
ENTRYPOINT ["java", "-jar", "app.jar"]
