# ShelfAware

ShelfAware is a full-stack reading journal and review app. It started as a small class project and is being rebuilt into a portfolio-grade Spring Boot + React project that showcases Java backend architecture, API design, authentication, persistence, and a modern frontend.

## What It Does

- Register users with BCrypt-hashed passwords
- Login with stateless JWT bearer tokens for SPA-friendly authentication
- Enter an isolated, pre-populated demo profile with one click
- Search local books and import external book metadata from Open Library
- Track books through `WANT_TO_READ`, `READING`, and `FINISHED`, with favorites kept independently
- Log page progress as dated reading sessions with automatic start and finish transitions
- Set annual book and page goals, follow streaks, and undo the latest progress update
- Write one review per user per book
- Keep reviews public or private
- Generate personal reading insights: monthly pages, completion counts, reading pace, streaks, and ratings
- Seed a demo library through Flyway migrations

## Backend Highlights

- Spring Boot 3 / Java 17
- REST controllers with DTO request/response models
- Service layer with transactional business logic
- Spring Data JPA entities and repositories
- Spring Security with JWT resource-server authentication
- Bean Validation for request contracts
- Global API error responses
- Flyway database migrations
- External API adapter using Spring's `RestClient`
- H2 for local fast startup, PostgreSQL profile for production-like runs
- MockMvc integration test covering register -> login -> import book -> shelf -> review -> insights

## Frontend Highlights

- React + TypeScript + Vite
- JWT-aware API client with persisted auth state
- Book discovery flow backed by the Spring Boot Open Library adapter
- Local library, personal shelf, book detail, review, and insights screens
- TanStack Query for server state
- Recharts-powered reading analytics
- Reading Journey home with goals, progress rings, quick session logging, streaks, and activity history
- Responsive product UI with a dedicated ShelfAware visual identity
- Vitest and Testing Library coverage for core Journey interactions

## Project Structure

```text
src/main/java/com/shelfaware
  api/          Request and response DTOs
  controller/   REST API controllers
  domain/       JPA entities and enums
  exception/    API error handling
  external/     Third-party API clients
  repository/   Spring Data repositories
  security/     Spring Security and JWT support
  service/      Transactional business logic

frontend/src
  api/          Typed API client
  state/        Auth context and token persistence
  App.tsx       Routes and screens
  styles.css    Frontend design system
```

## Run Locally

Use the default H2-backed backend profile:

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

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://127.0.0.1:5173
```

By default, the frontend calls:

```text
http://localhost:8080
```

Override that with `frontend/.env.local`:

```text
VITE_API_BASE_URL=http://localhost:8080
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
POST /api/auth/login
POST /api/auth/demo
GET  /api/health
GET  /api/books
GET  /api/books/{bookId}
GET  /api/books/external-search?q={query}
GET  /api/books/{bookId}/reviews
```

Authenticated:

```http
GET  /api/auth/me
POST /api/books
POST /api/books/import
GET  /api/me/shelf
  PUT  /api/me/shelf/{bookId}
POST /api/me/shelf/{bookId}/progress
DELETE /api/me/reading-sessions/{sessionId}
GET  /api/me/reading-goals/{year}
PUT  /api/me/reading-goals/{year}
GET  /api/me/journey?year={year}
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

Login and save the token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"liam","password":"spring-boot-portfolio"}' \
  | jq -r '.accessToken')
```

Search Open Library through the ShelfAware backend:

```bash
curl "http://localhost:8080/api/books/external-search?q=spring%20boot&limit=5"
```

Import a selected external result:

```bash
curl -X POST http://localhost:8080/api/books/import \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"externalSource":"OPEN_LIBRARY","externalId":"/works/OL27687548W","title":"Spring Boot in Practice","authors":"Somnath Musib","isbn":"9781617298813","categories":"Java, Spring Boot","pageCount":600}'
```

Track it:

```bash
curl -X PUT http://localhost:8080/api/me/shelf/4 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"READING","privateNotes":"Reference this while improving my Java portfolio."}'
```

Review it:

```bash
curl -X PUT http://localhost:8080/api/books/4/reviews/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"rating":5,"body":"Practical, focused, and useful for backend design.","publicReview":true}'
```

## Tests

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd frontend
npm run build
npm test
```

Backend coverage includes Flyway favorite migration, streak calculation, Spring context startup, and a JWT-authenticated journey workflow. Frontend coverage exercises dashboard empty states, goal editing, progress entry, and mutation failures in addition to the production TypeScript build.

## Deployment

The production layout uses Vercel for the frontend, a Docker host for the Spring Boot API, and PostgreSQL for persistent data. See [DEPLOYMENT.md](DEPLOYMENT.md) for the complete Neon, Render, and Vercel walkthrough, environment variables, CORS configuration, and verification checklist.

## Next Milestones

- Add OpenAPI documentation
- Add Testcontainers for PostgreSQL-backed integration tests
- Add multi-year goal browsing and reading-session corrections beyond latest-session undo
- Add optional reading-time tracking and format-aware progress for audiobooks
- Deploy backend and frontend as a live portfolio demo
