# Étape 1 : Build
FROM gradle:7.6-jdk17 AS builder
WORKDIR /app
COPY workspace/springboot-app/ /app/
RUN gradle bootJar

# Étape 2 : Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/memoires-vives.jar /app/memoires-vives.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/memoires-vives.jar"]