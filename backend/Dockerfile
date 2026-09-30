FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-privileged user and group for runtime security
RUN addgroup -S a2agroup && adduser -S a2a -G a2agroup

COPY --from=build --chown=a2a:a2agroup /app/target/*.jar app.jar

USER a2a:a2agroup
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
