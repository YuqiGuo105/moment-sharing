# 1) Build stage: compile & package with Maven
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2) Run stage: minimal JRE
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the fat-jar
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Create the exact path your app.properties points at:
#   firebase.credentials-file=./backend/src/main/resources/…
RUN mkdir -p backend/src/main/resources

# Copy the JSON from the build context into that path
# (build context is 'backend/', so on the host it's at backend/src/main/resources/…)
COPY src/main/resources/blog-d45ae-firebase-adminsdk-fbsvc-a6f01fd4a3.json \
     backend/src/main/resources/

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
