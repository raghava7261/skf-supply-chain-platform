# Build context must be repo root (see docker-compose.yml)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -N install
COPY common ./common
COPY purchase-order-service ./purchase-order-service
RUN mvn -q -f common/pom.xml install -DskipTests
RUN mvn -q -f purchase-order-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/purchase-order-service/target/purchase-order-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
