# ShelfAware

ShelfAware is a Spring Boot reading journal and review API. It started as a small class project and is being rebuilt into a portfolio-grade backend that showcases Java, Spring Boot, persistence, security, validation, testing, and deployable API design.

## What It Does

- Register users with BCrypt-hashed passwords
- Search and create books
- Track books on a personal shelf: `WANT_TO_READ`, `READING`, `FINISHED`, `FAVORITE`
- Write one review per user per book
- Keep reviews public or private
- Generate personal reading insights: shelf counts, review count, average rating, and rating distribution
- Seed a demo library through Flyway migrations

## Backend Highlights

- Spring Boot 3 / Java 17
- REST controllers with DTO request/response models
- Service layer with transactional business logic
- Spring Data JPA entities and repositories
- Spring Security with HTTP Basic as the first auth baseline
- Bean Validation for request contracts
- Global API error responses
- Flyway database migrations
- H2 for local fast startup, PostgreSQL profile for production-like runs
- MockMvc integration test covering register -> create book -> shelf -> review -> insights

## Project Structure

```text
src/main/java/com/shelfaware
  api/          Request and response DTOs
  controller/   REST API controllers
  domain/       JPA entities and enums
  exception/    API error handling
  repository/   Spring Data repositories
  security/     Spring Security configuration and principal
  service/      Transactional business logic
```

## Run Locally

Use the default H2-backed profile:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

The H2 console is available at:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:shelfaware
```

## PostgreSQL Mode

Start PostgreSQL:

```bash
docker compose up -d
```

Run the app with the PostgreSQL profile:

```bash
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="postgres"; .\mvnw.cmd spring-boot:run
```

## Core API

Public:

```http
POST /api/auth/register
GET  /api/books
GET  /api/books/{bookId}
GET  /api/books/{bookId}/reviews
```

Authenticated:

```http
GET  /api/auth/me
POST /api/books
GET  /api/me/shelf
PUT  /api/me/shelf/{bookId}
PUT  /api/books/{bookId}/reviews/me
GET  /api/me/reviews
GET  /api/me/insights
```

## Example Workflow

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"displayName":"Liam","email":"liam@example.com","username":"liam","password":"spring-boot-portfolio"}'
```

Add a book:

```bash
curl -u liam:spring-boot-portfolio -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Spring Boot in Practice","authors":"Somnath Musib","isbn":"9781617298813","categories":"Java, Spring Boot","pageCount":600}'
```

Track it:

```bash
curl -u liam:spring-boot-portfolio -X PUT http://localhost:8080/api/me/shelf/4 \
  -H "Content-Type: application/json" \
  -d '{"status":"READING","privateNotes":"Reference this while improving my Java portfolio."}'
```

Review it:

```bash
curl -u liam:spring-boot-portfolio -X PUT http://localhost:8080/api/books/4/reviews/me \
  -H "Content-Type: application/json" \
  -d '{"rating":5,"body":"Practical, focused, and useful for backend design.","publicReview":true}'
```

## Tests

```bash
./mvnw test
```

Current coverage includes Spring context startup and an authenticated end-to-end shelf workflow.

## Next Milestones

- Add JWT auth for a cleaner SPA integration
- Add external book search/import through Open Library or Google Books
- Build the React + TypeScript frontend
- Add OpenAPI documentation
- Add Testcontainers for PostgreSQL-backed integration tests
- Deploy backend and frontend as a live portfolio demo
