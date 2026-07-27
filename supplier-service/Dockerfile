# NOTE: build context must be the repo root (see docker-compose.yml), since this
# module depends on the shared `common` module and Docker can't COPY from
# outside its build context otherwise.

# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -N install
COPY common ./common
COPY supplier-service ./supplier-service
RUN mvn -q -f common/pom.xml install -DskipTests
RUN mvn -q -f supplier-service/pom.xml clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/supplier-service/target/supplier-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
