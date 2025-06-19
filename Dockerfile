FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/nemo-server-2.0.6-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
