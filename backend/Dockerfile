# --- Stage 1: Build the application ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy all project files
COPY . .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application, skipping tests
RUN ./mvnw clean package -DskipTests

# --- Stage 2: Create the final, lightweight image ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 10008

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]