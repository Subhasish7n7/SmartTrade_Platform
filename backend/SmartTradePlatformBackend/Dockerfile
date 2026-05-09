# Use Maven + JDK 21 to build the app
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy everything
COPY . .

# Build jar (skip ALL tests)
RUN mvn clean package -Dmaven.test.skip=true


# Use lightweight Java runtime
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]