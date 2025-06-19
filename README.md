# Moment Sharing

This project contains a Spring Boot backend and a React frontend.

## Running the backend

Use the Maven wrapper to start the application:

```bash
cd backend
./mvnw spring-boot:run
```

The service will start on port `8080` by default. A simple health check is
available at `http://localhost:8080/api/ping`, which should return `pong`.

## API documentation

Swagger UI is available once the backend is running. Open your browser at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification can be fetched from `/v3/api-docs`.

## Testing

The backend tests run against an in-memory H2 database. Schema and sample data
are imported from `schema-test.sql` and `data-test.sql` under
`backend/src/test/resources`. Supabase integration is disabled for the `test`
profile, so no external services are required.

