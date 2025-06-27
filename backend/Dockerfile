# Build stage
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend ./backend
# Copy frontend pom only for Maven module structure
COPY frontend/pom.xml ./frontend/pom.xml
RUN mvn -pl backend -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
