# ---------- STAGE 1: BUILD ----------
FROM maven:3.9.12-eclipse-temurin-25-alpine AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget netcat-openbsd bash

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]