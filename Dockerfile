# Use Maven image to build the JAR file
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the project and create the JAR file
RUN mvn clean package -DskipTests

# Use OpenJDK image to run the app
FROM openjdk:17-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/Ecommerce-multi-vendor-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
