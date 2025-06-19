# moment-sharing

This repository contains the backend and frontend modules for the Moment Sharing application.

## Swagger

After starting the backend (`mvn spring-boot:run` in the `backend` directory), the API documentation is available at:

- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/swagger-ui.html`

The Storage API supports operations using only the file's URL:

- `GET /api/storage/download?url={fileUrl}` – download a file
- `POST /api/storage/upload?url={fileUrl}` – upload a file from the given URL
- `DELETE /api/storage/delete?url={fileUrl}` – delete the file at that URL
