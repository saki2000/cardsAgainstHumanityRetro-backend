# Stage 1: Build the application with Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create the final, smaller image
FROM openjdk:21-slim-bullseye
WORKDIR /app
# Copy the JAR file from the build stage
COPY --from=build /app/target/retro-against-humanity-backend-2.0.0.jar app.jar
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]