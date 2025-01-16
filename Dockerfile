FROM openjdk:17-jdk-slim
COPY workspace/springboot-app/build/libs/memoires-vives.jar /app/memoires-vives.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/memoires-vives.jar"]