# Federal Holidays API

A RESTful Spring Boot API for managing federal holidays for **Canada** and **USA**, backed by MySQL, containerized with Docker.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA** + **MySQL 8**
- **OpenAPI 3 / Swagger UI** via SpringDoc
- **OpenCSV** for file parsing
- **Lombok** for boilerplate reduction
- **JUnit 5 + Mockito** for testing
- **JaCoCo** for coverage enforcement (≥70%)
- **Docker + Docker Compose** for local orchestration

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

---

## Running Locally

###  Docker Compose 

Starts both MySQL and the Spring Boot app:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

To stop:

```bash
docker compose down
```

To stop and remove all data volumes:

```bash
docker compose down -v
```



## Running Tests

```bash
./mvnw test
```

JaCoCo coverage report is generated at:

```
target/site/jacoco/index.html
```

To enforce the 70% coverage gate:

```bash
./mvnw verify
```

---

## API Documentation

Once the app is running:

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/holidays` | List all holidays (supports `?country=` and `?year=` filters) |
| `GET` | `/api/v1/holidays/{id}` | Get a single holiday by ID |
| `POST` | `/api/v1/holidays` | Create a new holiday |
| `PUT` | `/api/v1/holidays/{id}` | Update an existing holiday |
| `POST` | `/api/v1/holidays/upload` | Upload a CSV file of holidays |

### Query Parameters for `GET /api/v1/holidays`

| Param | Type | Description |
|---|---|---|
| `country` | `CANADA` or `USA` | Filter by country |
| `year` | integer | Filter by year (requires `country`) |

### Supported Countries

| Enum Value | Description |
|---|---|
| `CANADA` | Canada |
| `USA` | United States of America |

Adding a new country requires only:
1. Adding a new value to the `Country` enum
2. No other code changes needed — the service, controller, and repository are all country-agnostic

---

## Request / Response Examples

### Create a Holiday

**POST** `/api/v1/holidays`

```json
{
  "name": "Canada Day",
  "date": "2024-07-01",
  "country": "CANADA",
  "description": "Celebrates the anniversary of Canadian Confederation"
}
```

**Response 201:**

```json
{
  "id": 1,
  "name": "Canada Day",
  "date": "2024-07-01",
  "country": "CANADA",
  "description": "Celebrates the anniversary of Canadian Confederation"
}
```

---

### Upload CSV

**POST** `/api/v1/holidays/upload` — `multipart/form-data`, field name: `file`

CSV format (headers required):

```csv
name,date,country,description
Canada Day,2024-07-01,CANADA,National holiday
Independence Day,2024-07-04,USA,USA independence day
```

**Response 200:**

```json
{
  "successCount": 2,
  "failureCount": 0,
  "created": [...],
  "errors": []
}
```

A sample CSV is included: `sample-holidays.csv`

---

## Postman Collection

Import `Federal-Holidays-API.postman_collection.json` into Postman. Set the `baseUrl` variable to `http://localhost:8080`.

---

## Error Responses

| Status | Scenario |
|---|---|
| `400` | Validation failure or invalid input |
| `404` | Holiday not found |
| `409` | Duplicate holiday (same date + country) |
| `413` | File exceeds 10MB |
| `500` | Unexpected server error |

---

## Project Structure

```
src/
├── main/java/com/holidays/api/
│   ├── controller/       # REST controllers
│   ├── service/          # Business logic
│   ├── repository/       # Spring Data JPA repositories
│   ├── model/            # JPA entities and enums
│   ├── dto/              # Request/Response DTOs
│   ├── exception/        # Custom exceptions and global handler
│   ├── util/             # Mapper and CSV parser
│   └── config/           # OpenAPI configuration
└── test/java/com/holidays/api/
    ├── controller/       # MockMvc controller tests
    └── service/          # Unit tests for service, mapper, parser
```
