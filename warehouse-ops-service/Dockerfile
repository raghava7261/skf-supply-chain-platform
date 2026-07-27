# Build context must be repo root (see docker-compose.yml)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -N install
COPY common ./common
COPY warehouse-ops-service ./warehouse-ops-service
RUN mvn -q -f common/pom.xml install -DskipTests
RUN mvn -q -f warehouse-ops-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/warehouse-ops-service/target/warehouse-ops-service.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
