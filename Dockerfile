# Multi-stage backend image for EC2 Docker Compose deployment.
# The local src/main/resources/application.yaml is intentionally excluded by
# .dockerignore; containers use src/main/resources/application-prod.yaml plus
# environment variables instead.

FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Prime Maven dependencies first so Docker can cache them across source edits.
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

COPY --from=build /workspace/target/life-compass-0.0.1-SNAPSHOT.jar /app/app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
