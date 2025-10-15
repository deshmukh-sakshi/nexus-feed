# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the application
RUN mvn clean package -DskipTests

# Expose port
EXPOSE 10008

# Run the application
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]