# Compilación (requiere Maven en la imagen o ejecutar `mvn package` en CI y copiar el JAR)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV SERVER_PORT=8000
EXPOSE 8000
COPY --from=build /app/target/fondos-api-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
