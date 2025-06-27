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
`firebase.credentials-file` property. For example, if your credentials file is stored
at `backend/blog-d45ae-firebase-adminsdk-fbsvc-a6f01fd4a3.json`, run:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--firebase.credentials-file=blog-d45ae-firebase-adminsdk-fbsvc-a6f01fd4a3.json"
```

## API documentation

Swagger UI is available once the backend is running. Open your browser at:

```
http://localhost:8080/swagger-ui.html
```

The OpenAPI specification can be fetched from `/v3/api-docs`.


## Frontend environment

Create a `.env.local` file inside the `frontend` directory based on `.env.example`:

```bash
cd frontend
cp .env.example .env.local
```

Fill in your Firebase credentials. The `REACT_APP_FIREBASE_STORAGE_BUCKET` value should use the `appspot.com` domain, for example:

```
REACT_APP_FIREBASE_STORAGE_BUCKET=blog-d45ae.appspot.com
```

Using the `firebasestorage.app` domain will cause unauthorized errors when accessing uploaded files.

Set `REACT_APP_BACKEND_BASE_URL` to the URL of the Spring Boot backend. When running locally, use:

```
REACT_APP_BACKEND_BASE_URL=http://localhost:8080
```

## Running with Docker

Dockerfiles are provided for the backend and frontend. The easiest way to run the
full stack is using Docker Compose:

```bash
docker compose up --build
```

The backend will be exposed on `http://localhost:8080` and the frontend on
`http://localhost:3000`.

If you need to provide a Firebase service account file, mount it into the backend
container and set the `firebase.credentials-file` property via an environment
variable or command line argument.

