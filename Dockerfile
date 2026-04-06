# Imagen de ejecución alineada con despliegue (Render / Docker Compose).
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY mvnw pom.xml .mvn ./
COPY src ./src
RUN chmod +x mvnw \
    && ./mvnw -ntp -B -DskipTests package \
    && mv target/logistica-auth-*.jar /app/application.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/application.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
