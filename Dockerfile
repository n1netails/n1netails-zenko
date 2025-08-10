FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/n1netails-zenko.jar app.jar
EXPOSE 9900
ENTRYPOINT ["java", "-jar", "app.jar"]
