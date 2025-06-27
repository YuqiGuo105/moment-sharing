# Moment Sharing

This project contains a Spring Boot backend and a React frontend.

## Running the backend

Use the Maven wrapper to start the application:

```bash
cd backend
./mvnw spring-boot:run
```

The service starts on port `8080` by default.

If you are using a Firebase service account JSON file, specify its path using the
`firebase.credentials-file` property:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--firebase.credentials-file=/path/to/key.json"
```

## API documentation

Swagger UI is available once the backend is running. Open your browser at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification can be fetched from `/v3/api-docs`.

