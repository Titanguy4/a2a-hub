FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Upgrade base OS packages and create non-privileged non-root user
RUN apk upgrade --no-cache && \
    addgroup -g 10001 -S spring && \
    adduser -u 10001 -S spring -G spring

COPY --from=build --chown=spring:spring /app/target/calculator-agent-*.jar app.jar

USER 10001:10001
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -q --spider http://localhost:8082/.well-known/agent-card.json || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
